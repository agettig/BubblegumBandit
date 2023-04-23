package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;

import edu.cornell.gdiac.assets.AssetDirectory;
import java.lang.reflect.Field;

public class DoorModel extends TileModel {

    /** Whether this is a horizontal camera tile or a vertical camera tile. */
    private boolean isHorizontal;

    /** Upper left point of the camera after exiting top / left of camera tile. */
    private Vector2 firstUpperLeft;

    /** Lower right point of the camera after exiting top / left of camera tile. */
    private Vector2 firstLowerRight;

    /** Upper left point of the camera after exiting bottom / right of camera tile. */
    private Vector2 secondUpperLeft;

    /** Lower right point of the camera after exiting bottom / right of camera tile. */
    private Vector2 secondLowerRight;

    /** Whether the camera tile first mode fixes the x axis */
    private boolean isFirstFixedX;

    /** Whether the camera tile first mode fixes the y axis */
    private boolean isFirstFixedY;

    /** Whether the camera tile second mode fixes the x axis */
    private boolean isSecondFixedX;

    /** Whether the camera tile second mode the y axis */
    private boolean isSecondFixedY;

    /** Gets the value of isHorizontal */
    public boolean isHorizontal() {
        return isHorizontal;
    }

    /** Gets the value of isFirstFixedX */
    public boolean isFirstFixedX() {
        return isFirstFixedX;
    }

    /** Gets the value of isFirstFixedY */
    public boolean isFirstFixedY() {
        return isFirstFixedY;
    }

    /** Gets the value of isSecondFixedX */
    public boolean isSecondFixedX() {
        return isSecondFixedX;
    }

    /** Gets the value of isSecondFixedY */
    public boolean isSecondFixedY() {
        return isSecondFixedY;
    }

    /** Gets the value of firstUpperLeft */
    public Vector2 getFirstUpperLeft() {
        return firstUpperLeft;
    }

    /** Gets the value of firstLowerRight */
    public Vector2 getFirstLowerRight() {
        return firstLowerRight;
    }

    /** Gets the value of secondUpperLeft */
    public Vector2 getSecondUpperLeft() {
        return secondUpperLeft;
    }

    /** Gets the value of secondLowerRight */
    public Vector2 getSecondLowerRight() {
        return secondLowerRight;
    }

    /** Constructs a new DoorModel
     * Uses generic values to start
     */
    public DoorModel() {
        super();
        firstUpperLeft = new Vector2();
        firstLowerRight = new Vector2();
        secondUpperLeft = new Vector2();
        secondLowerRight = new Vector2();
    }

    /** Initializes the camera tile model.
     *
     * @param directory the asset directory of the level
     * @param x the x position of the camera tile
     * @param y the y position of the camera tile
     * @param scale the scale of the level
     * @param levelHeight the height of the level
     * @param objectJson the json value representing the camera tile
     * @param constants the json value representing the constants of the camera tile
     */
    public void initialize(AssetDirectory directory, float x, float y, Vector2 scale, float levelHeight, JsonValue objectJson, JsonValue constants, boolean isHorizontal) {
        // make the body fixture into a sensor
        setName("door");

        setPosition(x,y);
        float width = objectJson.getFloat("width") / scale.x;
        float height = objectJson.getFloat("height") / scale.y;
        setDimension(width, height);
        setSensor(true);
        this.isHorizontal = isHorizontal;

        setBodyType(BodyDef.BodyType.StaticBody);

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

        // Set object data
        isFirstFixedX = false;
        isFirstFixedY = false;
        isSecondFixedX = false;
        isSecondFixedY = false;

        // I'm sorry about this.
        JsonValue property = objectJson.get("properties").child();
        while (property != null) {
            String name = property.getString("name");
            float value = property.getFloat("value");
            if (value != -1) {
                float yValue = levelHeight - value - 1;
                switch (name) {
                    case "leftx1":
                        firstUpperLeft.x = value;
                        isFirstFixedX = true;
                        break;
                    case "lefty1":
                        firstUpperLeft.y = yValue + 1;
                        isFirstFixedY = true;
                        break;
                    case "leftx2":
                        firstLowerRight = new Vector2();
                        firstLowerRight.x = value + 1f;
                        break;
                    case "lefty2":
                        firstLowerRight.y = yValue;
                        break;
                    case "rightx1":
                        secondUpperLeft = new Vector2();
                        secondUpperLeft.x = value;
                        isSecondFixedX = true;
                        break;
                    case "righty1":
                        secondUpperLeft.y = yValue + 1;
                        isSecondFixedY = true;
                        break;
                    case "rightx2":
                        secondLowerRight = new Vector2();
                        secondLowerRight.x = value + 1f;
                        break;
                    case "righty2":
                        secondLowerRight.y = yValue;
                        break;
                    default:
                        throw new UnsupportedOperationException(name + " is not a valid property for a door");
                }
            }
            property = property.next();
        }
        setDrawScale(scale);
    }
}
