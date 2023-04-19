package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

import java.util.ArrayList;

public class ShipBackground {

    /**Width of the level */
    private int width;

    /**Height of the level */
    private int height;

    /**
     * The background of the level, cropped if necessary
     */
    private TextureRegion ship_bg;


    public ShipBackground(TextureRegion bg) {
        ship_bg = bg;
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
//        makeMinimapTiles(directory);
//
//        //Find all positions of floors/platforms.
//        floorPositions = new ArrayList<>();
//        JsonValue layer = levelFormat.get("layers").child();
//        JsonValue tileLayer = null;
//        while (layer != null) {
//            String layerName = layer.getString("name");
//            if ("Terrain".equals(layerName)) {
//                tileLayer = layer;
//            }
//            layer = layer.next();
//        }
//        int[] worldData = tileLayer.get("data").asIntArray();
//
//
//        // Iterate over each tile in the world and create if it exists
//        for (int i = 0; i < worldData.length; i++) {
//            int tileVal = worldData[i];
//            if (tileVal != 0) {
//                float x = (i % width) + 1f;
//                expandedTilesLong = Math.max(expandedTilesLong, (int)x);
//                float y = height - (i / width) - 1f;
//                expandedTilesTall = Math.max(expandedTilesTall, (int)y);
//                floorPositions.add(new Vector2(x, y));
//            }
//        }
    }


    public void draw(GameCanvas canvas){

        PolygonRegion polyReg = new PolygonRegion(ship_bg,
                new float[] {      // Four vertices
                        0, 0,            // Vertex 0         3--2
                        width, 0,          // Vertex 1         | /|
                        width, height,        // Vertex 2         |/ |
                        0, height           // Vertex 3         0--1
                }, new short[] {
                0, 1, 2,         // Two triangles using vertex indices.
                0, 2, 3          // Take care of the counter-clockwise direction.
        });
//
        canvas.draw(polyReg, 0, 0);

    }

}
