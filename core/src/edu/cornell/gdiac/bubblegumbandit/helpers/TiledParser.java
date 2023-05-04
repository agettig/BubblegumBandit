package edu.cornell.gdiac.bubblegumbandit.helpers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

import java.util.Comparator;
import java.util.HashMap;

public class TiledParser {

    public static int boardIdOffset;

    /** Returns the tileset based on the provided Json file
     *
     * @param directory the asset directory of the level
     * @param levelFormat the json value representing the level
     * @returns  the array representing the tileset */
    public static HashMap<Integer, TextureRegion> createTileset(AssetDirectory directory, JsonValue levelFormat) {

        HashMap<Integer, TextureRegion> tileset = new HashMap<>();
        // The level tileset info only has id offset and file path.
        JsonValue levelTilesetJson = levelFormat.get("tilesets").child();
        while (levelTilesetJson != null) {
            if (levelTilesetJson.get("source") == null) { // The tileset isn't associated with a file.
                levelTilesetJson = levelTilesetJson.next();
                continue;
            }
            String tilesetSource = levelTilesetJson.get("source").asString();
            int idOffset = levelTilesetJson.getInt("firstgid");

            // Find the key from the file path. The key of the tileset is the file name.
            int substringStart = tilesetSource.lastIndexOf("/") + 1;
            int substringEnd = tilesetSource.lastIndexOf(".");
            String tilesetName = tilesetSource.substring(substringStart, substringEnd);
            String tilesetFileType = tilesetSource.substring(substringEnd, tilesetSource.length());
            if (tilesetName.equals("board")) {
                // We need to hold on to the board id offset for AI purposes.
                boardIdOffset = idOffset;
            }
            if (!tilesetFileType.equals(".json")) { // Skip non-JSON tilesets (ex. board and camera tilesets).
                levelTilesetJson = levelTilesetJson.next();
                continue;
            }

            // Load the actual tileset JSON info based on the key.
            JsonValue tilesetJson = directory.getEntry(tilesetName, JsonValue.class);
            if (tilesetJson == null) {
                throw new RuntimeException("Missing the value for JSON key: " + tilesetName + " in assets.json."
                        + "\n If you don't want to load this tileset, make it a .tmx file.");
            }
            JsonValue tile = tilesetJson.get("tiles").child();
            if (tilesetJson.get("image") != null) { // Tileset represents one image (texture atlas)
                Texture t = directory.getEntry(tile.get("type").asString(), Texture.class);
                int imgWidth = tilesetJson.getInt("imagewidth");
                int tileWidth = tilesetJson.getInt("tilewidth");
                int tileHeight = tilesetJson.getInt("tileheight");
                int offsetX = 0;
                int offsetY = 0;
                while (tile != null) {
                    TextureRegion texture = new TextureRegion(t, offsetX, offsetY, tileWidth, tileHeight);
                    tileset.put(tile.getInt("id") + idOffset, texture);
                    offsetX += tileWidth;
                    if (offsetX == imgWidth) {
                        offsetX = 0;
                        offsetY += tileHeight;
                    }
                    tile = tile.next();
                }
            } else { // Tileset represents multiple images = multiple textures
                while (tile != null) {
                    JsonValue textureName = tile.get("type");
                    TextureRegion texture = null;
                    if (textureName != null) {
                        // Texture name may be empty, or may not refer to an actual texture
                        Texture t = directory.getEntry(textureName.asString(), Texture.class);
                        if (t != null) {
                            texture = new TextureRegion(directory.getEntry(textureName.asString(), Texture.class));
                        }
                    }
                    tileset.put(tile.getInt("id") + idOffset, texture);
                    tile = tile.next();
                }
            }
            levelTilesetJson = levelTilesetJson.next();
        }
        return tileset;
    }

    private static Comparator<TileRect> compareRect = new Comparator<TileRect>() {
        @Override
        public int compare(TileRect o1, TileRect o2) {
            return Integer.compare(o1.startX, o2.startX);
        }
    };

    public class TileRect {
        public int startX;
        public int startY;
        public int endX;
        public int endY;

        public TileRect(int startX, int startY, int endX, int endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        public String toString() {
            return "StartX: " + startX + " StartY: " + startY + " EndX: " + endX + " EndY: " + endY;
        }
    }

    public Array<TileRect> mergeTiles(int mapWidth, int mapHeight, int[] worldData) {
        Array<TileRect> rectangles = new Array<>();

        // This pass merges rectangles on the x-axis
        for (int y = 0; y <= mapHeight - 1; y++) {
            int startX = -1;
            int endX = -1;
            for (int x = 0; x <= mapWidth - 1; x++) {
                if (worldData[y * mapWidth + x] != 0) {
                    if (startX == -1) {
                        startX = x;
                    }
                    endX = x;
                } else if (startX != -1) {
                    TileRect newRect = new TileRect(startX, y, endX, y);
                    rectangles.add(newRect);
                    startX = -1;
                    endX = -1;
                }
            }
            if (startX != -1) {
                TileRect newRect = new TileRect(startX, y, endX, y);
                rectangles.add(newRect);
            }
        }

        Array<TileRect> mergedRects = new Array<>();
        rectangles.sort(compareRect);

        // This pass merges rectangles of equal width on the y-axis
        TileRect newRect = new TileRect(-1, -1, -1, -1);
        for (TileRect r : rectangles) {
            if (newRect.startY == -1) {
                newRect.startX = r.startX;
                newRect.startY = r.startY;
                newRect.endX = r.endX;
                newRect.endY = r.endY;
            } else if (r.startX == newRect.startX && r.endX == newRect.endX && r.startY == newRect.endY + 1) {
                newRect.endY = r.endY;
            } else {
                mergedRects.add(newRect);
                newRect = new TileRect(r.startX, r.startY, r.endX, r.endY);
            }
        }
        mergedRects.add(newRect);
        return mergedRects;
    }
}
