package edu.cornell.gdiac.json;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.json.gum.GumJointPair;
import java.util.Iterator;
import java.util.NoSuchElementException;
import com.badlogic.gdx.utils.Queue;


/**
 * Manages the projectiles fired by the enemies
 */
public class ProjectilePool{

    /** The maximum number of projectile objects we support */
    private static final int MAX_PROJECTILE = 1024;

    /** The queue of active projectiles */
    protected Queue<ProjectileModel> queue;


    /**
     * Creates a queue of projectiles.
     *
     * The game will never support more than MAX_PROJECTILES projectiles on screen at a time.
     */
    public ProjectilePool(){
        queue = new Queue<ProjectileModel>();
    }


    /**
     * Added given projectile to the queue
     */

    public void addToQueue(ProjectileModel p) {
        // Check if any room in queue.
        // If maximum is reached, no projectile is created.
        if (queue.size == MAX_PROJECTILE) {
            return;
        }

        // Add a new projectile at the end.
//        ProjectileModel p = new ProjectileModel(x, y, vx, vy);
        queue.addLast(p);
    }


    /**
     * Updates all the projectiles in this pool.
     *
     * This method should be called once per game loop.
     * It moves projectiles forward
     */
    public void update() {
//        for (int i = 0; i < queue.length; i++){
//            ProjectileModel p = queue[i];
//            p.getPosition().add(p.getLinearVelocity());
//        }
//
//        // Remove dead photons
//        while (size > 0 && !queue[head].isAlive()) {
//            // As photons are predeclared, all we have to do is move head forward.
//            if (!queue[head].isDirty()) { size--; }
//            head = ((head + 1) % queue.length);
//        }

        for (ProjectileModel p : queue){
            p.getPosition().add(p.getLinearVelocity());
        }
    }



//    /**
//     * Destroys the giving projectile, removing it from the pool.
//     *
//     * A destroyed projectile reduces the size so that it is not drawn or used in
//     * collisions.  However, the memory is not reclaimed immediately.  It will
//     * only be reclaimed when we reach it in the queue.
//     *
//     * @param p the projectile to destroy
//     */
//    public void destroy(ProjectileModel p) {
//        p.destroy();
//        size--;
//    }


//    /** Applies physics properties most recently added projectile
//     *
//     * must be called directly after allocate
//     * */
//    public void activatePhysics(String name, float density, Vector2 scale, TextureRegion texture, float gravity){
//        queue[tail].activatePhysics(name, density,scale, texture, gravity);
//    }
//
//    public ProjectileModel lastadded(){
//        return queue[tail];
//    }



}