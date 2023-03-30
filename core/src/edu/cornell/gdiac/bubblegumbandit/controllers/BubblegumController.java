package edu.cornell.gdiac.bubblegumbandit.controllers;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.helpers.GumJointPair;
import edu.cornell.gdiac.bubblegumbandit.models.level.LevelModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.gum.GumModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.physics.obstacle.Obstacle;


/**
 * Controls Bubblegum objects on the screen.
 * */
public class BubblegumController {

    /** The current amount of gum ammo of the player. */
    private int gumAmmo;

    private int startingGum;

    /**Amount of active gum. */
    private static int activeGum;

    /**The map of stuck Bubblegum obstacles to their joints. */
    private static ObjectMap<GumModel, Array<GumJointPair>> stuckBubblegum;

    /**The queue of non-assembled Bubblegum obstacles and their jointDefs. */
    private static Queue<GumJointPair> bubblegumAssemblyQueue;

    /**The queue of joints to remove. */
    private static Queue<GumJointPair> jointsToRemove;

    /**The queue of mid-air Bubblegum obstacles. */
    private static Queue<GumModel> midAirBubblegumQueue;

    /** Stores the stuck gum texture */
    private TextureRegion stuckGumTexture;

    /**Stores rotated gum */
    private TextureRegion rotatedStuckGumTexture;

    /**
     * Instantiates the Bubblegum controller and its queues.
     * */
    public BubblegumController(){
        stuckBubblegum = new ObjectMap<>();
        bubblegumAssemblyQueue = new Queue<>();
        midAirBubblegumQueue = new Queue<>();
        jointsToRemove = new Queue<>();
    }

    /** Initialize bubblegumController stats */
    public void initialize(AssetDirectory directory, JsonValue json) {
        gumAmmo = json.get("starting_gum").asInt();
        startingGum = gumAmmo;
        String key = json.get("stuckTexture").asString();
        stuckGumTexture = new TextureRegion(directory.getEntry(key, Texture.class));
        String key2 = json.get("rotatedStuckTexture").asString();
        rotatedStuckGumTexture = new TextureRegion(directory.getEntry(key2, Texture.class));
    }

    public void resetAmmo() {
        gumAmmo = startingGum;
    }
    /** gets the amount of bubblegum player has */
    public int getAmmo() {
        return gumAmmo;
    }

    /** reduces max gum by 1 */
    public void fireGum() {
        gumAmmo -= 1;
    }

    /** increases amount of ammo by ammo */
    public void addAmmo(int ammo) {
        gumAmmo += ammo;
    }

    /** Returns the stuck gum texture. */
    public TextureRegion getStuckGumTexture() { return stuckGumTexture; }

    public TextureRegion getRotatedGumTexture() {return rotatedStuckGumTexture; }

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

        Array<GumJointPair> gumJoints = stuckBubblegum.get(pair.getGum());
        if (gumJoints == null) {
            gumJoints = new Array<>();
            gumJoints.add(pair);
            stuckBubblegum.put(pair.getGum(), gumJoints);
        } else {
            gumJoints.add(pair);
        }
    }

    /**
     * Queues a gum instance and its associated joints to be removed at the next opportunity.
     *
     * @param gum the gum model to be removed
     */
    public void removeGum(GumModel gum) {
        for (GumJointPair j : stuckBubblegum.get(gum)) {
            jointsToRemove.addLast(j);
        }
        stuckBubblegum.remove(gum);

    }

    /**
     * Clears all Bubblegum queues and sets the amount of active gum to 0.
     * */
    public void resetAllBubblegum(){
        if(activeGum == 0) return;
        if(bubblegumAssemblyQueue == null) return;
        if(stuckBubblegum == null) return;
        if(midAirBubblegumQueue == null) return;

        bubblegumAssemblyQueue.clear();
        stuckBubblegum.clear();
        midAirBubblegumQueue.clear();
        activeGum = 0;
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
     * Returns true if the number of active Bubblegum objects is
     * equal to or greater than the gum limit.
     *
     * @returns true if the gum limit has been reached
     * */
    public boolean gumLimitReached(){
        return gumAmmo == 0;
    }

    /**
     * Adds a new Bubblegum to
     * equal to or greater than the gum limit.
     *
     * @returns true if the gum limit has been reached
     * */
    public void addNewBubblegum(GumModel gum){
        activeGum++;
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

    /**
     * Returns a WeldJointDef connecting gum and another obstacle.
     */
    public WeldJointDef createGumJoint(Obstacle gum, Obstacle ob, boolean vertical) {
        Vector2 gumPos = gum.getPosition();
        Vector2 obPos = ob.getPosition();
        float yDiff = gumPos.y - obPos.y;
        float xDiff = gumPos.x - obPos.x;

        WeldJointDef jointDef = new WeldJointDef();
        jointDef.bodyA = gum.getBody();
        jointDef.bodyB = ob.getBody();
        jointDef.referenceAngle = gum.getAngle() - ob.getAngle();
        Vector2 anchor = new Vector2();
        jointDef.localAnchorA.set(anchor);
        anchor.set(gum.getX() - ob.getX(), gum.getY() - ob.getY() - yDiff*0.25f);
        if (vertical) {
            anchor.set(gum.getX() - ob.getX() - xDiff*0.25f, gum.getY() - ob.getY());
        }
        jointDef.localAnchorB.set(anchor);
        return jointDef;
    }


    /**
     * Adds every joint in the joint queue to the world before clearing the queue.
     * Also removes every joint in the queue of joints to be removed.
     */
    public void updateJoints(LevelModel level) {
        for(int i = 0; i < bubblegumAssemblyQueue.size; i++){
            GumJointPair pairToAssemble = dequeueAssembly();
            WeldJointDef weldJointDef = pairToAssemble.getJointDef();
            WeldJoint createdWeldJoint = (WeldJoint) level.getWorld().createJoint(weldJointDef);
            GumJointPair activePair = new GumJointPair(pairToAssemble.getGum(), createdWeldJoint);
            addToStuckBubblegum(activePair);
        }
        for (GumJointPair gumJoint : jointsToRemove) {
            gumJoint.getGum().markRemoved(true);
            try {
                Obstacle ob1 = (Obstacle) gumJoint.getJoint().getBodyA().getUserData();
                Obstacle ob2 = (Obstacle) gumJoint.getJoint().getBodyB().getUserData();
                ob1.setStuck(false);
                ob2.setStuck(false);
            } catch (Exception ignored) {

            }

        }
    }

    /**
     * Add a new gum projectile to the world and send it in the right direction.
     */
    public GumModel createGumProjectile(Vector2 target, JsonValue gumJV, BanditModel avatar, Vector2 origin, Vector2 scale, TextureRegion texture) {


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
        gum.setGravityScale(gumJV.getFloat("gravity", 0));
        addNewBubblegum(gum);

        // Compute position and velocity
        gumVel.scl(gumJV.getFloat("speed", 0));

        gum.setVX(gumVel.x);
        gum.setVY(gumVel.y);

        return gum;
    }
}