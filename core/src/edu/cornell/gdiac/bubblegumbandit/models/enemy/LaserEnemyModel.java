package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;


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

    /** The point at which the most up-to-date laser beam intersects.  */
    private Vector2 beamIntersect;

    /**Current phase of this LaserEnemyModel. */
    private LASER_PHASE phase;

    /**How many more seconds this LaserEnemyModel needs to wait before
     * shooting a laser. */
    private float cooldownTimer = 0;

    /**How many seconds this LaserEnemyModel needs to wait in between
     * laser shots. */
    private final float LASER_COOLDOWN = 1;

    /**true if the laser beam emitted by this LaserEnemyModel is
     * making contact with the player; otherwise, false. */
    private boolean hittingBandit;

    /**How many seconds this LaserEnemyModel has been charging,
     * locking, and firing. Resets to zero for each laser shot.*/
    private float age;

    private float firingTimer;
    /**Amount of gum needed to stick the robot*/
    private int gumToStick;
    /**Amount of gum currently stuck to robot*/
    private int gumStuck;


    /**
     * Every phase that this LaserEnemyModel goes through when it attacks.
     * */
    private enum LASER_PHASE{
        INACTIVE,
        CHARGING,
        LOCKED,
        FIRING
    }

    private PolygonShape shape;

    /**Creates a LaserEnemy.
     *
     * @param world The Box2D world
     * @param id the id of this Enemy
     * */
    public LaserEnemyModel(World world, int id){
        super(world, id);
        setFaceRight(false);
        shape = new PolygonShape();
        shape.setAsBox(.5f,.5f);
        gumToStick = 1;
        gumStuck = 0;
    }

    /**Initializes this LaserEnemyModel from JSON. Sets its
     * phase to INACTIVE.
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
        setPhase(LASER_PHASE.INACTIVE);
    }

    /**
     * Returns true if this LaserEnemyModel is in its inactive phase.
     *
     * @return true if this LaserEnemyModel is in its inactive phase;
     * otherwise, returns false.
     * */
    public boolean inactiveLaser() {return phase == LASER_PHASE.INACTIVE;}

    /**Increments the number of times robot has been hit with gum by 1 */
    public void addGumHit() {gumStuck += 1;}

    /**Check how much gum robot has been hit with*/
    public boolean shouldStick() {return gumStuck == gumToStick;}


    /**
     * Returns true if this LaserEnemyModel is in its charging phase.
     *
     * @return true if this LaserEnemyModel is in its charging phase;
     * otherwise, returns false.
     * */
    public boolean chargingLaser() {return phase == LASER_PHASE.CHARGING;}


    /**
     * Returns true if this LaserEnemyModel is in its locked phase.
     *
     * @return true if this LaserEnemyModel is in its locked phase;
     * otherwise, returns false.
     * */
    public boolean lockingLaser(){return phase == LASER_PHASE.LOCKED;}


    /**
     * Returns true if this LaserEnemyModel is in its firing phase.
     *
     * @return true if this LaserEnemyModel is in its firing phase;
     * otherwise, returns false.
     * */
    public boolean firingLaser() { return phase == LASER_PHASE.FIRING; }


    /**
     * Sets the Vector2 at which this LaserEnemyModel's laser
     * beam intersected with an object of interest. This Vector2
     * is determined by the LaserController, which determines
     * what objects this beam may touch.
     *
     * @param intersect  the Vector2 at which this LaserEnemyModel's
     *                   laser beam intersected with an object of interest.
     * */
    public void setBeamIntersect(Vector2 intersect){
        beamIntersect = intersect;
    }


    /**
     * Returns the Vector2 at which this LaserEnemyModel's laser
     * beam intersected with an object of interest.
     *
     * @return the Vector2 at which this LaserEnemyModel's laser
     *         beam intersected with an object of interest.
     * */
    public Vector2 getBeamIntersect(){return beamIntersect;}

    /**
     * Resets the attack cycle of this LaserEnemyModel. This can be
     * called in any phase.
     * */
    public void resetLaserCycle(){
        age = 0;
        firingTimer = 0;
        phase = LASER_PHASE.INACTIVE;
        cooldownTimer = LASER_COOLDOWN;
    }


    /**
     * Processes logic for when this LaserEnemyModel is in its charging
     * phase. Updates the phase to charging if not in it already.
     * */
    private void processChargingPhase(){
        if(phase != LASER_PHASE.CHARGING) setPhase(LASER_PHASE.CHARGING);

        //Put additional charging logic here.
    }


    /**
     * Processes logic for when this LaserEnemyModel is in its locked
     * phase. Updates the phase to locked if not in it already.
     * */
    private void processLockedPhase(){
        if(phase != LASER_PHASE.LOCKED) setPhase(LASER_PHASE.LOCKED);

        //Put additional locking logic here.
    }

    /**
     * Processes logic for when this LaserEnemyModel is in its firing
     * phase. Updates the phase to firing if not in it already.
     *
     * @param dt The Delta Time value.
     * */
    private void processFiringPhase(float dt){
        if(phase != LASER_PHASE.FIRING) setPhase(LASER_PHASE.FIRING);

        //Put additional firing logic here.
        firingTimer += dt;
    }

    /**
     * Returns a percentage that represents how far along this
     * LaserEnemyModel is along its firing phase. For example,
     * a return value of .75 means this LaserEnemyModel is 75%
     * done with its firing phase.
     *
     * @param fireTime How long the entire firing phase lasts
     *
     * @return completion percentage of this LaserEnemyModel's
     *         firing phase.
     * */
    public float getFiringDistance(float fireTime){
        return (firingTimer / fireTime);
    }

    /**
     * Updates this LaserEnemyModel's laser phase. This method asserts that
     * only valid transitions execute:
     * <p>
     *
     * Any --> Inactive, Inactive --> Charging, Charging --> Locked, Locked --> Firing.
     *
     * @param newPhase The LASER_PHASE that this LaserEnemyModel will update to.
     * */
    private void setPhase(LASER_PHASE newPhase){
        assert newPhase != LASER_PHASE.CHARGING || phase == LASER_PHASE.INACTIVE;
        assert newPhase != LASER_PHASE.LOCKED || phase == LASER_PHASE.CHARGING;
        assert newPhase != LASER_PHASE.FIRING || phase == LASER_PHASE.LOCKED;
        phase = newPhase;
    }


    /**
     * Returns true if this LaserEnemyModel's vision component can
     * see the Bandit's model.
     *
     * @return true if this LaserEnemyModel's vision component can
     *         see the Bandit's model; otherwise, false.
     * */
    public boolean canSeeBandit(BanditModel bandit){
        return vision.canSee(bandit);
    }


    /**
     * Reduces the time this LaserEnemyModel needs to wait before
     * firing another laser shot.
     *
     * @param dt The amount of time to reduce by.
     * */
    public void decrementCooldown(float dt){
        cooldownTimer -= dt;
    }

    /**
     * Returns true if this LaserEnemyModel still needs to wait before
     * firing a laser beam.
     *
     * @return true if this LaserEnemyModel must wait some time before
     *         firing again; otherwise, false.
     * */
    public boolean coolingDown(){
        return cooldownTimer > 0;
    }

    /**
     * Adds to the age of this LaserEnemyModel.
     * <p>
     * Depending on its age, this LaserEnemyModel checks if it should
     * enter a new phase.
     *
     * @param amount How much time to add.
     * @param chargeTime How much time this LaserEnemyModel spends, in total,
     *                   charging its laser.
     * @param lockTime How much time this LaserEnemyModel spends, in total,
     *                 locking its laser on some target.
     * @param fireTime How much time this LaserEnemyModel spends, in total,
     *                 firing its laser.
     * */
    public void ageLaser(float amount, float chargeTime, float lockTime, float fireTime){

        //Return if invalid values or if this Enemy is cooling down.
        if(amount < 0) return;
        if(chargeTime < 0) return;
        if(lockTime < 0) return;
        if(fireTime < 0) return;
        if(coolingDown()) return;

        age += amount;

        //Now we determine if we need to update our phase.
        float timeToReset = chargeTime + lockTime + fireTime;
        float timeToFire = chargeTime + lockTime;

        if(age >= timeToReset) resetLaserCycle();
        else if(age >= timeToFire) processFiringPhase(amount);
        else if(age >= chargeTime) processLockedPhase();
        else processChargingPhase();
    }

    /**
     * Tells this LaserEnemyModel if its laser is making contact with
     * the bandit.
     *
     * @param hittingBandit true if this LaserEnemyModel's laser is
     *                      hitting the Bandit; otherwise, false.
     * */
    public void setHittingBandit(boolean hittingBandit) {
        this.hittingBandit = hittingBandit;
    }

    /**
     * Returns true if the laser beam emitting from this LaserEnemyModel
     * is making contact with the Bandit. Otherwise, returns false.
     *
     * @return true if this LaserEnemyModel's laser beam is touching the
     *         Bandit; otherwise, false.
     * */
    public boolean isHittingBandit() { return hittingBandit; }

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
