package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

import java.lang.reflect.Field;

public class Collectible extends WheelObstacle {

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

    public Collectible() {
        super(0, 0, 1f);
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

    /** Initializes a new piece of floating gum.
     *
     * @param directory the asset directory for the game
     * @param x  the x position of the gum
     * @param y  the y position of the gum
     * @param scale the scale of the world
     * @param json  the constants json of a floating gum
     */
    public void initialize(AssetDirectory directory, float x, float y, Vector2 scale, JsonValue json) {
        setX(x);
        initialY = y;
        setY(y);
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
        setRadius(texture.getRegionWidth() / (2.0f * scale.x));
        setDrawScale(scale);
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

    public void draw(GameCanvas canvas) {
        if (texture != null) {
            canvas.drawWithShadow(texture,Color.WHITE,texture.getRegionWidth() / 2f,texture.getRegionHeight() /2f ,getX()*drawScale.x,getY()*drawScale.y,getAngle(),1,1);
        }
    }

}
