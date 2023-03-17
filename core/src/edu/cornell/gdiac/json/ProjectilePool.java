package edu.cornell.gdiac.json;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.json.gum.GumJointPair;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Manages the projectiles fired by the enemies
 */
public class ProjectilePool implements Iterable<ProjectileModel>{

    /** The maximum number of projectile objects we support */
    private static final int MAX_PROJECTILE = 1024;

    /** Array implementation of a circular queue. */
    protected ProjectileModel[] queue;
    /** Index of head element in the queue */
    protected int head;
    /** Index of tail element in the queue */
    protected int tail;
    /** Number of elements currently in the queue */
    protected int size;

    /** Custom iterator so we can use this object in for-each loops */
    private ProjectileIterator iterator = new ProjectileIterator();

    /**
     * Creates a (pre-allocated) pool of photons with the MAX_PHOTONS capacity.
     *
     * The game will never support more than MAX_PHOTONS photons on screen at a time.
     */
    public ProjectilePool(){
        queue = new ProjectileModel[MAX_PROJECTILE];

        head = 0;
        tail = -1;
        size = 0;

        // Predeclared all photons for efficiency
        for (int i = 0; i < queue.length; i++)
            queue[i] = new ProjectileModel();
    }


    /**
     * Updates all the projectiles in this pool.
     *
     * This method should be called once per game loop.
     * It moves projectiles forward
     */
    public void update() {
        // Remove dead photons
        while (size > 0 && !queue[head].isAlive()) {
            // As photons are predeclared, all we have to do is move head forward.
            if (!queue[head].isDirty()) { size--; }
            head = ((head + 1) % queue.length);
        }
    }

    /**
     * Allocates a new projectile with the given attributes.
     *
     * @param x  The initial x-coordinate of the photon
     * @param y  The initial y-coordinate of the photon
     * @param vx The x-value of the photon velocity
     * @param vy The y-value of the photon velocity
     */

    public void allocate(float x, float y, float vx, float vy) {
        // Check if any room in queue.
        // If maximum is reached, remove the oldest projectile.
        if (size == queue.length) {
            head = ((head + 1) % queue.length);
            size--;
        }

        // Add a new projectile at the end.
        // Already declared, so just initialize.
        tail = ((tail + 1) % queue.length);
        queue[tail].set(x, y, vx, vy);
        size++;
    }

    /**
     * Destroys the giving projectile, removing it from the pool.
     *
     * A destroyed projectile reduces the size so that it is not drawn or used in
     * collisions.  However, the memory is not reclaimed immediately.  It will
     * only be reclaimed when we reach it in the queue.
     *
     * @param p the projectile to destroy
     */
    public void destroy(ProjectileModel p) {
        p.destroy();
        size--;
    }

    /**
     * Returns a projectile iterator, satisfying the Iterable interface.
     *
     * This method allows us to use this object in for-each loops.
     *
     * @return a projectile iterator.
     */
    public Iterator<ProjectileModel> iterator() {
        // Take a snapshot of the current state and return iterator.
        iterator.limit = size;
        iterator.pos = head;
        iterator.cnt = 0;
        return iterator;
    }

    /**
     * Implementation of a custom iterator.
     */
    private class ProjectileIterator implements Iterator<ProjectileModel>{
        /** The current position in the photon queue */
        public int pos = 0;
        /** The number of photons shown already */
        public int cnt = 0;

        /** The number of photons to iterator over (snapshot to allow deletion) */
        public int limit =0;

        /**
         * @return true if there are still items left to iterate
         */
        public boolean hasNext() {
            return cnt < limit;
        }

        public ProjectileModel next(){
            if (cnt > limit) {
                throw new NoSuchElementException();
            }
            int idx = pos;
            while (!queue[pos].isAlive()){
                pos = ((pos+1) % queue.length);
            }
            cnt++;
            return queue[idx];
        }
    }




}