package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

/**
 * Class to represent a projectile object.
 */
public class ProjectileModel extends WheelObstacle implements Pool.Poolable{

    /** Marks whether this projectile is still alive */
    private boolean alive;

    /** Gravity scale of bullets */
    private float gravity = 0;

    /** damage that the bullet deals upon impact with player */
    private float damage = 10;

    /**
     * Creates a new projectile with the given attributes. Should only be called by ProjectileController.
     *
     * @param x  The initial x-coordinate of the projectile
     * @param y  The initial y-coordinate of the projectile
     */
    public ProjectileModel(JsonValue data, float x, float y, float radius){
        super(x, y, radius);
        setName("projectile");
        activatePhysics(data);
        alive = true;
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

    /** Applies physics properties to this projectile */
    private void activatePhysics(JsonValue data){
        this.setName(data.name());
        this.setDensity(data.getFloat("density", 0));
        this.setBullet(true);
        this.setGravityScale(gravity);
    }

    /**
     *
     * @return the damage that this bullet deals to the player
     */
    public float getDamage(){
        return damage;
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
        float angle = getLinearVelocity().angleRad();
        canvas.drawWithShadow(texture,Color.WHITE,origin.x,origin.y,
            getX()*drawScale.x,getY()*drawScale.x,angle,1,1);

    }
}