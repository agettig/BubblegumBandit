package edu.cornell.gdiac.json.enemies;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

public class StationaryEnemy extends Enemy {

    public StationaryEnemy(World world) {
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
