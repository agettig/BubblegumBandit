package edu.cornell.gdiac.json;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

/**
 * Class to represent a projectile object.
 */
public class ProjectileModel extends WheelObstacle {

    /** Size of the projectiles radius */
    private float radius = 10;

    /** Marks whether this photon is dead, but not deallocated */
    private boolean dirty;

    /**
     * Creates an empty Photon.
     *
     * This constructor is used in memory allocation.
     */
    public ProjectileModel(){
        super(0, 0, 0);
        this.setRadius(radius);
        dirty = false;
    }

    /**
     * "Allocates" a new photon with the given attributes. Should only be called by ProjectilePool.
     *
     * @param x  The initial x-coordinate of the photon
     * @param y  The initial y-coordinate of the photon
     * @param vx The x-value of the photon velocity
     * @param vy The y-value of the photon velocity
     */
    public void set(float x, float y, float vx, float vy){
        this.setPosition(x, y);
        Vector2 vel = new Vector2(vx, vy);
        this.setLinearVelocity(vel);
    }

    /**
     * Destroy this photon immediately, removing it from the screen.
     *
     * This method will mark the photon as dirty, so that it can be processed
     * properly later
     */
    public void destroy() {
        dirty = true;
    }

    /**
     * Returns whether this projectile is still alive.
     *
     * Dead photons are not drawn, and are not processed in collision detection.
     *
     * @return whether this projectile is still alive.
     */
    public boolean isAlive() {
        return !dirty;
    }
}