package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.bubblegumbandit.helpers.GumJointPair;
import edu.cornell.gdiac.bubblegumbandit.helpers.Gummable;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.RollingEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.*;
import edu.cornell.gdiac.bubblegumbandit.models.level.gum.GumModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCamera;
import edu.cornell.gdiac.physics.obstacle.Obstacle;


public class CollisionController implements ContactListener {


    public static final short CATEGORY_ENEMY = 0x0002;
    public static final short CATEGORY_TERRAIN = 0x0004;
    public static final short CATEGORY_GUM = 0x0008;
    public static final short CATEGORY_PLAYER = 0x0020;
    public static final short CATEGORY_PROJECTILE = 0x0010;
    public static final short CATEGORY_EVENTTILE = 0x0040;
    public static final short CATEGORY_COLLECTIBLE = 0x0080;
    public static final short CATEGORY_UNSTICK = 0x0100;


    public static final short MASK_PLAYER = -1;
    public static final short MASK_ENEMY = ~(CATEGORY_ENEMY);
    public static final short CATEGORY_BACK = 0x0012;

    public static final short MASK_TERRAIN = -1; // Collides with everything
    public static final short MASK_GUM = ~(CATEGORY_GUM);
    public static final short MASK_GUM_LIMIT = ~(CATEGORY_PLAYER | CATEGORY_GUM | CATEGORY_ENEMY);
    public static final short MASK_PROJECTILE = ~(CATEGORY_PROJECTILE | CATEGORY_ENEMY);

    public static final short MASK_BACK = ~(CATEGORY_GUM | CATEGORY_ENEMY | CATEGORY_PLAYER);
    public static final short MASK_EVENTTILE = CATEGORY_PLAYER;
    public static final short MASK_COLLECTIBLE = CATEGORY_PLAYER;
    public static final short MASK_UNSTICK = ~CATEGORY_PLAYER;

    /**
     * The amount of gum collected when collecting floating gum
     */
    private static final int AMMO_AMOUNT = 5;
    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;

    /** Bubblegum Controller reference */
    private final BubblegumController bubblegumController;

    /** Reference to the LevelModel */
    private LevelModel levelModel;

    /** Reference to the game camera */
    private GameCamera camera;

    /** true if the win condition has been met */
    private boolean winConditionMet;


    /** Resets this CollisionController. */
    public void reset(){
        winConditionMet = false;
    }

    /**
     * Construct a new CollisionController.
     *
     * This constructor initializes all the caching objects so that
     * there is no heap allocation during collision detection.
     *
     * @param levelModel the level model
     * @param controller the bubblegum controller
     */
    public CollisionController(LevelModel levelModel, BubblegumController controller){
        sensorFixtures = new ObjectSet<Fixture>();
        bubblegumController = controller;
        this.levelModel = levelModel;
    }

    /** Initializes this CollisionController
     *
     * @param camera the game camera for the scene
     */
    public void initialize(GameCamera camera) {
        this.camera = camera;
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

        try {
            Obstacle obstacleA = (Obstacle) bodyA.getUserData();
            Obstacle obstacleB = (Obstacle) bodyB.getUserData();


            if (obstacleA instanceof Gummable) {
                obstacleA.startCollision(obstacleB);
            }
            if (obstacleB instanceof Gummable) {
                obstacleB.startCollision(obstacleA);
            }

            resolveGroundContact(obstacleA, fixA, obstacleB, fixB);
            resolveGumCollision(obstacleA, obstacleB);
            resolveWinCondition(obstacleA, obstacleB);
            checkProjectileCollision(obstacleA, obstacleB);
            resolveFloatingGumCollision(obstacleA, obstacleB);
            resolveGummableGumCollision(obstacleA, obstacleB);
            resolveStarCollision(obstacleA, obstacleB);
            resolveOrbCollision(obstacleA, obstacleB);
            checkMediumEnemyCollision(obstacleA, obstacleB);

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
        if (((avatar.getSensorName2().equals(fd2) || avatar.getSensorName().equals(fd2)) && avatar != bd1) ||
                ((avatar.getSensorName2().equals(fd1) || avatar.getSensorName().equals(fd1)) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                avatar.setGrounded(false);
            }
        }

        try{
            Obstacle ob1 = (Obstacle) body1.getUserData();
            Obstacle ob2 = (Obstacle) body2.getUserData();

            if (ob1 instanceof Gummable && ob1.getGummed()) {
                ob1.endCollision(ob2);
            }
            if (ob2 instanceof Gummable && ob2.getGummed()) {
                ob2.endCollision(ob1);
            }

            if (ob1.getName().equals("cameraTile") && avatar == bd2) {
                updateCamera(ob1);
            } else if (ob2.getName().equals("cameraTile") && avatar == bd1) {
                updateCamera(ob2);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    /** Updates the camera based on the collision between the player and the camera tile.
     *
     * @param ob the camera tile */
    private void updateCamera(Obstacle ob) {
        CameraTileModel camTile = (CameraTileModel) ob;
        BanditModel avatar = levelModel.getBandit();

        Vector2 ul;
        Vector2 lr;
        boolean isFirst = !((camTile.isHorizontal() && avatar.getX() > camTile.getX()) ||
                (!camTile.isHorizontal() && avatar.getY() < camTile.getY()));
        if (isFirst) {
            ul = camTile.getFirstUpperLeft();
            lr = camTile.getFirstLowerRight();
        } else {
            ul = camTile.getSecondUpperLeft();
            lr = camTile.getSecondLowerRight();
        }
        Vector2 scale = levelModel.getScale();
        float zoomWidth = 0;
        float zoomHeight = 0;
        boolean fixCamera = false;

        if ((isFirst && camTile.isFirstFixedX()) || (!isFirst && camTile.isSecondFixedX())) {
            float centerX = (ul.x + lr.x) * scale.x / 2f;
            zoomWidth = Math.abs(ul.x - lr.x) * scale.x;
            camera.setFixedX(true);
            camera.setTargetX(centerX);
            fixCamera = true;
        }
        if ((isFirst && camTile.isFirstFixedY()) || (!isFirst && camTile.isSecondFixedY())) {
            float centerY = (ul.y + lr.y) * scale.y / 2f;
            zoomHeight = Math.abs(ul.y - lr.y) * scale.y;
            camera.setFixedY(true);
            camera.setTargetY(centerY);
            fixCamera = true;
        }
        if (fixCamera) {
            camera.setZoom(zoomWidth, zoomHeight);
        }
        else {
            // Change camera to track the player
            camera.setFixedX(false);
            camera.setFixedY(false);
            camera.setZoom(1);
        }
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

        if (bandit.isOrbCollected() && (winConditionA ||winConditionB)){ winConditionMet = true;}
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
        if (bodyA.getName().equals("unstickProjectile") || bodyB.getName().equals("unstickProjectile")) return;
        if (bodyA.isRemoved() || bodyB.isRemoved()) return;
        if (bodyA.getName().equals("gumProjectile") && bodyB.equals(levelModel.getBandit())) return;
        if (bodyB.getName().equals("gumProjectile") && bodyA.equals(levelModel.getBandit())) return;

        GumModel gum = null;
        Obstacle body = null;
        Gummable gummable = null;
        TileModel tile;
        if (isGumObstacle(bodyA)) {
            gum = (GumModel) bodyA;
            body = bodyB;
            if (bodyB instanceof Gummable) {
                gummable = (Gummable) bodyB;
            }
        };
        if (isGumObstacle(bodyB)) {
            gum = (GumModel) bodyB;
            body = bodyA;
            if (bodyA instanceof Gummable) {
                gummable = (Gummable) bodyA;
            }
        };
        if (gum != null && gum.getName().equals("gumProjectile")) {
            // Do this once gum is turning from a projectile to sticky
            gum.setVX(0);
            gum.setVY(0);
            gum.setTexture(bubblegumController.getStuckGumTexture());
            gum.setOutline(bubblegumController.getStuckOutline());
            gum.setName("stickyGum");
            // Changing radius resets filter for some reason
            gum.getFilterData().maskBits = MASK_GUM;
            gum.getFilterData().categoryBits = CATEGORY_GUM;
        }
        //0 = horizontal, 1 = vertical, 2 = rightCorner, 3 = leftCorner
        int orientation = 0;
        if (gum != null && gum.canAddObstacle(body)){
            if (gummable != null) {
                if (!gum.onTile()) {
                    gum.markRemoved(true);
                    gummable.setGummed(true);
                    gummable.endCollision(gum);
                    for (Obstacle ob : gummable.getCollisions()) {
                        System.out.println(ob.getName());
                        bubblegumController.createGummableJoint(gummable, ob);
                    }
                }
            }
            else if (body instanceof TileModel) {
                tile = (TileModel) body;
                orientation = checkGumPosition(gum, tile);
                gum.onTile(true);
            }
            else if (body.equals(levelModel.getBandit())) {
                // Make bandit stuck
                levelModel.getBandit().setStuck(true);
            }
            WeldJointDef weldJointDef = bubblegumController.createGumJoint(gum, body, orientation);
            GumJointPair pair = new GumJointPair(gum, weldJointDef);
            bubblegumController.addToAssemblyQueue(pair);
            gum.addObstacle(body);
            gum.setCollisionFilters();
        }
    }

    /**
     * Check if gum hit a vertical side or corner of a tile.
     * @param gum
     * @param tile
     * @return int that corresponds with gum orientation.
     */
    public int checkGumPosition(GumModel gum, TileModel tile) {
        Vector2 gumPos = gum.getPosition();
        Vector2 tilePos = tile.getPosition();
        Boolean x = gumPos.x > (tilePos.x + 0.5f) || gumPos.x < (tilePos.x - 0.5f);
        Boolean y = gumPos.y < (tilePos.y + 0.5f) && gumPos.y > (tilePos.y - 0.5f);

        if (x && y) {
            gum.setTexture(bubblegumController.getRotatedGumTexture());
            gum.setOutline(bubblegumController.getRotatedOutline());
            return 1;
        }
        if (tile.hasCorner()) {
            if (gumPos.x > tilePos.x + 0.35f) {
                if (tile.topRight() && gumPos.y > tilePos.y + 0.5f) {
                    gum.setTexture(bubblegumController.getTopRightGumTexture());
                    gum.setOutline(bubblegumController.getTopRightOutline());
                    return 2;
                }
                if (tile.bottomRight() && gumPos.y < tilePos.y - 0.5f) {
                    gum.setTexture(bubblegumController.getBottomRightGumTexture());
                    gum.setOutline(bubblegumController.getBottomRightOutline());
                    return 2;
                }
            }
            if (gumPos.x < tilePos.x - 0.35f) {
                if (tile.bottomLeft() && gumPos.y < tilePos.y - 0.5f) {
                    gum.setTexture(bubblegumController.getBottomLeftGumTexture());
                    gum.setOutline(bubblegumController.getBottomLeftOutline());
                     return 3;
                }
                if (tile.topLeft() && gumPos.y > tilePos.y + 0.5f) {
                    gum.setTexture(bubblegumController.getTopLeftGumTexture());
                    gum.setOutline(bubblegumController.getTopLeftOutline());
                    return 3;
                }
            }
        }
        return 0;
    }
     /** Adds a joint that sticks gummable obstacles to the tile if the gummable has been hit with gum
     * @param ob1
     * @param ob2
     */
    public void resolveGummableGumCollision(Obstacle ob1, Obstacle ob2) {
        if (ob1 == null || ob2 == null) return;
        if (ob1.isRemoved() || ob2.isRemoved()) return;

        Gummable gummable;

        if (ob1 instanceof Gummable) {
            gummable = (Gummable) ob1;
            if (gummable.getGummed() && !ob2.equals(levelModel.getBandit())) {
                bubblegumController.createGummableJoint(gummable, ob2);
                SoundController.playSound("enemySplat", 1f);
                ob2.setStuck(true);
            }
        }
        else if (ob2 instanceof Gummable) {
            gummable = (Gummable) ob2;
            if (gummable.getGummed() && !ob1.equals(levelModel.getBandit())) {
                bubblegumController.createGummableJoint(gummable, ob1);
                SoundController.playSound("enemySplat", 1f);
                ob1.setStuck(true);
            }
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
    private void resolveProjectileCollision(ProjectileModel p, Obstacle o) {
        if (p.isRemoved()) return;
        if (o.equals(levelModel.getBandit())) {
            levelModel.getBandit().hitPlayer(p.getDamage());
        }
        p.destroy();
    }

    /**
     * Checks if there was an rolling enemy collision in the Box2D world.
     * <p>
     * Examines two Obstacles in a collision.
     * *
     * @param bd1 The first Obstacle in the collision.
     * @param bd2 The second Obstacle in the collision.
     */
    private void checkMediumEnemyCollision(Obstacle bd1, Obstacle bd2) {
        BanditModel bandit = levelModel.getBandit();

        // TODO: REFACTOR to more general knockback
        if (bd1 instanceof RollingEnemyModel && bd2.equals(bandit)) {
            if (!bd1.getGummed() && !bd1.getStuck()) {
                boolean leftMedium = (bd1.getX() < bd2.getX());
                boolean knockBackUp = levelModel.getWorld().getGravity().y < 0;
                bandit.hitPlayer(((RollingEnemyModel)bd1).getDamage());
                bandit.setKnockback(true);
                bandit.getBody().applyLinearImpulse(leftMedium ? 2f : -2f, knockBackUp ? 2f : -2f, bandit.getX(), bandit.getY(), true);
            }
        } else if (bd2 instanceof RollingEnemyModel && bd1.equals(bandit)) {
            if (!bd2.getGummed() && !bd2.getStuck()) {
                boolean leftMedium = (bd1.getX() > bd2.getX());
                boolean knockBackUp = levelModel.getWorld().getGravity().y > 0;
                bandit.hitPlayer(((RollingEnemyModel)bd2).getDamage());
                bandit.setKnockback(true);
                bandit.getBody().applyLinearImpulse(leftMedium ? 2f : -2f, knockBackUp ? 2f : -2f, bandit.getX(), bandit.getY(), true);
            }
        }
    }


    /**
     * Resolves collisions for ground contact, adding the necessary
     * sensor fixtures.
     */
    private void resolveGroundContact(Obstacle bodyA, Fixture fixA, Obstacle bodyB, Fixture fixB) {

        BanditModel bandit = levelModel.getBandit();

        Object dataA = fixA.getUserData();
        Object dataB = fixB.getUserData();

        if ((bandit.getSensorName().equals(dataB) && bandit != bodyA && !bodyA.getName().equals("cameratile")) ||
                (bandit.getSensorName().equals(dataA) && bandit != bodyB && !bodyB.getName().equals("cameratile"))) {
            bandit.setGrounded(true);
            bandit.setKnockback(false);
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
        if (bd1.getName().equals("floatingGum") && bd2 == levelModel.getBandit() && !((Collectible) bd1).getCollected()){
            collectGum(bd1);
            ((Collectible) bd1).setCollected(true);
            SoundController.playSound("collectItem", 0.75f);
        } else if (bd2.getName().equals("floatingGum") && bd1 == levelModel.getBandit() && !((Collectible) bd2).getCollected()) {
            collectGum(bd2);
            ((Collectible) bd2).setCollected(true);
            SoundController.playSound("collectItem", 0.75f);
        }
    }

    /**Check if there was a collision between the player and a star, if so have the player collect the star*/
    public void resolveStarCollision(Obstacle bd1, Obstacle bd2) {
        if (bd1.getName().equals("star") && bd2 == levelModel.getBandit() && !((Collectible) bd1).getCollected()) {
            ((Collectible) bd1).setCollected(true);
            levelModel.getBandit().collectStar();
            SoundController.playSound("collectItem", .75f);
            bd1.markRemoved(true);
        } else if (bd2.getName().equals("star") && bd1 == levelModel.getBandit() && !((Collectible) bd2).getCollected()) {
            ((Collectible) bd2).setCollected(true);
            levelModel.getBandit().collectStar();
            SoundController.playSound("collectItem", .75f);
            bd2.markRemoved(true);
        }
    }

    /**
     * Check if there was a collision between the player and the orb, if so have the player collect the orb
     */
    public void resolveOrbCollision(Obstacle bd1, Obstacle bd2) {
        if (bd1.getName().equals("orb") && bd2 == levelModel.getBandit() && !((Collectible) bd1).getCollected()) {
            ((Collectible) bd1).setCollected(true);
            levelModel.getBandit().collectOrb();
            bd1.markRemoved(true);
            SoundController.playSound("collectItem", 0.75f);
        } else if (bd2.getName().equals("orb") && bd1 == levelModel.getBandit() && !((Collectible) bd2).getCollected()) {
            ((Collectible) bd2).setCollected(true);
            levelModel.getBandit().collectOrb();
            bd2.markRemoved(true);
            SoundController.playSound("collectItem", 0.75f);
        }
    }

    private void collectGum(Obstacle bd1) {
        bd1.markRemoved(true);
        bubblegumController.addAmmo(AMMO_AMOUNT);
    }

}
