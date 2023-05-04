package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

public class ProjectileEnemyModel extends EnemyModel {

    /**Maximum speed/velocity of a moving enemy. */
    private static float MAX_SPEED = 1f;

    /**Force to apply to MovingEnemies for movement.*/
    private final float MOVEMENT_FORCE = 6.3f;

    /**Impulse to apply to MovingEnemies for jumping.*/
    private final float JUMPING_IMPULSE = 3f;

    /**Vision radius of a MovingEnemy. */
    private float visionRadius;

    /**[CONTROVERSIAL] Ticks to manage game time.*/
    private long ticks;

    /**Sets the vision radius of a MovingEnemy.
     *
     * @param visionRadius The new vision radius */
    public void setVisionRadius(float visionRadius) {
        this.visionRadius = visionRadius;
        vision.setRadius(visionRadius);
    }

    /**Creates a MovingEnemy.
     *
     * @param world The box2d world
     * @param id the id of this Enemy
     * */
    public ProjectileEnemyModel(World world, int id){
        super(world, id);
    }

    /**Initializes this MovingEnemy in the game. Sets its vision radius.
     *
     * @param directory The BubblegumBandit asset directory
     * @param x the x position of this enemy
     * @param y the y position of this enemy
     * @param constantsJson the constants json
     * @param isFacingRight whether the enemy spawns facing right
     * */
    public void initialize(AssetDirectory directory, float x, float y, JsonValue constantsJson, boolean isFacingRight){
        super.initialize(directory, x, y, constantsJson, isFacingRight);
        setVisionRadius(constantsJson.get("visionRadius").asFloat());
        attacking.setRadius(constantsJson.get("attackRadius").asFloat());
    }


    public void update(float dt){
        super.update(dt);
        if(fired()){
            animationController.setAnimation("fire", false);
        }
        else if (stuck || gummed){
            animationController.setAnimation("stuck", true);
        }
        else {
            animationController.setAnimation("patrol", true);
        }
    }
}
