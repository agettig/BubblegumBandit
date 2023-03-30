package edu.cornell.gdiac.bubblegumbandit.models;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.obstacle.BoxObstacle;

/**
 * Represents a Laser beam.
 * */
public class LaserModel extends BoxObstacle {

    /**How many seconds this LaserModel has been in the level. */
    private float age;

    /**true if this LaserModel is done charging and is now attacking. */
    private boolean attacking;

    private boolean hitTarget;



    /**
     * Constructs a LaserModel at a given position with a given
     * width and height.
     *
     * @param laserJSON JSON data with information about LaserModels.
     * @param x The X-coordinate at which this LaserModel will spawn.
     * @param y The Y-coordinate at which this LaserModel will spawn.
     * @param length How far a LaserModel extends from an EnemyModel.
     * @param girth Distance from the top to bottom of a LaserModel.
     * */
    public LaserModel(JsonValue laserJSON,
                      float x, float y, float length, float girth) {
        super(x, y, length, girth);
        activatePhysics(laserJSON);
    }

    /**
     * Sets the physical properties of this LaserModel
     *
     * @param laserJSON JSON data with information about LaserModels.
     * */
    private void activatePhysics(JsonValue laserJSON){
        setName("Charge Laser");
        setDensity(laserJSON.getFloat("density", 0));
        setBodyType(BodyDef.BodyType.KinematicBody);
        setSensor(true);
    }


    /**
     * Adds to the age of this LaserMode.
     *
     * @param amount How much time to add.
     * */
    public void ageLaser(float amount){
        if(amount < 0) return;
        age += amount;
    }

    /**
     * Returns the time that this LaserModel has lived in the level.
     *
     * @return the age of this LaserModel. */
    public float getAge() { return age; }

    /**
     * Informs this Laser that it is no longer charging and instead it
     * is attacking.
     * */
    public void setAttacking() {
        setName("Attack Laser");
        attacking = true;
    }

    /**
     * Returns true if this LaserModel is attacking, not charging.
     *
     * @return true if this LaserModel is attacking.
     * */
    public boolean isAttacking(){ return attacking; }

    /**
     * Informs this LaserModel that it has hit a target.
     * */
    public void setHitTarget(){hitTarget = true;}

    /**
     * Returns true if this LaserModel has hit a target.
     *
     * @return true if this LaserModel has hit a target.
     * */
    public boolean didHitTarget() {return hitTarget;}
}
