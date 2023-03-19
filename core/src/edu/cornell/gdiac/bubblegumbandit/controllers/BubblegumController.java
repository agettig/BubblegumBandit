package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.bubblegumbandit.helpers.GumJointPair;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.bubblegumbandit.models.projectiles.GumModel;


/**
 * Controls GumModel objects on the screen.
 * */
public class BubblegumController {

    /**Maximum amount of GumModel on screen at the same time. */
    private static final int MAX_GUM = 30;

    /**Amount of active gum. */
    private static int ACTIVE_GUM;

    /**The queue of stuck GumModel obstacles and their joints. */
    private static Queue<GumJointPair> stuckBubblegumQueue;

    /**The queue of non-assembled GumModel obstacles and their jointDefs. */
    private static Queue<GumJointPair> bubblegumAssemblyQueue;

    /**The queue of mid-air GumModel obstacles. */
    private static Queue<GumModel> midAirBubblegumQueue;

    /** Gum gravity scale when creating gum */
    private float gumGravity;

    /** Gum speed when creating gum */
    private float gumSpeed;

    /**
     * Instantiates the GumModel controller and its queues.
     * */
    public BubblegumController(){
        stuckBubblegumQueue = new Queue<GumJointPair>();
        bubblegumAssemblyQueue = new Queue<GumJointPair>();
        midAirBubblegumQueue = new Queue<GumModel>();
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
     * Adds a GumJointPair to the queue of active GumModel.
     * */
    public void addToStuckBubblegum(GumJointPair pair){

        //TODO: Fix this architecture so the method can be private.

        if(pair == null) return;
        if(pair.getGum() == null || pair.getJoint() == null) return;
        if(pair.getJointDef() != null) return;

        stuckBubblegumQueue.addLast(pair);
    }

    /**
     * Add a new gum projectile to the world and send it in the right direction.
     */
    public GumModel createGumProjectile(Vector2 target, JsonValue gumJV, BanditModel avatar, Vector2 origin, Vector2 scale,
                                    float gumSpeed, float gumGravity, TextureRegion texture) {

        if(gumLimitReached()) return null;

        Vector2 gumVel = new Vector2(target.x - origin.x, target.y - origin.y);
        gumVel.nor();

        // Prevent player from shooting themselves by clicking on player
        // TODO: Should be tied in with raycast in LevelModel, check if raycast hits player
        if (origin.x > avatar.getX() && gumVel.x < 0) { //  && gumVel.angleDeg() > 110 && gumVel.angleDeg() < 250)) {
            return null;
        } else if (origin.x < avatar.getX() && gumVel.x > 0) { //&& (gumVel.angleDeg() < 70 || gumVel.angleDeg() > 290)) {
            return null;
        }

        float radius = texture.getRegionWidth() / (2.0f * scale.x);

        //Create a new GumModel and assign it to the BubblegumController.
        GumModel gum = new GumModel(origin.x, origin.y, radius);
        gum.setName(gumJV.name());
        gum.setDensity(gumJV.getFloat("density", 0));
        gum.setDrawScale(scale);
        gum.setTexture(texture);
        gum.setBullet(true);
        gum.setGravityScale(gumGravity);
        addNewBubblegum(gum);

        // Compute position and velocity
        if (gumSpeed == 0) { // Use default gum speed
            gumVel.scl(gumJV.getFloat("speed", 0));
        } else { // Use slider gum speed
            gumVel.scl(gumSpeed);
        }
        gum.setVX(gumVel.x);
        gum.setVY(gumVel.y);

        return gum;
    }

    /**
     * Clears all GumModel queues and sets the amount of active gum to 0.
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
     * Returns true if the number of active GumModel objects is
     * equal to or greater than the gum limit.
     *
     * @returns true if the gum limit has been reached
     * */
    public boolean gumLimitReached(){
        return ACTIVE_GUM >= MAX_GUM;
    }

    /**
     * Adds a new GumModel to
     * equal to or greater than the gum limit.
     *
     * @returns true if the gum limit has been reached
     * */
    public void addNewBubblegum(GumModel gum){
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
