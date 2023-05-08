package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.InputController;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.models.level.ShockModel;

public class ShockEnemyModel extends EnemyModel {

    /** Whether the shock should disappear when the enemy leaves the ground */
    private final boolean SHOCK_DISAPPEARS = false;

    /** The current left shock of the enemy */
    private ShockModel leftShock;

    /** The current right shock of the enemy */
    private ShockModel rightShock;


    public void flipGravity() {
        super.flipGravity();
        if (SHOCK_DISAPPEARS && leftShock != null) {
            leftShock.destroy();
            rightShock.destroy();
        }
    }

    /** Sets the shock of the enemy */
    public void setShock(ShockModel leftShock, ShockModel rightShock) {
        this.leftShock = leftShock;
        this.rightShock = rightShock;
    }

    /**Creates a MovingEnemy.
     *
     * @param world The box2d world
     * @param id the id of this Enemy
     * */
    public ShockEnemyModel(World world, int id){
        super(world, id);
        leftShock = null;
        rightShock = null;
    }

    /**Initializes this MovingEnemy in the game. Sets its vision radius.
     *
     * @param directory The BubblegumBandit asset directory
     * @param x the x position of this enemy
     * @param y the y position of this enemy
     * @param constantsJson the constants json
     * @param isFacingRight whether the enemy spawns facing right
     * */
    public void initialize(AssetDirectory directory, float x, float y, JsonValue constantsJson, boolean isFacingRight){
        super.initialize(directory, x, y, constantsJson, isFacingRight);
        vision.setRadius(constantsJson.get("visionRadius").asFloat());
        attacking.setRadius(constantsJson.get("attackRadius").asFloat());
    }


    public void update(float dt){
        if (leftShock != null && leftShock.isAlive()) {
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
            updateMovement(InputController.CONTROL_NO_ACTION);
        } else {
            super.update(dt);
        }
        if(fired()){
            animationController.setAnimation("fire", false);
            SoundController.playSound("shockAttack", 1);
        }
        else if (stuck || gummed){
            animationController.setAnimation("stuck", true);
        }
        else {
            animationController.setAnimation("patrol", true);
        }
    }
}
