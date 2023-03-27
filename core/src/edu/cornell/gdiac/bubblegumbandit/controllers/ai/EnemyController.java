package edu.cornell.gdiac.bubblegumbandit.controllers.ai;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.bubblegumbandit.controllers.AIController;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledGraph;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.MovingEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.Board;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;

import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.CONTROL_NO_ACTION;

public class EnemyController {
    /**
     * ticks in update loop
     */
    private int ticks;

    /**
     * move for enemy to make
     */
    private int move;

    /**
     * attack range of enemy
     */
    private final int ATTACK_RANGE = 7;

    /**
     * chase range of enemy
     */
    private final int CHASE_RANGE = 10;

    /**
     * reference to enemy
     */
    public EnemyModel enemy;

    /**
     * reference to player / target
     */
    private BanditModel bandit;

    /**
     * graph for pathfinding
     */

    private TiledGraph tiledGraph;

    private EnemyStateMachine<EnemyController, EnemyState> enemyfsm;

    private Vector2 target;
    // Shooting Attributes & Constants

    /**
     * How long an enemy must wait until it can fire its weapon again
     */
    private static final int COOLDOWN = 120; //in ticks

    /**
     * The number of frames until we can fire again
     */
    private int firecool;

    /**
     * Whether this enemy is currently firing
     */
    private boolean firing = true;

    public EnemyStateMachine<EnemyController, EnemyState> getEnemyStateMachine() {
        return enemyfsm;
    }

    public EnemyModel getEnemy() {
        return enemy;
    }

    public BanditModel getBandit() {
        return bandit;
    }

    public EnemyController(EnemyModel enemy, BanditModel bandit, TiledGraph tiledGraph) {
        this.tiledGraph = tiledGraph;
        this.enemy = enemy;
        this.enemyfsm = new EnemyStateMachine(this, EnemyState.WANDER, EnemyState.PERCEIVE, tiledGraph);
        this.bandit = bandit;
        move = CONTROL_NO_ACTION;
        ticks = 0;
        firecool = 0;
        target = null;
    }

    /**
     * Returns true if we can both fire and hit our target
     * <p>
     * If we can fire now, and we could hit the target from where we are,
     * we should hit the target now.
     *
     * @return true if we can both fire and hit our target
     */
    private boolean canShootTarget() {
        return canFire() && enemy.vision.canSee(bandit);
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
            firecool = COOLDOWN;
        }
    }

    public void flipEnemy(){
        enemy.flippedGravity();
    }
}
