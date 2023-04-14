package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.AIController;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.LaserEnemyModel;
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
    private float TICK_DAMAGE = .05f;

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
        bodiesToIgnore.add("Laser Enemy");
        bodiesToIgnore.add("cameratile");
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

            boolean disqualified = false;

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
                chargeHitPoint = shootRaycast(world, enemy, bandit.getPosition(), bodiesToIgnore);
                enemy.setBeamIntersect(chargeHitPoint);
            }

            /* ---LOCKING AND FIRING PHASE---
             *
             * The LaserBeam shoots at the latest charging direction.
             *
             * */
            if (enemy.lockingLaser() || enemy.firingLaser()) {
                //We use the most recent charging hit point to shoot our locked laser towards.
                lockHitPoint = shootRaycast(world, enemy, chargeHitPoint, bodiesToIgnore);
                enemy.setBeamIntersect(lockHitPoint);

                //If we're firing and hitting the bandit, take some damage.
                if (enemy.firingLaser() && enemy.isHittingBandit()) {
                    bandit.hitPlayer(TICK_DAMAGE);
                }
            }
        }
    }


    /**
     * Returns the point at which a laser raycast intersects with
     * a body of interest. Performs the main raycast.
     *
     * @param world The Box2D world.
     * @param enemy The LaserEnemyModel ray-casting right now.
     * @param target The raycast target position.
     * @param ignores All names of bodies that the raycast should ignore.
     * */
    private Vector2 shootRaycast(World world,
                                 final LaserEnemyModel enemy,
                                 Vector2 target,
                                 final ArrayList<String> ignores){

        //The point at which our raycast will "hit."
        final Vector2 intersect = new Vector2();

        //The origin of the laser beam is at the LaserEnemy's face.
        float originAdjustX = enemy.getFaceRight() ? enemy.getWidth() + .5f :
                -enemy.getWidth() -.5f;
        float originAdjustY = enemy.isFlipped() ? -.5f : .5f;
        chargeOrigin.set(enemy.getX(), enemy.getY());

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

                //Return what the laser is hitting.
                if (!ignores.contains(ob.getName())) {
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
}
