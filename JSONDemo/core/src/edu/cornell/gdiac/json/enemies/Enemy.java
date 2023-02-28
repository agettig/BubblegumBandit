package edu.cornell.gdiac.json.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.json.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.CapsuleObstacle;

import java.lang.reflect.Field;

/**
 * Abstract enemy class.
 *
 * Initialization is done by reading the json
 * Note, enemies can only be initiated as stationary or moving enemies
 */
public abstract class Enemy extends CapsuleObstacle {

    // Physics constants

    /** The factor to multiply by the input */
    private float force;
    /** The amount to slow the character down */
    private float damping;
    /** The maximum character speed */
    private float maxspeed;
    /** The current horizontal movement of the character */
    private float   movement;
    /** Which direction is the character facing */
    private boolean faceRight;
    /** Cache for flipping player orientation */
    private float angle;

    /**
     * Returns left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovement() {
        return movement;
    }

    /**
     * Sets left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @param value left/right movement of this character.
     */
    public void setMovement(float value) {
        movement = value;
        // Change facing if appropriate
        if (movement < 0) {
            faceRight = false;
        } else if (movement > 0) {
            faceRight = true;
        }
    }

    /**
     * Returns how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return force;
    }

    /**
     * Sets how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @param value	how much force to apply to get the dude moving
     */
    public void setForce(float value) {
        force = value;
    }

    /**
     * Returns how hard the brakes are applied to get a dude to stop moving
     *
     * @return how hard the brakes are applied to get a dude to stop moving
     */
    public float getDamping() {
        return damping;
    }

    /**
     * Sets how hard the brakes are applied to get a dude to stop moving
     *
     * @param value	how hard the brakes are applied to get a dude to stop moving
     */
    public void setDamping(float value) {
        damping = value;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return maxspeed;
    }

    /**
     * Sets the upper limit on dude left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @param value	the upper limit on dude left-right movement.
     */
    public void setMaxSpeed(float value) {
        maxspeed = value;
    }

    public Enemy(float x, float y, float width, float height) {
        super(0,0,0.5f,1.0f);
        setFixedRotation(true);
    }

    /**
     * Initializes the dude via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the dude subtree
     *
     * @param directory the asset manager
     * @param json		the JSON subtree defining the dude
     */
    public void initialize(AssetDirectory directory, JsonValue json) {
        setName(json.name());
        float[] pos  = json.get("pos").asFloatArray();
        float[] size = json.get("size").asFloatArray();
        setPosition(pos[0],pos[1]);
        setDimension(size[0],size[1]);

        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        setBodyType(json.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(json.get("density").asFloat());
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());
        setForce(json.get("force").asFloat());
        setDamping(json.get("damping").asFloat());
        setMaxSpeed(json.get("maxspeed").asFloat());

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
        assert debugColor != null;
        debugColor.mul(opacity/255.0f);
        setDebugColor(debugColor);

        // Now get the texture from the AssetManager singleton
        String key = json.get("texture").asString();
        TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));
        setTexture(texture);

          // Get the sensor information
//        Vector2 sensorCenter = new Vector2(0, -getHeight()/2);
//        float[] sSize = json.get("sensorsize").asFloatArray();
//        sensorShape = new PolygonShape();
//        sensorShape.setAsBox(sSize[0], sSize[1], sensorCenter, 0.0f);
//
//        // Reflection is best way to convert name to color
//        try {
//            String cname = json.get("sensorcolor").asString().toUpperCase();
//            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
//            sensorColor = new Color((Color)field.get(null));
//        } catch (Exception e) {
//            sensorColor = null; // Not defined
//        }
//        opacity = json.get("sensoropacity").asInt();
//        sensorColor.mul(opacity/255.0f);
//        sensorName = json.get("sensorname").asString();
    }

    public abstract void applyForce();

    public abstract void update();

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            float effect = faceRight ? 1.0f : -1.0f;
            canvas.draw(texture,Color.RED,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
    }

    /**
     * Flips the player's angle and direction when the world gravity is flipped
     *
     * */
    public void flippedGravity(){
        angle = body.getAngle() == 0 ? 3.14f : 0f;
        body.setTransform(body.getPosition(), angle);
        faceRight = !faceRight;
    }
}
