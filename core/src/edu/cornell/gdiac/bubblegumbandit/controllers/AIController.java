
package edu.cornell.gdiac.bubblegumbandit.controllers;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.Board;


public class AIController implements InputController {

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
     * state of enemy
     */
    private EnemyState state;

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
    private Board board;

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


    public EnemyModel getEnemy() {
        return enemy;
    }

    public AIController(EnemyModel enemy, BanditModel bandit, Board board) {
        this.board = board;
        this.enemy = enemy;
        this.bandit = bandit;
        state = EnemyState.WANDER;
        move = CONTROL_NO_ACTION;
        ticks = 0;
        firecool = 0;
        target = null;
    }

    private enum EnemyState {
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
     * <p>
     * The returned int is a bit-vector of more than one possible input
     * option. This is why we do not use an enumeration of Control Codes;
     * Java does not (nicely) provide bitwise operation support for enums.
     * <p>
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

            markGoalTiles();
            move = board.getMoveAlongPathToGoalTile(enemy.getPosition(), bandit.isFlipped());
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
     * <p>
     * A Finite State Machine (FSM) is just a collection of rules that,
     * given a current state, and given certain observations about the
     * environment, chooses a new state. For example, if we are currently
     * in the ATTACK state, we may want to switch to the CHASE state if the
     * target gets out of range.
     */
    private void changeStateIfApplicable() {

        //Calculate distance between enemy and player
        Vector2 playerPosition = bandit.getPosition();
        Vector2 enemyPosition = enemy.getPosition();
        float distance = Vector2.dst(playerPosition.x, playerPosition.y,
                enemyPosition.x, enemyPosition.y);

        switch (state) {
            case WANDER:
                //If player enters sight, switch to chase.
                if(enemy.vision.canSee(bandit)){
                    state = EnemyState.CHASE;
                }
                break;

            case CHASE:
                //If player within an attack range, switch to attack.
                if (distance <= ATTACK_RANGE) state = EnemyState.ATTACK;
                if (distance > CHASE_RANGE) state = EnemyState.WANDER;
                // TODO add change back to wander if enemy can no londer reach player

            case ATTACK:
                //If player enters chase range, switch to chase.
                if (distance > ATTACK_RANGE && distance <= CHASE_RANGE) state = EnemyState.CHASE;
                if (distance > CHASE_RANGE) state = EnemyState.WANDER;
                // TODO add change back to wander if enemy can no londer reach player
                break;

            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                state = EnemyState.WANDER;
                break;
        }
    }

    /**
     * Mark all desirable tiles to move to.
     * <p>
     * This method implements pathfinding through the use of goal tiles.
     * It searches for all desirable tiles to move to (there may be more than
     * one), and marks each one as a goal. Then, the pathfinding method
     * getMoveAlongPathToGoalTile() moves the ship towards the closest one.
     * <p>
     * POSTCONDITION: There is guaranteed to be at least one goal tile
     * when completed.
     */
    private void markGoalTiles() {
        // Clear out previous pathfinding data.
        board.resetTiles();
        boolean setGoal = false; // Until we find a goal

        // Add initialization code as necessary
        //#region PUT YOUR CODE HERE

        //#endregion

        switch (state) {
            case STATIONARY: // Do not pre-empt with FSMState in a case
                // insert code here to mark tiles (if any) that spawning ships
                // want to go to, and set setGoal to true if we marked any.
                // Ships in the spawning state will immediately move to another
                // state, so there is no need for goal tiles here.

                //#region PUT YOUR CODE HERE

                //#endregion
                break;

            case WANDER: // Do not pre-empt with FSMState in a case
                // Insert code to mark tiles that will cause us to move around;
                // set setGoal to true if we marked any tiles.
                // NOTE: this case must work even if the ship has no target
                // (and changeStateIfApplicable should make sure we are never
                // in a state that won't work at the time)

                //#region PUT YOUR CODE HERE
                // marks all neighboring squares
                int x = (int) enemy.getX();
                int y = (int) enemy.getY();
                boolean leftMoveValid = board.isValidMove(x - 1, y, enemy.isFlipped());
                boolean rightMoveValid =  board.isValidMove(x + 1, y, enemy.isFlipped());

                if (enemy.getFaceRight()){
                    if (rightMoveValid){
                        board.setGoal(x+1, y);
                    } else if (leftMoveValid) {
                        board.setGoal(x-1, y);
                    }
                }
                else if (!enemy.getFaceRight()){
                    if (leftMoveValid){
                        board.setGoal(x-1, y);
                    } else if (rightMoveValid) {
                        board.setGoal(x+1, y);
                    }
                }
                else{
                    board.setGoal(x,y);
                }

                setGoal = true;
                //#endregion
                break;

            case CHASE: // Do not pre-empt with FSMState in a case

            case ATTACK:
                // Insert code here to mark tiles we can attack from, (see
                // canShootTargetFrom); set setGoal to true if we marked any tiles.

                // Insert code to mark tiles that will cause us to chase the target;
                // set setGoal to true if we marked any tiles.

                //#region PUT YOUR CODE HERE
                Vector2 pos = bandit.getPosition();
                board.setGoal((int) pos.x, (int) pos.y);
                setGoal = true;
                //#endregion
                break;
            //#endregion
        }

        // If we have no goals, mark current position as a goal
        // so we do not spend time looking for nothing:
        if (!setGoal) {
            int sx = (int) enemy.getPosition().x;
            int sy = (int) enemy.getPosition().y;
            board.setGoal(sx, sy);
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
        if (!enemy.getStuck() && !enemy.getGummed()) {
            enemy.flippedGravity();
        }
    }
}