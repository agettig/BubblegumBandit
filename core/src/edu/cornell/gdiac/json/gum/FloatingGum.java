package edu.cornell.gdiac.json.gum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.json.GameCanvas;
import edu.cornell.gdiac.json.Sensor;
import edu.cornell.gdiac.json.Vision;
import edu.cornell.gdiac.physics.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

public class FloatingGum extends WheelObstacle {

    private boolean notTouched;

    public FloatingGum() {
        //TODO: Have radius not be hardcoded, left for now
        super(100);
        notTouched = true;
    }

    public void initialize(AssetDirectory directory, JsonValue json) {
        setName(json.name());
        float[] pos  = json.get("pos").asFloatArray();
        setPosition(pos[0],pos[1]);
        setDensity(json.get("density").asFloat());
        setBodyType(BodyDef.BodyType.StaticBody);

        String key = json.get("texture").asString();
        TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));
        setTexture(texture);
    }
}
