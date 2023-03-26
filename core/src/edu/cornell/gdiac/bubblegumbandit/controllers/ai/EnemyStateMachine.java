package edu.cornell.gdiac.bubblegumbandit.controllers.ai;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledGraph;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledManhattanDistance;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledNode;

/** Default implementation of the {@link StateMachine} interface.
 *
 * @param <E> the type of the entity owning this state machine
 * @param <S> the type of the states of this state machine
 *
 * @author davebaol */
public class EnemyStateMachine<E, S extends State<E>> implements StateMachine<E, S> {


    private IndexedAStarPathFinder<TiledNode> pathFinder;

    private Heuristic heuristic;
    private TiledGraph tiledGraph;

    private GraphPath graphPath;
    /** The entity that owns this state machine. */
    protected E owner;

    /** The current state the owner is in. */
    protected S currentState;

    /** The last state the owner was in. */
    protected S previousState;

    /** The global state of the owner. Its logic is called every time the FSM is updated. */
    protected S globalState;


    /** Creates a {@code DefaultStateMachine} for the specified owner, initial state and global state.
     * @param owner the owner of the state machine
     * @param initialState the initial state
     * @param globalState the global state */
    public EnemyStateMachine (E owner, S initialState, S globalState, TiledGraph tiledGraph) {
        this.owner = owner;
        this.setInitialState(initialState);
        this.setGlobalState(globalState);
        this.tiledGraph = tiledGraph;
        this.pathFinder = new IndexedAStarPathFinder<>(tiledGraph, true);
        this.heuristic = new TiledManhattanDistance();
        this.graphPath = new DefaultGraphPath<>();

    }
    /** Returns the owner of this state machine. */
    public E getOwner () {
        return owner;
    }

    /** Sets the owner of this state machine.
     * @param owner the owner. */
    public void setOwner (E owner) {
        this.owner = owner;
    }

    @Override
    public void setInitialState (S state) {
        this.previousState = null;
        this.currentState = state;
    }

    @Override
    public void setGlobalState (S state) {
        this.globalState = state;
    }

    @Override
    public S getCurrentState () {
        return currentState;
    }

    @Override
    public S getGlobalState () {
        return globalState;
    }

    @Override
    public S getPreviousState () {
        return previousState;
    }

    /** Updates the state machine by invoking first the {@code execute} method of the global state (if any) then the {@code execute}
     * method of the current state. */
    @Override
    public void update () {
        // Execute the global state (if any)
        if (globalState != null) globalState.update(owner);

        // Execute the current state (if any)
        if (currentState != null) currentState.update(owner);
    }

    @Override
    public void changeState (S newState) {
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
    public boolean revertToPreviousState () {
        if (previousState == null) {
            return false;
        }

        changeState(previousState);
        return true;
    }

    /** Indicates whether the state machine is in the given state.
     * <p>
     * This implementation assumes states are singletons (typically an enum) so they are compared with the {@code ==} operator
     * instead of the {@code equals} method.
     *
     * @param state the state to be compared with the current state
     * @return true if the current state and the given state are the same object. */
    @Override
    public boolean isInState (S state) {
        return currentState == state;
    }

    /** Handles received telegrams. The telegram is first routed to the current state. If the current state does not deal with the
     * message, it's routed to the global state's message handler.
     *
     * @param telegram the received telegram
     * @return true if telegram has been successfully handled; false otherwise. */
    @Override
    public boolean handleMessage (Telegram telegram) {

        // First see if the current state is valid and that it can handle the message
        if (currentState != null && currentState.onMessage(owner, telegram)) {
            return true;
        }

        // If not, and if a global state has been implemented, send
        // the message to the global state
        if (globalState != null && globalState.onMessage(owner, telegram)) {
            return true;
        }

        return false;
    }
}
