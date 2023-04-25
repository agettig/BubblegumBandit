package edu.cornell.gdiac.bubblegumbandit.helpers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

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
                System.out.println("skipped "+levelTilesetJson.name);
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
}
