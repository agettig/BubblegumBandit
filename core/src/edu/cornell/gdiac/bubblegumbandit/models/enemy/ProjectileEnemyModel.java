package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.*;

public class ProjectileEnemyModel extends EnemyModel {


    /**Creates a ProjectileEnemy.
     *
     * @param world The box2d world
     * @param id the id of this Enemy
     * */
    public ProjectileEnemyModel(World world, int id){
        super(world, id);
    }


    /**Initializes this ProjectileEnemyModel from JSON and sets its vision radius.
     *
     * @param directory The BubblegumBandit asset directory
     * @param x the x position to set this ProjectileEnemyModel
     * @param y the y position to set this ProjectileEnemyModel
     * @param constantsJson the constants json
     * */
    public void initialize(AssetDirectory directory, float x, float y, JsonValue constantsJson){
        super.initialize(directory, x, y, constantsJson);
        setVision(constantsJson.get("visionradius").asFloat());
    }

    /**
     * Main update loop for a ProjectileEnemyModel.
     *
     * Takes a control code and performs the corresponding action.
     *
     * @param controlCode The code that tells this ProjectileEnemyModel
     *                    what to do.
     * @param dt Time since last frame.
     */
    @Override
    public void update(int controlCode, float dt) {
        super.update(controlCode, dt);

        // Determine how we are moving.
        boolean movingLeft  = (controlCode & CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (controlCode & CONTROL_MOVE_RIGHT) != 0;
        boolean movingUp    = (controlCode & CONTROL_MOVE_UP) != 0;
        boolean movingDown  = (controlCode & CONTROL_MOVE_DOWN) != 0;

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
            if (!isFlipped){
                setVY(4f);
                body.applyForceToCenter(0, 5,true);
            }
            else setVY(0);
            setVX(0);
        } else if (movingDown) {
            if (isFlipped){
                setVY(-4f);
                body.applyForceToCenter(0, -5,true);
            }
            else setVY(0);
            setVX(0);
        } else setVX(0);
    }
}
