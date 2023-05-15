package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.bubblegumbandit.helpers.Damage;
import edu.cornell.gdiac.bubblegumbandit.helpers.GumJointPair;
import edu.cornell.gdiac.bubblegumbandit.helpers.Gummable;
import edu.cornell.gdiac.bubblegumbandit.helpers.Shield;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.LaserEnemyModel;
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
    public static final short CATEGORY_EXIT = 0x0040;
    public static final short CATEGORY_COLLECTIBLE = 0x0080;
    public static final short CATEGORY_DOOR = 0x0100;

    public static final short MASK_PLAYER = -1;
    public static final short MASK_ENEMY = ~(CATEGORY_ENEMY);
    public static final short CATEGORY_BACK = 0x0012;

    public static final short MASK_TERRAIN = -1; // Collides with everything
    public static final short MASK_GUM = ~(CATEGORY_GUM);
    public static final short MASK_GUM_LIMIT = ~(CATEGORY_PLAYER | CATEGORY_GUM | CATEGORY_ENEMY);
    public static final short MASK_PROJECTILE = ~(CATEGORY_PROJECTILE | CATEGORY_ENEMY | CATEGORY_GUM);

    public static final short MASK_BACK = ~(CATEGORY_GUM | CATEGORY_ENEMY | CATEGORY_PLAYER);
    public static final short MASK_EXIT = CATEGORY_PLAYER;
    public static final short MASK_COLLECTIBLE = CATEGORY_PLAYER;
    public static final short MASK_doorSensor = CATEGORY_PLAYER | CATEGORY_ENEMY;
    public static final short MASK_DOOR = CATEGORY_PLAYER | CATEGORY_ENEMY | CATEGORY_GUM | CATEGORY_TERRAIN | CATEGORY_PROJECTILE;

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

    private boolean shouldFlipGravity;

    /**Temp queue for now for sticking robot joints */
    private Queue<WeldJointDef> stickRobots = new Queue<>();

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
        shouldFlipGravity = false;
    }

    /** Initializes this CollisionController
     *
     * @param camera the game camera for the scene
     */
    public void initialize(GameCamera camera) {
        this.camera = camera;
    }

    /** Gets whether gravity should be flipped due to a collision.
     * If gravity should be flipped, gravity is then set to not need to be flipped. */
    public boolean shouldFlipGravity() {
        if (shouldFlipGravity) {
            shouldFlipGravity = false;
            return true;
        }
        return false;
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

            if (obstacleA instanceof Gummable && !(obstacleB instanceof DoorModel)) {
                obstacleA.startCollision(obstacleB, fixA);
            }
            if (obstacleB instanceof Gummable && !(obstacleA instanceof DoorModel)) {
                obstacleB.startCollision(obstacleA, fixB);
            }

            if (obstacleA instanceof ShockModel && (obstacleB instanceof WallModel)) {
                if (fixA == (obstacleA.getBody().getFixtureList().get(0))) {
                    ((ShockModel) obstacleA).startCollision((WallModel) obstacleB);
                }
            }
            else if (obstacleB instanceof ShockModel && (obstacleA instanceof WallModel)) {
                if (fixB == (obstacleB.getBody().getFixtureList().get(0))) {
                    ((ShockModel) obstacleB).startCollision((WallModel) obstacleA);
                }
            }

            // check to see if laser enemy has landed on floor
            if (obstacleB instanceof WallModel && obstacleA instanceof LaserEnemyModel){
                resolveLaserEnemyTileCollision((LaserEnemyModel) obstacleA);
            }

            if (obstacleA instanceof WallModel && obstacleB instanceof LaserEnemyModel){
                resolveLaserEnemyTileCollision((LaserEnemyModel) obstacleB);
            }

            resolveGroundContact(obstacleA, fixA, obstacleB, fixB);
            resolveGumCollision(obstacleA, obstacleB, contact);
            resolveWinCondition(obstacleA, obstacleB);
            checkShockCollision(obstacleA, fixA, obstacleB, fixB);
            resolveFloatingGumCollision(obstacleA, obstacleB);
            resolveGummableGumCollision(obstacleA, obstacleB, fixA, fixB);
            resolveCaptiveCollision(obstacleA, obstacleB);
            resolveOrbCollision(obstacleA, obstacleB);
            resolveCrusherCollision(obstacleA, fixA, obstacleB, fixB);
            resolveDoorSensorCollision(obstacleA, fixA, obstacleB, fixB, true);
            checkMediumEnemyCollision(obstacleA, obstacleB);
            resolveHazardCollision(obstacleA, fixA, obstacleB, fixB);

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

        BanditModel bandit = levelModel.getBandit();
        if (((bandit.getSensorName2().equals(fd2) || bandit.getSensorName().equals(fd2)) && bandit != bd1) ||
                ((bandit.getSensorName2().equals(fd1) || bandit.getSensorName().equals(fd1)) && bandit != bd2)) {
            sensorFixtures.remove(bandit == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                bandit.setGrounded(false);
            }
        }

        try{
            Obstacle ob1 = (Obstacle) body1.getUserData();
            Obstacle ob2 = (Obstacle) body2.getUserData();

            if (ob1 instanceof Gummable) {
                ob1.endCollision(ob2, fix1);
            }
            if (ob2 instanceof Gummable) {
                ob2.endCollision(ob1, fix2);
            }

            if (ob1 instanceof ShockModel && (ob2 instanceof WallModel)) {
                if (fix1.equals(ob1.getBody().getFixtureList().get(0))) {
                    ((ShockModel) ob1).endCollision((WallModel) ob2);
                }
            }
            else if (ob2 instanceof ShockModel && (ob1 instanceof WallModel)) {
                if (fix2.equals(ob2.getBody().getFixtureList().get(0))) {
                    ((ShockModel) ob2).endCollision((WallModel) ob1);
                }
            }

            resolveDoorSensorCollision(ob1, fix1, ob2, fix2, false);

            if (ob1.getName().equals("door") && bandit == bd2) {
                updateCamera(ob1);
            } else if (ob2.getName().equals("door") && bandit == bd1) {
                updateCamera(ob2);
            }

            if (ob1 instanceof ShockModel && bandit == bd2) {
                bandit.removeShockFixture(fix1);
            } else if (ob2 instanceof ShockModel && bandit == bd1) {
                bandit.removeShockFixture(fix2);
            } else if (ob1.getName().equals("hazard") && bandit == bd2) {
                bandit.removeShockFixture(fix1);
            }  else if (ob2.getName().equals("hazard") && bandit == bd1) {
                bandit.removeShockFixture(fix2);
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

    /**
     * Resolves a collision between the laser enemy and tile
     * Shakes camera and damages bandit
     *
     * @param enemy Laser enemy that landed on the ground
     */
    private void resolveLaserEnemyTileCollision(LaserEnemyModel enemy){
        if (enemy.isJumping()){
            enemy.hasLanded();
            int trauma = enemy.isFlipped() ? -2 : 2;
            camera.addTrauma(enemy.getX() * enemy.getDrawScale().x, enemy.getY() * enemy.getDrawScale().y, trauma);
        }
    }

    /** Updates the camera based on the collision between the player and the door.
     *
     * @param ob the door */
    private void updateCamera(Obstacle ob) {
        DoorModel door = (DoorModel) ob;
        door.playerPassed = true;
        BanditModel avatar = levelModel.getBandit();

        Vector2 ul;
        Vector2 lr;
        boolean isFirst;
        if (!door.isHorizontal()) {
            isFirst = avatar.getX() - door.getX() < 0;
        } else {
            isFirst = avatar.getY() - door.getY() < 0;
        }
        if (isFirst) {
            ul = door.getFirstUpperLeft();
            lr = door.getFirstLowerRight();
        } else {
            ul = door.getSecondUpperLeft();
            lr = door.getSecondLowerRight();
        }
        Vector2 scale = levelModel.getScale();
        float zoomWidth = 0;
        float zoomHeight = 0;
        boolean fixCamera = false;
        boolean fixX = false;
        boolean fixY = false;

        if ((isFirst && door.isFirstFixedX()) || (!isFirst && door.isSecondFixedX())) {
            float centerX = (ul.x + lr.x) * scale.x / 2f;
            zoomWidth = Math.abs(ul.x - lr.x) * scale.x;
            camera.setFixedX(true);
            camera.setTargetX(centerX);
            fixCamera = true;
            fixX = true;
        }
        if ((isFirst && door.isFirstFixedY()) || (!isFirst && door.isSecondFixedY())) {
            float centerY = (ul.y + lr.y) * scale.y / 2f;
            zoomHeight = Math.abs(ul.y - lr.y) * scale.y;
            camera.setFixedY(true);
            camera.setTargetY(centerY);
            fixCamera = true;
            fixY = true;
        }
        if (fixCamera) {
            camera.setZoom(zoomWidth, zoomHeight);
            if (!fixX) {
                camera.setFixedX(false);
            }
            if (!fixY) {
                camera.setFixedY(false);
            }
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

        if (bandit.isOrbCollected() && (winConditionA ||winConditionB) && bandit.isGrounded() && !bandit.isFlipped()){ winConditionMet = true;}
    }

    /**
     *  Adds a GumJointPair to the BubblegumController's assembly
     *  queue if two colliding bodies are a valid gum/body collision.
     *
     *  @param bodyA The first body in the collision
     *  @param bodyB The second body in the collision
     * @param contact The contact
     */
    public void resolveGumCollision(Obstacle bodyA, Obstacle bodyB, Contact contact){
        //Safety check.
        if (bodyA == null || bodyB == null) return;
        // Gum should destroy projectiles, but not become sticky gum.
        if (bodyA.getName().equals("projectile") || bodyB.getName().equals("projectile")) return;
        if (bodyA.getName().equals("hazard") || bodyB.getName().equals("hazard")) return;
        if (bodyA.isRemoved() || bodyB.isRemoved()) return;
        if (bodyA.getName().equals("gumProjectile") && bodyB.equals(levelModel.getBandit())) return;
        if (bodyB.getName().equals("gumProjectile") && bodyA.equals(levelModel.getBandit())) return;

        GumModel gum = null;
        Obstacle body = null;
        Gummable gummable = null;
        TileModel tile = null;
        if (isGumObstacle(bodyA)) {
            gum = (GumModel) bodyA;
            body = bodyB;
            if (bodyB instanceof Shield) {
                if (((Shield) bodyB).isShielded()) {gum.markRemoved(true); return;}
            }
            if (bodyB instanceof Gummable) {
                gummable = (Gummable) bodyB;
            }
            if (bodyB instanceof DoorModel) {
                DoorModel door = (DoorModel) bodyB;
                if (door.isOpen()) {
                    return;
                }
            }
            if(bodyB instanceof EnemyModel){
                EnemyModel enemy = (EnemyModel) bodyB;
                enemy.stickWithGum(gum);
            }
        }

        if (isGumObstacle(bodyB)) {
            gum = (GumModel) bodyB;
            body = bodyA;
            if (bodyA instanceof Shield) {
                if (((Shield) bodyA).isShielded()) {gum.markRemoved(true); return;}
            }
            if (bodyA instanceof Gummable) {
                gummable = (Gummable) bodyA;
            }
            if (bodyA instanceof DoorModel) {
                DoorModel door = (DoorModel) bodyA;
                if (door.isOpen()) {
                    return;
                }
            }
            if(bodyA instanceof EnemyModel){
                EnemyModel enemy = (EnemyModel) bodyA;
                enemy.stickWithGum(gum);
            }
        }

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
        boolean isTile = false;
        if (gum != null && gum.canAddObstacle(body)){
            if (gummable != null) {
                if (gummable instanceof LaserEnemyModel) {
                    if (!((LaserEnemyModel) gummable).shouldStick()) {
                        gum.markRemoved(true);
                        ((LaserEnemyModel) gummable).addGumHit();
                        return;
                    }
                }
                if (!gum.getOnTile()) {
                    gum.markRemoved(true);
                    gummable.setGummed(true);
                    gummable.endCollision(gum, null);
                    if (!(gummable instanceof DoorModel)) {
                        if (gummable.getCollisions().size() > 0){
                            gummable.setStuck(true);
                        }
                        for (Obstacle ob : gummable.getCollisions()) {
                            bubblegumController.createGummableJoint(gummable, ob);
                        }
                    }
                }
                else {
                    gummable.setStuck(true);
                }
            }
            else if (body instanceof WallModel) {
                // Get the corresponding tile
                float contactX = contact.getWorldManifold().getPoints()[0].x;
                float contactY = contact.getWorldManifold().getPoints()[0].y;
                tile = levelModel.getTile(gum.getX(), gum.getY(), contactX, contactY);
                isTile = true;
                orientation = checkGumPosition(gum, tile);
                gum.setOnTile(true);
               // levelModel.makeGumSplat(gum.getX(), gum.getY());
            }
            else if (body.equals(levelModel.getBandit())) {
                // Make bandit stuck
                levelModel.getBandit().setStuck(true);
            }
            if (isTile) { // Need to make the joint at the tile position, not the wall
                WeldJointDef weldJointDef = bubblegumController.createGumJoint(gum, body, orientation, tile);
                GumJointPair pair = new GumJointPair(gum, weldJointDef);
                bubblegumController.addToAssemblyQueue(pair);
                gum.addObstacle(body);
            } else {
                // creates joint between gum and object
                WeldJointDef weldJointDef = bubblegumController.createGumJoint(gum, body, orientation);
                GumJointPair pair = new GumJointPair(gum, weldJointDef);
                bubblegumController.addToAssemblyQueue(pair);
                gum.addObstacle(body);
            }
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
    public void resolveGummableGumCollision(Obstacle ob1, Obstacle ob2, Fixture fix1, Fixture fix2) {
        if (ob1 == null || ob2 == null) return;
        if (ob1.isRemoved() || ob2.isRemoved()) return;
        if (ob1 instanceof DoorModel || ob2 instanceof DoorModel) return;
        if (fix1.getUserData() instanceof DoorModel || fix2.getUserData() instanceof DoorModel) {
            return;
        }

        Gummable gummable;

        if (ob1 instanceof Gummable) {
            gummable = (Gummable) ob1;
            if (gummable.getGummed()) { // && !ob2.equals(levelModel.getBandit())
                if (ob1.getName().contains("nemy") && ob2.equals(levelModel.getBandit())) {
                    return;
                }
                bubblegumController.createGummableJoint(gummable, ob2);
                SoundController.playSound("enemySplat", 1f);
                ob1.setStuck(true);
            }
        }
        else if (ob2 instanceof Gummable) {
            gummable = (Gummable) ob2;
            if (gummable.getGummed()) { // && !ob1.equals(levelModel.getBandit())
                if (ob2.getName().contains("nemy") && ob1.equals(levelModel.getBandit())) {
                    return;
                }
                bubblegumController.createGummableJoint(gummable, ob1);
                SoundController.playSound("enemySplat", 1f);
                ob2.setStuck(true);
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
    private void checkShockCollision(Obstacle bd1, Fixture fix1, Obstacle bd2, Fixture fix2) {

        // Check that obstacles are not null and not an enemy
        if (bd1 == null || bd2 == null) return;
        if (bd1.getName().contains("enemy") || bd2.getName().contains("enemy")) return;

        if (bd1 instanceof ShockModel) {
            ShockModel shock = (ShockModel) bd1;
            if (shock.isValidHit(bd2)) {
                resolveShockCollision(shock, fix1, bd2);
            }
        } else if (bd2 instanceof ShockModel) {
            ShockModel shock = (ShockModel) bd2;
            if ( shock.isValidHit(bd1))  {
                resolveShockCollision(shock, fix2, bd1);
            }
        }



    }

    /**
     * Checks if there was a collision between a crusher and an enemy or player.
     * *
     * @param bd1 The first Obstacle in the collision.
     * @param bd2 The second Obstacle in the collision.
     */
    private void resolveCrusherCollision(Obstacle bd1, Fixture fix1, Obstacle bd2, Fixture fix2) {

        // Check that obstacles are not null and one is a crusher sensor
        if (bd1 == null || bd2 == null) return;
        CrusherModel crusher;
        Obstacle crushed;

        float levelGrav = levelModel.getWorld().getGravity().y;
//        String sensorName = levelModel.getWorld().getGravity().y < 0 ? "crushing_bottom_sensor" : "crushing_top_sensor";
        if (fix1.getUserData() instanceof CrusherModel) {
            crusher = (CrusherModel) fix1.getUserData();
            crushed = bd2;
        } else if (fix2.getUserData() instanceof CrusherModel) {
            crusher = (CrusherModel) fix2.getUserData();
            crushed = bd1;
        } else {
            return;
        }

        BanditModel bandit = levelModel.getBandit();
        if (crushed.getName().contains("enemy")) {
            // Check if enemy is beneath crusher and stopped (if it's stopped, it's pinched).
            // If so, trigger its deletion.
            // This might cause bugs. If there is some unexpected crusher behavior, this is probably
            // what needs to be changed.
            if (Math.abs(crushed.getVY()) < 0.001f) {
                crushed.markRemoved(true);
            }
        } else if (crushed.equals(bandit)) {
            if (Math.abs(crushed.getVY()) < 0.001f) {
                // Block must be pushing on bandit.
                if ((bandit.getPosition().y > crusher.getPosition().y) == (levelGrav > 0)) {
                    // Flip gravity again and make the bandit take damage.
                    levelModel.getBandit().hitPlayer(Damage.CRUSH_DAMAGE, false);
                    shouldFlipGravity = true;
                }
            }
        } else if (crushed.getBodyType().equals(BodyType.StaticBody)) {
                // Screen shake cause block hit the floor
            if (crushed.getName().equals("glass") && !crusher.didSmash) {
                crushed.markRemoved(true);
                levelModel.makeShatter(crushed.getX(), crushed.getY());
                camera.addTrauma(crushed.getX() * crushed.getDrawScale().x, crushed.getY() * crushed.getDrawScale().y, CrusherModel.traumaAmt);
            }
            else if (!crusher.didSmash) {
                camera.addTrauma(crushed.getX() * crushed.getDrawScale().x, crushed.getY() * crushed.getDrawScale().y, CrusherModel.traumaAmt * (crusher.maxAbsFallVel / 20));
                float hw = crusher.getWidth() / 2;
                if (crushed.getX() < crusher.getX() + hw & crushed.getX() > crusher.getX() - hw) {
                    crusher.maxAbsFallVel = 0;
                    crusher.didSmash = true;
                }
            }
        }
    }

    /**
     * Checks if there was a collision between a hazard and the player.
     * *
     * @param bd1 The first Obstacle in the collision.
     * @param bd2 The second Obstacle in the collision.
     */
    private void resolveHazardCollision(Obstacle bd1, Fixture fix1, Obstacle bd2, Fixture fix2) {
        if (bd1 == null || bd2 == null) return;
        SpecialTileModel hazard;
        BanditModel bandit = levelModel.getBandit();

        if (bd1.getName().equals("hazard") && bd2.equals(bandit)) {
            hazard = (SpecialTileModel) bd1;
            bandit.addShockFixture(fix1);
        } else if (bd2.getName().equals("hazard") && bd1.equals(bandit)) {
            hazard = (SpecialTileModel) bd2;
            bandit.addShockFixture(fix2);
        } else {
            return;
        }
        levelModel.makeSpark(hazard.getX(), hazard.getY());
        // Bandit on top or below hazard
        if (Math.abs(bandit.getVY()) > 1) {
            boolean wasHit = applyKnockback(hazard, bandit, true, Damage.HAZARD_DAMAGE, 0, 5f, true);
            if (wasHit) {
                shouldFlipGravity = true;
                bandit.setVY(0);
            }
        } else { // Bandit colliding on side of hazard
            applyKnockback(hazard, bandit, true, Damage.HAZARD_DAMAGE, 15f, 5f, true);
        }
    }

    /**
     * Checks if there was a collision between a door sensor and an enemy or player.
     * *
     * @param bd1 The first Obstacle in the collision.
     * @param fix1 the particular fixture of the first collision obstacle
     * @param bd2 The second Obstacle in the collision.
     * @param fix2 the particular fixture of the second collision obstacle
     * @parma isBeginContact whether the collision is a begincontact event
     */
    private void resolveDoorSensorCollision(Obstacle bd1, Fixture fix1, Obstacle bd2, Fixture fix2, boolean isBeginContact) {

        // Check that obstacles are not null and one is a door sensor
        if (bd1 == null || bd2 == null) return;
        DoorModel door;
        Obstacle ob;

        if (fix1.getUserData() instanceof DoorModel) {
            door = (DoorModel) fix1.getUserData();
            ob = bd2;
        } else if (fix2.getUserData() instanceof DoorModel) {
            door = (DoorModel) fix2.getUserData();
            ob = bd1;
        } else {
            return;
        }
        if (ob.equals(levelModel.getBandit()) || ob.getName().contains("enemy")) {
            if (isBeginContact) {
                door.addObInRange(ob);
            } else {
                door.removeObInRange(ob);
            }
        }
        if (ob.equals(levelModel.getBandit())) {
            if (isBeginContact) {
                door.playerInRange = true;
            } else {
                door.playerInRange = false;
            }
        }
    }

    /**
     * Resolves the effects of a projectile collision
     * @param p the shock model
     * @param o the obstacle that the shock collided with
     * @param shockFixture the actual fixture of the shock obstacle in the collision
     */
    private void resolveShockCollision(ShockModel p, Fixture shockFixture, Obstacle o) {
        if (p.isRemoved()) return;
        if (o.equals(levelModel.getBandit())) {
            applyKnockback(p, (BanditModel) o, false, Damage.SHOCK_DAMAGE, 1f, 1f, true);
            levelModel.makeSpark(o.getX(), o.getY());
            levelModel.getBandit().addShockFixture(shockFixture);
        } else if (o instanceof WallModel) {
            boolean isBottom = p.getIsBottom();
            if ((isBottom && o.getY() > p.getY()) || (!isBottom && o.getY() < p.getY())) {
                p.stopShock();
            }
        } else if (o instanceof DoorModel) {
            DoorModel door = (DoorModel) o;
            if (!door.isOpen()) { // Closed doors stop shocks
                p.stopShock();
            }
        }
    }

    /** Applies knockback to the player if the player is not currently invulnerable
     *
     * Returns whether the knockback was applied */
    private boolean applyKnockback(Obstacle other, BanditModel bandit,
                                boolean yImpact, float damage, float impactX, float impactY, boolean shock) {
        boolean wasHit = bandit.hitPlayer(damage, false);
        if (wasHit) {
            boolean left = (other.getX() < bandit.getX());
            boolean knockbackUp = levelModel.getWorld().getGravity().y < 0;
            bandit.setKnockback(true, shock);
            if(shock) camera.startShockTrauma(); //move float out
            if(yImpact)  {
                bandit.getBody().applyLinearImpulse(left ? impactX : -impactX,
                        knockbackUp ? impactY : -impactY, bandit.getX(), bandit.getY(), true);
            }
            else {
                bandit.getBody().applyLinearImpulse(left ? impactX : -impactX,
                        0, bandit.getX(), bandit.getY(), true);
            }
        }
        return wasHit;
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
                bandit.hitPlayer(Damage.ROLLING_HIT_DAMAGE, false);
                bandit.setKnockback(true, false);
                bandit.getBody().applyLinearImpulse(leftMedium ? 2f : -2f, knockBackUp ? 2f : -2f, bandit.getX(), bandit.getY(), true);
                SoundController.playSound("knockback", 0.25f);
            }
        } else if (bd2 instanceof RollingEnemyModel && bd1.equals(bandit)) {
            if (!bd2.getGummed() && !bd2.getStuck()) {
                boolean leftMedium = (bd1.getX() > bd2.getX());
                boolean knockBackUp = levelModel.getWorld().getGravity().y > 0;
                bandit.hitPlayer(Damage.ROLLING_HIT_DAMAGE, false);
                bandit.setKnockback(true);
                bandit.getBody().applyLinearImpulse(leftMedium ? 2f : -2f, knockBackUp ? 2f : -2f, bandit.getX(), bandit.getY(), true);
                SoundController.playSound("knockback", 0.25f);
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

        if ((bandit.getSensorName().equals(dataB) && bandit != bodyA && !bodyA.getName().equals("door")) ||
                (bandit.getSensorName().equals(dataA) && bandit != bodyB && !bodyB.getName().equals("door"))) {
            DoorModel door = null;
            if (bodyA.getName().equals("doorH")) {
                door = (DoorModel) bodyA;
            } else if (bodyB.getName().equals("doorH")) {
                door = (DoorModel) bodyB;
            }
            if (door != null && door.isOpen()) {
                return;
            }
            bandit.setGrounded(true);
            bandit.setKnockback(false);
            SoundController.playSound("banditLanding", 1);
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
            SoundController.playSound("collectItem", 0.25f);
        } else if (bd2.getName().equals("floatingGum") && bd1 == levelModel.getBandit() && !((Collectible) bd2).getCollected()) {
            collectGum(bd2);
            ((Collectible) bd2).setCollected(true);
            SoundController.playSound("collectItem", 0.75f);
        }
    }

    /**Check if there was a collision between the player and a captive's cell, if so have the player free the NPC */
    public void resolveCaptiveCollision(Obstacle bd1, Obstacle bd2) {
        if (bd1.getName().equals("star") && bd2 == levelModel.getBandit() && !((Captive) bd1).getCollected()) {
            ((Captive) bd1).setCollected(true);
            levelModel.getBandit().collectStar();
            SoundController.playSound("collectItem", .75f);

        } else if (bd2.getName().equals("star") && bd1 == levelModel.getBandit() && !((Captive) bd2).getCollected()) {
            ((Captive) bd2).setCollected(true);
            levelModel.getBandit().collectStar();
            SoundController.playSound("collectItem", .75f);

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
//        bd1.markRemoved(true);
//        .addAmmo(AMMO_AMOUNT);
    }

}
