package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.AIController;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.LaserEnemyModel;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import java.util.HashSet;

/**
 * Controller to manage laser attacks.
 * */
public class LaserController {

    /**How long a LaserModel needs to charge up before locking in. */
    private float chargeTime = 1;

    /** How long a LaserModel needs to lock in before firing. */
    private float lockTime = 1;

    /**How long a LaserModel beam lasts after charging. */
    private float firingTime = 1;

    private Vector2 chargeOrigin;

    private Vector2 chargeDirection;

    /**Damage done by a laser attack to the Bandit */
    public static final float LASER_DAMAGE = 3f;




    /**All LaserModels created by the LaserController.
     * The mapping of an EnemyModel to a LaserModel
     * represents that EnemyModel's LaserModel.
     * */
    private final HashSet<LaserEnemyModel> lasers;

    /**
     * Constructs a LaserController and the ArrayList of LaserModels
     * with it.
     * */
    public LaserController() { lasers =  new HashSet<>();}


    /**
     * Returns true if a LaserEnemyModel can shoot a laser right now.
     *
     * @param laserEnemy The LaserEnemyModel to check.
     * */
    public boolean canFireLaser(LaserEnemyModel laserEnemy){
        return !laserEnemy.isFiringLaser() && !laserEnemy.isChargingLaser();
    }

    /**
     * Fires a LaserModel in the direction of a target.
     * @param controller Controller of the EnemyModel shooting this laser
     * */
    public void fireLaser(AIController controller){
        LaserEnemyModel enemy = (LaserEnemyModel) controller.getEnemy();
        if(enemy.isFiringLaser() || enemy.isChargingLaser()) return;
        enemy.setChargingLaser(false);
        enemy.setFiringLaser(false);
        enemy.setFired(false);
        enemy.setDamagedBandit(false);
        enemy.setHitBandit(false);
        enemy.resetAge();
        lasers.add(enemy);
    }

    /**
     * Updates all LaserModels controlled by the LaserController.
     *
     * @param dt Time since last frame.
     * @param world The Box2D world.
     * @param chargeTarget The position at which the Laser should aim.
     * */
    public void updateLasers(float dt, World world, Vector2 chargeTarget){
        for(final LaserEnemyModel enemy : lasers){
            enemy.ageLaser(dt);

            boolean canSeeTarget;
            if(enemy.getFaceRight()){
                canSeeTarget = chargeTarget.x >= enemy.getX();
            }
            else canSeeTarget = chargeTarget.x <= enemy.getX();



            if(enemy.getAge() >= chargeTime + firingTime + lockTime){
                enemy.setFiringLaser(false);
                enemy.setChargingLaser(false);
                enemy.resetAge();
                enemy.setHitBandit(false);
                enemy.setDamagedBandit(false);
            }

            //Firing phase.
            if(enemy.getAge() > chargeTime + lockTime){
                enemy.setChargingLaser(false);
                enemy.setFiringLaser(true);

                final Vector2 intersect = new Vector2();
                Vector2 chargeEndpoint =
                        new Vector2(chargeOrigin.x + chargeDirection.x,
                                chargeOrigin.y + chargeDirection.y);

                final Vector2 banditPos = new Vector2();

                RayCastCallback chargeRaycast = new RayCastCallback() {
                    @Override
                    public float reportRayFixture(Fixture fixture, Vector2 point,
                                                  Vector2 normal, float fraction) {
                        Obstacle ob = (Obstacle) fixture.getBody().getUserData();
                        if (!ob.getName().contains("Laser Enemy")
                                && !ob.getName().contains("bandit")) {
                            intersect.set(point);
                            return fraction;
                        }
                        if(ob.getName().equals("bandit")){
                            banditPos.set(ob.getPosition());
                        }
                        return -1;
                    }
                };
                world.rayCast(chargeRaycast, chargeOrigin, chargeEndpoint);
                enemy.setRaycastLine(intersect);
                enemy.setFired(true);
//                if (banditPos.x != 0 && banditPos.y != 0) {
//                    // Found bandit
//                    enemy.setHitBandit(true);
//                }
            }
            else if (enemy.getAge() < chargeTime) {  // Charge phase
                enemy.setFiringLaser(false);
                enemy.setChargingLaser(true);
                final Vector2 intersect = new Vector2();
                chargeOrigin = enemy.getPosition();
                chargeOrigin.x += enemy.getWidth()/2;
                chargeDirection =
                        new Vector2((chargeTarget.x- chargeOrigin.x),
                                (chargeTarget.y - chargeOrigin.y));
                chargeDirection.nor();
                chargeDirection.scl(Integer.MAX_VALUE);
                Vector2 chargeEndpoint =
                        new Vector2(chargeOrigin.x + chargeDirection.x,
                                chargeOrigin.y + chargeDirection.y);

                RayCastCallback chargeRaycast = new RayCastCallback() {
                    @Override
                    public float reportRayFixture(Fixture fixture, Vector2 point,
                                                  Vector2 normal, float fraction) {
                        Obstacle ob = (Obstacle) fixture.getBody().getUserData();
                        if (!ob.getName().contains("Laser Enemy")) {
                            //System.out.println(ob.getName());
                            intersect.set(point);
                            return fraction;
                        }
                        return -1;
                    }
                };
                world.rayCast(chargeRaycast, chargeOrigin, chargeEndpoint);
                enemy.setRaycastLine(intersect);
            }
            else if(enemy.getAge() < chargeTime + lockTime){             //Locking phase.
                enemy.setChargingLaser(true);
                enemy.setFiringLaser(false);
                final Vector2 intersect = new Vector2();
                Vector2 chargeEndpoint =
                        new Vector2(chargeOrigin.x + chargeDirection.x,
                                chargeOrigin.y + chargeDirection.y);

                RayCastCallback chargeRaycast = new RayCastCallback() {
                    @Override
                    public float reportRayFixture(Fixture fixture, Vector2 point,
                                                  Vector2 normal, float fraction) {
                        Obstacle ob = (Obstacle) fixture.getBody().getUserData();
                        if (!ob.getName().contains("Laser Enemy")) {
                            intersect.set(point);
                            return fraction;
                        }
                        return -1;
                    }
                };
                world.rayCast(chargeRaycast, chargeOrigin, chargeEndpoint);
                enemy.setRaycastLine(intersect);
            }

        }
    }
}
