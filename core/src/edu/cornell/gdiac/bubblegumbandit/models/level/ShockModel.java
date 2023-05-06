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
import com.badlogic.gdx.utils.Pool;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.helpers.Damage;
import edu.cornell.gdiac.bubblegumbandit.view.AnimationController;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;
import java.lang.reflect.Field;

/**
 * Class to represent a projectile object.
 */
public class ShockModel extends WheelObstacle implements Pool.Poolable {

    /** The time the shock persists */
    private final float PERSIST_TIME = 2.5f;

    /** The distance to travel before the shockwave stops */
    private final float TRAVEL_DISTANCE = 5;

    /** The percentage of travel at which the wave crest starts decreasing */
    private final float STARTS_DECREASING = .66f;

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

    /** The texture for the electrified floor */
    private TextureRegion electricFloorTexture;

    /** Sensor to detect collisions with the hazard */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;
    private String sensorName;
    private Color sensorColor;
    private float sensorWidth;
    private float sensorHeight;
    private PolygonShape debugSensorShape;
    private Vector2 sensorCenter;

    private AnimationController animationController;


    /**
     * Creates a new projectile with the given attributes. Should only be called by ProjectileController.
     *
     */
    public ShockModel(){
        super(0, 0, 1);
        alive = true;
        yScale = 1;
        finishedSpreading = false;
        timeAlive = 0;
        debugSensorShape = new PolygonShape();
    }

    public void initialize(AssetDirectory directory, TextureRegion electricFloorTexture, Vector2 scale, JsonValue data, float x, float y, float radius, boolean isBottom, boolean isLeft) {
        animationController = new AnimationController(directory, "shockArc");

        setName("projectile");
        setDrawScale(scale);
        initialX = x;
        this.setName(data.name());
        this.setDensity(data.getFloat("density", 0));
        this.setBullet(true);
        this.setGravityScale(gravity);
        this.isBottom = isBottom;
        this.setFixedRotation(true);

        setPosition(x, y);
        setSensor(true);
        setRadius(radius);
        this.electricFloorTexture = electricFloorTexture;

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
        if (!finishedSpreading) {
            float distanceTraveled  = Math.abs(getX() - initialX);
            float a = TRAVEL_DISTANCE * STARTS_DECREASING;
            if (distanceTraveled > a && yScale != 0) {
                // Lerp from yScale 1 to 0 once hit the STARTS_DECREASING threshold
//                yScale = 1 - ((distanceTraveled - a) / (TRAVEL_DISTANCE * (1 - STARTS_DECREASING)));
            }
            if (distanceTraveled > TRAVEL_DISTANCE) {
                finishedSpreading = true;
                setVX(0);
            }
        }
        timeAlive += dt;
        if (timeAlive > PERSIST_TIME) {
            destroy();
        }
    }

    /** Returns whether the obstacle colliding with the shock sensor is valid
     * @param ob The ob that has collided with the shock */
    public boolean isValidHit(Obstacle ob) {
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
//        float heightOffset = (origin.y * (1 - yScale));
        float y = getY()*drawScale.y;
        canvas.draw(animationController.getFrame(),Color.WHITE,origin.x,origin.y, getX()*drawScale.x,y,0,isLeft ? -1 : 1,yScale);

        float worldHalfWidth = origin.x / drawScale.x;
        float innerX = getX() + (getX() > initialX ? -worldHalfWidth : worldHalfWidth);
        float centerX = (innerX + initialX) / 2f;
        electricFloorTexture.setRegionWidth((int) (Math.abs(innerX - initialX) * drawScale.x) + 2);

        float halfFloorHeight = electricFloorTexture.getRegionHeight() / 2f;
        y = getY()*drawScale.y + (isBottom ? -origin.y + halfFloorHeight - 1 : origin.y - halfFloorHeight + 1);

        float originX = electricFloorTexture.getRegionWidth() / 2f;

        canvas.draw(electricFloorTexture, Color.WHITE, originX, halfFloorHeight, centerX*drawScale.x, y, 0, isLeft ? 1 : -1, yScale);
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);

        debugSensorShape.setAsBox(sensorWidth * drawScale.x / 2, sensorHeight * drawScale.y / 2);
        canvas.drawPhysics(debugSensorShape, sensorColor, (getX() + sensorCenter.x) * drawScale.x, (getY() + sensorCenter.y)*  drawScale.y);
    }
}