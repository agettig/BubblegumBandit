package edu.cornell.gdiac.json.gum;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.assets.AssetDirectory;


/**
 * Controls Bubblegum objects on the screen.
 * */
public class BubblegumController {

    /**Maximum amount of Bubblegum on screen at the same time. */
    private int MAX_GUM;

    /**Amount of active gum. */
    private static int ACTIVE_GUM;

    /**The queue of stuck Bubblegum obstacles and their joints. */
    private static Queue<GumJointPair> stuckBubblegumQueue;

    /**The queue of non-assembled Bubblegum obstacles and their jointDefs. */
    private static Queue<GumJointPair> bubblegumAssemblyQueue;

    /**The queue of mid-air Bubblegum obstacles. */
    private static Queue<Bubblegum> midAirBubblegumQueue;

    /**
     * Instantiates the Bubblegum controller and its queues.
     * */
    public BubblegumController(){
        stuckBubblegumQueue = new Queue<GumJointPair>();
        bubblegumAssemblyQueue = new Queue<GumJointPair>();
        midAirBubblegumQueue = new Queue<Bubblegum>();
    }

    /** Initialize bublegumController stats */
    public void initialize(JsonValue json) {
        MAX_GUM = json.get("starting_gum").asInt();
    }

    /**gets the amounut of bubblegum player has */
    public int getMAX_GUM() {
        return MAX_GUM;
    }

    /** reduces max gum by 1 */
    public void reduceMAX_GUM() {
        MAX_GUM -= 1;
    }

    /** increases max gum by 1 */
    public void increaseMAX_GUM() {
        MAX_GUM += 1;
    }


    /**
     * Adds a GumJointPair to the assembly queue if possible.
     * */
    public void addToAssemblyQueue(GumJointPair pair){

        //Safety checks
        if(pair == null) return;
        if(pair.getGum() == null || pair.getJointDef() == null) return;
        if(pair.getJoint() != null) return;

        bubblegumAssemblyQueue.addLast(pair);
    }

    /**
     * Returns and removes the first GumJointPair from the queue of GumJointPairs
     * that are to be assembled.
     *
     * @returns the first GumJointPair from the queue of GumJointPairs
     * that are to be assembled.
     * */
    public GumJointPair dequeueAssembly(){
        dequeueMidAir();
        return bubblegumAssemblyQueue.removeFirst();
    }

    /**
     * Adds a GumJointPair to the queue of active Bubblegum.
     * */
    public void addToStuckBubblegum(GumJointPair pair){

        //TODO: Fix this architecture so the method can be private.

        if(pair == null) return;
        if(pair.getGum() == null || pair.getJoint() == null) return;
        if(pair.getJointDef() != null) return;

        stuckBubblegumQueue.addLast(pair);
    }

    /**
     * Clears all Bubblegum queues and sets the amount of active gum to 0.
     * */
    public void resetAllBubblegum(){
        if(ACTIVE_GUM == 0) return;
        if(bubblegumAssemblyQueue == null) return;
        if(stuckBubblegumQueue == null) return;
        if(midAirBubblegumQueue == null) return;

        bubblegumAssemblyQueue.clear();
        stuckBubblegumQueue.clear();
        midAirBubblegumQueue.clear();
        ACTIVE_GUM = 0;
    }

    /**
     * For every GumJoint pair in the queue of active GumJointPairs,
     * (1) destroys the joint in the pair,
     * (2) destroys the body of the gum.
     * Then, clears the queue.
     * */
    public void collectBubblegum(){
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the number of GumJointPairs that have yet to be assembled.
     *
     * @returns the number of GumJointPairs to assemble
     * */
    public int numActivePairsToAssemble(){
        return bubblegumAssemblyQueue.size;
    }

    /**
     * Returns true if the number of active Bubblegum objects is
     * equal to or greater than the gum limit.
     *
     * @returns true if the gum limit has been reached
     * */
    public boolean gumLimitReached(){
        return MAX_GUM == 0;
    }

    /**
     * Adds a new Bubblegum to
     * equal to or greater than the gum limit.
     *
     * @returns true if the gum limit has been reached
     * */
    public void addNewBubblegum(Bubblegum gum){
        ACTIVE_GUM++;
        midAirBubblegumQueue.addLast(gum);
    }

    /**
     * Returns and removes the first BubbleGum from the queue of gum
     * that are still in the air.
     *
     * @returns the first BubbleGum from the queue of gum
     * that are still in the air.
     * */
    private void dequeueMidAir(){
        if(midAirBubblegumQueue.isEmpty()) return;
        midAirBubblegumQueue.removeFirst();
    }

}
