package edu.cornell.gdiac.bubblegumbandit.controllers;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.bubblegumbandit.helpers.GumJointPair;
import edu.cornell.gdiac.bubblegumbandit.models.level.ExitModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.bubblegumbandit.models.projectiles.GumModel;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.bubblegumbandit.models.level.LevelModel;


public class CollisionController implements ContactListener {

    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;

    /** Bubblegum Controller reference */
    private final BubblegumController bubblegumController;

    /** Reference to the LevelModel */
    private LevelModel levelModel;

    /** true if the win condition has been met */
    private boolean winConditionMet;


    /**
     * Construct a new CollisionController.
     *
     * This constructor initializes all the caching objects so that
     * there is no heap allocation during collision detection.
     */
    public CollisionController(LevelModel levelModel){
        sensorFixtures = new ObjectSet<Fixture>();
        bubblegumController = new BubblegumController();
        this.levelModel = levelModel;
    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     * <p>
     * This is where we check for gum collisions
     *
     * @param contact The two bodies that collided
     */
    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        Body bodyA = fixA.getBody();
        Body bodyB = fixB.getBody();

        try{
            Obstacle obstacleA = (Obstacle) bodyA.getUserData();
            Obstacle obstacleB = (Obstacle) bodyB.getUserData();

            resolveGumCollision(obstacleA, obstacleB);
            resolveWinCondition(obstacleA, obstacleB);
            resolveGroundContact(obstacleA, fixA, obstacleB, fixB);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    @Override
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        BanditModel avatar = levelModel.getAvatar();
        if ((avatar.getSensorName2().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName2().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                avatar.setGrounded(false);
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    /**
     * Checks if the bandit has collided with the exit and sets the
     * win condition met field accordingly.
     * */
    private void resolveWinCondition(Obstacle bodyA, Obstacle bodyB){
        // Check for win condition
        BanditModel bandit = levelModel.getAvatar();
        ExitModel door = levelModel.getExit();

        boolean winConditionA = bodyA == bandit && bodyB == door;
        boolean winConditionB = bodyA == door && bodyB == bandit;

        if (winConditionA ||winConditionB){ winConditionMet = true;}
    }

    /**
     *  Adds a GumJointPair to the BubblegumController's assembly
     *  queue if two colliding bodies are a valid gum/body collision.
     *
     *  @param bodyA The first body in the collision
     *  @param bodyB The second body in the collision
     */
    public void resolveGumCollision(Obstacle bodyA, Obstacle bodyB){
        //Safety check.
        if (bodyA == null || bodyB == null) return;

        if (isGumObstacle(bodyA)) {
            GumModel gum = (GumModel) bodyA;
            gum.setVX(0);
            gum.setVY(0);

            WeldJointDef weldJointDef = createGumJoint(gum, bodyB);
            GumJointPair pair = new GumJointPair(gum, weldJointDef);
            bubblegumController.addToAssemblyQueue(pair);

        }
        else if (isGumObstacle(bodyB)) {
            GumModel gum = (GumModel) bodyB;
            gum.setVX(0);
            gum.setVY(0);

            WeldJointDef weldJointDef = createGumJoint(gum, bodyA);
            GumJointPair pair = new GumJointPair(gum, weldJointDef);
            bubblegumController.addToAssemblyQueue(pair);
        }
    }

    /**
     * Resolves collisions for ground contact, adding the necessary
     * sensor fixtures.
     * */
    private void resolveGroundContact(Obstacle bodyA, Fixture fixA, Obstacle bodyB, Fixture fixB){

        BanditModel bandit = levelModel.getAvatar();

        Object dataA = fixA.getUserData();
        Object dataB = fixB.getUserData();

        if ((bandit.getSensorName().equals(dataB) && bandit != bodyA) ||
                (bandit.getSensorName().equals(dataA) && bandit != bodyB)) {
            bandit.setGrounded(true);
            sensorFixtures.add(bandit == bodyA ? fixB : fixA);
        }
    }

    /**
     * Returns true if an Obstacle is a gum projectile.
     * <p>
     * An Obstacle is a gum projectile if its name equals
     * "gumProjectile".
     *
     * @param o the Obstacle to check
     * @returns true if the Obstacle is a gum projectile
     */
    private boolean isGumObstacle(Obstacle o) {
        return o.getName().equals("stickyGum") ||
                o.getName().equals("gumProjectile");
    }

    /**
     * Returns a WeldJointDef connecting gum and another obstacle.
     */
    private WeldJointDef createGumJoint(Obstacle gum, Obstacle ob) {
        WeldJointDef jointDef = new WeldJointDef();
        jointDef.bodyA = gum.getBody();
        jointDef.bodyB = ob.getBody();
        jointDef.referenceAngle = gum.getAngle() - ob.getAngle();
        Vector2 anchor = new Vector2();
        jointDef.localAnchorA.set(anchor);
        anchor.set(gum.getX() - ob.getX(), gum.getY() - ob.getY());
        jointDef.localAnchorB.set(anchor);
        return jointDef;
    }

    /**
     * Returns true if the CollisionController has detected that the
     * bandit has collided with the exit.
     *
     * @return true if win condition has been met, false otherwise.
     * */
    public boolean isWinConditionMet(){
        return winConditionMet;
    }

    public void clearSensorFixtures(){
        sensorFixtures.clear();
    }
}
