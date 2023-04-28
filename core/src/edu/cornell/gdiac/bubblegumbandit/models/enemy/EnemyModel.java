package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.helpers.Gummable;
import edu.cornell.gdiac.bubblegumbandit.models.level.TileModel;
import edu.cornell.gdiac.bubblegumbandit.view.AnimationController;
import edu.cornell.gdiac.bubblegumbandit.models.FlippingObject;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.CapsuleObstacle;

import java.lang.reflect.Field;

import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.*;

/**
 * Abstract enemy class.
 * <p>
 * Initialization is done by reading the json
 */
public abstract class EnemyModel extends CapsuleObstacle implements Gummable {

    private TextureRegion outline;

    /** EnemyModel's unique ID */
    private int id;

    /** The amount to slow the character down  */
    private float damping;

    /** true if this EnemyModel is facing right; false if facing left */
    private boolean faceRight;

    // SENSOR FIELDS

    private Color sensorColor;

    //endRegion

    public RayCastCone vision;


    public RayCastCone getSensing() {
        return sensing;
    }

    private RayCastCone sensing;

    public RayCastCone attacking;

    /**Reference to the Box2D world */
    private World world;

    public int getNextAction() {
        return nextAction;
    }

    protected int nextAction;

    protected int previousAction;

    public void setNextAction(int nextAction) {
        this.previousAction = this.nextAction;
        this.nextAction = nextAction;
    }

    /** EnemyModel's y-scale: used for flipping gravity. */
    protected float yScale;

    /**
     * Animation controller, controls animations
     */
    protected AnimationController animationController;

    private TextureRegion gummedTexture;

    private TextureRegion squishedGum;
    private TextureRegion squishedGumOutline;

    private CircleShape sensorShape;
    /** The name of the sensor for detection purposes */


    private String sensorName;
    /** The color to paint the sensor in debug mode */
    private TextureRegion gummed_robot;



    /** Texture of the gum overlay when gummed */
    private TextureRegion gumTexture;

    private float speed;

    public static float WANDER_SPEED;

    public static float CHASE_SPEED;

    public static float PURSUE_SPEED;

    /**tile that the robot is currently standing on, or last stood on if in the air */
    private TileModel tile;

    /** Position of enemy in need of help */
    private Vector2 helpingTarget;

    // endRegion

    // Stuck in Gum Fields





    // endRegion

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

    /** Returns this EnemyModel's Y-Scale.
     *
     * @return this EnemyModel's Y-Scale.
     * */
    public float getYScale(){ return yScale;}

    /** Makes this EnemyModel face right.
     *
     *@param isRight if this EnemyModel is facing right.*/
    public void setFaceRight(boolean isRight) {
        faceRight = isRight;
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
        faceRight = true;
        isFlipped = false;
        nextAction = CONTROL_NO_ACTION;
        yScale = 1f;
        this.world = world;
        this.id = id;
        vision = new RayCastCone(7f, 0f, (float) Math.PI/2, Color.YELLOW);
        sensing = new RayCastCone(4f, (float) Math.PI, (float) Math.PI, Color.PINK);
        attacking = new RayCastCone(6f, 0, (float) Math.PI/2, Color.BLUE);
        gummed = false;
        stuck = false;
        collidedObs = new ObjectSet<>();
        tile = null;
        helpingTarget = null;
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
     * @param directory the asset manager
     * @param x the x position of this enemy
     * @param y the y position of this enemy
     * @param constantsJson the JSON subtree defining all enemies
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

        squishedGum = new TextureRegion(directory.getEntry("splatGum", Texture.class));
        squishedGumOutline = new TextureRegion(directory.getEntry("stuckOutline", Texture.class));

        // Now get the texture from the AssetManager singleton
        String key = constantsJson.get("texture").asString();
        TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));

        gummedTexture = texture;

        key = constantsJson.get("outline").asString();
        outline = new TextureRegion(directory.getEntry(key, Texture.class));
        setTexture(texture);
        String animationKey;
        if(constantsJson.get("animations")!=null) {
            animationKey = constantsJson.get("animations").asString();
            animationController = new AnimationController(directory, animationKey);
        }

        // Get the sensor information
        int listeningRadius = constantsJson.get("listeningRadius").asInt();

        sensorShape = new CircleShape();
        sensorShape.setRadius(listeningRadius);


        String gumKey = constantsJson.get("gumTexture").asString();
        gumTexture = new TextureRegion(directory.getEntry(gumKey, Texture.class));

        // initialize sensors

        // Reflection is best way to convert name to color
        try {
            String cname = constantsJson.get("sensorColor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            sensorColor = new Color((Color)field.get(null));
        } catch (Exception e) {
            sensorColor = null; // Not defined
        }
        opacity = constantsJson.get("sensorOpacity").asInt();
        sensorColor.mul(opacity/255.0f);
        sensorName = constantsJson.get("sensorName").asString();
        sensorColor.mul(opacity / 255.0f);
        sensorColor = Color.RED;

    }

    public CircleShape getSensorShape() {
        return sensorShape;
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
    }

    public boolean fired(){
        return (nextAction & CONTROL_FIRE) == CONTROL_FIRE;
    }

    public RayCastCone getAttacking() {
        return attacking;
    }

    public void updateMovement(int nextAction){
        // Determine how we are moving.
        boolean movingLeft  = (nextAction & CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (nextAction & CONTROL_MOVE_RIGHT) != 0;
        boolean movingUp    = (nextAction & CONTROL_MOVE_UP) != 0;
        boolean movingDown  = (nextAction & CONTROL_MOVE_DOWN) != 0;

        // Process movement command.
        if (movingLeft) {
            if ((previousAction & CONTROL_MOVE_LEFT) == 0){
                setY((int) getY() + .5f);
            }
            setVX(-speed);
            setFaceRight(false);
        } else if (movingRight) {
            if ((previousAction & CONTROL_MOVE_RIGHT) == 0){
                setY((int) getY() + .5f);
            }
            setVX(speed);
            setFaceRight(true);
        } else if (movingUp) {

            if (!isFlipped){
               setX((int) getPosition().x + .5f);
                setVY(speed);
            }
            else{
                setX((int) getPosition().x + .5f);
            }
            setVX(0);
        } else if (movingDown) {
            if (isFlipped){
                setX((int) getPosition().x + .5f);
                setVY(-speed);
            }
            else{
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
        if (texture != null) {
            float effect = faceRight ? 1.0f : -1.0f;
            TextureRegion drawn = texture;
            float x = getX() * drawScale.x;
            if(animationController!=null) {
                drawn = animationController.getFrame();
                 x-=getWidth()/2*drawScale.x*effect;
            }
//            if (stuck || gummed){
//                drawn = gummedTexture;
//            }

            // TODO: Fix rolling robots so don't have to do this
            float y = getY() * drawScale.y;
            if (getName().equals("mediumEnemy")||getName().equals("shieldedMediumEnemy")) {
                y += 10*yScale;
            }

            //if gum, overlay with gumTexture
            if (gummed) {

                canvas.drawWithShadow(drawn, Color.WHITE, origin.x, origin.y, getX() * drawScale.x,
                        y, getAngle(), effect, yScale);
                if(getVY()==0) {
                    canvas.draw(gumTexture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x,
                        y, getAngle(), 1, yScale);
                } else {
                    canvas.draw(squishedGum, Color.WHITE, origin.x, origin.y,
                        getX() * drawScale.x+(drawn.getRegionWidth()-squishedGum.getRegionWidth())/2f,
                        y-squishedGum.getRegionHeight()*yScale/2, getAngle(), 1, yScale);
                }

            } else {
                canvas.drawWithShadow(drawn, Color.WHITE, origin.x, origin.y, x,
                    y, getAngle(), effect, yScale);
            }
//
        }
    }
    public void drawWithOutline(GameCanvas canvas) {
        if (outline != null && gummedTexture != null) {
            float y = getY() * drawScale.y;
            if (getName().equals("mediumEnemy")||getName().equals("shieldedMediumEnemy")) {
                y += 10*yScale;
            }
            float effect = faceRight ? 1.0f : -1.0f;
            canvas.drawWithShadow(gummedTexture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x,
                    y, getAngle(), effect, yScale);
            if (getVY()==0) {
                canvas.draw(outline, Color.WHITE, origin.x, origin.y, getX()* drawScale.x-5,
                    y-5*yScale, getAngle(), 1, yScale);
            } else {
                canvas.draw(squishedGumOutline, Color.WHITE, origin.x, origin.y,
                    getX() * drawScale.x+(gummedTexture.getRegionWidth()
                        -squishedGum.getRegionWidth())/2f-5f,
                    y-squishedGum.getRegionHeight()*yScale/2-5*yScale, getAngle(), 1, yScale);

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
        canvas.drawPhysics(sensorShape, sensorColor, getX(), getY(), drawScale.x, drawScale.y);
//        canvas.drawPhysics(robotShape, sensorColor, getX(), getY(), 0, drawScale.x, drawScale.y);
        vision.drawDebug(canvas, getX(), getY(), drawScale.x, drawScale.y);
        sensing.drawDebug(canvas, getX(), getY(), drawScale.x, drawScale.y);
        attacking.drawDebug(canvas, getX(), getY(), drawScale.x, drawScale.y);
    }

    /**
     * Updates this EnemyModel's vision component.
     */
    public void updateRayCasts() {

        vision.setDirection(faceRight? (float) 0 : (float) Math.PI);
        sensing.setDirection(!faceRight? (float) 0 : (float) Math.PI);
        attacking.setDirection(faceRight? (float) 0 : (float) Math.PI);
        vision.update(world, getPosition());
        sensing.update(world, getPosition());
        attacking.update(world, getPosition());
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
        return true;
    }

    /**
     * Flips the player's angle and direction when the world gravity is flipped
    /**
     * Negates this EnemyModel's current "flipped" state (if it is
     * grounded).
     */
    public void flipGravity() {
        if (!(getStuck())) isFlipped = !isFlipped;
    }


    public void changeSpeed(float speed){
        this.speed = speed;
    }

    public float getYFeet(){
        return getY();
    }
}
