package edu.cornell.gdiac.json.gum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.json.GameCanvas;
import edu.cornell.gdiac.json.Sensor;
import edu.cornell.gdiac.json.Vision;
import edu.cornell.gdiac.physics.obstacle.BoxObstacle;
import edu.cornell.gdiac.physics.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

import java.lang.reflect.Field;

public class FloatingGum extends BoxObstacle {

    public FloatingGum() {
        super(0, 0, 1, 1);
        setSensor(true);
    }

    public void initialize(AssetDirectory directory, JsonValue json) {
        setName(json.name());
        setDensity(json.get("density").asFloat());
        setBodyType(BodyDef.BodyType.StaticBody);
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());

        // Reflection is best way to convert name to color
        Color debugColor;
        try {
            String cname = json.get("debugcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color)field.get(null));
        } catch (Exception e) {
            debugColor = null; // Not defined
        }
        int opacity = json.get("debugopacity").asInt();
        debugColor.mul(opacity/255.0f);
        setDebugColor(debugColor);

        String key = json.get("texture").asString();
        TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));
        setTexture(texture);
    }

    public void setPosition(JsonValue pos) {
        int[] p = pos.get("pos").asIntArray();
        setPosition(p[0] ,p[1]);
    }
}
