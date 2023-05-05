package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.helpers.Damage;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

/**
 * Class to represent a projectile object.
 */
public class ShockModel extends WheelObstacle implements Pool.Poolable {

    /** The time the shock persists after fully spreading */
    private final float PERSIST_TIME = 1.5f;

    /** The distance to travel before the shockwave disappears */
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
    }

    public void initialize(TextureRegion electricFloorTexture, JsonValue data, float x, float y, float radius, boolean isBottom) {
        setName("projectile");
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
        if (finishedSpreading) {
            timeAlive += dt;
            if (timeAlive > PERSIST_TIME) {
                destroy();
            }
        } else {
            float distanceTraveled  = Math.abs(getX() - initialX);
            float a = TRAVEL_DISTANCE * STARTS_DECREASING;
            if (distanceTraveled > a) {
                // Lerp from yScale 1 to 0 once hit the STARTS_DECREASING threshold
                yScale = 1 - ((distanceTraveled - a) / (TRAVEL_DISTANCE * (1 - STARTS_DECREASING)));
            }
            if (distanceTraveled > TRAVEL_DISTANCE) {
                finishedSpreading = true;
                setVX(0);
            }
        }
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

    @Override
    public void draw(GameCanvas canvas) {
        float heightOffset = (origin.y * (1 - yScale));
        float y = getY()*drawScale.y + (isBottom ? -heightOffset : heightOffset);
        canvas.draw(texture,Color.WHITE,origin.x,origin.y,
            getX()*drawScale.x,y,0,1,yScale);

        float worldHalfWidth = origin.x / drawScale.x;
        float outerX = getX() + (getX() > initialX ? worldHalfWidth : -worldHalfWidth);
        float centerX = (outerX + initialX) / 2f;
        electricFloorTexture.setRegionWidth((int) (Math.abs(outerX - initialX) * drawScale.x) + 2);

        float halfFloorHeight = electricFloorTexture.getRegionHeight() / 2f;
        y = getY()*drawScale.y + (isBottom ? -origin.y + halfFloorHeight - 1 : origin.y - halfFloorHeight + 1);

        float originX = electricFloorTexture.getRegionWidth() / 2f;

        canvas.draw(electricFloorTexture, Color.WHITE, originX, halfFloorHeight, centerX*drawScale.x, y, 0, 1, 1);
    }
}