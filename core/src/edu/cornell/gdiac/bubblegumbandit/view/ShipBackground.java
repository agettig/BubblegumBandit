package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

import java.util.ArrayList;

public class ShipBackground {

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
    private ArrayList<Vector2> floorPositions;

    /** the smallest x coordinate to offset all other x values with */
    private float x_offset;

    /** the smallest y coordinate to offset all other y values with */

    private float y_offset;

    private float[] vertices;

    //ids for corner tiles, determine the vertices of the polygon
    private IntArray cornerIds = new IntArray(new int[] {25, 21, 22, 27});


    private PolygonRegion spaceReg;
    private  PolygonRegion shipReg;

    public ShipBackground(TextureRegion bg, TextureRegion space_bg) {
        ship_bg = bg;
        this.space_bg = space_bg;
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
        floorPositions = new ArrayList<>();
        JsonValue layer = levelFormat.get("layers").child();
        JsonValue tileLayer = null;
        while (layer != null) {
            String layerName = layer.getString("name");
            if ("Terrain".equals(layerName)) {
                tileLayer = layer;
            }
            layer = layer.next();
        }

        // width x height matrix as array
        int[] worldData = tileLayer.get("data").asIntArray();

        // Iterate over each tile in the world and create if it exists
        for (int i = 0; i < worldData.length; i++) {
            int tileVal = worldData[i];
            if (cornerIds.contains(tileVal)) {

                float x = (i % width) + 1f;
                float y = height - (i / width) - 1f;
                floorPositions.add(new Vector2(x, y));

                if (x < x_offset) x_offset = x;
                if (y < y_offset) y_offset = y;

            }
        }


        vertices = new float[(floorPositions.size()) * 2];
        for (int i = 0; i < vertices.length; i+=2){
            vertices[i] = (floorPositions.get(i/2).x - x_offset) * 64;
            vertices[i + 1] = (floorPositions.get(i/2).y - y_offset) * 64;
            System.out.println(vertices[i] + ", " + vertices[i + 1] );
        }

        createPolygons();
    }

    public void createPolygons(){
        //        new float[] {      // Four vertices
//                0, 0,            // Vertex 0         3--2
//                1000, 0,          // Vertex 1         | /|
//                1000, 1000,        // Vertex 2         |/ |
//                0, 1000           // Vertex 3         0--1

//        new short[] {
//                0, 1, 2,         // Two triangles using vertex indices.
//                0, 2, 3          // Take care of the counter-clockwise direction.
//        });

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

        shipReg = new PolygonRegion(ship_bg,
                vertices,
                new short[]{
                        6, 7, 5,         // Two triangles using vertex indices.
                        6, 5, 4,          // Take care of the counter-clockwise direction.
                        6, 4, 1,
                        6, 1, 0,
                        6, 1, 3,
                        6, 2, 3
                });
    }

    public void draw(GameCanvas canvas){
        canvas.begin();

        canvas.draw(spaceReg, 0,0);
        canvas.draw(shipReg, x_offset * 64, y_offset * 64);

        canvas.end();

    }

}
