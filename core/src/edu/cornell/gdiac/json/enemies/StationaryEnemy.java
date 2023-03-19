package edu.cornell.gdiac.json.enemies;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.json.controllers.InputController;

public class StationaryEnemy extends Enemy {

    public StationaryEnemy(World world,int i) {
        super(world, i);
    }

    public void initialize(AssetDirectory directory, JsonValue json) {
        super.initialize(directory, json);
    }


    @Override
    public void update(int controlCode) {

        super.update(controlCode);

        switch(controlCode){
            case InputController.CONTROL_MOVE_LEFT:
                break;
            case InputController.CONTROL_MOVE_RIGHT:
                break;
            case InputController.CONTROL_FIRE:
                break;
            case InputController.CONTROL_MOVE_UP: //jump
                break;
            default: break;
        }
    }
}
