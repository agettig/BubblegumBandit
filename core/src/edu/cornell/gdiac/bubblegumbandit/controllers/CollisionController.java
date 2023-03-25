package edu.cornell.gdiac.bubblegumbandit.controllers;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.bubblegumbandit.helpers.GumJointPair;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.ExitModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.ProjectileModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.gum.FloatingGum;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.gum.GumModel;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.bubblegumbandit.models.level.LevelModel;


public class CollisionController implements ContactListener {

    public static final short CATEGORY_PLAYER = 0x0001;
    public static final short CATEGORY_ENEMY = 0x0002;
    public static final short CATEGORY_TERRAIN = 0x0004;
    public static final short CATEGORY_GUM = 0x0008;
    public static final short CATEGORY_PROJECTILE = 0x0010;

    public static final short MASK_PLAYER = ~CATEGORY_GUM;
    public static final short MASK_ENEMY = ~CATEGORY_ENEMY;
    public static final short MASK_TERRAIN = -1; // Collides with everything
    public static final short MASK_GUM = ~(CATEGORY_PLAYER | CATEGORY_GUM);
    public static final short MASK_GUM_LIMIT = ~(CATEGORY_PLAYER | CATEGORY_GUM | CATEGORY_ENEMY);
    public static final short MASK_PROJECTILE = ~(CATEGORY_PROJECTILE | CATEGORY_ENEMY);

    /** The amount of gum collected when collecting floating gum */
    private static final int AMMO_AMOUNT = 5;
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

    /**Temp queue for now for sticking robot joints */
    private Queue<WeldJointDef> stickRobots = new Queue<>();

    public void resetWinCondition(){
        winConditionMet = false;
    }


    /**
     * Construct a new CollisionController.
     *
     * This constructor initializes all the caching objects so that
     * there is no heap allocation during collision detection.
     */
    public CollisionController(LevelModel levelModel, BubblegumController controller){
        sensorFixtures = new ObjectSet<Fixture>();
        bubblegumController = controller;
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
            checkProjectileCollision(obstacleA, obstacleB);
            resolveFloatingGumCollision(obstacleA, obstacleB);
            createEnemyTileJoint(obstacleA, obstacleB);

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

        BanditModel avatar = levelModel.getBandit();
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
        BanditModel bandit = levelModel.getBandit();
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
        // Gum should destroy projectiles, but not become sticky gum.
        if (bodyA.getName().equals("projectile") || bodyB.getName().equals("projectile")) return;

        GumModel gum = null;
        Obstacle body = null;
        EnemyModel enemy = null;
        if (isGumObstacle(bodyA)) {
            gum = (GumModel) bodyA;
            if (bodyB instanceof EnemyModel) {
                enemy = (EnemyModel) bodyB;
            }
            body = bodyB;
        };
        if (isGumObstacle(bodyB)) {
            gum = (GumModel) bodyB;
            if (bodyA instanceof EnemyModel) {
                enemy = (EnemyModel) bodyA;
            }
            body = bodyA;
        };
        if (gum != null && gum.getName().equals("gumProjectile")) {
            // Do this once gum is turning from a projectile to sticky
            gum.setVX(0);
            gum.setVY(0);
            gum.setTexture(bubblegumController.getStuckGumTexture());
            gum.setName("stickyGum");
            gum.setRadius(gum.getRadius() * 1.5f);
            // Changing radius resets filter for some reason
            gum.getFilterData().maskBits = MASK_GUM;
            gum.getFilterData().categoryBits = CATEGORY_GUM;
        }

        if (gum != null && gum.canAddObstacle(body)){
            if (body instanceof EnemyModel) {
                gum.markRemoved(true);
                enemy.setTexture(enemy.getGummedTexture());
                enemy.setGummed(true);
            }
            else {
                WeldJointDef weldJointDef = bubblegumController.createGumJoint(gum, body);
                GumJointPair pair = new GumJointPair(gum, weldJointDef);
                bubblegumController.addToAssemblyQueue(pair);
                gum.addObstacle(body);
                gum.setCollisionFilters();
            }
        }
    }

    /** Adjust isGrounded to also save what floor it is on, call in gameController to weld robots on ground that are gummed*/
    public void createEnemyTileJoint(Obstacle ob1, Obstacle ob2) {
        WeldJointDef jointDef = new WeldJointDef();
        EnemyModel enemy;

        if (ob1 instanceof EnemyModel) {
            enemy = (EnemyModel) ob1;
            if ((ob2.getName().contains("tile") || ob2.getName().contains("wall")) && enemy.getGummed() == true) {
                jointDef.bodyA = ob2.getBody();
                jointDef.bodyB = ob1.getBody();
                Vector2 anchor = new Vector2();
                jointDef.localAnchorB.set(anchor);
                anchor.set(ob1.getX() - ob2.getX(), ob1.getY() - ob2.getY());
                jointDef.localAnchorA.set(anchor);
                stickRobots.addLast(jointDef);
            }
        }
        if (ob2 instanceof EnemyModel) {
            enemy = (EnemyModel) ob2;
            if ((ob1.getName().contains("tile") || ob2.getName().contains("wall")) && enemy.getGummed() == true) {
                jointDef.bodyA = ob1.getBody();
                jointDef.bodyB = ob2.getBody();
                Vector2 anchor = new Vector2();
                jointDef.localAnchorA.set(anchor);
                anchor.set(ob1.getX() - ob2.getX(), ob1.getY() - ob2.getY());
                jointDef.localAnchorB.set(anchor);
                stickRobots.addLast(jointDef);
            }
        }
    }


    public void addRobotJoints(LevelModel level) {
        if (stickRobots.size == 0) return;
        for (WeldJointDef joint : stickRobots) {
            level.getWorld().createJoint(joint);
        }
    }

    /**
     * Checks if there was an enemy projectile collision in the Box2D world.
     * <p>
     * Examines two Obstacles in a collision.
     * *
     * @param bd1 The first Obstacle in the collision.
     * @param bd2 The second Obstacle in the collision.
     */
    private void checkProjectileCollision(Obstacle bd1, Obstacle bd2) {

        // Check that obstacles are not null and not an enemy
        if (bd1 == null || bd2 == null) return;
        if (bd1.getName().contains("enemy") || bd2.getName().equals("enemy")) return;

        if (bd1.getName().equals("projectile")) {
            resolveProjectileCollision((ProjectileModel) bd1, bd2);
        } else if (bd2.getName().equals("projectile")) {
            resolveProjectileCollision((ProjectileModel) bd2, bd1);
        }
    }

    /**
     * Resolves the effects of a projectile collision
     * @param p
     * @param o
     */
    private void resolveProjectileCollision(ProjectileModel p, Obstacle o){
        if (p.isRemoved()) return;
        if (o.getName().equals("avatar")){
           levelModel.getBandit().hitPlayer(p.getDamage());
        }
        p.destroy();
    }

    /**
     * Resolves collisions for ground contact, adding the necessary
     * sensor fixtures.
     * */
    private void resolveGroundContact(Obstacle bodyA, Fixture fixA, Obstacle bodyB, Fixture fixB){

        BanditModel bandit = levelModel.getBandit();

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

    public void resolveFloatingGumCollision(Obstacle bd1, Obstacle bd2){
        if (bd1 instanceof FloatingGum && bd2 == levelModel.getBandit() && !((FloatingGum) bd1).getCollected()){
            collectGum(bd1);
            ((FloatingGum) bd1).setCollected(true);
        } else if (bd2 instanceof FloatingGum && bd1 == levelModel.getBandit() && !((FloatingGum) bd2).getCollected()) {
            collectGum(bd2);
            ((FloatingGum) bd2).setCollected(true);
        }
    }

    private void collectGum(Obstacle bd1) {
        bd1.markRemoved(true);
        bubblegumController.addAmmo(AMMO_AMOUNT);
    }

}
