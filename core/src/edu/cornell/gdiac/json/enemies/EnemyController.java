
package edu.cornell.gdiac.json.enemies;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.json.Board;
import edu.cornell.gdiac.json.PlayerModel;
import edu.cornell.gdiac.json.controllers.InputController;

public class EnemyController implements InputController {

    /**ticks in update loop */
    private int ticks;

    /**move for enemy to make */
    private int move;

    /**attack range of enemy */
    private final int ATTACK_RANGE = 7;

    /**chase range of enemy */
    private final int CHASE_RANGE = 10;

    /**state of enemy */
    private EnemyState state;

    /**reference to enemy */
    private Enemy enemy;

    /**reference to player / target */
    private PlayerModel player;

    /**graph for pathfinding */
    private Board board;

    private Vector2 target;

    public EnemyController(Enemy enemy, PlayerModel player, Board board){
        this.board = board;
        this.enemy = enemy;
        this.player = player;
        state = EnemyState.STATIONARY;
        move = CONTROL_NO_ACTION;
        ticks = 0;

        target = null;
    }

    private enum EnemyState{
            /**
             * The enemy is stationary
             */
            STATIONARY,
            /**
             * The enemy is wandering back and forth
             * Switches directions when hitting a wall
             */
            WANDER,
            /**
             * The ship has a target, but must get closer
             */
            CHASE,
            /**
             * The ship has a target and is attacking it
             */
            ATTACK
     }

    /**
     * Returns the action selected by this InputController
     *
     * The returned int is a bit-vector of more than one possible input
     * option. This is why we do not use an enumeration of Control Codes;
     * Java does not (nicely) provide bitwise operation support for enums.
     *
     * This function tests the environment and uses the FSM to chose the next
     * action of the ship. This function SHOULD NOT need to be modified.  It
     * just contains code that drives the functions that you need to implement.
     *
     * @return the action selected by this InputController
     */
    public int getAction() {
        // Increment the number of ticks.
        ticks++;

        // Do not need to rework ourselves every frame. Just every 10 ticks.
        if ((enemy.getId() + ticks) % 20 == 0) {
            // Process the FSM
            changeStateIfApplicable();

            //markGoalTiles();
            move = board.getMoveAlongPathToGoalTile(enemy.getPosition(), player.getPosition(), true);
        }

        int action = move;

        // If we're attacking someone and we can shoot him now, then do so.
        if (state == EnemyState.ATTACK && canShootTarget()) {
            action |= CONTROL_FIRE;
        }

        return action;
    }

    // FSM Code for Targeting (MODIFY ALL THE FOLLOWING METHODS)

    /**
     * Change the state of the ship.
     *
     * A Finite State Machine (FSM) is just a collection of rules that,
     * given a current state, and given certain observations about the
     * environment, chooses a new state. For example, if we are currently
     * in the ATTACK state, we may want to switch to the CHASE state if the
     * target gets out of range.
     */
    private void changeStateIfApplicable() {

        //Calculate distance between enemy and player
        Vector2 playerPosition = player.getPosition();
        Vector2 enemyPosition = enemy.getPosition();
        float distance = Vector2.dst(playerPosition.x, playerPosition.y,
                enemyPosition.x, enemyPosition.y);

        switch (state) {
            case WANDER:
                //If player enters sight, switch to chase.
                if(enemy.vision.canSee(player)){
                    state = EnemyState.CHASE;
                }
                break;

            case CHASE:
                //If player within an attack range, switch to attack.
                if(distance <= ATTACK_RANGE) state = EnemyState.ATTACK;
                if(distance > CHASE_RANGE) state = EnemyState.WANDER;

            case ATTACK:
                //If player enters chase range, switch to chase.
                if(distance > ATTACK_RANGE && distance <= CHASE_RANGE) state = EnemyState.CHASE;
                if(distance > CHASE_RANGE) state = EnemyState.WANDER;
                break;

            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                state = EnemyState.WANDER;
                break;
        }
    }

    /**
     * Acquire a target to attack (and put it in field target).
     *
     * Insert your checking and target selection code here. Note that this
     * code does not need to reassign <c>target</c> every single time it is
     * called. Like all other methods, make sure it works with any number
     * of players (between 0 and 32 players will be checked). Also, it is a
     * good idea to make sure the ship does not target itself or an
     * already-fallen (e.g. inactive) ship.
     */
    private void selectTarget() {
        //#region PUT YOUR CODE HERE


        //#endregion
    }

    /**
     * Returns true if we can hit a target from here.
     *
     * Insert code to return true if a shot fired from the given (x,y) would
     * be likely to hit the target. We can hit a target if it is in a straight
     * line from this tile and within attack range. The implementation must take
     * into consideration whether or not the source tile is a Power Tile.
     *
     * @param x The x-index of the source tile
     * @param y The y-index of the source tile
     *
     * @return true if we can hit a target from here.
     */
    private boolean canShootTargetFrom(int x, int y) {
        return true;
    }

    /**
     * Returns true if we can both fire and hit our target
     *
     * If we can fire now, and we could hit the target from where we are,
     * we should hit the target now.
     *
     * @return true if we can both fire and hit our target
     */
    private boolean canShootTarget() {
        //#region PUT YOUR CODE HERE

        return false;
        //#endregion
    }

    // Pathfinding Code (MODIFY ALL THE FOLLOWING METHODS)






    // Add any auxiliary methods or data structures here
    //#region PUT YOUR CODE HERE



}
