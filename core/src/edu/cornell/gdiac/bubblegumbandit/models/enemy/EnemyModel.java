package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.bubblegumbandit.Sensor;
import edu.cornell.gdiac.physics.obstacle.CapsuleObstacle;

import java.lang.reflect.Field;

import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.*;

/**
 * Abstract enemy class.
 * <p>
 * Initialization is done by reading the json
 * Note, enemies can only be initiated as stationary or moving enemies
 */
public abstract class EnemyModel extends CapsuleObstacle implements Telegraph {

    // Physics constants
    private int id;

    private boolean heardPlayer = false;

    /**
     * The factor to multiply by the input
     */
    private float force;
    /**
     * The amount to slow the character down
     */
    private float damping;
    /**
     * The maximum character speed
     */
    private float maxspeed;
    /**
     * The current horizontal movement of the character
     */
    private float movement;
    /**
     * Which direction is the character facing
     */
    private boolean faceRight;
    /**
     * Cache for flipping player orientation
     */
    private float angle;
    /**
     * Whether our feet are on the ground
     */
    private boolean isGrounded;

    // SENSOR FIELDS
    /**
     * Ground sensor to represent our feet
     */
    private Sensor[] sensors;
    private Color sensorColor;

    //endRegion

    public Vision vision;

    private Vision sensing;

    private World world;

    /**
     * Cache for internal force calculations
     */
    private Vector2 forceCache = new Vector2();

    /**
     * Whether this enemy is flipped
     */
    protected boolean isFlipped;

    /**
     * The y scale of this enemy (for flipping when gravity swaps)
     */
    private float yScale;

    // SENSOR FIELDS
    /** Ground sensor to represent our feet */
    private Fixture sensorFixture;
    private CircleShape sensorShape;
    /** The name of the sensor for detection purposes */
    private String sensorName;
    /** The color to paint the sensor in debug mode */


    // endRegion

    /**
     * Returns left/right movement of this character.
     * <p>
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovement() {
        return movement;
    }

    /**
     * Sets left/right movement of this character.
     * <p>
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
     * <p>
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return force;
    }

    /**Returns this enemy's ID
     *
     * @returns the id of this enemy*/
    public int getId(){ return id; };

    /**
     * Sets how much force to apply to get the dude moving
     * <p>
     * Multiply this by the input to get the movement value.
     *
     * @param value how much force to apply to get the dude moving
     */
    public void setForce(float value) {
        force = value;
    }

    /** Returns whether or not the dude is facing right
     *
     * @return whether or not the enemy is facing right*/
    public boolean getFaceRight(){
        return faceRight;
    }

    /** Changes the direction the dude is facing
     *
     *@param isRight whether or not the dude is facing right*/
    public void setFaceRight(boolean isRight) {
        faceRight = isRight;
    }

    public boolean isFlipped() {
        return isFlipped;
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
     * @param value how hard the brakes are applied to get a dude to stop moving
     */
    public void setDamping(float value) {
        damping = value;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     * <p>
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return maxspeed;
    }

    /**
     * Sets the upper limit on dude left-right movement.
     * <p>
     * This does NOT apply to vertical movement.
     *
     * @param value the upper limit on dude left-right movement.
     */
    public void setMaxSpeed(float value) {
        maxspeed = value;
    }

    /**
     * Returns true if the dude is on the ground.
     *
     * @return true if the dude is on the ground.
     */
    public boolean isGrounded() {
        return isGrounded;
    }

    /**
     * Sets whether the dude is on the ground.
     *
     * @param value whether the dude is on the ground.
     */
    public void setGrounded(boolean value) {
        isGrounded = value;
    }

    public EnemyModel(World world, int id) {
        super(0, 0, 0.5f, 1.0f);
        setFixedRotation(true);
        isGrounded = true;
        faceRight = true;
        isFlipped = false;
        yScale = 1f;
        this.world = world;
        this.id = id;
        vision = new Vision(7f, 0f, (float) Math.PI/2, Color.YELLOW);
        sensing = new Vision(4f, (float) Math.PI, (float) Math.PI, Color.PINK);
    }

    /**
     * Initializes the dude via the given JSON value
     * <p>
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the dude subtree
     *
     * @param directory the asset manager
     * @param id the id of this enemy
     * @param x the x position of this enemy
     * @param y the y position of this enemy
     * @param constantsJson the JSON subtree defining all enemies
     */
    public void initialize(AssetDirectory directory, float x, float y, JsonValue constantsJson) {
        setName("enemy" + id);
        float[] size = constantsJson.get("size").asFloatArray();
        setPosition(x, y);
        setDimension(size[0], size[1]);

        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        setBodyType(BodyDef.BodyType.DynamicBody);
        setDensity(constantsJson.get("density").asFloat());
        setFriction(constantsJson.get("friction").asFloat());
        setRestitution(constantsJson.get("restitution").asFloat());
        setForce(constantsJson.get("force").asFloat());
        setDamping(constantsJson.get("damping").asFloat());
        setMaxSpeed(constantsJson.get("maxspeed").asFloat());

        // Reflection is best way to convert name to color
        Color debugColor;
        try {
            String cname = constantsJson.get("debugcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            debugColor = null; // Not defined
        }
        int opacity = constantsJson.get("debugopacity").asInt();
        assert debugColor != null;
        debugColor.mul(opacity / 255.0f);
        setDebugColor(debugColor);

        // Now get the texture from the AssetManager singleton
        String key = constantsJson.get("texture").asString();
        TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));
        setTexture(texture);

        // Get the sensor information
        int listeningRadius = constantsJson.get("listeningradius").asInt();

        sensorShape = new CircleShape();
        sensorShape.setRadius(listeningRadius);

        // Reflection is best way to convert name to color
        try {
            String cname = constantsJson.get("sensorcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            sensorColor = new Color((Color)field.get(null));
        } catch (Exception e) {
            sensorColor = null; // Not defined
        }
        opacity = constantsJson.get("sensoropacity").asInt();
        sensorColor.mul(opacity/255.0f);
        sensorName = constantsJson.get("sensorname").asString();
    }


    public void update(int controlCode) {
        if (yScale < 1f && !isFlipped) {
            yScale += 0.1f;
        } else if (yScale > -1f && isFlipped) {
            yScale -= 0.1f;
        }
        updateVision();

    }



    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            float effect = faceRight ? 1.0f : -1.0f;
            float yFlip = isFlipped ? -1 : 1;
            canvas.drawWithShadow(texture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x,
                getY() * drawScale.y, getAngle(), effect, yScale);
            vision.draw(canvas, getX(), getY(), drawScale.x, drawScale.y);
            sensing.draw(canvas, getX(), getY(), drawScale.x, drawScale.y);
        }
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape, sensorColor, getX(), getY(), drawScale.x, drawScale.y);
        vision.drawDebug(canvas, getX(), getY(), drawScale.x, drawScale.y);
        sensing.drawDebug(canvas, getX(), getY(), drawScale.x, drawScale.y);
    }

    public void updateVision() {
        vision.setDirection(faceRight? (float) 0 : (float) Math.PI);
        sensing.setDirection(!faceRight? (float) 0 : (float) Math.PI);
        vision.update(world, getPosition());
        sensing.update(world, getPosition());
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     *
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        // Ground Sensor
        // -------------
        // We only allow the dude to jump when he's on the ground.
        // Double jumping is not allowed.
        //
        // To determine whether or not the dude is on the ground,
        // we create a thin sensor under his feet, which reports
        // collisions with the world but has no collision response.
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = getDensity();
        sensorDef.isSensor = true;
        sensorDef.shape = sensorShape;
        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.getFilterData().categoryBits = CATEGORY_ENEMY_LISTENING;
        sensorFixture.getFilterData().maskBits = MASK_ENEMY_LISTENING;
        sensorFixture.setUserData(sensorName);
        return true;
    }

    /**
     * Shoots at a target position.
     *
     * @param targetPosition the screen position to shoot at.
     * */
    public void shoot(Vector2 targetPosition){
        return;
    }

    /**
     * Flips the player's angle and direction when the world gravity is flipped
     */
    public void flippedGravity() {
        isFlipped = !isFlipped;
    }

    public void setHeardPlayer(boolean heardPlayer){
          this.heardPlayer = heardPlayer;
    }

}
