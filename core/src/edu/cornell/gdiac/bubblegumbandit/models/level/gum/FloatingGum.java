package edu.cornell.gdiac.bubblegumbandit.models.level.gum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics.obstacle.BoxObstacle;

import java.lang.reflect.Field;

public class FloatingGum extends BoxObstacle {

    /** Whether the gum has been collected */
    private boolean collected;

    /** The timer used to bob the gum */
    private float counter;

    /** The base y position of the floating gum */
    private float initialY;

    // FLOATING GUM CONSTANTS
    /** The amount the gum bobs in physics coordinates */
    private float bobAmount;

    /** The speed the gum bobs */
    private float bobSpeed;

    public FloatingGum() {
        super(0, 0, 0.5f, 0.5f);
        setSensor(true);
        collected = false;
        // Start at random time so gum doesn't always bob together
        counter = (float) (Math.random() * Math.PI * 2);
    }
    public void setCollected(boolean bool) {
        collected = bool;
    }

    public boolean getCollected() {
        return collected;
    }
    public void initialize(AssetDirectory directory, JsonValue json) {
        setName(json.name());
        setDensity(json.get("density").asFloat());
        setBodyType(BodyDef.BodyType.KinematicBody);
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());
        bobAmount = json.get("bobAmount").asFloat();
        bobSpeed = json.get("bobSpeed").asFloat();

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
        initialY = p[1];
    }

    public void update(float dt) {
        counter += dt * bobSpeed;
        setY(initialY + bobAmount * (float) Math.sin(counter));

        // Just roll over to prevent overflow
        if (counter >= Math.PI * 20) {
            counter -= Math.PI * 20;
        }
    }

}
