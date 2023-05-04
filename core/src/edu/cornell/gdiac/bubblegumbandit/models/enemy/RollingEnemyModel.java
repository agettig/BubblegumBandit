package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.CONTROL_MOVE_LEFT;
import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.CONTROL_MOVE_RIGHT;

public class RollingEnemyModel extends EnemyModel {

    /**
     * How long the enemy can roll to attack the player + charge time
     */
    private final int ATTACK_TIME = 80;

    /**
     * Time that rolling enemies take to charge
     */
    private final int CHARGE_TIME = 20;

    /**
     * Time need for enemy to cooldown in between attacks
     */
    private final int COOLDOWN = 120;

    private int cooldownTime;

    /**
     * Damage from bumping into a moving robot
     */
    private float damage = 10;

    /**
     * If the robot is attacking
     */
    private boolean isRolling;

    /**
     * Sets the damage of a MovingEnemyModel.
     *
     * @param damage The new damage
     */
    public void setDamage(float damage) {
        this.damage = damage;
    }

    /**
     * How long the enemy has been attacking for
     */
    private int attackDuration;

    /**
     * Returns the damage of a RollingEnemyModel.
     */
    public float getDamage() {
        return damage;
    }

    private int rollingAttackSpeed;

    /**
     * Creates a LaserEnemy.
     *
     * @param world The Box2D world
     * @param id    the id of this Enemy
     */
    public RollingEnemyModel(World world, int id) {
        super(world, id);
        isRolling = false;
        cooldownTime = 0;
    }

    /**
     * Initializes this LaserEnemyModel from JSON and sets its vision radius.
     *
     * @param directory     The BubblegumBandit asset directory
     * @param x             the x position to set this ProjectileEnemyModel
     * @param y             the y position to set this ProjectileEnemyModel
     * @param constantsJson the constants json
     */
    public void initialize(AssetDirectory directory, float x, float y,
                           JsonValue constantsJson) {
        super.initialize(directory, x, y+.01f, constantsJson);
        rollingAttackSpeed = constantsJson.get("mediumAttack").asInt();
        setName("mediumEnemy");
        attackDuration = 0;
    }

    @Override
    /**
     * Updates rolling enemy
     * */
    public void update(float delta) {


        updateYScale();
        updateRayCasts();

        // update attack state
        if (fired() && cooldownTime <= 0) {
            if (isRolling) {
                if (attackDuration == ATTACK_TIME) {
                    isRolling = false;
                    cooldownTime = COOLDOWN;
                } else {
                    attackDuration++;
                }
            } else {
                attackDuration = 0;
                isRolling = true;
            }
        }
        if (isRolling && !stuck && !gummed){
            animationController.setAnimation("roll", true);
        }
        else if (stuck || gummed){
            animationController.setAnimation("stuck", true);
        }
        else{
            animationController.setAnimation("patrol", true);
        }

        cooldownTime--;


        boolean movingLeft = (nextAction & CONTROL_MOVE_LEFT) != 0 && (previousAction & CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (nextAction & CONTROL_MOVE_RIGHT) != 0 && (previousAction & CONTROL_MOVE_RIGHT) != 0;

        if (fired() && isRolling && (movingLeft || movingRight) && cooldownTime <= 0) {
            int speed = 0;
            if (movingLeft) {
                speed = -rollingAttackSpeed;
                setFaceRight(false);
            } else {
                speed = rollingAttackSpeed;
                setFaceRight(true);
            }
            if (attackDuration < CHARGE_TIME){
                speed = -speed;
            }
            setVX(speed);
        } else {
            updateMovement(nextAction);
        }
    }

    private void updateYScale(){
        if (!isFlipped && yScale < 1) {
            if (yScale != -1 || !stuck) {
                yScale += 0.1f;
            }
        } else if (isFlipped && yScale > -1) {
            if (yScale != 1 || !stuck) {
                yScale -= 0.1f;
            }
        }
    }

    /**
     * Resets attack
     */
    public void resetAttack() {
        isRolling = false;
        attackDuration = 0;
    }
}
