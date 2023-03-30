package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.models.LaserModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.LaserEnemyModel;

import java.util.HashMap;

/**
 * Controller to manage laser attacks.
 * */
public class LaserController {

    /**Reference to JSON with laser data.*/
    private JsonValue laserJSON;

    /**Current level's drawing scale. */
    private Vector2 drawScale;

    /**How long a LaserModel needs to charge up before firing. */
    private float chargeTime;

    /**How long a LaserModel beam lasts after charging. */
    private float attackTime;

    /**Damage done by a laser attack to the Bandit */
    public static final float LASER_DAMAGE = 1f;

    /**Texture for a LaserModel when attacking */
    private Texture attackTexture;

    /**Texture for a LaserModel when charging */
    private Texture chargeTexture;




    /**All LaserModels created by the LaserController.
     * The mapping of an EnemyModel to a LaserModel
     * represents that EnemyModel's LaserModel.
     * */
    private final HashMap<EnemyModel, LaserModel> lasers;

    /**
     * Constructs a LaserController and the ArrayList of LaserModels
     * with it.
     * */
    public LaserController() { lasers =  new HashMap<EnemyModel, LaserModel>();}

    /**
     * Initializes a LaserController based off JSON data.
     * @param laserJSON JSON data containing information about lasers.
     * @param directory BubblegumBandit's AssetDirectory reference.
     * @param xScale The X-Value of the current level's draw scale.
     * @param yScale The Y-Value of the current level's draw scale.
     * */
    public void initialize(JsonValue laserJSON, AssetDirectory directory,
                           float xScale, float yScale){

        this.laserJSON = laserJSON;
        drawScale = new Vector2(xScale, yScale);
        chargeTime = laserJSON.getFloat("charge_time");
        attackTime = laserJSON.getFloat("attack_time");

        attackTexture = directory.getEntry(
                laserJSON.get("attack_texture").asString(),
                Texture.class
        );

        chargeTexture = directory.getEntry(
                laserJSON.get("charge_texture").asString(),
                Texture.class
        );
    }

    /**
     * Returns true if a LaserEnemyModel can shoot a laser right now.
     *
     * @param laserEnemy The LaserEnemyModel to check.
     * */
    public boolean canFireLaser(LaserEnemyModel laserEnemy){
        return laserEnemy.canFireLaser();
    }

    /**
     * Fires a LaserModel in the direction of a target.
     * @param controller Controller of the EnemyModel shooting this laser
     * @param targetX the X-position of the target.
     * */
    public LaserModel fireLaser(AIController controller, float targetX){

        //Calculate laser texture width and height
        EnemyModel enemy = controller.getEnemy();
        float enemyX = enemy.getX();
        float enemyY = enemy.getY();

        //Create the laser (which itself is the charging sensor)

        float sign = Math.signum(targetX - enemyX);
        LaserModel laser = new LaserModel(laserJSON, sign,
                enemyX + sign * drawScale.x/4, enemyY,
                        drawScale.x/2, enemy.getHeight());
        laser.setDrawScale(drawScale);

        //Attach the laser to the enemy, start the cool down, and return.
        ((LaserEnemyModel)enemy).resetCooldown();
        ((LaserEnemyModel)enemy).setFiringLaser(true);
        lasers.put(enemy, laser);
        return laser;
    }

    /**
     * Updates all LaserModels controlled by the LaserController.
     *
     * @param dt Time since last frame.
     * */
    public void updateLasers(float dt){
        for(EnemyModel enemy : lasers.keySet()){
            LaserModel laser = lasers.get(enemy);
            float laserSpeed = 2f;
            float laserEpsilon = .01f;
            float sign = laser.getSign();
            laser.ageLaser(dt);
            laser.setY(enemy.getY());

            if(laser.getAge() < chargeTime){
                TextureRegion chargeTextureRegion = new TextureRegion(chargeTexture);
                chargeTextureRegion.setRegionWidth((int) (laser.getWidth() * drawScale.x));
                chargeTextureRegion.setRegionHeight((int) (laser.getHeight()/2 * drawScale.y));
                laser.setTexture(chargeTextureRegion);
            }

            //Check if the charge-up beam should turn into the real thing.
            else if(laser.getAge() >= chargeTime && !laser.isAttacking()){
                laser.setAttacking();
                laser.setX(enemy.getX());
                laser.setWidth(laserEpsilon);

                TextureRegion attackTextureRegion = new TextureRegion(attackTexture);
                attackTextureRegion.setRegionWidth((int) (laser.getWidth() * drawScale.x));
                attackTextureRegion.setRegionHeight((int) (laser.getHeight() * drawScale.y));
                laser.setTexture(attackTextureRegion);
            }
            //Check if the laser is currently attacking
            else if (laser.isAttacking()){
                laser.setWidth(laser.getWidth() + laserSpeed);
                laser.setX(laser.getX() + laserSpeed/2 * sign);

                TextureRegion attackTextureRegion = new TextureRegion(attackTexture);
                attackTextureRegion.setRegionWidth((int) (laser.getWidth() * drawScale.x));
                attackTextureRegion.setRegionHeight((int) (laser.getHeight() * drawScale.y));
                laser.setTexture(attackTextureRegion);
            }

            //Check if the laser is expired.
            if(laser.getAge() >= chargeTime + attackTime) eraseLaser(enemy);
        }
    }

    /**
     * Removes a LaserModel from the level.
     *
     * @param owner The EnemyModel that shot the LaserModel.
     * */
    private void eraseLaser(EnemyModel owner){
        if(owner == null) return;
        if(!lasers.containsKey(owner)) return;
        LaserModel laserToRemove = lasers.get(owner);
        ((LaserEnemyModel)owner).setFiringLaser(false);
        laserToRemove.markRemoved(true);
    }


}
