package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.helpers.Shield;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;

public class ShieldedRollingEnemyModel extends RollingEnemyModel implements Shield {

    /**
     * is true when the enemies shield is on
     */
    private boolean isShielded;

    /**
     * Creates a ShieldedRollingEnemy.
     *
     * @param world The Box2D world
     * @param id    the id of this Enemy
     */
    public ShieldedRollingEnemyModel(World world, int id) {
        super(world, id);
        isShielded = true;
    }

    public boolean isShielded() {
        return isShielded;
    }

    public void isShielded(boolean value) {
        if (value == false) {
            setUnshieldedTexture();
        }
        else {
            setShieldedTexture();
        }
        isShielded = value;
    }

    public void setShieldedTexture() {
        animationController.setAnimation("patrol");
    }

    public void setUnshieldedTexture() {
        animationController.setAnimation("roll");
    }

    /**
     * Initializes this ShieldedRollingEnemyModel from JSON and sets its vision radius.
     *
     * @param directory     The BubblegumBandit asset directory
     * @param x             the x position to set this ProjectileEnemyModel
     * @param y             the y position to set this ProjectileEnemyModel
     * @param constantsJson the constants json
     */
    public void initialize(AssetDirectory directory, float x, float y,
                           JsonValue constantsJson) {
        super.initialize(directory, x, y, constantsJson);
        setName("shieldedRollingEnemy");
    }
}
