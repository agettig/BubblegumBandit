package edu.cornell.gdiac.bubblegumbandit.controllers;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.CATEGORY_PROJECTILE;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.MASK_PROJECTILE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.AIController;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.ShockEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.LevelModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.ShockModel;


/**
 * Manages the projectiles fired by the enemies
 */
public class ShockController {

    /** The maximum number of projectile objects we support */
    private static final int MAX_PROJECTILE = 1024;

    /** The JSON data for a projectile */
    private JsonValue projJV;

    /** The queue of active projectiles */
    protected Queue<ShockModel> queue;

    /** The texture of a projectile */
    private TextureRegion projTexture;

    /** The draw scale of the level. */
    private Vector2 drawScale;

    /** The radius of a projectile. */
    private float radius;

    /** The speed of the projectile. */
    private float speed;

    private AssetDirectory directory;

    /**
     * Creates a queue of projectiles.
     *
     * The game will never support more than MAX_PROJECTILES projectiles on screen at a time.
     */
    public ShockController(){
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
        this.directory = directory;
        this.projJV = projJV;
        drawScale = new Vector2(xScale, yScale);
        String key = projJV.get("texture").asString();
        projTexture = new TextureRegion(directory.getEntry(key, Texture.class));
        radius = projTexture.getRegionWidth() / (2.0f * drawScale.x);
        speed = projJV.getFloat("speed", 0);
    }

    /**
     * Creates the shock, adds it to the world, and updates the enemy's cooldown.
     * Precondition: controller's enemy is a ShockEnemy
     *
     * @param controller The AIController that is firing
     * @param level the level model of the game
     * @param isGravDown whether gravity is down
     */
    public void fireWeapon(LevelModel level, AIController controller, boolean isGravDown){
        EnemyModel e = controller.getEnemy();
        assert (e instanceof ShockEnemyModel);
        ShockEnemyModel shockEnemy = (ShockEnemyModel) e;
        // Snap position to floor / ceiling
        float projY;
        if (isGravDown) {
            float ground = e.getY() - (e.getHeight() / 2);
            projY = ground + radius + 0.01f;
        } else {
            float ground = e.getY() + (e.getHeight() / 2);
            projY = ground - radius - 0.01f;
        }
        ShockModel left = new ShockModel();
        ShockModel right = new ShockModel();
        left.initialize(directory, drawScale, projJV, e.getX(), projY, radius, isGravDown, true);
        right.initialize(directory, drawScale, projJV, e.getX(), projY, radius, isGravDown, false);

        //Physics Constants
        left.setTexture(projTexture);
        right.setTexture(projTexture);

        shockEnemy.setShock(left, right);

        addToQueue(left);
        addToQueue(right);
        controller.coolDown(false);

        level.activate(left);
        level.activate(right);
        left.setFilter(CATEGORY_PROJECTILE, MASK_PROJECTILE);
        right.setFilter(CATEGORY_PROJECTILE, MASK_PROJECTILE);

        SoundController.playSound("shockAttack", 0.25f);
    }

    /**
     * Added given projectile to the queue
     */
    public void addToQueue(ShockModel p) {
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