package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import java.lang.reflect.Field;

/**
 * A class representing a glass tile on the screen
 **/
public class SpecialTileModel extends TileModel {

    public void initialize(AssetDirectory directory, float x, float y, JsonValue constants, String name) {
        setName(name);

        setPosition(x,y);
        setDimension(1, 1);
        setSensor(false);

        setBodyType(constants.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(constants.get("density").asFloat());
        setFriction(constants.get("friction").asFloat());
        setRestitution(constants.get("restitution").asFloat());

        // Reflection is best way to convert name to color
        Color debugColor;
        int opacity = constants.get("debugopacity").asInt();
        try {
            String cname = constants.get("debugcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color)field.get(null));
            debugColor.mul(opacity/255.0f);
            setDebugColor(debugColor);
        } catch (Exception ignored) {
        }

        String key = constants.get("texture").asString();
        TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));
        setTexture(texture);
    }

    public void draw(GameCanvas canvas) {
        canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX()*drawScale.x, getY()*drawScale.y, getAngle(), 1, 1);
    }

}