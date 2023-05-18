package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController;
import edu.cornell.gdiac.bubblegumbandit.helpers.Gummable;
import edu.cornell.gdiac.bubblegumbandit.helpers.Shield;
import edu.cornell.gdiac.bubblegumbandit.models.level.CrusherModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.TileModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.gum.GumModel;
import edu.cornell.gdiac.bubblegumbandit.view.AnimationController;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.CapsuleObstacle;

import edu.cornell.gdiac.physics.obstacle.Obstacle;
import java.lang.reflect.Field;
import java.util.HashSet;

import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.*;

/**
 * Abstract enemy class.
 * <p>
 * Initialization is done by reading the json
 */
public abstract class EnemyModel extends CapsuleObstacle implements Gummable, Shield {

    protected TextureRegion outline;

    /**
     * EnemyModel's unique ID
     */
    private int id;

    /**
     * The amount to slow the character down
     */
    private float damping;

    /**
     * true if this EnemyModel is facing right; false if facing left
     */
    protected boolean faceRight;

    // SENSOR FIELDS

    private Color sensorColor;

    //endRegion

    public RayCastCone vision;


    public RayCastCone getSensing() {
        return sensing;
    }

    private RayCastCone sensing;

    public RayCastCone attacking;

    /**
     * Ray casts used to detect blocks and hazards in enemy paths
     */
    public RayCastEnv envRays;

    /**
     * Reference to the Box2D world
     */
    protected World world;

    public int getNextAction() {
        return nextAction;
    }

    /** Index of the current frame of the animation playing
     * for this EnemyModel.*/
    private int currentFrameNum;

    protected int nextAction;

    protected int previousAction;

    public void setNextAction(int nextAction) {
        this.previousAction = this.nextAction;
        this.nextAction = nextAction;
    }

    /**
     * EnemyModel's y-scale: used for flipping gravity.
     */
    protected float yScale;

    /**
     * Animation controller, controls animations
     */
    protected AnimationController animationController;

    protected TextureRegion gummedTexture;

    protected TextureRegion squishedGum;
    protected TextureRegion squishedGumOutline;

    private CircleShape listeningCircle;
    /**
     * The name of the sensor for detection purposes
     */

    private String sensorName;
    /**
     * The color to paint the sensor in debug mode
     */


    /**
     * Texture of the gum overlay when gummed
     */
    protected TextureRegion gumTexture;

    private float speed;

    public static float WANDER_SPEED;

    public static float CHASE_SPEED;

    public static float PURSUE_SPEED;

    /**
     * tile that the robot is currently standing on, or last stood on if in the air
     */
    private TileModel tile;

    /**
     * Position of enemy in need of help
     */
    private Vector2 helpingTarget;

    // endRegion

    /**
     * Whether the enemy has a shield
     */
    private boolean hasShield;

    /**
     * Whether a shielded enemy's shield is lowered, happens during attacking
     */
    protected boolean isShielded;

    /**
     * texture for the shield surrounding an enemy
     */
    protected TextureRegion shield;

    /** The GumModel instance that stuck this EnemyModel. */
    private HashSet<GumModel> stuckGum;

    /** The current frame of the enemy */
    protected TextureRegion curFrame;

    protected CrusherModel crusher = null;

    protected float crushScale;

    protected boolean isCrushing = false;

    /** Start crushing the enemy */
    public void crush(CrusherModel crusher) {
        Filter f = getFilterData();
        f.maskBits = CollisionController.MASK_CRUSHED_ENEMY;
        setFilterData(f);
        this.crusher = crusher;
        isCrushing = true;
    }

    public void shouldCrush(CrusherModel crusher) {
        this.crusher = crusher;
    }

    protected int turnCooldown;

    // endRegion

    /**
     * Returns this EnemyModel's unique integer ID.
     *
     * @returns this EnemyModel's unique ID.
     */
    public int getId() {
        return id;
    }

    ;

    /**
     * Returns true if this EnemyModel is facing right;
     * otherwise, returns false.
     *
     * @return true if this EnemyModel is facing right;
     * otherwise, false.
     */
    public boolean getFaceRight() {
        return faceRight;
    }

    /**
     * Returns this EnemyModel's Y-Scale.
     *
     * @return this EnemyModel's Y-Scale.
     */
    public float getYScale() {
        return yScale;
    }

    /**
     * Makes this EnemyModel face right.
     *
     * @param isRight if this EnemyModel is facing right.
     */
    public void setFaceRight(boolean isRight) {
        if (turnCooldown <= 0){
            faceRight = isRight;
        }
    }

    public boolean setFaceRight(boolean isRight, int cooldown){
        if (turnCooldown <= 0){
            faceRight = isRight;
            turnCooldown = cooldown;
        }
        return faceRight;
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
     * Returns true if this EnemyModel is upside-down.
     *
     * @returns true if this EnemyModel is upside-down.
     */
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
     * @param id    The unique ID to assign to this EnemyModel.
     */
    public EnemyModel(World world, int id) {
        super(0, 0, 0.5f, 1.0f);
        setFixedRotation(true);
        faceRight = true;
        isFlipped = false;
        nextAction = CONTROL_NO_ACTION;
        yScale = 1f;
        this.world = world;
        this.id = id;
        vision = new RayCastCone(7f, 0f, (float) Math.PI / 2, Color.YELLOW);
        sensing = new RayCastCone(4f, (float) Math.PI, (float) Math.PI, Color.PINK);
        attacking = new RayCastCone(6f, 0, (float) Math.PI / 2, Color.BLUE);
        gummed = false;
        stuck = false;
        collidedObs = new ObjectSet<>();
        tile = null;
        helpingTarget = null;
        turnCooldown = 0;
    }

    public Vector2 getHelpingTarget() {
        return helpingTarget;
    }

    public void setHelpingTarget(Vector2 helpingTarget) {
        this.helpingTarget = helpingTarget;
    }


    /**
     * Initializes this EnemyModel's physics values from JSON.
     *
     * @param directory     the asset manager
     * @param x             the x position of this enemy
     * @param y             the y position of this enemy
     * @param constantsJson the JSON subtree defining all enemies
     * @param isFacingRight whether the enemy spawns facing right
     */
    public void initialize(AssetDirectory directory, float x, float y, JsonValue constantsJson, boolean isFacingRight) {
        setName("enemy" + id);
        float[] size = constantsJson.get("size").asFloatArray();
        setPosition(x, y);
        setDimension(size[0], size[1]);
        faceRight = isFacingRight;
        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        setBodyType(BodyDef.BodyType.DynamicBody);
        setDensity(constantsJson.get("density").asFloat());
        setFriction(constantsJson.get("friction").asFloat());
        setRestitution(constantsJson.get("restitution").asFloat());
        setDamping(constantsJson.get("damping").asFloat());
        WANDER_SPEED = constantsJson.get("wanderSpeed").asFloat();
        CHASE_SPEED = constantsJson.get("chaseSpeed").asFloat();
        PURSUE_SPEED = constantsJson.get("pursueSpeed").asFloat();
        speed = WANDER_SPEED;

        // Reflection is best way to convert name to color
        Color debugColor;
        try {
            String cname = constantsJson.get("debugColor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            debugColor = null; // Not defined
        }
        int opacity = constantsJson.get("debugOpacity").asInt();
        assert debugColor != null;
        debugColor.mul(opacity / 255.0f);
        setDebugColor(debugColor);

        String key = constantsJson.get("midairGumTexture").asString();
        squishedGum = new TextureRegion(directory.getEntry(key, Texture.class));


        key = constantsJson.get("midairGumOutlineTexture").asString();
        squishedGumOutline = new TextureRegion(directory.getEntry(key, Texture.class));



        // Now get the texture from the AssetManager singleton
        key = constantsJson.get("texture").asString();
        TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));

        gummedTexture = texture;

        key = constantsJson.get("outline").asString();
        outline = new TextureRegion(directory.getEntry(key, Texture.class));
        setTexture(texture);
        String animationKey;
        if (constantsJson.get("animations") != null) {
            animationKey = constantsJson.get("animations").asString();
            animationController = new AnimationController(directory, animationKey);
        }

        // Get the sensor information
        int listeningRadius = constantsJson.get("listeningRadius").asInt();

        listeningCircle = new CircleShape();
        listeningCircle.setRadius(listeningRadius);


        String gumKey = constantsJson.get("gumTexture").asString();
        gumTexture = new TextureRegion(directory.getEntry(gumKey, Texture.class));

        // initialize sensors

        // Reflection is best way to convert name to color
        try {
            String cname = constantsJson.get("sensorColor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            sensorColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            sensorColor = null; // Not defined
        }
        opacity = constantsJson.get("sensorOpacity").asInt();
        sensorColor.mul(opacity / 255.0f);
        sensorName = constantsJson.get("sensorName").asString();
        sensorColor.mul(opacity / 255.0f);
        sensorColor = Color.RED;

        String shieldKey = constantsJson.get("shield").asString();
        shield = new TextureRegion(directory.getEntry(shieldKey, Texture.class));

        stuckGum = new HashSet<>();

        envRays = new RayCastEnv(Color.GREEN, getHeight());
    }

    public CircleShape getListeningCircle() {
        return listeningCircle;
    }

    public boolean isCrushing() {
        return isCrushing;
    }

    /** End the crush without destroying the enemy (enemy not fully squished) */
    public void endCrush() {
        crusher = null;
        isCrushing = false;
        Filter f = getFilterData();
        f.maskBits = CollisionController.MASK_ENEMY;
        setFilterData(f);
        crushScale = 1;
    }


    public void update(float delta) {
        if (!isFlipped && yScale < 1) {
            if (yScale != -1 || !stuck) {
                yScale += 0.1f;
            }
        } else if (isFlipped && yScale > -1) {
            if (yScale != 1 || !stuck) {
                yScale -= 0.1f;
            }
        }
        updateRayCasts();
        updateMovement(nextAction);
        updateFrame();
        updateCrush();
    }

    public void updateCrush() {
        if (crusher != null) {
            if (isCrushing) {
                if (world.getGravity().y < 0) {
                    float bottomOfCrusher = crusher.getY() - (crusher.getHeight() / 2f);
                    float bottomOfEnemy = getY() - (getHeight() / 2);
                    crushScale = (bottomOfCrusher - bottomOfEnemy) / getHeight();
                    if (crushScale < -0.1f) {
                        endCrush();
                    }
                    else if (crushScale <= 0.05f) {
                        markRemoved(true);
                    } else if (crushScale > 1) {
                        endCrush();
                    }
                } else {
                    float topOfCrusher = crusher.getY() + (crusher.getHeight() / 2f);
                    float topOfEnemy = getY() + (getHeight() / 2);
                    crushScale = (topOfEnemy - topOfCrusher) / getHeight();
                    if (crushScale < -0.1f) {
                        endCrush();
                    }
                    else if (crushScale <= 0.05f) {
                        markRemoved(true);
                    } else if (crushScale > 1) {
                        endCrush();
                    }
                }
            } else {
                boolean shouldStartCrush = false;
                for (Obstacle ob : getCollisions()) {
                    if (!(ob instanceof CrusherModel)) {
                        shouldStartCrush = true;
                    }
                }
                if (shouldStartCrush) {
                    crush(crusher);
                }
            }
        } else {
            crushScale = 1;
        }
    }

    /** Update the frame of the animation */
    protected void updateFrame() {
        curFrame = texture;
        if(animationController!=null) {
            currentFrameNum = animationController.getFrameNum();
            curFrame = animationController.getFrame();
        }
    }

    public boolean fired() {
        return (nextAction & CONTROL_FIRE) == CONTROL_FIRE;
    }


    public RayCastCone getAttacking() {
        return attacking;
    }

    public int getCurrentFrameNum(){return currentFrameNum;}

    public void updateMovement(int nextAction){
        // Determine how we are moving.

        // turn if block or hazard in the way
        if (envRays.getBodies().size > 0) {
            setFaceRight(!faceRight, 60);
            return;
        }
        boolean movingLeft = (nextAction & CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (nextAction & CONTROL_MOVE_RIGHT) != 0;
        boolean movingUp = (nextAction & CONTROL_MOVE_UP) != 0;
        boolean movingDown = (nextAction & CONTROL_MOVE_DOWN) != 0;

        // Process movement command.
        if (movingLeft) {
            if ((previousAction & CONTROL_MOVE_LEFT) == 0) {
                setY((int) getY() + .5f);
            }

            boolean faceRight = setFaceRight(false, 60);

            if (!faceRight) {
                setVX(-speed);
            }
            else{
                setVX(0);
            }
        } else if (movingRight) {
            if ((previousAction & CONTROL_MOVE_RIGHT) == 0) {
                setY((int) getY() + .5f);
            }
            boolean faceRight = setFaceRight(true, 60);

            if (faceRight) {
                setVX(speed);
            }
            else{
                setVX(0);
            }
        } else if (movingUp) {

            if (!isFlipped) {
                setX((int) getPosition().x + .5f);
                setVY(speed);
            } else {
                setX((int) getPosition().x + .5f);
            }
            setVX(0);
        } else if (movingDown) {
            if (isFlipped) {
                setX((int) getPosition().x + .5f);
                setVY(-speed);
            } else {
                setX((int) getPosition().x + .5f);
            }
            setVX(0);
        } else {
            setVX(0);
            setVX(0);
        }
    }

    /**
     * Draws this EnemyModel.
     * Draws this EnemyModel.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
            float effect = faceRight ? 1.0f : -1.0f;
            float x = getX() * drawScale.x;
            float y = getY() * drawScale.y;
            y += ((1 - crushScale) * texture.getRegionHeight() * (world.getGravity().y < 0 ? -.5f : .5f));
            float gumY = y;
            float gumX = x;

            if (animationController != null) {
                x -= (getWidth() / 2) * drawScale.x * effect;
            }

            if (curFrame != null) {
                canvas.drawWithShadow(curFrame, Color.WHITE, origin.x, origin.y, x, y, getAngle(), effect, yScale * crushScale);
            }

            //if gummed, overlay with gumTexture
            if (gummed) {
                //if speed is below threshold, draw static gum
                if(stuck) {
                    //gumY += yScale*gumTexture.getRegionHeight()/2;
                    canvas.draw(gumTexture, Color.WHITE, origin.x, origin.y, gumX,
                            gumY, getAngle(), 1, yScale*crushScale);
                } else {
                   // gumY += yScale*squishedGum.getRegionHeight()/2;
                    canvas.draw(squishedGum, Color.WHITE, origin.x, origin.y, gumX,
                       gumY-yScale*squishedGum.getRegionHeight()/2, getAngle(), 1, yScale*crushScale);
                }
//
            }

            //if shielded, overlay shield
            if (isShielded) {
                canvas.draw(shield, Color.WHITE, origin.x, origin.y, (getX() - (getDimension().x / 2)) * drawScale.x,
                        y - shield.getRegionHeight() / 8f * yScale, getAngle(), 1, yScale*crushScale);
            }
//            color = new Color(1f,0.8f,1f,1); //honestly a nice color filter
        }

    /**
     * Draw method for when highlighting the enemy before unsticking them
     */
    public void drawWithOutline(GameCanvas canvas) {
        if (outline != null && gummedTexture != null) {
            if (stuck) {
                float y = getY() * drawScale.y; //-yScale*outline.getRegionHeight()/2;
                canvas.draw(outline, Color.WHITE, origin.x, origin.y, getX()*drawScale.x-5,
                        y - 5 * yScale, getAngle(), 1, yScale);
            } else {
                float y = getY() * drawScale.y; //-yScale*squishedGumOutline.getRegionHeight()/2;
                canvas.draw(squishedGumOutline, Color.WHITE, origin.x, origin.y,
                    getX()*drawScale.x-5,
                    y-5*yScale-yScale*squishedGumOutline.getRegionHeight()/2, getAngle(), 1, yScale);
            }
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
        canvas.drawPhysics(listeningCircle, sensorColor, getX(), getY(), drawScale.x, drawScale.y);
//        canvas.drawPhysics(robotShape, sensorColor, getX(), getY(), 0, drawScale.x, drawScale.y);
        vision.drawDebug(canvas, getX(), getY(), drawScale.x, drawScale.y);
        sensing.drawDebug(canvas, getX(), getY(), drawScale.x, drawScale.y);
        attacking.drawDebug(canvas, getX(), getY(), drawScale.x, drawScale.y);
        envRays.drawDebug(canvas, getX(), getY(), drawScale.x, drawScale.y);
    }

    /**
     * Updates this EnemyModel's vision component.
     */
    public void updateRayCasts() {

        vision.setDirection(faceRight ? (float) 0 : (float) Math.PI);
        sensing.setDirection(!faceRight ? (float) 0 : (float) Math.PI);
        attacking.setDirection(faceRight ? (float) 0 : (float) Math.PI);
        envRays.setFaceRight(faceRight);
        vision.update(world, getPosition());
        sensing.update(world, getPosition());
        attacking.update(world, getPosition());
        envRays.update(world, getPosition());
    }

    /**
     * Creates the physics Body(s) for this EnemyModel and adds
     * them to the Box2D world.
     *
     * @param world Box2D world to store body
     * @return true if object allocation succeeded; otherwise,
     * false.
     */
    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) {
            return false;
        }
        return true;
    }


    /**
     * Passes in an instance of a GumModel that stuck this EnemyModel.
     *
     * @param gum The instance of the GumModel that stuck this EnemyModel.
     * */
    public void stickWithGum(GumModel gum){
        if(gum == null) return;
        if(stuckGum == null) stuckGum = new HashSet<>();
        stuckGum.add(gum);
    }

    /**
     * Returns a HashSet of GumModels that have stuck this EnemyModel.
     *
     * @return a HashSet of GumModels that have stuck this EnemyModel
     * */
    public HashSet<GumModel> getStuckGum(){
        return new HashSet<>(stuckGum);
    }

    /**
     * Empties the HashSet of GumModels that have stuck
     * this EnemyMode.
     * */
    protected void clearStuckGum(){
        stuckGum.clear();
    }

    /**
     * Flips the player's angle and direction when the world gravity is flipped
     * /**
     * Negates this EnemyModel's current "flipped" state (if it is
     * grounded).
     */
    public void flipGravity() {
        if (!(getStuck())) isFlipped = !isFlipped;
    }


    public void changeSpeed(float speed) {
        this.speed = speed;
    }

    public float getYFeet() {
        return getY();
    }


    //shielded attributes
    public void hasShield(boolean value) {
        hasShield = value;
    }

    public boolean isShielded() {
        return isShielded;
    }

    public void isShielded(boolean value) {
        if (hasShield) {
            isShielded = value;
        }
    }

}