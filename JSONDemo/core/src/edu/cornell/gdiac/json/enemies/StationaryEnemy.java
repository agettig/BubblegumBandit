package edu.cornell.gdiac.json.enemies;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.assets.JsonValueLoader;

public class StationaryEnemy extends Enemy{

    public StationaryEnemy(){
        super(0,0,1,1);
    }

    public void initialize(AssetDirectory directory, JsonValue json){
        super.initialize(directory, json);

    }

    @Override
    public void applyForce() {

    }

    @Override
    public void update() {

    }
}
