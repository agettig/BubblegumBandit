package edu.cornell.gdiac.json;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
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
        // Add projectile to end.
        queue.addLast(p);
    }


    /**
     * Updates all the projectiles in this pool.
     *
     * This method should be called once per game loop.
     * It moves projectiles forward and removes dead projectiles.
     */
    public void update() {
        //move projectiles forward
        for (ProjectileModel p : queue){
            p.getPosition().add(p.getLinearVelocity());
        }
        // Remove dead projectiles
        while (queue.notEmpty() && !queue.first().isAlive()) {
            queue.removeFirst();
        }
//        System.out.println(queue.size);
    }


    /** Clears all projectiles from the queue */
    public void reset(){
        queue.clear();
    }




}