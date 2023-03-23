package edu.cornell.gdiac.json;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics.obstacle.BoxObstacle;

import java.lang.reflect.Field;

/**
 * An obstacle representing non-interactable elements of the background that are affected by changes in gravity
 */
public class BackObjModel extends BoxObstacle {

    public BackObjModel(float x, float y, float width, float height){
        super(x, y, width, height);
    }

    public BackObjModel(){
        super (0, 0, 1, 1);
    }

    /**
     * Initializes the dude via the given JSON value
     * <p>
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the dude subtree
     *
     * @param directory the asset manager
     * @param json      the JSON subtree defining the dude
     */
    public void initialize(AssetDirectory directory, JsonValue json, JsonValue info) {
        setName(json.get("name").asString());

        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        setBodyType(BodyDef.BodyType.DynamicBody);
        setDensity(json.get("density").asFloat());
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());

        // Reflection is best way to convert name to color
        Color debugColor;
        try {
            String cname = json.get("debugcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            debugColor = null; // Not defined
        }
        int opacity = json.get("debugopacity").asInt();
        assert debugColor != null;
        debugColor.mul(opacity / 255.0f);
        setDebugColor(debugColor);

        String key = info.get("texture").asString();
        TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));
        setInfo(info, texture);
    }

    public void setInfo(JsonValue info, TextureRegion texture){
        int[] p = info.get("pos").asIntArray();
        setPosition(p[0] ,p[1]);
        setTexture(texture);

        setWidth(texture.getRegionWidth()/64f);
        setHeight(texture.getRegionHeight()/64f);
    }
}
