package edu.cornell.gdiac.bubblegumbandit.helpers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

import java.util.HashMap;

public class TiledParser {

    /** Returns the tileset based on the provided Json file
     *
     * @param directory the asset directory of the level
     * @param tileJson the json representing the tileset
     * @returns  the array representing the tileset */
    public static TextureRegion[] createTileset(AssetDirectory directory, JsonValue tileJson) {
        TextureRegion[] tileset = new TextureRegion[tileJson.getInt("tileCount") + 1];
        JsonValue tile = tileJson.get("tiles").child();
        while (tile != null) {
            JsonValue property = tile.get("properties").child();
            while (!property.get("name").asString().equals("file")) {
                property = property.next();
            }
            String textureName = property.get("value").asString();
            TextureRegion texture = new TextureRegion(directory.getEntry(textureName, Texture.class));
            tileset[tile.getInt("id") + 1] = texture; // +1 because 0 represents nothing
            tile = tile.next();
        }
        return tileset;
    }
}
