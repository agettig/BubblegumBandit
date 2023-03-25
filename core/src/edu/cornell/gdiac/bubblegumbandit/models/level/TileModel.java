package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.BoxObstacle;

import java.lang.reflect.Field;

/**
 * A class representing a tile on the screen
 **/
public class TileModel extends BoxObstacle {
    /** The texture of this tile */
    private TextureRegion tileTexture;

    /**
     * Create a new TileModel with degenerate settings
     */
    public TileModel() {
        super(0,0,1,1);
        tileTexture = null;
    }


    /**
     * Initializes the platform via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the platform subtree
     *
     * @param texture the texture of the tile
     * @param x		the x position of the tile
     * @param y the y position of the tile
     * @param constants     the JSON subtree defining the platform constants
     */
    public void initialize(TextureRegion texture, float x, float y, JsonValue constants) {
        setName("tile");
        setPosition(x,y);
        setDimension(1, 1);

        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        setBodyType(constants.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(constants.get("density").asFloat());
        setFriction(constants.get("friction").asFloat());
        setRestitution(constants.get("restitution").asFloat());

        // Reflection is best way to convert name to color
        Color debugColor;
        try {
            String cname = constants.get("debugcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color)field.get(null));
        } catch (Exception e) {
            debugColor = null; // Not defined
        }
        int opacity = constants.get("debugopacity").asInt();
        debugColor.mul(opacity/255.0f);
        setDebugColor(debugColor);

        setTexture(texture);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            canvas.drawWithShadow(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,
                    getY()*drawScale.y,getAngle(),1,1);
        }
    }
}
