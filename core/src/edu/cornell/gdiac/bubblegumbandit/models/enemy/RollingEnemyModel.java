package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation.SwingOut;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.models.level.CrusherModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.bubblegumbandit.models.level.gum.GumModel;

import java.util.HashSet;

import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.CONTROL_MOVE_LEFT;
import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.CONTROL_MOVE_RIGHT;

/**
 * Represents a medium-sized enemy that rolls into its target.
 * */
public class RollingEnemyModel extends EnemyModel {

    /**
     * Upper bound for how many seconds an attack (charging + rolling) can last
     */
    private final int ATTACK_TIME = 75;

    /**
     * Time that rolling enemies take to charge
     */
    private final int CHARGE_TIME = 15;

    /** Damage taken from bumping into a RollingEnemyModel */
    private final float DAMAGE = 10;

    /** Time a RollingEnemyModel must wait in-between attacks  */
    private final int COOLDOWN = 120;

    /** How many more seconds until this RollingEnemyModel can roll again */
    private int rollCoolDown;

    /**
     * If the robot is attacking
     */
    /** How many seconds this RollingEnemyModel has been trying to unstick
     *  itself */
    private float unstickStopWatch;

    /** true if this RollingEnemyModel is in the process of unsticking itself.*/
    private boolean unsticking;

    /** true if this RollingEnemyModel is attacking */
    private boolean isRolling;

    /**
     * How long the enemy has been attacking for
     */
    /** How many seconds this RollingEnemyModel has been attacking for  */
    private int attackDuration;

    /** Velocity at which a RollingEnemyModel rolls. */
    private int ROLL_SPEED;

    /** How many seconds it takes for a RollingEnemyModel to unstick itself*/
    private final float UNSTICK_TIME = 3f;

    /**rate at which gum disappears during unsticking*/
    private float unstickingRate;
    private float unstickingFraction;
    private float gumTextureHeight;
    private float gumTextureWidth;
    private float outlineTextureHeight;

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
        rollCoolDown = 0;
    }

    /**
     * Initializes this RollingEnemyModel from JSON and sets its vision radius.
     *
     * @param directory     The BubblegumBandit asset directory
     * @param x             the x position to set this ProjectileEnemyModel
     * @param y             the y position to set this ProjectileEnemyModel
     * @param constantsJson the constants json
     * @param isFacingRight whether the enemy spawns facing right
     */
    public void initialize(AssetDirectory directory, float x, float y,
                           JsonValue constantsJson, boolean isFacingRight) {
        super.initialize(directory, x, y+.01f, constantsJson, isFacingRight);
        ROLL_SPEED = constantsJson.get("mediumAttack").asInt();
        setName("mediumEnemy");
        attackDuration = 0;
        unstickingFraction = 1;
        unstickingRate = 0.005f;
        gumTextureHeight = gumTexture.getRegionHeight();
        gumTextureWidth = gumTexture.getRegionWidth();
        outlineTextureHeight = outline.getRegionHeight();
    }

    @Override
    /**
     * Updates rolling enemy
     * */
    public void update(float delta) {
        turnCooldown--;
        updateYScale();
        updateRayCasts();
        updateAnimations();
        updateAttackState();
        updateMovement();
        updateUnstick(delta);
        updateFrame();
        updateCrush();
        if (unsticking) {
            unstickingFraction -= unstickingRate;
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
     * Sets this RollingEnemyModel's animation based on its attack
     * state.
     * */
    private void updateAnimations(){
        if (isRolling && !stuck && !gummed){
            animationController.setAnimation("roll", true, false);
        }
        else if (stuck || gummed){
            //TODO: Replace the if() animation to an unsticking one
            if(unsticking) animationController.setAnimation("roll", true, false);
            else animationController.setAnimation("stuck", true, false);
        }
        else{
            animationController.setAnimation("patrol", true, false);
        }
    }

    /**
     * Updates this RollingEnemyModel's attack state.
     * */
    private void updateAttackState(){
        if (fired() && rollCoolDown <= 0) {
            if (isRolling) {
                if (attackDuration == ATTACK_TIME) {
                    isRolling = false;
                    rollCoolDown = COOLDOWN;
                    SoundController.stopSound("rolling");
                } else {
                    attackDuration++;
                }
            } else {
                attackDuration = 0;
                isRolling = true;
                SoundController.playSound("rolling", 0.75f);
            }
        }
        rollCoolDown--;
    }

    /**
     * Moves this RollingEnemyModel based on its attack state.
     * */
    private void updateMovement(){
        boolean movingLeft = (nextAction & CONTROL_MOVE_LEFT) != 0 && (previousAction & CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (nextAction & CONTROL_MOVE_RIGHT) != 0 && (previousAction & CONTROL_MOVE_RIGHT) != 0;

        if (fired() && isRolling && (movingLeft || movingRight) && rollCoolDown <= 0) {
            float speed = 0;
            if (movingLeft) {
                speed = -ROLL_SPEED;
                setFaceRight(false);
            } else {
                speed = ROLL_SPEED;
                setFaceRight(true);
            }
            if (attackDuration < CHARGE_TIME){
                speed = (-speed * .75f);
            }
            setVX(speed);

        } else {
            updateMovement(nextAction);
        }
    }

    /**
     * Updates how long this RollingEnemyModel has been
     * trying to unstick itself. Handles other unstick
     * logic.
     *
     * @param dt Time since last frame
     * */
    private void updateUnstick(float dt){
        if(getStuck() || getGummed()){
            unsticking = true;
            unstickStopWatch += dt;
           // System.out.println(unstickStopWatch);
        }
        else{
            unsticking = false;
            unstickStopWatch = 0;
        }
    }

    /**
     * Returns true if this RollingEnemyModel is stuck and
     * should unstick itself.
     *
     * @return true if this RollingEnemyModel is stuck and
     * should unstick; otherwise, false.
     * */
    public boolean shouldUnstick(){
        return unstickStopWatch >= UNSTICK_TIME;
    }

    /**
     * Resets this RollingEnemyModel's unsticking loop.
     * */
    public void resetUnstick(){
        clearStuckGum();
        unstickStopWatch = 0;
        unsticking = false;
        unstickingFraction = 1;
    }


    /**
     * Passes in an instance of a GumModel that stuck this RollingEnemyModel.
     * Restarts the unsticking process.
     *
     * @param gum The instance of the GumModel that stuck this EnemyModel.
     * */
    @Override
    public void stickWithGum(GumModel gum) {
        super.stickWithGum(gum);
        unstickStopWatch = 0;
    }

    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        if (unsticking && gummed) {
            gumTexture.setRegionHeight((int) (unstickingFraction * gumTextureHeight));
        }
        else if (unsticking && getStuck()) {
            System.out.println("Hi");
            HashSet<GumModel> stuckGum = getStuckGum();
            for(GumModel g : stuckGum){
                g.getTexture().setRegionHeight((int) (unstickingFraction * g.getOutlineHeight()));
            }
        }
    }

    /**
     * Draw method for when highlighting the enemy before unsticking them
     */
    public void drawWithOutline(GameCanvas canvas) {
        super.drawWithOutline(canvas);
        if (unsticking && gummed) {
            outline.setRegionHeight((int) (unstickingFraction * outlineTextureHeight));
        }
        else if (unsticking && getStuck()) {
            System.out.println("Hi");
            HashSet<GumModel> stuckGum = getStuckGum();
            for(GumModel g : stuckGum){
                g.getOutlineTexture().setRegionHeight((int) (unstickingFraction * g.getOutlineHeight()));
            }
        }
    }

    public boolean isUnsticking() {
        return unsticking;
    }

    /**
     * Resets this RollingEnemyModel's attack.
     */
    public void resetAttack() {
        isRolling = false;
        attackDuration = 0;
    }

    public boolean isRolling(){
        return isRolling;
    }
}
