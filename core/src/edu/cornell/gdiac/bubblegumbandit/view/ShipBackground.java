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

public class ShipBackground {

    /** An earclipping triangular to make sure we work with convex shapes */
    private static final EarClippingTriangulator TRIANGULATOR = new EarClippingTriangulator();

    /**Width of the level */
    private int width;

    /**Height of the level */
    private int height;

    /**
     * The background of the ship, cropped if necessary
     */
    private TextureRegion ship_bg;

    /**
     * The space background of the level, cropped if necessary
     */
    private TextureRegion space_bg;

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

    private float[] vertices;

    //ids for corner tiles, determine the vertices of the polygon
    private IntArray cornerIds = new IntArray(new int[] {25, 21, 22, 27, 11, 10});

    private PolygonRegion spaceReg;
    private PolygonRegion shipReg;

    /** Shape information for this physics object */
    protected PolygonShape[] shapes;

    /** A cache value for the fixtures (for resizing) */
    private Fixture[] geoms;

    private PolygonObstacle interior;

    /**
     * The font for debugging
     */
    protected BitmapFont debugFont;


    private Vector2 centroid;

    public ShipBackground(TextureRegion bg, TextureRegion space_bg) {
        ship_bg = bg;
        this.space_bg = space_bg;
    }

    /** Resets all variables to their inital values */
    public void reset(){
        y_max = 0;
        x_max = 0;
        shipReg = null;
        spaceReg = null;
    }

    /** Initializes the minimap for a given level.
     *
     * @param directory The asset directory.
     * @param levelFormat the current level.
     * @param physicsWidth The physics height of the level.
     * @param physicsHeight The physics width of the level.
     */
    public void initialize(AssetDirectory directory, JsonValue levelFormat, int physicsWidth, int physicsHeight) {

//        //Make the Minimap's background and map tiles.
//        //Set fields.
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
//            if (cornerIds.contains(tileVal)) {
            if (tileVal != 0) {

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

    public void createPolygons(){

//        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid),
//                new float[] {      // Four vertices
//                        0, 0,            // Vertex 0         3--2
//                        100, 0,          // Vertex 1         | /|
//                        100, 100,        // Vertex 2         |/ |
//                        0, 100           // Vertex 3         0--1
//                }, new short[] {
//                0, 1, 2,         // Two triangles using vertex indices.
//                0, 2, 3          // Take care of the counter-clockwise direction.
//        });


        //space polygon is just a large rectangle
        spaceReg = new PolygonRegion(space_bg,
                new float[] {      // Four vertices
                        0, 0,            // Vertex 0         3--2
                        width*64, 0,          // Vertex 1         | /|
                        width*64, height*64,        // Vertex 2         |/ |
                        0, height * 64           // Vertex 3         0--1
                }, new short[] {
                0, 1, 2,         // Two triangles using vertex indices.
                0, 2, 3          // Take care of the counter-clockwise direction.
        });

        //create ship polygon
        //sort vectors in cornerPositions in a clockwise order using polar coordinates


        //centroid is the middle x and y position

        float centerX = x_offset + ((x_max - x_offset)/2);
        float centerY = y_offset + ((y_max - y_offset)/2);
        centroid = new Vector2(centerX, centerY);


        //angles of rotations of all corner positions relative to the centroid
//        OrderedMap<Float, Vector2> cwPositions = new OrderedMap<>();
        Vertex[] cwPositions = new Vertex[cornerPositions.size()];
        for (int i = 0; i < cwPositions.length; i++){
            Vector2 point = cornerPositions.get(i);
            cwPositions[i] = new Vertex(point, centroid);
        }

        //sort angles
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
//        vertices = new float[(cornerPositions.size()) * 2];
//        for (int i = 0; i < vertices.length; i+=2){
//            vertices[i] = (cornerPositions.get(i/2).x - x_offset) * 64;
//            vertices[i + 1] = (cornerPositions.get(i/2).y - y_offset) * 64;
//            System.out.println(vertices[i] + ", " + vertices[i + 1] );
//        }

        vertices = new float[cwPositions.length * 2];
        for (int i = 0; i < vertices.length; i += 2){
//            System.out.println(cwPositions[i].angle);
            vertices[i] = ((cwPositions[i/2].coordinates).x - x_offset) * 64;
            vertices[i + 1] = ((cwPositions[i/2].coordinates).y - y_offset) * 64;
            System.out.println(vertices[i] + ", " + vertices[i + 1] );
        }

//        vertices = sortVertices();

        ShortArray array  = TRIANGULATOR.computeTriangles(vertices);
        trimColinear(vertices,array);
//        short[] triangles = new short[array.items.length];
//        System.arraycopy(array.items, 0, triangles, 0, triangles.length);

        shipReg = new PolygonRegion(ship_bg, vertices,array.toArray() );


    }



    /** returns a list of vertices in a clockwise order


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


    public void draw(GameCanvas canvas){
        canvas.begin();

        if (spaceReg != null && shipReg != null) {

            canvas.draw(spaceReg, 0, 0);
            canvas.draw(shipReg, x_offset * 64, y_offset * 64);
//        canvas.draw(shipReg, 0, 0);

            for (int i = 0; i < vertices.length; i += 2) {
                canvas.drawText(valueOf(i / 2), debugFont, vertices[i] + x_offset * 64, vertices[i + 1] + y_offset * 64);
            }

            canvas.drawText("X", debugFont, centroid.x * 64, centroid.y * 64);
        }

//        interior.draw(canvas);

        canvas.end();


    }

    private class Vertex{

        public Vector2 coordinates;
        public float angle;
        public float distance;

        public Vertex(Vector2 coordinates, Vector2 centroid){
            this.coordinates = coordinates;
            Vector2 shifted = new Vector2(coordinates.x - centroid.x, coordinates.y - centroid.y);
            this.angle = shifted.angleDeg();
            this.distance = 0;//coordinates.dst(centroid);
        }

        /** return
         * 0 = equal
         * -1 = this vertex is less than
         * 1 = this vertuex is greater than
         *
         * @param v
         * @return
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
