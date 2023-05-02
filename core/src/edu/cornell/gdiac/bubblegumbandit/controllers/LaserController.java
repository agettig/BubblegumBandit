package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.AIController;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.LaserEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.DoorModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.TileModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.physics.obstacle.Obstacle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Controller to manage laser attacks.
 * */
public class LaserController {

    /**How long a LaserModel needs to charge up before locking in. */
    private float chargeTime = 1;

    /** How long a LaserModel needs to lock in before firing. */
    private float lockTime = .75f;

    /**How long a LaserModel beam lasts after charging. */
    private float firingTime = 1.5f;

    /**How much damage the laser does to the bandit each frame. */
    private float TICK_DAMAGE = .5f;

    /**Start point of the laser raycast. */
    private final Vector2 chargeOrigin;

    /**Direction of the laser raycast. */
    private final Vector2 chargeDirection;

    /**Endpoint of the laser raycast. */
    private final Vector2 chargeEndpoint;

    /**The point where the charging laser intersected with something. */
    private Vector2 chargeHitPoint;

    /**Distance vector between the Bandit and a LaserEnemy. */
    private Vector2 banditEnemyDist;

    /**The point where the locked laser is hitting. */
    private Vector2 lockHitPoint;

    /**Names of bodies that the laser raycast should ignore. */
    ArrayList<String> bodiesToIgnore;

    /**Enemies that this LaserController should remove from its main
     * loop cycle. */
    Set<LaserEnemyModel> enemiesToRemove;;



    /**All LaserModels created by the LaserController.
     * The mapping of an EnemyModel to a LaserModel
     * represents that EnemyModel's LaserModel.
     * */
    private final HashSet<LaserEnemyModel> lasers;

    /**
     * Constructs a LaserController and the ArrayList of LaserModels
     * with it.
     * */
    public LaserController() {

        chargeOrigin = new Vector2();
        chargeDirection = new Vector2();
        chargeEndpoint = new Vector2();
        banditEnemyDist = new Vector2();
        lasers =  new HashSet<>();
        bodiesToIgnore = new ArrayList<>();
        bodiesToIgnore.add("laserEnemy");
        bodiesToIgnore.add("exit");
        bodiesToIgnore.add("gumProjectile");
        enemiesToRemove = new HashSet<>();
    }

    /**
     * Fires a LaserModel in the direction of a target.
     * @param controller Controller of the EnemyModel shooting this laser
     * */
    public void fireLaser(AIController controller){
        LaserEnemyModel enemy = (LaserEnemyModel) controller.getEnemy();
        if(enemy.chargingLaser()) return;
        if(enemy.lockingLaser()) return;
        if(enemy.firingLaser()) return;
        if(lasers.contains(enemy)) return;
        lasers.add(enemy);
    }

    /**
     * Updates all LaserModels controlled by the LaserController.
     *
     * @param dt Time since last frame.
     * @param world The Box2D world.
     * @param bandit The Bandit.
     * */
    public void updateLasers(float dt, World world, BanditModel bandit) {


        //Removal of Enemies that began shooting but can no longer see the Bandit
        enemiesToRemove.clear();
        for (final LaserEnemyModel enemy : lasers) {

            //Disqualification #2: cannot see bandit.
            boolean disqualified = !enemy.canSeeBandit(bandit);

            //Disqualification #1: too far.
            banditEnemyDist.set(
                    bandit.getX() - enemy.getX(),
                    bandit.getY() - enemy.getY()
            );
            float distance = Math.abs(banditEnemyDist.len());
            float range = 8;
            if (distance > range) disqualified = true;

            //Disqualification #2: enemy can't see.
            if (enemy.getFaceRight()) {
                if (bandit.getX() < enemy.getX()) disqualified = true;
            }
            if (!enemy.getFaceRight()) {
                if (bandit.getX() > enemy.getX()) disqualified = true;
            }

            if (disqualified && enemy.chargingLaser()) {
                enemiesToRemove.add(enemy);
                enemy.resetLaserCycle();
            }
        }

        for (final LaserEnemyModel enemy : enemiesToRemove) {
            lasers.remove(enemy);
        }

        //Main loop
        for (final LaserEnemyModel enemy : lasers) {

            //Update the LaserEnemyModel's age and the Bandit reference.
            enemy.ageLaser(dt, chargeTime, lockTime, firingTime);

            /* ---CHARGING PHASE---
             *
             * The LaserBeam follows the bandit's position.
             *
             * */
            if (enemy.chargingLaser()) {
                chargeHitPoint = shootRaycastAt(world, enemy, bandit.getPosition(), bodiesToIgnore);
                enemy.setBeamIntersect(chargeHitPoint);
            }

            /* ---LOCKING AND FIRING PHASE---
             *
             * The LaserBeam shoots at the latest charging direction.
             *
             * */
            if (enemy.lockingLaser()) {
                //We use the most recent charging hit point to shoot our locked laser towards.
//                bodiesToIgnore.add("bandit");
                lockHitPoint = shootRaycastTowards(world, enemy, bodiesToIgnore);
                enemy.setBeamIntersect(lockHitPoint);
            }

            if(enemy.firingLaser()){
                //We use the most recent charging hit point to shoot our locked laser towards.
                bodiesToIgnore.remove("bandit");
                lockHitPoint = shootRaycastTowards(world, enemy, bodiesToIgnore);
                enemy.setBeamIntersect(lockHitPoint);

                //If we're hitting the bandit, take some damage.
                if (enemy.isHittingBandit()) bandit.hitPlayer(TICK_DAMAGE, true);
            }

        }
    }


    /**
     * Returns the point at which a laser raycast intersects with
     * a body of interest. Performs a raycast from a
     * LaserEnemyModel's position towards some target.
     *
     * @param world The Box2D world.
     * @param enemy The LaserEnemyModel ray-casting right now.
     * @param target The raycast target position.
     * @param ignores All names of bodies that the raycast should ignore.
     * */
    private Vector2 shootRaycastAt(World world,
                                   final LaserEnemyModel enemy,
                                   Vector2 target,
                                   final ArrayList<String> ignores){

        //The point at which our raycast will "hit."
        final Vector2 intersect = new Vector2();

        //Set the origin.
        float verticalOriginOffset = enemy.getHeight()/10f;
        float horizontalOriginOffset = enemy.getWidth() * .6f;
        if(enemy.isFlipped()) verticalOriginOffset = -verticalOriginOffset;
        if(!enemy.getFaceRight()) horizontalOriginOffset = -horizontalOriginOffset;
        chargeOrigin.set(enemy.getX() + horizontalOriginOffset, enemy.getY() + verticalOriginOffset);

        //The direction of the laser beam is at the passed-in target.
        chargeDirection.set(
                target.x - chargeOrigin.x,
                target.y - chargeOrigin.y
        );

        //We normalize the direction and scale it so that it reaches the screen's end.
        chargeDirection.nor();
        chargeDirection.scl(Integer.MAX_VALUE);


        //With the origin and direction, we can calculate the endpoint.
        chargeEndpoint.set(
                chargeOrigin.x + chargeDirection.x,
                chargeOrigin.y + chargeDirection.y
        );

        //Time to raycast.
        RayCastCallback chargeRaycast = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point,
                                          Vector2 normal, float fraction) {
                Obstacle ob = (Obstacle) fixture.getBody().getUserData();

                //Special case: doors. We need to check locked status.
                if(ob.getName().equals("door")){
                    DoorModel door = (DoorModel) ob;
                    if(!door.isOpen()){
                        intersect.set(point);
                        return fraction;
                    }
                }

                //Return what the laser is hitting.
                else if (!ignores.contains(ob.getName())) {
                    enemy.setHittingBandit(ob.getName().equals("bandit"));
                    intersect.set(point);
                    return fraction;
                }
                return -1;
            }
        };
        world.rayCast(chargeRaycast, chargeOrigin, chargeEndpoint);
        return intersect;
    }

    /**
     * Returns the point at which a laser raycast intersects with
     * a body of interest. Performs a raycast from a
     * LaserEnemyModel's position towards some direction.
     *
     * @param world The Box2D world.
     * @param enemy The LaserEnemyModel ray-casting right now.
     * @param ignores All names of bodies that the raycast should ignore.
     * */
    private Vector2 shootRaycastTowards(World world,
                                        final LaserEnemyModel enemy,
                                        final ArrayList<String> ignores){

        //The point at which our raycast will "hit."
        final Vector2 intersect = new Vector2();
        final Vector2 banditIntersect = new Vector2();

        //Set the charging origin.
        float verticalOriginOffset = enemy.getHeight()/10f;
        float horizontalOriginOffset = enemy.getWidth() * .6f;
        if(enemy.isFlipped()) verticalOriginOffset = -verticalOriginOffset;
        if(!enemy.getFaceRight()) horizontalOriginOffset = -horizontalOriginOffset;
        chargeOrigin.set(enemy.getX() + horizontalOriginOffset, enemy.getY() + verticalOriginOffset);

        //We normalize the direction and scale it so that it reaches the screen's end.
        chargeDirection.nor();
        chargeDirection.scl(Integer.MAX_VALUE);

        //With the origin and direction, we can calculate the endpoint.
        chargeEndpoint.set(
                chargeOrigin.x + chargeDirection.x,
                chargeOrigin.y + chargeDirection.y
        );


        //Time to raycast.
        RayCastCallback chargeRaycast = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point,
                                          Vector2 normal, float fraction) {
                Obstacle ob = (Obstacle) fixture.getBody().getUserData();

                //Special case: doors. We need to check locked status.
                if(ob.getName().equals("door")){
                    System.out.println("Here!");
                    DoorModel door = (DoorModel) ob;
                    if(!door.isOpen()){
                        intersect.set(point);
                        return fraction;
                    }
                }

                //Return what the laser is hitting.
                else if (!ignores.contains(ob.getName())) {
                    enemy.setHittingBandit(ob.getName().equals("bandit"));
                    intersect.set(point);
                    return fraction;
                }
                return -1;
            }
        };
        world.rayCast(chargeRaycast, chargeOrigin, chargeEndpoint);




        /*

        Step 1. Shoot a raycast SUPER far in the direction of the intersect point.

        Step 2. Based on laser percent done, draw a certain amount of that line.

        Step 3. Clamp this line based off whether we're hitting the actual intersect.

        ----




         */

        Vector2 directionOfIntersect = new Vector2(intersect).sub(chargeOrigin);
        directionOfIntersect.nor();
        Vector2 farAwayPoint = new Vector2(directionOfIntersect).scl(40).add(chargeOrigin);
        Vector2 lerpedPoint = new Vector2(chargeOrigin).lerp(farAwayPoint, enemy.getFiringDistance(firingTime));

        float lerpedDist = chargeOrigin.dst(lerpedPoint);
        float endDist = chargeOrigin.dst(intersect);
        Vector2 point = lerpedDist < endDist ? lerpedPoint : intersect;

        return enemy.firingLaser() ? point : intersect;
    }
}