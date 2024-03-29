package edu.cornell.gdiac.bubblegumbandit.controllers.ai;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledGraph;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.ShockEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;

public class AIController implements Telegraph {

    /**
     * Cooldown in ticks for shocking
     */
    private final int SHOCK_COOLDOWN = 180;
    
    /**
     * reference to enemy
     */
    public EnemyModel enemy;

    /**
     * reference to player / target
     */
    private BanditModel bandit;



    /**
     * Horizontal distance that enemies will not move towards the player
     * Enemies will move if the horizontal distance to player < ENEMY_PROXIMITY
     * */
    private final float ENEMY_PROXIMITY = 2.0f;

    /**
     * graph for pathfinding
     */

    private TiledGraph tiledGraphGravityUp;

    private TiledGraph tiledGraphGravityDown;

    private EnemyStateMachine<AIController, EnemyState> enemyfsm;

    // Shooting Attributes & Constants

    /**
     * How long an enemy must wait until it can fire its weapon again
     */
    private int cooldown = 120; //in ticks

    /**
     * The number of frames until we can fire again
     */
    private int firecool;

    /**
     * Whether this enemy is currently firing
     */
    private boolean firing = true;

    public EnemyStateMachine<AIController, EnemyState> getEnemyStateMachine() {
        return enemyfsm;
    }

    public EnemyModel getEnemy() {
        return enemy;
    }

    public BanditModel getBandit() {
        return bandit;
    }

    public AIController(EnemyModel enemy, BanditModel bandit, TiledGraph tiledGraphGravityUp, TiledGraph tiledGraphGravityDown) {
        this.tiledGraphGravityUp = tiledGraphGravityUp;
        this.enemy = enemy;
        this.enemyfsm = new EnemyStateMachine(this, EnemyState.WANDER, EnemyState.PERCEIVE, tiledGraphGravityUp, tiledGraphGravityDown);
        this.bandit = bandit;
        this.tiledGraphGravityDown = tiledGraphGravityDown;
        firecool = 0;
        MessageManager.getInstance().addListener(this, MessageType.NEED_BACKUP);

        if (enemy instanceof ShockEnemyModel) {
            cooldown = SHOCK_COOLDOWN;
        }
    }

    /**
     * Returns true if we can both fire and hit our target
     * <p>
     * If we can fire now, and we could hit the target from where we are,
     * we should hit the target now.
     *
     * @return true if we can both fire and hit our target
     */
    public boolean canShootTarget() {
        return canFire() && enemy.getAttacking().canSee(bandit);
    }

    /**Returns true if the bandit is within listening range of the enemy */
    public boolean enemyHeardBandit() {
        Vector2 playerPosition = bandit.getPosition();
        Vector2 enemyPosition = enemy.getPosition();
        CircleShape hearingCircle = enemy.getListeningCircle();

        float xDiff = Math.abs(playerPosition.x - enemyPosition.x);
        float yDiff = Math.abs(playerPosition.y - enemyPosition.y);
        double distance = Math.sqrt(Math.pow(xDiff + yDiff, 2));
        if (distance < hearingCircle.getRadius()) {
            return true;
        }
        else return false;
    }

    /**
     * Returns true if the horizontal distance between the player and enemy
     * is less than ENEMY_PROXIMITY
     * else returns false
     * */
    public boolean enemyCloseToBandit(){
        float banditX = bandit.getX();
        float enemyX = enemy.getX();
        return Math.abs(banditX - enemyX) < ENEMY_PROXIMITY;
    }

    /**
     * @return whether this robot can fire its weapon and is actively firing.
     */
    public boolean canFire() {
        return firing && firecool <= 0;
    }

    /**
     * Sets whether the robot is actively firing.
     *
     * @param value whether the robot is actively firing.
     */
    public void setFiring(boolean value) {
        firing = value;
    }

    /**
     * Reset or cool down the ship weapon.
     * <p>
     * If flag is true, the weapon will cool down by one animation frame.  Otherwise
     * it will reset to its maximum cooldown.
     *
     * @param flag whether to cooldown or reset
     */
    public void coolDown(boolean flag) {
        if (flag && firecool > 0) {
            firecool--;
        } else if (!flag) {
            firecool = cooldown;
        }
    }

    public void flipEnemy(){
        if (!enemy.getStuck() /*&& !enemy.getGummed()*/) {
            enemy.flipGravity();
        }
    }

    /**
     * Returns the type of tile the player is currently on
     * */
    public int getTileType(){
        if (enemy.isFlipped()){
            return tiledGraphGravityUp.getNode((int) enemy.getX(), (int)enemy.getY()).getType();
        }
        return tiledGraphGravityDown.getNode((int) enemy.getX(), (int)enemy.getY()).getType();
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        return enemyfsm.handleMessage(msg);
    }
}
