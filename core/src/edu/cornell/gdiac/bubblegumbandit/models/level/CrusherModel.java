package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController;
import edu.cornell.gdiac.bubblegumbandit.helpers.Gummable;
import edu.cornell.gdiac.bubblegumbandit.helpers.Unstickable;
import edu.cornell.gdiac.bubblegumbandit.models.FlippingObject;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.BoxObstacle;
import edu.cornell.gdiac.physics.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import java.lang.reflect.Field;

/**
 * A class representing the crushing block on the screen
 **/
public class CrusherModel extends CapsuleObstacle implements Gummable{

    public static final float traumaAmt = 0.5f;

    /** Used to handle flipping logic */
    private FlippingObject flippingObject;

    private TextureRegion gummedTexture;

    private TextureRegion outlineTexture;

    /** Bottom sensor to detect bottom crushes */
    private Fixture bottomSensorFixture;
    private PolygonShape bottomSensorShape;
    private String bottomSensorName;

    private Color sensorColor;

    /** Top sensor to detect bottom crushes */
    private Fixture topSensorFixture;
    private PolygonShape topSensorShape;
    private String topSensorName;

    /** The velocity with the maximum magnitude during a given fall */
    public float maxAbsFallVel;

    public boolean didSmash;

    /** Box obstacle for player collisions */
    private Fixture boxFixture;
    private PolygonShape boxShape;

    /** Sets the category of capsule object and sensors to capsuleCategory,
     * category of box object to boxCategory,
     * mask of capsule object to capsuleMask,
     * mask of box object to boxMask,
     * and mask of sensor object to sensorMask */
    public void setFixtureMasks(short capsuleCategory, short boxCategory, short capsuleMask, short boxMask, short sensorMask) {
        setFilter(capsuleCategory, capsuleMask);

        Filter f;

        f = topSensorFixture.getFilterData();
        f.maskBits = sensorMask;
        topSensorFixture.setFilterData(f);
        bottomSensorFixture.setFilterData(f);

        f = boxFixture.getFilterData();
        f.categoryBits = boxCategory;
        f.maskBits = boxMask;
        boxFixture.setFilterData(f);
    }

    /**
     * Create a new TileModel with degenerate settings
     */
    public CrusherModel() {
        super(0,0,4,2);
        collidedObs = new ObjectSet<>();
        maxAbsFallVel = 0;
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

        setName("crushingBlock");
        setPosition(x,y);
        float width = objectJson.getFloat("width") * .98f / scale.x; // Make it a little smaller so it can slot in 4 block gaps
        float height = objectJson.getFloat("height") * .98f / scale.y;
        setDimension(width, height);


        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        setBodyType(BodyType.DynamicBody);
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

        // Initialize the sensors used to detect when things are being crushed.
        // Get the sensor information
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        float[] sSize = constants.get("sensorsize").asFloatArray();
        bottomSensorShape = new PolygonShape();
        bottomSensorShape.setAsBox(sSize[0], sSize[1], sensorCenter, 0.0f);

        // Reflection is best way to convert name to color
        try {
            String cname = constants.get("sensorcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            sensorColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            sensorColor = null; // Not defined
        }
        opacity = constants.get("sensoropacity").asInt();
        sensorColor.mul(opacity / 255.0f);
        bottomSensorName = constants.get("bottomsensorname").asString();

        sensorCenter = new Vector2(0, getHeight() / 2);
        topSensorShape = new PolygonShape();
        topSensorShape.setAsBox(sSize[0], sSize[1], sensorCenter, 0.0f);

        // Reflection is best way to convert name to color
        topSensorName = constants.get("topsensorname").asString();
        boxShape = new PolygonShape();
        boxShape.setAsBox(objectJson.getFloat("width") / (scale.x * 2f), objectJson.getFloat("height") / (scale.y * 2f));
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = getDensity();
        sensorDef.isSensor = true;
        sensorDef.shape = bottomSensorShape;
        bottomSensorFixture = body.createFixture(sensorDef);
        bottomSensorFixture.setUserData(this);

        sensorDef.shape = topSensorShape;
        topSensorFixture = body.createFixture(sensorDef);
        topSensorFixture.setUserData(this);

        sensorDef.shape = boxShape;
        sensorDef.isSensor = false;
        boxFixture = body.createFixture(sensorDef);
        boxFixture.setDensity(getDensity());
        setDensity(getDensity() * 0.5f);
        boxFixture.setUserData(this);
        return true;
    }

    public void update(float dt) {
        flippingObject.updateYScale(isFlipped);
        if (getVY() > maxAbsFallVel && getVY() > 0) {
            maxAbsFallVel = getVY();
        } else if (getVY() < maxAbsFallVel && getVY() < 0) {
            maxAbsFallVel = getVY();
        }
        if (Math.abs(getVY()) > 0.5f) {
            didSmash = false;
        }
        if (Math.abs(getVY()) > 5f) {
            setVX(0);
        }
    }

    @Override
    public void flipGravity() {
        if (!gummed && !stuck) {
            isFlipped = !isFlipped;
            didSmash = false;
        }

    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            canvas.drawWithShadow(texture, Color.WHITE, origin.x, origin.y, getX()*drawScale.x, getY()*drawScale.y, getAngle(), 1, flippingObject.getScale());
            if(gummed)  canvas.draw(gummedTexture, Color.WHITE, 0f, 0f, getX()*drawScale.x-gummedTexture.getRegionWidth()/2f, getY()*drawScale.y-texture.getRegionHeight()*(3/4f)*flippingObject.getScale(), getAngle(), 1, flippingObject.getScale());

        }
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
//        Vector2 bottomSensorPos = bottomSensorFixture.getBody().getPosition();
//        Vector2 topSensorPos = topSensorFixture.getBody().getPosition();
//        canvas.drawPhysics(bottomSensorShape, sensorColor, bottomSensorPos.x * drawScale.x, bottomSensorPos.y * drawScale.y);
//        canvas.drawPhysics(topSensorShape, sensorColor, topSensorPos.x * drawScale.x, topSensorPos.y * drawScale.y);
    }


    public void drawWithOutline(GameCanvas canvas) {
        canvas.drawWithShadow(texture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x,
            getY() * drawScale.y, getAngle(), 1, flippingObject.getScale());
        canvas.draw(outlineTexture, Color.WHITE, 0f, 0f,
            getX() * drawScale.x -5 -gummedTexture.getRegionWidth() / 2f,
            getY() * drawScale.y - (texture.getRegionHeight()*(3/4f)+5) * flippingObject.getScale(),
            getAngle(), 1, flippingObject.getScale());
    }
}
