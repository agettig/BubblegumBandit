package edu.cornell.gdiac.bubblegumbandit.controllers;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.bubblegumbandit.helpers.GumJointPair;
import edu.cornell.gdiac.bubblegumbandit.helpers.Gummable;
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
    private static ObjectMap<GumModel, ObjectSet<GumJointPair>> stuckBubblegum;

    /** The map of Gummable objects to their joints. */
    private static ObjectMap<Gummable, ObjectSet<Joint>> stuckToGummable;

    /**The queue of non-assembled Bubblegum obstacles and their jointDefs. */
    private static Queue<GumJointPair> bubblegumAssemblyQueue;

    /**The queue of joints between gum and objects to remove. */
    private static Queue<GumJointPair> gumJointsToRemove;

    /**The queue of joints between gummable objects and objects to remove. */
    private static Queue<Joint> gummableJointsToRemove;


    /**The queue of mid-air Bubblegum obstacles. */
    private static Queue<GumModel> midAirBubblegumQueue;

    /** Queue of joints between Gummable object and collided object */
    private static Queue<WeldJointDef> gummableAssemblyQueue;

    /** Stores the stuck gum texture */
    private TextureRegion stuckGumTexture;

    /**Stores rotated gum */
    private TextureRegion rotatedStuckGumTexture;

    /**Stores corner gum */
    private TextureRegion topRightGumTexture;
    private TextureRegion bottomRightGumTexture;
    private TextureRegion bottomLeftGumTexture;
    private TextureRegion topLeftGumTexture;

    /** Stores outline gum textures */
    private TextureRegion stuckOutline;
    private TextureRegion rotatedOutline;
    private TextureRegion topRightOutline;
    private TextureRegion bottomRightOutline;
    private TextureRegion topLeftOutline;
    private TextureRegion bottomLeftOutline;


    /**
     * Instantiates the Bubblegum controller and its queues.
     * */
    public BubblegumController(){
        stuckBubblegum = new ObjectMap<>();
        bubblegumAssemblyQueue = new Queue<>();
        midAirBubblegumQueue = new Queue<>();
        gumJointsToRemove = new Queue<>();
        gummableJointsToRemove = new Queue<>();
        gummableAssemblyQueue = new Queue<>();
        stuckToGummable = new ObjectMap<>();
    }

    /** Initialize bubblegumController stats */
    public void initialize(AssetDirectory directory, JsonValue json) {
        gumAmmo = json.get("startingGum").asInt();
        startingGum = gumAmmo;
        String key = json.get("stuckTexture").asString();
        stuckGumTexture = new TextureRegion(directory.getEntry(key, Texture.class));
        key = json.get("rotatedStuckTexture").asString();
        rotatedStuckGumTexture = new TextureRegion(directory.getEntry(key, Texture.class));
        key = json.get("topRightStuckTexture").asString();
        topRightGumTexture = new TextureRegion(directory.getEntry(key, Texture.class));
        key = json.get("bottomRightStuckTexture").asString();
        bottomRightGumTexture = new TextureRegion(directory.getEntry(key, Texture.class));
        key= json.get("bottomLeftStuckTexture").asString();
        bottomLeftGumTexture = new TextureRegion(directory.getEntry(key, Texture.class));
        key = json.get("topLeftStuckTexture").asString();
        topLeftGumTexture = new TextureRegion(directory.getEntry(key, Texture.class));

        // Set outline textures
        stuckOutline = new TextureRegion(directory.getEntry("stuckOutline", Texture.class));
        rotatedOutline = new TextureRegion(directory.getEntry("rotatedOutline", Texture.class));
        topLeftOutline = new TextureRegion(directory.getEntry("topLeftOutline", Texture.class));
        topRightOutline = new TextureRegion(directory.getEntry("topRightOutline", Texture.class));
        bottomLeftOutline = new TextureRegion(directory.getEntry("bottomLeftOutline", Texture.class));
        bottomRightOutline = new TextureRegion(directory.getEntry("bottomRightOutline", Texture.class));

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
    public TextureRegion getTopRightGumTexture() {return topRightGumTexture; }

    public TextureRegion getBottomRightGumTexture() {return bottomRightGumTexture;}

    public TextureRegion getBottomLeftGumTexture() {return bottomLeftGumTexture;}

    public TextureRegion getTopLeftGumTexture() {return topLeftGumTexture;}

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
    private void addToStuckBubblegum(GumJointPair pair){

        if(pair == null) return;
        if(pair.getGum() == null || pair.getJoint() == null) return;
        if(pair.getJointDef() != null) return;

        ObjectSet<GumJointPair> gumJoints = stuckBubblegum.get(pair.getGum());
        if (gumJoints == null) {
            gumJoints = new ObjectSet<>();
            gumJoints.add(pair);
            stuckBubblegum.put(pair.getGum(), gumJoints);
        } else {
            gumJoints.add(pair);
        }
    }

    /**
     * Adds a joint between a Gummable and an object to the active joint map.
     */
    public void addToGummableMap(Joint joint) {
        assert joint != null;
        Gummable gummable = (Gummable) joint.getBodyA().getUserData();

        ObjectSet<Joint> gummableJoints = stuckToGummable.get(gummable);
        if (gummableJoints == null) {
            gummableJoints = new ObjectSet<>();
            gummableJoints.add(joint);
            stuckToGummable.put(gummable, gummableJoints);
        } else {
            gummableJoints.add(joint);
        }
    }

    /**
     * Queues a gum instance and its associated joints to be removed at the next opportunity.
     *
     * @param gum the gum model to be removed
     */
    public void removeGum(GumModel gum) {
        for (GumJointPair j : stuckBubblegum.get(gum)) {
            gumJointsToRemove.addLast(j);
        }
        stuckBubblegum.remove(gum);
    }

    /**
     * Queues a gummable's associated joints to be removed at the next opportunity and ungums the gummable.
     *
     * @param gummable the gummable whose joints should be removed
     */
    public void removeGummable(Gummable gummable) {
        gummable.setGummed(false);

        for (Joint j : stuckToGummable.get(gummable)) {
            gummableJointsToRemove.addLast(j);
        }
        stuckToGummable.get(gummable).clear();
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
    public WeldJointDef createGumJoint(Obstacle gum, Obstacle ob, int orientation) {
        Vector2 gumPos = gum.getPosition();
        Vector2 obPos = ob.getPosition();
        float yDiff = gumPos.y - obPos.y;
        float xDiff = gumPos.x - obPos.x;

        WeldJointDef jointDef = new WeldJointDef();
        jointDef.bodyA = gum.getBody();
        jointDef.bodyA.setUserData(gum);
        jointDef.bodyB = ob.getBody();
        jointDef.bodyB.setUserData(ob);
        jointDef.referenceAngle = gum.getAngle() - ob.getAngle();
        Vector2 anchor = new Vector2();
        jointDef.localAnchorA.set(anchor);
        anchor.set(gum.getX() - ob.getX(), gum.getY() - ob.getY() - yDiff*0.4f);
        if (orientation == 1) {
            anchor.set(gum.getX() - ob.getX() - xDiff*0.4f, gum.getY() - ob.getY());
        }
        else if (orientation == 2) {
            anchor.set(gum.getX() - ob.getX() - xDiff*0.5f, gum.getY() - ob.getY() - yDiff*0.65f);
        }
        else if (orientation == 3) {
            anchor.set(gum.getX() - ob.getX() - xDiff*0.75f, gum.getY() - ob.getY() - yDiff*0.65f);
        }
        jointDef.localAnchorB.set(anchor);
        return jointDef;
    }

    /** Creates a joint between a gummable object and another object and adds it
     * to the queue of gummable joints.
     * @param gummable The gummable object to be connected
     * @param ob The other object to be connected
     * */
    public void createGummableJoint(Gummable gummable, Obstacle ob) {
        WeldJointDef jointDef = new WeldJointDef();
        Obstacle gummableOb = (Obstacle) gummable;
        jointDef.bodyA = gummableOb.getBody();
        jointDef.bodyB = ob.getBody();
        Vector2 anchor = new Vector2();
        jointDef.localAnchorB.set(anchor);
        anchor.set(ob.getX() - gummableOb.getX(), ob.getY() - gummableOb.getY());
        jointDef.localAnchorA.set(anchor);
        gummableAssemblyQueue.addLast(jointDef);
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
            SoundController.playSound("gumSplat", 0.5f);
        }
        for (int i = 0; i < gummableAssemblyQueue.size; i++) {
            Joint joint = level.getWorld().createJoint(gummableAssemblyQueue.removeFirst());
            addToGummableMap(joint);
            SoundController.playSound("enemySplat", 1f);
        }
        for (int i = 0; i < gumJointsToRemove.size; i++) {
            GumJointPair gumJoint = gumJointsToRemove.removeFirst();
            gumJoint.getGum().markRemoved(true);
            try {
                Obstacle ob1 = (Obstacle) gumJoint.getJoint().getBodyA().getUserData();
                ob1.setStuck(false);
                if (ob1.isFlipped() == level.getWorld().getGravity().y < 0) {
                    ob1.flippedGravity();
                }
            }
            catch (Exception ignored) {
            }
            try {
                Obstacle ob2 = (Obstacle) gumJoint.getJoint().getBodyB().getUserData();
                ob2.setStuck(false);

                if (ob2.isFlipped() == level.getWorld().getGravity().y < 0) {
                    ob2.flippedGravity();
                }
            }
            catch (Exception ignored) {

            }

        }
        for (int i = 0; i < gummableJointsToRemove.size; i++) {
            Joint j = gummableJointsToRemove.removeFirst();
            try {
                Obstacle ob1 = (Obstacle) j.getBodyA().getUserData();
                ob1.setStuck(false);
                if (ob1.isFlipped() == level.getWorld().getGravity().y < 0) {
                    ob1.flippedGravity();
                }
            } catch (Exception ignored) {

            }
            try {
                Obstacle ob2 = (Obstacle) j.getBodyB().getUserData();
                ob2.setStuck(false);
                if (ob2.isFlipped() == level.getWorld().getGravity().y < 0) {
                    ob2.flippedGravity();
                }
            } catch (Exception ignored) {

            }
            level.getWorld().destroyJoint(j);
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
        GumModel gum = new GumModel(origin.x, origin.y, radius*2f);
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

    public TextureRegion getStuckOutline() {return stuckOutline;}
    public TextureRegion getRotatedOutline() {return rotatedOutline;}
    public TextureRegion getTopLeftOutline() {return topLeftOutline;}
    public TextureRegion getTopRightOutline() {return topRightOutline;}
    public TextureRegion getBottomLeftOutline() {return bottomLeftOutline;}
    public TextureRegion getBottomRightOutline() {return bottomRightOutline;}


}