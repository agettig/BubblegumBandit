package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics.obstacle.PolygonObstacle;
import org.w3c.dom.ls.LSOutput;

import javax.swing.plaf.IconUIResource;
import java.util.ArrayList;
import java.util.Vector;

import static java.lang.String.valueOf;


/**
 * ShipBackground creates the background of the current level.
 *
 * ShipBackground requires that the json file of the given level contains the layer "Corners" to draw the background.
 * Otherwise, no background will be drawn.
 */
public class Background {

    /** An ear-clipping triangular to make sure we work with convex shapes */
    private static final EarClippingTriangulator TRIANGULATOR = new EarClippingTriangulator();

    /**Width of the level */
    private int width;

    /**Height of the level */
    private int height;

    /**
     * The background of the ship, cropped if necessary
     */
    private final TextureRegion shipBg;

    /**
     * The space background of the level, cropped if necessary
     */
    private final TextureRegion spaceBg;

    /** The positions of the tiles */
    private ArrayList<Vector2> cornerPositions;

    /** the smallest x coordinate to offset all other x values with */
    private float x_offset;

    /** the smallest y coordinate to offset all other y values with */
    private float y_offset;

    /** largest x coordinate, used to find centroid */
    private float x_max;
    /** largest y coordinate, used to find centroid */
    private float y_max;

    /** A list of the x and y positions of each vertex in the ship's polygon */
    private float[] vertices;

    /** Polygon representing space */
    private PolygonRegion spaceReg;
    /** Polygon representing the spaceship */
    private PolygonRegion shipReg;

    /**
     * Font used in debugging
     */
    protected BitmapFont debugFont;

    /** the center of the polygon region, used to sort the vertices*/
    private Vector2 centroid;

    /** Tile id number representing the centroid of the polygon */
    private final static int CENTER_ID = 2;

    /** scale of the physics world */
    private static final float SCALE = 64;

    /**
     * Initializes the textures of the background
     *
     * @param shipBg TextureRegion of the spaceship
     * @param spaceBg TextureRegion of space
     */
    public Background(TextureRegion shipBg, TextureRegion spaceBg) {
        this.shipBg = shipBg;
        this.spaceBg = spaceBg;
    }

    /** Resets all variables to their initial values */
    public void reset(){
        y_max = 0;
        x_max = 0;
        shipReg = null;
        spaceReg = null;
    }

    /** Initializes the background for a given level.
     *
     * @param directory The asset directory.
     * @param levelFormat the current level.
     * @param physicsWidth The physics height of the level.
     * @param physicsHeight The physics width of the level.
     */
    public void initialize(AssetDirectory directory, JsonValue levelFormat, int physicsWidth, int physicsHeight) {

        //Make the Minimap's background and map tiles.
        //Set fields.
        width = physicsWidth;
        height = physicsHeight;
        x_offset = width;
        y_offset = height;


        //Find all positions of floors/platforms.
        cornerPositions = new ArrayList<>();
        JsonValue layer = levelFormat.get("layers").child();
        JsonValue tileLayer = null;
        while (layer != null) {
            String layerName = layer.getString("name");
            if ("Corners".equals(layerName)) {
                tileLayer = layer;
            }
            layer = layer.next();
        }
        //if no Corners layer was found
        if (tileLayer == null){
            return;
        }

        // width x height matrix as array
        int[] worldData = tileLayer.get("data").asIntArray();

        // Iterate over each tile in the world and create if it exists
        for (int i = 0; i < worldData.length; i++) {
            int tileVal = worldData[i];
            //mark the centroid
            if (tileVal == CENTER_ID) {
                float x = (i % width) + 1f;
                float y = height - (i / width) - 1f;
                centroid = new Vector2(x, y);
            }
            //mark the corners
            else if (tileVal != 0) {
                float x = (i % width) + 1f;
                float y = height - (i / width) - 1f;
                cornerPositions.add(new Vector2(x, y));

                if (x < x_offset) x_offset = x;
                if (y < y_offset) y_offset = y;
                if (x > x_max) x_max = x;
                if (y > y_max) y_max = y;

            }
        }
        createPolygons();

        debugFont = directory.getEntry("display", BitmapFont.class);

    }

    /** Creates the polygons representing space and the spaceship. */
    public void createPolygons(){

        //space polygon is just a large rectangle
        spaceReg = new PolygonRegion(spaceBg,
                new float[] {
                        0, 0,
                        width*SCALE*3, 0,
                        width*SCALE*3, height*SCALE*3,
                        0, height * SCALE*3
                }, new short[] {
                0, 1, 2,         // Two triangles using vertex indices.
                0, 2, 3          // Take care of the counter-clockwise direction.
        });

        //create ship polygon
        //sort vectors in cornerPositions in a clockwise order using polar coordinates relative to a central point
        sortVertices();

        //create triangles
        ShortArray array  = TRIANGULATOR.computeTriangles(vertices);
        trimColinear(vertices,array);

        shipReg = new PolygonRegion(shipBg, vertices,array.toArray() );


    }

    /**
     *  Using the list of vertices in CornerPositions,
     *  sorts the vertices in a clockwise order relative to a central position within the polygon.
     *  Using this sorted list, creates a list of floats representing each of the vertices that is compatible with
     *  PolygonRegion.
     *
     *  The final list is stored in vertices
     */
    private void sortVertices(){
        //centroid is the middle x and y position
//        float centerX = x_offset + ((x_max - x_offset)/2);
//        float centerY = y_offset + ((y_max - y_offset)/2);
//        centroid = new Vector2(centerX, centerY);

        //angles of rotations of all corner positions relative to the centroid
        Vertex[] cwPositions = new Vertex[cornerPositions.size()];
        for (int i = 0; i < cwPositions.length; i++){
            Vector2 point = cornerPositions.get(i);
            cwPositions[i] = new Vertex(point, centroid);
        }

        //sort angles via insertion sort
        int k = 0;
        while (k < cwPositions.length){
            int j = k;
            while (0<j && cwPositions[j].compareTo(cwPositions[j-1]) > 0){
                Vertex temp = cwPositions[j];
                cwPositions[j] = cwPositions[j-1];
                cwPositions[j-1] = temp;
                j--;
            }
            k++;
        }

        // change corner positions into a list of vertices compatible with PolygonRegion
        vertices = new float[cwPositions.length * 2];
        for (int i = 0; i < vertices.length; i += 2){
//            System.out.println(cwPositions[i].angle);
            vertices[i] = ((cwPositions[i/2].coordinates).x - x_offset) * SCALE;
            vertices[i + 1] = ((cwPositions[i/2].coordinates).y - y_offset) * SCALE;
//            System.out.println(vertices[i] + ", " + vertices[i + 1] );
        }
    }

    /**
     * Removes colinear vertices from the given triangulation.
     *
     * For some reason, the LibGDX triangulator will occasionally return colinear
     * vertices.
     *
     * @param points  The polygon vertices
     * @param indices The triangulation indices
     */
    private void trimColinear(float[] points, ShortArray indices) {
        int colinear = 0;
        for(int ii = 0; ii < indices.size/3-colinear; ii++) {
            float t1 = points[2*indices.items[3*ii  ]]*(points[2*indices.items[3*ii+1]+1]-points[2*indices.items[3*ii+2]+1]);
            float t2 = points[2*indices.items[3*ii+1]]*(points[2*indices.items[3*ii+2]+1]-points[2*indices.items[3*ii  ]+1]);
            float t3 = points[2*indices.items[3*ii+2]]*(points[2*indices.items[3*ii  ]+1]-points[2*indices.items[3*ii+1]+1]);
            if (Math.abs(t1+t2+t3) < 0.0000001f) {
                indices.swap(3*ii  ,  indices.size-3*colinear-3);
                indices.swap(3*ii+1,  indices.size-3*colinear-2);
                indices.swap(3*ii+2,  indices.size-3*colinear-1);
                colinear++;
            }
        }
        indices.size -= 3*colinear;
        indices.shrink();
    }


    /** Draws the repeating space background and the cropped ship background */
    public void draw(GameCanvas canvas){

        if (spaceReg == null || shipReg == null) return;
        canvas.begin();

        canvas.draw(spaceReg, -width * SCALE, -height * SCALE);
        canvas.draw(shipReg, x_offset * SCALE, y_offset * SCALE);

        canvas.end();


    }

    /** Draws the number associated to each vertex and marks the centroid with an 'X' */
    public void drawDebug(GameCanvas canvas){
        canvas.begin();
        for (int i = 0; i < vertices.length; i += 2) {
            canvas.drawText(valueOf(i / 2), debugFont, vertices[i] + x_offset * SCALE, vertices[i + 1] + y_offset * SCALE);
        }
        canvas.drawText("X", debugFont, centroid.x * SCALE, centroid.y * SCALE);
        canvas.end();
    }


    /**
     * A Vertex represents a vertex of the ship's polygon.
     */
    private class Vertex{

        /** position of the vertex */
        public Vector2 coordinates;

        /** Angle of rotation relative to the centroid, in degrees */
        public float angle;

        /** Distance away from the centroid */
        public float distance;

        public Vertex(Vector2 coordinates, Vector2 centroid){
            this.coordinates = coordinates;
            Vector2 shifted = new Vector2(coordinates.x - centroid.x, coordinates.y - centroid.y);
            this.angle = shifted.angleDeg();
            this.distance = 0;//coordinates.dst(centroid);
        }

        /**
         * Compares the angle and distance of the current Vertex to the given Vertex.
         * Returns a value of 0, 1, or -1:
         *
         *  0 = vertices are equal
         *  -1 = this vertex is less than the given vertex
         *  1 = this vertex is greater than the given vertex
         *
         * @param v The given vertex
         * @return A value of 0, 1, or -1
         */
        public int compareTo(Vertex v){
            if (this.angle < v.angle){
                return -1;
            }
            if (this.angle > v.angle){
                return 1;
            }
            //this.angle == v.angle
            if (this.distance > v.distance){
                return -1;
            }
            if (this.distance < v.distance){
                return  1;
            }
            return 0;

        }
    }





}