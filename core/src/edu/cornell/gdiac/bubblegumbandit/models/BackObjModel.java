package edu.cornell.gdiac.bubblegumbandit.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.BoxObstacle;
import edu.cornell.gdiac.physics.obstacle.PolygonObstacle;

import java.lang.reflect.Field;

/**
 * An obstacle representing non-interactable elements of the background that are affected by changes in gravity
 */
public class BackObjModel extends BoxObstacle {

    /**
     * Which direction is the object facing
     */
    private boolean faceRight;

    /**
     * Whether this object is flipped
     */
    protected boolean isFlipped;

    /** Manager for the scale for flipping during gravity swaps */
    private FlippingObject fo = new FlippingObject();

    public BackObjModel(){
        super (0, 0, 1, 1);
        isFlipped = false;
    }

    /**
     * Initializes the dude via the given JSON value
     * <p>
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the dude subtree
     *
     * @param directory the asset manager
     * @param json      the JSON subtree defining the dude
     */
    public void initialize(AssetDirectory directory, JsonValue json, JsonValue info) {
        setName(json.get("name").asString());

        //physics information
        setBodyType(BodyDef.BodyType.DynamicBody);
        setDensity(json.get("density").asFloat());
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());

        setDebug(json);

        //unique information
        String key = info.get("texture").asString();
        TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));

        int[] p = info.get("pos").asIntArray();
        setPosition(p[0] ,p[1]);
        setTexture(texture);

        setWidth(texture.getRegionWidth()/64f);
        setHeight(texture.getRegionHeight()/64f);

        faceRight = info.get("faceRight").asBoolean();
    }

    private void setDebug(JsonValue json){
        // Reflection is best way to convert name to color
        Color debugColor;
        try {
            String cname = json.get("debugcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            debugColor = null; // Not defined
        }
        int opacity = json.get("debugopacity").asInt();
        assert debugColor != null;
        debugColor.mul(opacity / 255.0f);
        setDebugColor(debugColor);
    }

    public void update(float dt) {
//        if (yScale < 1f && !isFlipped) {
//            yScale += 0.1f;
//        } else if (yScale > -1f && isFlipped) {
//            yScale -= 0.1f;
//        }
        fo.updateYScale(isFlipped);

    }

    /**
     * Set the objects flipped state after world gravity is flipped
     */
    public void flip() {
        isFlipped = !isFlipped;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            float direction = faceRight ? 1.0f : -1.0f;
            canvas.drawWithShadow(texture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x,
                    getY() * drawScale.y, getAngle() , direction, fo.getYScale());
        }
    }
}
