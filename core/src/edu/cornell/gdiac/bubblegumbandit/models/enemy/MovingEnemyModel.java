package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.*;
import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.CONTROL_MOVE_DOWN;

public class MovingEnemyModel extends EnemyModel {
    /**Vision radius of a MovingEnemy. */
    private float visionRadius;

    /**Damage from bumping into a moving robot*/
    private float damage = 1;

    /**Sets the vision radius of a MovingEnemyModel.
     *
     * @param visionRadius The new vision radius */
    public void setVisionRadius(float visionRadius) {
        this.visionRadius = visionRadius;
    }

    /**Sets the damage of a MovingEnemyModel.
     *
     * @param damage The new damage */
    public void setDamage(float damage) {
        this.damage = damage;
    }

    /**Returns the damage of a RollingEnemyModel.
     */
    public float getDamage() {return damage; }

    /**
     * Creates a LaserEnemy.
     *
     * @param world The Box2D world
     * @param id    the id of this Enemy
     */
    public MovingEnemyModel(World world, int id) {
        super(world, id);
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
        super.initialize(directory, x, y, constantsJson);
        setName("rollingrobot");
        //setDamage(constantsJson.get("damage").asFloat());
        setVisionRadius(constantsJson.get("visionradius").asFloat());
    }

    /**
     * Main update loop for a RollingEnemyModel. Takes a control code
     * and performs the corresponding action.
     *
     * @param controlCode The code that tells this RollingEnemyModel
     *                    what to do.
     * @param dt          Time since last frame.
     */
    @Override
    public void update(int controlCode, float dt) {
        super.updateMovement(controlCode);

        // Determine how we are moving.
        boolean movingLeft = (controlCode & CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (controlCode & CONTROL_MOVE_RIGHT) != 0;
        boolean movingUp = (controlCode & CONTROL_MOVE_UP) != 0;
        boolean movingDown = (controlCode & CONTROL_MOVE_DOWN) != 0;

        // Process movement command.
        if (movingLeft) {
            setVX(-4f);
            setVY(0);
            setFaceRight(false);
        } else if (movingRight) {
            setVX(4f);
            setVY(0);
            setFaceRight(true);
        } else if (movingUp) {
            if (!isFlipped) {
                setVY(4f);
                body.applyForceToCenter(0, 5, true);
            } else setVY(0);
            setVX(0);
        } else if (movingDown) {
            if (isFlipped) {
                setVY(-4f);
                body.applyForceToCenter(0, -5, true);
            } else setVY(0);
            setVX(0);
        } else setVX(0);
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        return false;
    }
}
