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


/**
 * Manages the projectiles fired by the enemies
 */
public class ProjectilePool{

//    /** The maximum number of projectile objects we support */
//    private static final int MAX_PROJECTILE = 1024;
//
//    /** Array implementation of a circular queue. */
//    protected ProjectileModel[] queue;
//    /** Index of head element in the queue */
//    protected int head;
//    /** Index of tail element in the queue */
//    protected int tail;
//    /** Number of elements currently in the queue */
//    protected int size;
//
//    /**
//     * Creates a (pre-allocated) pool of photons with the MAX_PHOTONS capacity.
//     *
//     * The game will never support more than MAX_PHOTONS photons on screen at a time.
//     */
//    public ProjectilePool(){
//        queue = new ProjectileModel[MAX_PROJECTILE];
//
//        head = 0;
//        tail = -1;
//        size = 0;
//
//        System.out.println("yoyoyo");
//        // Predeclared all photons for efficiency
////        for (int i = 0; i < queue.length; i++)
////            queue[i] = new ProjectileModel();
//    }
//
//
//    /**
//     * Updates all the projectiles in this pool.
//     *
//     * This method should be called once per game loop.
//     * It moves projectiles forward
//     */
//    public void update() {
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
//    }
//
//    /**
//     * Allocates a new projectile with the given attributes.
//     *
//     * @param x  The initial x-coordinate of the photon
//     * @param y  The initial y-coordinate of the photon
//     * @param vx The x-value of the photon velocity
//     * @param vy The y-value of the photon velocity
//     */
//
//    public void allocate(float x, float y, float vx, float vy) {
//        // Check if any room in queue.
//        // If maximum is reached, remove the oldest projectile.
//        if (size == queue.length) {
//            head = ((head + 1) % queue.length);
//            size--;
//        }
//
//        // Add a new projectile at the end.
//        // Already declared, so just initialize.
//        tail = ((tail + 1) % queue.length);
//        queue[tail].set(x, y, vx, vy);
//        size++;
//    }
//
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
//
//
////    /** Applies physics properties most recently added projectile
////     *
////     * must be called directly after allocate
////     * */
////    public void activatePhysics(String name, float density, Vector2 scale, TextureRegion texture, float gravity){
////        queue[tail].activatePhysics(name, density,scale, texture, gravity);
////    }
////
////    public ProjectileModel lastadded(){
////        return queue[tail];
////    }

    /** Array containing active projectiles */
    private final Array<ProjectileModel> activeProjectiles = new Array<ProjectileModel>();

    /** Projectile pool  */
    private final Pool<ProjectileModel> projectileModelPool = new Pool<ProjectileModel>() {
        @Override
        protected ProjectileModel newObject(){
            return new ProjectileModel();
        }
    };

    /**
     * Creates a pool of projectiles.
     *
     * An object pool reuses inactive or "dead" objects, instead of created new objects every time.
     * Source: https://libgdx.com/wiki/articles/memory-management
     */
    public ProjectilePool() {

    }

    /**
     * Updates all the projectiles in this pool.
     *
     * This method should be called once per game loop.
     * It moves projectiles forward and frees dead projectiles.
     */
    public void update() {
        //update position
        for (int i = 0; i < activeProjectiles.size; i++){
            ProjectileModel p = activeProjectiles.get(i);
            p.getPosition().add(p.getLinearVelocity());
        }

        //free dead projectiles
        ProjectileModel item;
        int len = activeProjectiles.size;
        for (int i = len; i >= 0; i--){
            item = activeProjectiles.get(i);
            if (!item.isAlive()){
                activeProjectiles.removeIndex(i);
                projectileModelPool.free(item);
            }
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
        ProjectileModel item = projectileModelPool.obtain();
        item.set(x, y, vx, vy);
        activeProjectiles.add(item);
    }


}