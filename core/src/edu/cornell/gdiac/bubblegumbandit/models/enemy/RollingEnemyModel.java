package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.models.level.gum.GumModel;

import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.CONTROL_MOVE_LEFT;
import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.CONTROL_MOVE_RIGHT;

/**
 * Represents a medium-sized enemy that rolls into its target.
 * */
public class RollingEnemyModel extends EnemyModel {

    /** Upper bound for how many seconds an attack (charging + rolling) can last */
    private final int ATTACK_TIME = 60;

    /** Time a RollingEnemyModel must wait in-between attacks  */
    private final int COOLDOWN = 120;

    /** Damage taken from bumping into a RollingEnemyModel */
    private final float DAMAGE = 10;

    /** Velocity at which a RollingEnemyModel rolls. */
    private int ROLL_SPEED;

    /** How many more seconds until this RollingEnemyModel can attack again */
    private int cooldownTime;

    /** true if this RollingEnemyModel is attacking */
    private boolean isRolling;

    /** How many seconds this RollingEnemyModel has been attacking for  */
    private int attackDuration;

    /**
     * Returns the damage a RollingEnemyModel deals when it bumps into
     * its target.
     *
     * @return the damage a RollingEnemyModel deals
     */
    public float getDamage() {
        return DAMAGE;
    }

    /**
     * Creates a RollingEnemyModel.
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
     * Initializes this RollingEnemyModel from JSON and sets its vision radius.
     *
     * @param directory     The BubblegumBandit asset directory
     * @param x             the x position to set this ProjectileEnemyModel
     * @param y             the y position to set this ProjectileEnemyModel
     * @param constantsJson the constants json
     */
    public void initialize(AssetDirectory directory, float x, float y,
                           JsonValue constantsJson) {
        super.initialize(directory, x, y+.01f, constantsJson);
        ROLL_SPEED = constantsJson.get("mediumAttack").asInt();
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
        updateAnimations();
        updateAttackState();
        updateMovement();
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
     * Sets this RollingEnemyModel's animation based on its attack
     * state.
     * */
    private void updateAnimations(){
        if (isRolling && !stuck && !gummed){
            animationController.setAnimation("roll", true);
        }
        else if (stuck || gummed){
            animationController.setAnimation("stuck", true);
        }
        else{
            animationController.setAnimation("patrol", true);
        }
    }

    /**
     * Updates this RollingEnemyModel's attack state.
     * */
    private void updateAttackState(){
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
        cooldownTime--;
    }

    /**
     * Moves this RollingEnemyModel based on its attack state.
     * */
    private void updateMovement(){
        boolean movingLeft = (nextAction & CONTROL_MOVE_LEFT) != 0 && (previousAction & CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (nextAction & CONTROL_MOVE_RIGHT) != 0 && (previousAction & CONTROL_MOVE_RIGHT) != 0;

        if (fired() && isRolling && (movingLeft || movingRight) && cooldownTime <= 0) {
            if (movingLeft) {
                setVX(-ROLL_SPEED);
                setFaceRight(false);
            } else {
                setVX(ROLL_SPEED);
                setFaceRight(true);
            }
        } else {
            updateMovement(nextAction);
        }
    }





    /**
     * Resets this RollingEnemyModel's attack.
     */
    public void resetAttack() {
        isRolling = false;
        attackDuration = 0;
    }
}
