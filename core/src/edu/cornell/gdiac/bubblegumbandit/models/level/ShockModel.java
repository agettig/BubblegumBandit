package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.helpers.Damage;
import edu.cornell.gdiac.bubblegumbandit.view.AnimationController;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.BoxObstacle;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;
import edu.cornell.gdiac.util.FilmStrip;
import java.lang.reflect.Field;

/**
 * Class to represent a projectile object.
 */
public class ShockModel extends BoxObstacle implements Pool.Poolable {

    /** Floor animation fps */
    private final int FLOOR_FPS = 4;

    /** The time the shock persists before dissipating */
    private final float PERSIST_TIME = 2.5f;

    /** The distance to travel before the shockwave stops */
    private final float TRAVEL_DISTANCE = 5;

    /** The amount of time the shock dissipates */
    private final float DISSIPATE_TIME = .15f;

    /** The percentage of travel at which the wave crest starts decreasing */
//    private final float STARTS_DECREASING = .66f;

    /** Whether the projectile has finished spreading */
    private boolean finishedSpreading;

    /** The amount of time the shock has been persisting */
    private float timeAlive;

    /** Marks whether this projectile is still alive */
    private boolean alive;

    /** Gravity scale of shockwave */
    private float gravity = 0;

    /** The initial x of the shock. */
    private float initialX;

    /** The y scale of the shock. */
    private float yScale;

    /** Whether the projectile is riding the bottom */
    private boolean isBottom;

    /** The first texture for the electrified floor */
    private TextureRegion electricFloorTexture;

    /** The second texture for the electrified floor */
    private TextureRegion electricFloorTexture2;

    /** The current texture for the floor */
    private TextureRegion curFloor;

    /** Sensor to detect collisions with the hazard */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;
    private String sensorName;
    private Color sensorColor;
    private float sensorWidth;
    private float sensorHeight;
    private PolygonShape debugSensorShape;
    private Vector2 sensorCenter;

    /** Animation controller for the crest */
    private AnimationController animationController;

    /** Maintains a set of collided walls */
    private ObjectSet<WallModel> collisions;

    /** Y scale factor for shrinking down before disappearing */
    private float yScaleFactor;

    private boolean doesDamage;

    private Color tintColor = Color.WHITE;

    private TextureRegion curFrame;

    /**
     * Returns whether this shock model is on the bottom
     * @return Whether this shock model is on the bottom
     */
    public boolean getIsBottom() {
        return isBottom;
    }

    /**
     * Creates a new projectile with the given attributes. Should only be called by ProjectileController.
     *
     */
    public ShockModel(){
        super(0, 0, 1, 1);
        alive = true;
        yScale = 1;
        finishedSpreading = false;
        timeAlive = 0;
        debugSensorShape = new PolygonShape();
        collisions = new ObjectSet<>();
        doesDamage = true;
        yScaleFactor = 1;
    }

    /**
     * Marks that a collision has started between the shock model and obstacle ob
     * @param wall the wall in the collision
     */
    public void startCollision(WallModel wall) {
        collisions.add(wall);
    }

    /**
     * Marks that a collision has ended between the shock model and obstacle ob
     * @param wall the wall in the collision
     */
    public void endCollision(WallModel wall) {
        collisions.remove(wall);
    }

    public void initialize(AssetDirectory directory, Vector2 scale, JsonValue data, float x, float y, float radius, boolean isBottom, boolean isLeft) {
        animationController = new AnimationController(directory, "shockArc");
//        floorAnimationController = new AnimationController(directory, "shockFloor");

        setName("projectile");
        setDrawScale(scale);
        initialX = x;
        this.setName(data.name());
        this.setDensity(data.getFloat("density", 0));
        this.setBullet(true);
        this.setGravityScale(gravity);
        this.isBottom = isBottom;
        this.setFixedRotation(true);

        setPosition(x, y + (isBottom ? -.02f : .02f));
        setSensor(true);
        setWidth(radius / 2);
        setHeight(radius * 2);

        String key = data.get("floorTexture").asString();
        electricFloorTexture = new TextureRegion(directory.getEntry(key, Texture.class));
        key = data.get("floorTexture2").asString();
        electricFloorTexture2 = new TextureRegion(directory.getEntry(key, Texture.class));
        curFloor = electricFloorTexture;

        // Initialize the sensors used to detect shocking.
        float worldHalfWidth = origin.x / drawScale.x;
        float innerX = initialX + (isLeft ? -TRAVEL_DISTANCE : TRAVEL_DISTANCE) + (isLeft ? worldHalfWidth : -worldHalfWidth);
        sensorWidth = Math.abs(innerX - initialX);
        sensorHeight = electricFloorTexture.getRegionHeight() / drawScale.y;

        float halfFloorHeight = electricFloorTexture.getRegionHeight() / (2f * drawScale.y);
        y = (isBottom ? halfFloorHeight - radius : radius - halfFloorHeight);

        sensorCenter = new Vector2(sensorWidth / (isLeft ? 2 : -2), y);
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(sensorWidth / 2, sensorHeight / 2f, sensorCenter, 0.0f);

        // Reflection is best way to convert name to color
        try {
            String cname = data.get("sensorColor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            sensorColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            sensorColor = null; // Not defined
        }
        int opacity = data.get("sensorOpacity").asInt();
        sensorColor.mul(opacity / 255.0f);
        sensorName = data.get("sensorName").asString();

        float speed = data.getFloat("speed");
        this.setVX(isLeft ? -speed : speed);

        curFrame = texture;
    }
    /**
     * Destroy this photon immediately, removing it from the screen.
     *
     * This method will mark the photon as dead, so that it can be processed
     * properly later
     */
    public void destroy() {
        alive = false;
    }

    /**
     * Returns whether this projectile is still alive.
     *
     * Dead photons are not drawn, and are not processed in collision detection.
     *
     * @return whether this projectile is still alive.
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Callback method when the object is freed. It is automatically called by Pool.free()
     * Must reset every meaningful field of this projectile.
     */
    @Override
    public void reset() {
        this.setPosition(0,0);
        alive = false;
    }

    public void update(float dt) {
        if (collisions.size == 0) {
            stopShock();
        }

        if (!finishedSpreading) {
            float distanceTraveled  = Math.abs(getX() - initialX);
//            float a = TRAVEL_DISTANCE * STARTS_DECREASING;
//            if (distanceTraveled > a && yScale != 0) {
//                // Lerp from yScale 1 to 0 once hit the STARTS_DECREASING threshold
////                yScale = 1 - ((distanceTraveled - a) / (TRAVEL_DISTANCE * (1 - STARTS_DECREASING)));
//            }
            if (distanceTraveled > TRAVEL_DISTANCE) {
                finishedSpreading = true;
                setVX(0);
            }
        }
        timeAlive += dt;
        if (timeAlive > PERSIST_TIME + DISSIPATE_TIME) {
            destroy();
        }
        else if (timeAlive > PERSIST_TIME) {
            doesDamage = false;
            float y = (float ) Math.pow(((timeAlive - PERSIST_TIME) / DISSIPATE_TIME), 2);
            yScaleFactor = 1 - y;
        }
        if ((int) (timeAlive * FLOOR_FPS) % 2 == 0) {
            curFloor = electricFloorTexture;
        } else {
            curFloor = electricFloorTexture2;
        }

        curFrame = texture;
        if (animationController != null) {
            curFrame = animationController.getFrame();
        }

        float worldHalfWidth = origin.x / drawScale.x;
        float innerX = getX() + (getX() > initialX ? -worldHalfWidth : worldHalfWidth);
        curFloor.setRegionWidth((int) (Math.abs(innerX - initialX) * drawScale.x) + 2);
    }

    /** Returns whether the obstacle colliding with the shock sensor is valid
     * @param ob The ob that has collided with the shock */
    public boolean isValidHit(Obstacle ob) {
        if (!doesDamage) {
            return false;
        }
        if (getX() < initialX) { // If is moving left
            return ob.getX() < initialX; // Valid hit if obstacle is left of start
        } else if (getX() > initialX) {
            return ob.getX() > initialX; // Valid hit if obstacle is right of start
        }
        return false;
    }

    /**
     * Returns whether this projectile should be removed.
     *
     * Dead photons are not drawn, and are not processed in collision detection.
     *
     * @return whether this projectile is still alive.
     */
    public boolean isRemoved() {
        return !alive;
    }

    /** Stop this shock from moving any further. */
    public void stopShock() {
        setVX(0);
//        yScale = 0;
        finishedSpreading = true;
    }


    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) {
            return false;
        }

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = getDensity();
        sensorDef.isSensor = true;
        sensorDef.shape = sensorShape;
        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(this);

        return true;
    }


    @Override
    public void draw(GameCanvas canvas) {
        boolean isLeft = getX() < initialX;

        yScale = isBottom ? 1 : -1;
        float heightOffset = (origin.y * (1 - yScaleFactor) / 2f);
        float y = getY()*drawScale.y + (heightOffset * (isBottom ? -1 : 1));
        if (curFrame != null) {
            canvas.draw(curFrame,tintColor,origin.x,origin.y, getX()*drawScale.x,y,0,isLeft ? -1 : 1,yScale*yScaleFactor);
        }

        float worldHalfWidth = origin.x / drawScale.x;
        float innerX = getX() + (getX() > initialX ? -worldHalfWidth : worldHalfWidth);
        float centerX = (innerX + initialX) / 2f;
        float halfFloorHeight = curFloor.getRegionHeight() / 2f;
        y = getY()*drawScale.y + (isBottom ? -origin.y + halfFloorHeight - 1 : origin.y - halfFloorHeight + 1);
        y += (curFloor.getRegionHeight() * (1 - yScaleFactor) * (isBottom ? -1 : 1) / 4f);
        float originX = curFloor.getRegionWidth() / 2f;

        canvas.draw(curFloor, tintColor, originX, halfFloorHeight, centerX*drawScale.x, y, 0, isLeft ? -1 : 1, yScale*yScaleFactor);
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);

        debugSensorShape.setAsBox(sensorWidth * drawScale.x / 2, sensorHeight * drawScale.y / 2);
        canvas.drawPhysics(debugSensorShape, sensorColor, (getX() + sensorCenter.x) * drawScale.x, (getY() + sensorCenter.y)*  drawScale.y);
    }
}