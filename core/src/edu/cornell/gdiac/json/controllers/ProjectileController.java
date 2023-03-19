package edu.cornell.gdiac.json.controllers;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.json.LevelModel;
import edu.cornell.gdiac.json.ProjectileModel;
import edu.cornell.gdiac.json.enemies.Enemy;


/**
 * Manages the projectiles fired by the enemies
 */
public class ProjectileController{

    /** The maximum number of projectile objects we support */
    private static final int MAX_PROJECTILE = 1024;

    /** The JSON data for a projectile */
    private JsonValue projJV;

    /** The queue of active projectiles */
    protected Queue<ProjectileModel> queue;

    /** The texture of a projectile */
    private TextureRegion projTexture;

    /** The draw scale of the level. */
    private Vector2 drawScale;

    /** The radius of a projectile. */
    private float radius;

    /** The speed of the projectile. */
    private float speed;

    /**
     * Creates a queue of projectiles.
     *
     * The game will never support more than MAX_PROJECTILES projectiles on screen at a time.
     */
    public ProjectileController(){
        queue = new Queue<>();
    }

    /**
     * Initializes the projectile controller based on JSON data.
     *
     * @param projJV The JsonValue representing the projectile
     * @param directory The asset directory containing the projectile asset
     * @param xScale the x scale of the level
     * @param yScale the y scale of the level
     */
    public void initialize(JsonValue projJV, AssetDirectory directory, float xScale, float yScale) {
        this.projJV = projJV;
        drawScale = new Vector2(xScale, yScale);
        String key = projJV.get("texture").asString();
        projTexture = new TextureRegion(directory.getEntry(key, Texture.class));
        radius = projTexture.getRegionWidth() / (2.0f * drawScale.x);
        speed = projJV.getFloat("speed", 0);
    }

    /**
     * Creates a projectile and updates the enemy's cooldown.
     *
     * @param c The AIController that is firing
     */
    public ProjectileModel fireWeapon(AIController c, float targetX, float targetY){
        Enemy e = c.enemy;
        ProjectileModel p = new ProjectileModel(projJV, e.getX(), e.getY(), radius);
        //set velocity
        Vector2 vel = new Vector2(targetX - e.getX(), targetY - e.getY());
        vel.nor();
        vel.scl(speed);
        p.setVX(vel.x);
        p.setVY(vel.y);

        //Physics Constants
        p.setDrawScale(drawScale);
        p.setTexture(projTexture);

        addToQueue(p);
        c.coolDown(false);
        return p;
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
     * It removes dead projectiles.
     */
    public void update() {
        // Remove dead projectiles
        while (queue.notEmpty() && !queue.first().isAlive()) {
            queue.removeFirst();
        }
    }


    /** Clears all projectiles from the queue */
    public void reset(){
        queue.clear();
    }




}