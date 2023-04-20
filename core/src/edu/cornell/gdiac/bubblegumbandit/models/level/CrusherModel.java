package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.helpers.Gummable;
import edu.cornell.gdiac.bubblegumbandit.helpers.Unstickable;
import edu.cornell.gdiac.bubblegumbandit.models.FlippingObject;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.BoxObstacle;
import java.lang.reflect.Field;

/**
 * A class representing a tile on the screen
 **/
public class CrusherModel extends BoxObstacle implements Gummable {

    private FlippingObject flippingObject;

    private TextureRegion gummedTexture;

    private TextureRegion outlineTexture;

    /**
     * Create a new TileModel with degenerate settings
     */
    public CrusherModel() {
        super(0,0,1,1);
        collidedObs = new ObjectSet<>();
    }

    /**
     * Initializes the crusher via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the crusher subtree
     *
     * @param directory the asset directory containing the texture
     * @param scale the scale of the level
     * @param x		the x position of the tile
     * @param y the y position of the tile
     * @param objectJson     the JSON subtree defining the object json
     * @param constants the JSON subtree defining the constants for the object
     */
    public void initialize(AssetDirectory directory, Vector2 scale, float x, float y, JsonValue objectJson, JsonValue constants) {
        setName("crushing_block");
        setPosition(x,y);
        float width = objectJson.getFloat("width") / scale.x;
        float height = objectJson.getFloat("height") / scale.y;
        setDimension(width, height);


        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        setBodyType(constants.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setFixedRotation(true);
        setDensity(constants.get("density").asFloat());
        setFriction(constants.get("friction").asFloat());
        setRestitution(constants.get("restitution").asFloat());
        setGravityScale(constants.get("gravityscale").asFloat());

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
        String key = constants.get("texture").asString();
        TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));
        setTexture(texture);
        setDrawScale(scale);
        flippingObject = new FlippingObject(constants.getFloat("rotaterate"));

        key = constants.get("gummedtexture").asString();
        texture = new TextureRegion(directory.getEntry(key, Texture.class));
        gummedTexture = texture;

        key = constants.get("outlinetexture").asString();
        texture = new TextureRegion(directory.getEntry(key, Texture.class));
        outlineTexture = texture;

    }

    public void update(float dt) {
        flippingObject.updateYScale(isFlipped);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            if (gummed) {
                canvas.drawWithShadow(gummedTexture, Color.WHITE, origin.x, origin.y, getX()*drawScale.x, getY()*drawScale.y, getAngle(), 1, flippingObject.getScale());
            } else {
                canvas.drawWithShadow(texture, Color.WHITE, origin.x, origin.y, getX()*drawScale.x, getY()*drawScale.y, getAngle(), 1, flippingObject.getScale());
            }
        }
    }

    @Override
    public void drawWithOutline(GameCanvas canvas) {
        canvas.draw(outlineTexture, Color.WHITE, origin.x, origin.y, getX()*drawScale.x, getY()*drawScale.y, getAngle(), 1.1f, flippingObject.getScale()*1.1f);
        canvas.drawWithShadow(gummedTexture, Color.WHITE, origin.x, origin.y, getX()*drawScale.x, getY()*drawScale.y, getAngle(), 1, flippingObject.getScale());
    }
}
