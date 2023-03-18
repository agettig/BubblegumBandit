package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

public class StationaryEnemyModel extends EnemyModel {

    public StationaryEnemyModel(World world) {
        super(world);
    }

    public void initialize(AssetDirectory directory, JsonValue json) {
        super.initialize(directory, json);
    }

    @Override
    public void applyForce() {

    }

    @Override
    public void update() {
        super.update();
    }
}
