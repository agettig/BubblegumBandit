package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;

/**
 * Represents an EnemyModel that attacks with a laser beam.
 * <p>
 * A LaserEnemyModel fires its laser beam via the LaserController and
 * in two phases: the charging phase and firing phase. In the charging
 * phase, the LaserEnemyModel points a tracking laser at the Bandit.
 * After a short charging time, the tracking laser locks onto the Bandit's
 * position and enters the firing phase; it shoots a thicker beam outwards
 * that collides with the first non-Bandit obstacle it hits. This beam
 * damages the player.
 * */
public class LaserEnemyModel extends EnemyModel{

    /** Latest raycast sent out by a LaserEnemyModel during its charging phase. */
    private Vector2 raycastLine;

    /** true if this LaserEnemyModel is firing a laser*/
    private boolean firing;

    /** true if this LaserEnemyModel is charging a laser*/
    private boolean charging;

    /** true if this LaserEnemyModel has shot its firing laser.*/
    private boolean shot;

    /** true if this LaserEnemyModel's firing laser damaged the Bandit.*/
    private boolean damagedBandit;

    /**How many seconds this LaserEnemyModel has been charging and firing.*/
    private float age;

    private boolean didHitBandit;

    private PolygonShape shape;

    /**Creates a LaserEnemy.
     *
     * @param world The Box2D world
     * @param id the id of this Enemy
     * */
    public LaserEnemyModel(World world, int id){
        super(world, id);
        didHitBandit = false;
        shape = new PolygonShape();
        shape.setAsBox(.5f,.5f);
    }

    /**Initializes this LaserEnemyModel from JSON and sets its vision radius.
     *
     * @param directory The BubblegumBandit asset directory
     * @param x the x position to set this ProjectileEnemyModel
     * @param y the y position to set this ProjectileEnemyModel
     * @param constantsJson the constants json
     * */
    public void initialize(AssetDirectory directory, float x, float y,
                           JsonValue constantsJson){
        super.initialize(directory, x, y, constantsJson);
        setName("Laser Enemy");

    }

    /**
     * Returns true if this LaserEnemyModel is in its firing phase.
     *
     * @return true if this LaserEnemyModel is in its firing phase;
     * otherwise, returns false.
     * */
    public boolean isFiringLaser() { return firing; }

    /**
     * Returns true if this LaserEnemyModel is in its charging phase.
     *
     * @return true if this LaserEnemyModel is in its charging phase;
     * otherwise, returns false.
     * */
    public boolean isChargingLaser() {return charging;}


    /**
     * Returns the Raycast that represents this LaserEnemyModel's raycast
     * laser beam.
     *
     * @return the Raycast that represents this LaserEnemyModel's raycast
     *         laser beam.
     * */
    public Vector2 getRaycastLine() {
        return raycastLine;
    }

    /**
     * Sets the Raycast that represents this LaserEnemyModel's raycast
     * laser beam.
     *
     * @param raycastChargeLine  the Raycast that represents this
     *                           LaserEnemyModel's raycast laser beam.
     * */
    public void setRaycastLine(Vector2 raycastChargeLine){

        this.raycastLine = raycastChargeLine;
    }

    /**
     * Informs this LaserEnemyModel if in its firing phase.
     *
     * @param firing if this LaserEnemyModel is in its firing phase
     * */
    public void setFiringLaser(boolean firing){
        this.firing = firing;}

    /**
     * Informs this LaserEnemyModel if in its charging phase.
     *
     * @param charging if this LaserEnemyModel is in its charging phase
     * */
    public void setChargingLaser(boolean charging) { this.charging = charging; }

    /**
     * Sets this LaserEnemyModel's age to 0.
     * */
    public void resetAge(){
        System.out.println("Resetting age");
        age = 0;
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
     * Returns true if this LaserEnemyModel fired its attack laser.
     *
     * @return true if this LaserEnemyModel fired its attack laser;
     *         otherwise, false.
     * */
    public boolean didFire() {
        return shot;
    }

    /**
     * Tells this LaserEnemyModel if it fired its attack laser.
     *
     * @param shot if this LaserEnemyModel fired its attack laser.
     * */
    public void setFired(boolean shot) {
        this.shot = shot;
    }

    /**
     * Returns true if this LaserEnemyModel's attack laser damaged the
     * Bandit.
     *
     * @return true if this LaserEnemyModel's attack laser damaged the
     *         Bandit; otherwise, false.
     * */
    public boolean hasDamagedBandit() {
        return damagedBandit;
    }

    /**
     * Tells this LaserEnemyModel if it damaged the Bandit.
     *
     * @param damagedBandit if this LaserEnemyModel damaged
     *                      the Bandit.
     * */
    public void setDamagedBandit(boolean damagedBandit) {
        this.damagedBandit = damagedBandit;
    }

    public void setHitBandit(boolean didHitBandit) { this.didHitBandit = didHitBandit; }

    public boolean didHitBandit() { return didHitBandit; }

    @Override
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        float y = getYFeet();
        canvas.drawPhysics(shape, Color.CORAL, (int) getX() + .5f, y, 0,drawScale.x, drawScale.y );
    }

    @Override
    public float getYFeet(){
        float y = (int) super.getY();
        if (!isFlipped){
            y -= .5f;
        }
        else{
            y += 1.5f;
        }
        return y;
    }

}
