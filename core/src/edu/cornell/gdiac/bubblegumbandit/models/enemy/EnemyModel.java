package edu.cornell.gdiac.bubblegumbandit.models.enemy;

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

/**
 * Abstract enemy class.
 * <p>
 * Initialization is done by reading JSON level files.
 */
public abstract class EnemyModel extends CapsuleObstacle {

    /** EnemyModel's unique ID */
    private int id;

    /** The amount to slow the character down  */
    private float damping;

    /** Default vision radius for an EnemyModel */
    private final float DEFAULT_VISION_RADIUS = 7f;

    /** true if this EnemyModel is facing right; false if facing left */
    private boolean faceRight;

    /** true if the EnemyModel's feet are on the ground */
    private boolean isGrounded;

    /** Sensors associated with this EnemyModel  */
    private Sensor[] sensors;

    /**Color to represent an EnemyModel sensor */
    private Color sensorColor;

    /**EnemyModel's vision component for RayCasting */
    public Vision vision;

    /**Reference to the Box2D world */
    private World world;

    /** true if this EnemyModel is upside-down.*/
    protected boolean isFlipped;

    /** EnemyModel's y-scale: used for flipping gravity. */
    private float yScale;


    /**Returns this EnemyModel's unique integer ID.
     *
     * @returns this EnemyModel's unique ID. */
    public int getId() { return id; };


    /** Returns true if this EnemyModel is facing right;
     * otherwise, returns false.
     *
     * @return true if this EnemyModel is facing right;
     *        otherwise, false.*/
    public boolean getFaceRight(){
        return faceRight;
    }


    /** Makes this EnemyModel face right.
     *
     *@param isRight if this EnemyModel is facing right.*/
    public void setFaceRight(boolean isRight) {
        faceRight = isRight;
    }


    /**
     * Returns true if this EnemyModel is upside-down.
     *
     * @returns true if this EnemyModel is upside-down.
     * */
    public boolean isFlipped() {
        return isFlipped;
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
     * Creates an EnemyModel.
     *
     * @param world The Box2D world.
     * @param id The unique ID to assign to this EnemyModel.
     * */
    public EnemyModel(World world, int id) {
        super(0, 0, 0.5f, 1.0f);
        setFixedRotation(true);
        isGrounded = true;
        faceRight = true;
        isFlipped = false;
        yScale = 1f;
        this.world = world;
        this.id = id;
        setVision(DEFAULT_VISION_RADIUS);
    }

    /**
     * Assigns a Vision component to this EnemyModel with a specified
     * radius.
     *
     * @param radius the radius of the Vision
     * */
    protected void setVision(float radius){
        float range = (float) Math.PI/2;
        vision = new Vision(radius, 0f,range, Color.YELLOW);
    }


    /**
     * Initializes this EnemyModel's physics values from JSON.
     *
     * @param directory the asset manager
     * @param x the x position of this EnemyModel
     * @param y the y position of this EnemyModel
     * @param constantsJson the JSON subtree defining all EnemyModels
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
        setDamping(constantsJson.get("damping").asFloat());

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

        // initialize sensors
        int numSensors = constantsJson.get("numsensors").asInt();
        initializeSensors(constantsJson, numSensors);

        // Reflection is best way to convert name to color
        try {
            String cname = constantsJson.get("sensorcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            sensorColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            sensorColor = null; // Not defined
        }
        opacity = constantsJson.get("sensoropacity").asInt();
        sensorColor.mul(opacity / 255.0f);
        sensorColor = Color.RED;
    }

    /**
     * Initializes this EnemyModel's Sensor values from JSON.
     *
     * @param json the JSON subtree defining all EnemyModels
     * @param numSensors the number of Sensors on an EnemyModel
     */
    public void initializeSensors(JsonValue json, int numSensors) {
        // Get the sensor information
        sensors = new Sensor[numSensors];
        String sensorName;
        JsonValue sensor = json.get("sensors").child();
        for (int i = 0; i < numSensors; i++) {
            float[] sSize = sensor.get("sensorsize").asFloatArray();
            float[] sCenter = sensor.get("sensorcenter").asFloatArray();
            float[] printLoc = sensor.get("printOffset").asFloatArray();
            sensorName = sensor.name();
            sensors[i] = new Sensor(new Vector2(sCenter[0], sCenter[1]), sSize[0],
                sSize[1], sensorName, printLoc[0], printLoc[1]);
            sensor = sensor.next();
        }
    }


    /**
     * Main update loop for an EnemyModel. <p>
     *
     * Adjusts y-scale to match its "flipped" status.
     *
     * @param controlCode The code that tells this EnemyModel what to do.
     * @param dt Time since last frame.
     * */
    public void update(int controlCode, float dt) {
        if (yScale < 1f && !isFlipped) {
            yScale += 0.1f;
        } else if (yScale > -1f && isFlipped) {
            yScale -= 0.1f;
        }
        updateVision();
    }

    /**
     * Draws this EnemyModel.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            float effect = faceRight ? 1.0f : -1.0f;
            canvas.drawWithShadow(texture, Color.WHITE, origin.x, origin.y,
                    getX() * drawScale.x, getY() * drawScale.y,
                    getAngle(), effect, yScale);
        }
    }

    /**
     * Draws this EnemyModel in Debug Mode.
     *
     * @param canvas Drawing context
     */
    @Override
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        vision.drawDebug(canvas, getX(), getY(), drawScale.x, drawScale.y);
    }

    /**
     * Updates this EnemyModel's vision component.
     */
    public void updateVision() {
        vision.setDirection(faceRight? (float) 0 : (float) Math.PI);
        vision.update(world, getPosition());
    }

    /**
     * Creates the physics Body(s) for this EnemyModel and adds
     * them to the Box2D world.
     *
     * @param world Box2D world to store body
     *
     * @return true if object allocation succeeded; otherwise,
     * false.
     */
    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) {
            return false;
        }

        // Ground Sensor
        // -------------
        // We only allow the EnemyModel to jump when grounded.
        //
        // To determine if the EnemyModel is grounded,
        // create a thin sensor under its feet, which reports
        // collisions with the world but has no collision response.
        FixtureDef sensorDef;
        for (Sensor sensor : sensors) {
            sensorDef = new FixtureDef();
            sensorDef.density = getDensity();
            sensorDef.isSensor = true;
            sensorDef.shape = sensor.getSensorShape();
            sensor.setFixture(body.createFixture(sensorDef));
            sensor.getFixture().setUserData(sensor.getSensorName());
        }
        return true;
    }

    /**
     * Negates this EnemyModel's current "flipped" state (if it is
     * grounded).
     */
    public void flip() {
        isFlipped = !isFlipped;
    }
}
