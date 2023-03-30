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
    public static HashMap<Integer, TextureRegion> createTileset(AssetDirectory directory, JsonValue tileJson) {
        HashMap<Integer, TextureRegion> tileset = new HashMap<>();
        JsonValue tile = tileJson.get("tiles").child();
        while (tile != null) {
            String textureName = tile.getString("type");
            System.out.println(textureName);
            TextureRegion texture = new TextureRegion(directory.getEntry(textureName, Texture.class));
            tileset.put(tile.getInt("id") + 1, texture);
            tile = tile.next();
        }
        return tileset;
    }
}
