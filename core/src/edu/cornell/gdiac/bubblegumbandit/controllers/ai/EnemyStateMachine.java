package edu.cornell.gdiac.bubblegumbandit.controllers.ai;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledGraph;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledManhattanDistance;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledNode;

import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.*;

/**
 * Default implementation of the {@link StateMachine} interface.
 *
 * @param <E> the type of the entity owning this state machine
 * @param <S> the type of the states of this state machine
 * @author davebaol
 */
public class EnemyStateMachine<E, S extends State<E>> implements StateMachine<E, S> {


    private ModifiedIndexedPathFinder<TiledNode> pathFinderGravityUp;

    private ModifiedIndexedPathFinder<TiledNode> pathFinderGravityDown;

    private Heuristic heuristic;
    private TiledGraph tiledGravityUpGraph;

    private TiledGraph tiledGravityDownGraph;

    private GraphPath graphPath;

    private int ticks;
    /**
     * The entity that owns this state machine.
     */
    protected E owner;

    /**
     * The current state the owner is in.
     */
    protected S currentState;

    /**
     * The last state the owner was in.
     */
    protected S previousState;

    /**
     * The global state of the owner. Its logic is called every time the FSM is updated.
     */
    protected S globalState;


    /**
     * Creates a {@code DefaultStateMachine} for the specified owner, initial state and global state.
     *
     * @param owner        the owner of the state machine
     * @param initialState the initial state
     * @param globalState  the global state
     */
    public EnemyStateMachine(E owner, S initialState, S globalState, TiledGraph tiledGravityUpGraph, TiledGraph tiledGravityDownGraph) {
        this.owner = owner;
        this.setInitialState(initialState);
        this.setGlobalState(globalState);
        this.tiledGravityUpGraph = tiledGravityUpGraph;
        this.tiledGravityDownGraph = tiledGravityDownGraph;
        this.pathFinderGravityDown = new ModifiedIndexedPathFinder<>(tiledGravityDownGraph, true);
        this.pathFinderGravityUp = new ModifiedIndexedPathFinder<>(tiledGravityUpGraph, true);
        this.heuristic = new TiledManhattanDistance();
        this.graphPath = new DefaultGraphPath<>();
        this.ticks = 0;

    }

    /**
     * Returns the owner of this state machine.
     */
    public E getOwner() {
        return owner;
    }

    /**
     * Sets the owner of this state machine.
     *
     * @param owner the owner.
     */
    public void setOwner(E owner) {
        this.owner = owner;
    }

    @Override
    public void setInitialState(S state) {
        this.previousState = null;
        this.currentState = state;
    }

    @Override
    public void setGlobalState(S state) {
        this.globalState = state;
    }

    @Override
    public S getCurrentState() {
        return currentState;
    }

    @Override
    public S getGlobalState() {
        return globalState;
    }

    @Override
    public S getPreviousState() {
        return previousState;
    }

    /**
     * Updates the state machine by invoking first the {@code execute} method of the global state (if any) then the {@code execute}
     * method of the current state.
     */
    @Override
    public void update() {
        ticks++;
        // Execute the global state (if any)
        if (globalState != null) globalState.update(owner);

        // Execute the current state (if any)
        if (currentState != null) currentState.update(owner);
    }

    @Override
    public void changeState(S newState) {
        // Keep a record of the previous state
        previousState = currentState;

        // Call the exit method of the existing state
        if (currentState != null) currentState.exit(owner);

        // Change state to the new state
        currentState = newState;

        // Call the entry method of the new state
        if (currentState != null) currentState.enter(owner);
    }

    @Override
    public boolean revertToPreviousState() {
        if (previousState == null) {
            return false;
        }

        changeState(previousState);
        return true;
    }

    /**
     * Indicates whether the state machine is in the given state.
     * <p>
     * This implementation assumes states are singletons (typically an enum) so they are compared with the {@code ==} operator
     * instead of the {@code equals} method.
     *
     * @param state the state to be compared with the current state
     * @return true if the current state and the given state are the same object.
     */
    @Override
    public boolean isInState(S state) {
        return currentState == state;
    }

    /**
     * Handles received telegrams. The telegram is first routed to the current state. If the current state does not deal with the
     * message, it's routed to the global state's message handler.
     *
     * @param telegram the received telegram
     * @return true if telegram has been successfully handled; false otherwise.
     */
    @Override
    public boolean handleMessage(Telegram telegram) {

        // First see if the current state is valid and that it can handle the message
        if (currentState != null && currentState.onMessage(owner, telegram)) {
            return true;
        }

        // If not, and if a global state has been implemented, send
        // the message to the global state
        if (globalState != null && globalState.onMessage(owner, telegram)) {
            return true;
        }

//        System.out.println(telegram.message);
//        System.out.println(telegram.receiver);
//        System.out.println(telegram.sender);

        return false;
    }

    private int getControlAction(int startX, int startY, int endX, int endY) {

        if (startX + 1 == endX) {
            return CONTROL_MOVE_RIGHT;
        } else if (startX - 1 == endX) {
            return CONTROL_MOVE_LEFT;
        } else if (startY + 1 == endY) {
            return CONTROL_MOVE_UP;
        } else {
            return CONTROL_MOVE_DOWN;
        }
    }

    public int getNextMove(int targetX, int targetY) {
        EnemyController controller = (EnemyController) owner;
        graphPath.clear();
        int startX = (int) controller.getEnemy().getX();
        int startY = (int) controller.getEnemy().getY();
        if (startX == targetX && startY == targetY) return CONTROL_NO_ACTION;
        boolean found = false;
        if (((EnemyController) owner).getEnemy().isFlipped()){
            found = pathFinderGravityUp.searchNodePath(tiledGravityUpGraph.getNode(startX,
                            startY),
                    tiledGravityUpGraph.getNode(targetX, targetY),
                    heuristic, graphPath);
        }
        else{
            found = pathFinderGravityDown.searchNodePath(tiledGravityDownGraph.getNode(startX,
                            startY),
                    tiledGravityDownGraph.getNode(targetX, targetY),
                    heuristic, graphPath);
        }

        if (found) {
            int nextX = ((TiledNode) graphPath.get(1)).getX();
            int nextY = ((TiledNode) graphPath.get(1)).getY();
            return getControlAction(startX, startY, nextX, nextY);
        }

        return CONTROL_NO_ACTION;
    }

    /**
     * Returns true if enemy is on a valid path tile, not free-falling
     * */
    public boolean canMove(){
        int x = (int) ((EnemyController) owner).getEnemy().getX();
        int y = (int) ((EnemyController) owner).getEnemy().getY();
        // TODO fix tile types
        if (((EnemyController) owner).getEnemy().isFlipped()){
            return tiledGravityUpGraph.getNode(x, y).getType() != 0;
        }
        else{
            return tiledGravityDownGraph.getNode(x,y).getType() != 0;
        }
    }

    public int getTicks(){
        return ticks;
    }

    public void sendMessage(EnemyController recipient, int messageType, Object extraInfo ){
        MessageManager.getInstance().dispatchMessage(
                0.0f,
                this,
                recipient,
                messageType,
                extraInfo);
    }

    public void broadcastMessage(int messageType, Object extraInfo){
        MessageManager.getInstance().dispatchMessage(messageType, extraInfo);
    }

}
