package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.helpers.Damage;
import edu.cornell.gdiac.bubblegumbandit.models.level.CrusherModel;
import edu.cornell.gdiac.bubblegumbandit.view.AnimationController;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.physics.obstacle.Obstacle;


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
 */
public class LaserEnemyModel extends EnemyModel {

    /**
     * The point at which the most up-to-date laser beam intersects.
     */
    private Vector2 beamIntersect;

    /** The point at which the laser beam starts.*/
    private Vector2 beamOrigin;

    /**Current phase of this LaserEnemyModel. */
    private LASER_PHASE phase;

    /**
     * How many more seconds this LaserEnemyModel needs to wait before
     * shooting a laser.
     */
    private float cooldownTimer = 0;

    /**
     * How many seconds this LaserEnemyModel needs to wait in between
     * laser shots.
     */
    private final float LASER_COOLDOWN = 1;

    /**
     * true if the laser beam emitted by this LaserEnemyModel is
     * making contact with the player; otherwise, false.
     */
    private boolean hittingBandit;

    /**
     * How many seconds this LaserEnemyModel has been charging,
     * locking, and firing. Resets to zero for each laser shot.
     */
    private float age;

    /**Timer for the firing phase. */
    private float firingTimer;

    /**
     * Amount of gum needed to stick the robot
     */
    private int gumToStick;

    /**
     * Amount of gum currently stuck to robot
     */
    private int gumStuck;

    /**
     * Whether the laser enemy has jumped
     * Used to determine whether or not to apply linear impulse
     */
    private boolean hasJumped;

    /**
     * Whether the laser enemy is currently jumping
     */
    private boolean isJumping;

    /**
     * Jump cooldown
     */
    private int jumpCooldown = 0;

    /**
     * Range in damage for bandit
     */
    private int stompRange = 5;


    /**
     * Jump cooldown time
     */
    private final int JUMP_COOLDOWN = 120;

    /**
     * Vector to represent start of ray cast for jumping damage
     */
    private Vector2 beginVector = new Vector2();

    /**
     * Vector to represent end of ray cast for jumping damage
     */
    private Vector2 endVector = new Vector2();

    public boolean isShouldJumpAttack() {
        return shouldJumpAttack;
    }

    public void setShouldJumpAttack(boolean shouldJumpAttack) {
        this.shouldJumpAttack = shouldJumpAttack;
    }

    /**
     * Whether the laser enemy is jumping or laser shooting
     */
    private boolean shouldJumpAttack = true;
    /** Texture for gum after just one shot */
    private TextureRegion halfStuck;

    /** Texture for gum after just one shot, with outline
     * currently draw with outline is never called unless the enemy is gummed, however
     * */
    private TextureRegion halfStuckOutline;






    /**
     * Every phase that this LaserEnemyModel goes through when it attacks.
     */
    private enum LASER_PHASE {
        INACTIVE,
        CHARGING,
        LOCKED,
        FIRING
    }

    private PolygonShape shape;

    /**
     * Creates a LaserEnemy.
     *
     * @param world The Box2D world
     * @param id    the id of this Enemy
     */
    public LaserEnemyModel(World world, int id) {
        super(world, id);
        setFaceRight(false);
        shape = new PolygonShape();
        shape.setAsBox(.5f, .5f);
        gumToStick = 1;
        gumStuck = 0;
        isJumping = false;
        hasJumped = false;
    }

    /**
     * Initializes this LaserEnemyModel from JSON. Sets its
     * phase to INACTIVE.
     *
     * @param directory     The BubblegumBandit asset directory
     * @param x             the x position to set this ProjectileEnemyModel
     * @param y             the y position to set this ProjectileEnemyModel
     * @param constantsJson the constants json
     * @param isFacingRight whether the enemy spawns facing right
     * */
    public void initialize(AssetDirectory directory, float x, float y,
                           JsonValue constantsJson, boolean isFacingRight){
        super.initialize(directory, x, y, constantsJson, isFacingRight);
        halfStuck = new TextureRegion(directory.getEntry("halfStuck", Texture.class));
        halfStuckOutline = new TextureRegion(directory.getEntry("halfStuckOutline", Texture.class));
        setName("laserEnemy");
        setPhase(LASER_PHASE.INACTIVE);
    }

    public boolean isJumping() {
        return isJumping;
    }

    public void update(float dt) {
        // laser enemy can no longer jump set attack to laser
        if (getGummed() || getStuck()){
            setShouldJumpAttack(false);
        }

        // if jumping
        if (isJumping){
            // apply linear impulse
            if (!hasJumped && jumpCooldown <=0){
               int impulse = isFlipped ? -30 : 30;
                getBody().applyLinearImpulse(new Vector2(0, impulse), getPosition(), true);
                hasJumped = true;
            }

            if (!isFlipped && yScale < 1) {
                if (yScale != -1 || !stuck) {
                    yScale += 0.1f;
                }
            } else if (isFlipped && yScale > -1) {
                if (yScale != 1 || !stuck) {
                    yScale -= 0.1f;
                }
            }
            updateRayCasts();

            return;
        }
        if (shouldJumpAttack){
            jumpCooldown --;
        }
        if (phase == LASER_PHASE.INACTIVE) super.update(dt);
        else {
            if (!isFlipped && yScale < 1) {
                if (yScale != -1 || !stuck) {
                    yScale += 0.1f;
                }
            } else if (isFlipped && yScale > -1) {
                if (yScale != 1 || !stuck) {
                    yScale -= 0.1f;
                }
            }
            updateRayCasts();
        }
        // attack animations
        if (chargingLaser()) {
            animationController.setAnimation("charge", true);
        }
        else if (stuck || gummed){
            animationController.setAnimation("stuck", true);
        } else {
            animationController.setAnimation("patrol", true);
        }
    }

    /**
     * Sets laser enemy to jump
     */
    public void jump(){
        if (jumpCooldown <= 0) isJumping = true;
    }

    /**
     * Resolves laser enemy landing after jump attack
     */
    public void hasLanded(){
        isJumping = false;
        hasJumped = false;
        jumpCooldown = JUMP_COOLDOWN;
        setShouldJumpAttack(false);

        // damage bandit if in range
        RayCastCallback rayPass = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point,
                                          Vector2 normal, float fraction) {

                boolean isBandit = fixture.getBody().getUserData() instanceof BanditModel;
                if (isBandit) {
                    BanditModel bandit = (BanditModel) fixture.getBody().getUserData();
                    bandit.hitPlayer(Damage.LASER_JUMP_DAMAGE, false);
                    int yImpulse = isFlipped ? -10 : 10;
                    int xImpulse = getX() > bandit.getX() ? -5 : 5;
                    bandit.getBody().applyLinearImpulse(new Vector2(xImpulse,yImpulse), getPosition(), true);
                    bandit.stun(180);
                };
                return -1;
            }
        };

        beginVector.x = getX() - stompRange;
        beginVector.y = getYFeet();

        endVector.x = getX() + stompRange;
        endVector.y = getYFeet();

        world.rayCast(rayPass, beginVector, endVector);
    }

    /**
     * Returns true if this LaserEnemyModel is in its inactive phase.
     *
     * @return true if this LaserEnemyModel is in its inactive phase;
     * otherwise, returns false.
     */
    public boolean inactiveLaser() {
        return phase == LASER_PHASE.INACTIVE;
    }

    /**
     * Increments the number of times robot has been hit with gum by 1
     */
    public void addGumHit() {
        gumStuck += 1;
    }

    /**
     * Check how much gum robot has been hit with
     */
    public boolean shouldStick() {
        return gumStuck == gumToStick;
    }

    public void resetGumStuck(){
        gumStuck = 0;
    }


    /**
     * Returns true if this LaserEnemyModel is in its charging phase.
     *
     * @return true if this LaserEnemyModel is in its charging phase;
     * otherwise, returns false.
     */
    public boolean chargingLaser() {
        return phase == LASER_PHASE.CHARGING;
    }


    /**
     * Returns true if this LaserEnemyModel is in its locked phase.
     *
     * @return true if this LaserEnemyModel is in its locked phase;
     * otherwise, returns false.
     */
    public boolean lockingLaser() {
        return phase == LASER_PHASE.LOCKED;
    }


    /**
     * Returns true if this LaserEnemyModel is in its firing phase.
     *
     * @return true if this LaserEnemyModel is in its firing phase;
     * otherwise, returns false.
     */
    public boolean firingLaser() {
        return phase == LASER_PHASE.FIRING;
    }


    /**
     * Sets the Vector2 at which this LaserEnemyModel's laser
     * beam intersected with an object of interest. This Vector2
     * is determined by the LaserController, which determines
     * what objects this beam may touch.
     *
     * @param intersect the Vector2 at which this LaserEnemyModel's
     *                  laser beam intersected with an object of interest.
     */
    public void setBeamIntersect(Vector2 intersect) {
        beamIntersect = intersect;
    }

    /**
     * Returns the Vector2 at which this LaserEnemyModel's laser
     * beam intersected with an object of interest.
     *
     * @return the Vector2 at which this LaserEnemyModel's laser
     * beam intersected with an object of interest.
     */
    public Vector2 getBeamIntersect() {
        return beamIntersect;
    }

    /**
     * Sets the Vector2 at which this LaserEnemyModel's laser
     * beam started firing.
     *
     * @param origin  the Vector2 at which this LaserEnemyModel's
     *                   laser beam started firing.
     * */
    public void setBeamOrigin(Vector2 origin){
        beamOrigin = origin;
    }

    /**
     * Returns the Vector2 at which this LaserEnemyModel's laser
     * beam started firing.
     *
     * @return the Vector2 at which this LaserEnemyModel's
     *         laser beam started firing.
     * */
    public Vector2 getBeamOrigin(){
        return beamOrigin;
    }

    /**
     * Resets the attack cycle of this LaserEnemyModel. This can be
     * called in any phase.
     */
    public void resetLaserCycle() {
        age = 0;
        firingTimer = 0;
        phase = LASER_PHASE.INACTIVE;
        cooldownTimer = LASER_COOLDOWN;
        setShouldJumpAttack(true);
    }


    /**
     * Processes logic for when this LaserEnemyModel is in its charging
     * phase. Updates the phase to charging if not in it already.
     */
    private void processChargingPhase() {
        if (phase != LASER_PHASE.CHARGING) setPhase(LASER_PHASE.CHARGING);

        //Put additional charging logic here.
    }


    /**
     * Processes logic for when this LaserEnemyModel is in its locked
     * phase. Updates the phase to locked if not in it already.
     */
    private void processLockedPhase() {
        if (phase != LASER_PHASE.LOCKED) setPhase(LASER_PHASE.LOCKED);

        //Put additional locking logic here.
    }

    /**
     * Processes logic for when this LaserEnemyModel is in its firing
     * phase. Updates the phase to firing if not in it already.
     *
     * @param dt The Delta Time value.
     */
    private void processFiringPhase(float dt) {
        if (phase != LASER_PHASE.FIRING) setPhase(LASER_PHASE.FIRING);

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
     * @return completion percentage of this LaserEnemyModel's
     * firing phase.
     */
    public float getFiringDistance(float fireTime) {
        return (firingTimer / fireTime);
    }

    /**
     * Updates this LaserEnemyModel's laser phase. This method asserts that
     * only valid transitions execute:
     * <p>
     * <p>
     * Any --> Inactive, Inactive --> Charging, Charging --> Locked, Locked --> Firing.
     *
     * @param newPhase The LASER_PHASE that this LaserEnemyModel will update to.
     */
    private void setPhase(LASER_PHASE newPhase) {
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
        Vector2 enemyPosition = getPosition();
        Vector2 banditPosition = bandit.getPosition();
        float minAngle = 310;
        float maxAngle = 50;

        Vector2 directionToBandit = banditPosition.cpy().sub(enemyPosition);
        Vector2 referenceDirection = getFaceRight() ?
                new Vector2(1, 0) : new Vector2(-1, 0);
        float angle = directionToBandit.angleDeg(referenceDirection);

        return ((angle >= minAngle && angle <= 360) ||
                (angle >= 0 && angle <= maxAngle)) &&
                vision.canSee(bandit);
    }


    /**
     * Reduces the time this LaserEnemyModel needs to wait before
     * firing another laser shot.
     *
     * @param dt The amount of time to reduce by.
     */
    public void decrementCooldown(float dt) {
        cooldownTimer -= dt;
    }

    /**
     * Returns true if this LaserEnemyModel still needs to wait before
     * firing a laser beam.
     *
     * @return true if this LaserEnemyModel must wait some time before
     * firing again; otherwise, false.
     */
    public boolean coolingDown() {
        return cooldownTimer > 0;
    }

    /**
     * Adds to the age of this LaserEnemyModel.
     * <p>
     * Depending on its age, this LaserEnemyModel checks if it should
     * enter a new phase.
     *
     * @param amount     How much time to add.
     * @param chargeTime How much time this LaserEnemyModel spends, in total,
     *                   charging its laser.
     * @param lockTime   How much time this LaserEnemyModel spends, in total,
     *                   locking its laser on some target.
     * @param fireTime   How much time this LaserEnemyModel spends, in total,
     *                   firing its laser.
     */
    public void ageLaser(float amount, float chargeTime, float lockTime, float fireTime) {

        if (shouldJumpAttack) return;
        //Return if invalid values or if this Enemy is cooling down.
        if (amount < 0) return;
        if (chargeTime < 0) return;
        if (lockTime < 0) return;
        if (fireTime < 0) return;
        if (coolingDown()) return;

        age += amount;

        //Now we determine if we need to update our phase.
        float timeToReset = chargeTime + lockTime + fireTime;
        float timeToFire = chargeTime + lockTime;

        if (age >= timeToReset) {
            resetLaserCycle();
        }
        else if (age >= timeToFire) {
            processFiringPhase(amount);
        }
        else if (age >= chargeTime) {
            processLockedPhase();
        }
        else {
            processChargingPhase();
        }
    }

    /**
     * Tells this LaserEnemyModel if its laser is making contact with
     * the bandit.
     *
     * @param hittingBandit true if this LaserEnemyModel's laser is
     *                      hitting the Bandit; otherwise, false.
     */
    public void setHittingBandit(boolean hittingBandit) {
        this.hittingBandit = hittingBandit;
    }

    /**
     * Returns true if the laser beam emitting from this LaserEnemyModel
     * is making contact with the Bandit. Otherwise, returns false.
     *
     * @return true if this LaserEnemyModel's laser beam is touching the
     * Bandit; otherwise, false.
     */
    public boolean isHittingBandit() {
        return hittingBandit;
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        float y = getYFeet();
        canvas.drawPhysics(shape, Color.CORAL, (int) getX() + .5f, y, 0, drawScale.x, drawScale.y);
    }

    @Override
    public float getYFeet() {
        float y = (int) super.getY();
        if (!isFlipped) {
            y -= .5f;
        } else {
            y += 1.5f;
        }
        return y;
    }


    @Override
    public void setFaceRight(boolean isRight) {
        if (chargingLaser() || lockingLaser() || firingLaser() || getStuck() || getGummed()) return;
        super.setFaceRight(isRight);
    }

    @Override
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            float effect = getFaceRight() ? 1.0f : -1.0f;
            TextureRegion drawn = texture;
            float x = getX() * drawScale.x;
            float y = getY() * drawScale.y;
            float gumY = y;
            float gumX = x;

            if(animationController!=null) {
                drawn = animationController.getFrame();
                x-=(getWidth()/2)*drawScale.x*effect;
            }

            canvas.drawWithShadow(drawn, Color.WHITE, origin.x, origin.y, x, y, getAngle(), effect, yScale);

            //if gummed, overlay with gumTexture

            if (gummed) {
                if(stuck) {
                    canvas.draw(gumTexture, Color.WHITE, origin.x, origin.y, gumX,
                        gumY, getAngle(), 1, yScale);
                } else {
                    canvas.draw(squishedGum, Color.WHITE, origin.x, origin.y, gumX,
                        gumY-yScale*squishedGum.getRegionHeight()/2, getAngle(), 1, yScale);
                }
//
            } else if (gumStuck>0){
                    canvas.draw(halfStuck, Color.WHITE,
                        origin.x, origin.y, (getX() - (getDimension().x/2))* drawScale.x, y, getAngle(), 1, yScale);
            }

            //if shielded, overlay shield
            if (isShielded()){
                canvas.draw(shield, Color.WHITE, origin.x , origin.y, (getX() - (getDimension().x/2))* drawScale.x ,
                    y - shield.getRegionHeight()/8f * yScale, getAngle(), 1, yScale);
            }
        }
    }




}
