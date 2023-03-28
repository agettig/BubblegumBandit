package edu.cornell.gdiac.bubblegumbandit.helpers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

import java.util.HashMap;

public class TiledParser {

    /** Returns the tileset based on the provided Json file
     *
     * @param directory the asset directory of the level
     * @param levelJson the json representing the level
     * @returns  the array representing the tileset */
    public static HashMap<Integer, TextureRegion> createTileset(AssetDirectory directory, JsonValue levelJson) {
        HashMap<Integer, TextureRegion> tileset = new HashMap<>();
        JsonValue tileJson = levelJson.get("tilesets").child();
        while (tileJson != null) {
            int idOffset = tileJson.getInt("firstgid");
            JsonValue tile = tileJson.get("tiles").child();
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
            tileJson = tileJson.next();
        }
        return tileset;
    }
}
