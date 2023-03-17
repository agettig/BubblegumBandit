package edu.cornell.gdiac.json;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

/**
 * Class to represent a projectile object.
 */
public class ProjectileModel extends WheelObstacle implements Pool.Poolable{

    /** Size of the projectiles radius */
    private float radius = 10;

    /** Marks whether this projectile is still alive */
    private boolean alive;

//    /**
//     * Creates an empty Projectile.
//     *
//     * This constructor is used in memory allocation.
//     */
//    public ProjectileModel(){
//        super(0, 0, 0);
//        this.setRadius(radius);
//        alive = false;
//
//        //physics attributes
//        this.setBullet(true);
//    }

    /**
     * Creates a new projectile with the given attributes. Should only be called by ProjectilePool.
     *
     * @param x  The initial x-coordinate of the photon
     * @param y  The initial y-coordinate of the photon
     */
    public ProjectileModel(float x, float y, float radius){
        super(x, y, radius);
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

//    /** Applies physics properties to this projectile */
//    public void activatePhysics(String name, float density, Vector2 scale, TextureRegion texture, float gravity){
//        this.setName(name);
//        this.setDensity(density);
//        this.setDrawScale(scale);
//        this.setTexture(texture);
//        this.setBullet(true);
//        this.setGravityScale(gravity);
//    }
}