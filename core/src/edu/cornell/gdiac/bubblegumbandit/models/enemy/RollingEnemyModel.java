package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

public class RollingEnemyModel extends EnemyModel {
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
    public RollingEnemyModel(World world, int id) {
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
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        return false;
    }
}
