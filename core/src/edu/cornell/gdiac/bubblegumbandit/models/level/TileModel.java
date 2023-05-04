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
    /** Whether the tile has an open corner */
    private boolean hasCorner;
    /** Whether the tile has a corner in the top right */
    private boolean topRight;
    /** Whether the tile has a corner in the top left */
    private boolean topLeft;
    /** Whether the tile has a corner in the bottom right */
    private boolean bottomRight;
    /** Whether the tile has a corner in the bottom left */
    private boolean bottomLeft;

    /**
     * Create a new TileModel with degenerate settings
     */
    public TileModel() {
        super(0,0,1,1);
        hasCorner = false;
        topRight = false;
        topLeft = false;
        bottomRight = false;
        bottomLeft = false;
    }

    /**
     * Getters and setters for tile corners
     * @return
     */
    public boolean hasCorner() {return hasCorner;}
    public void hasCorner(boolean value) {hasCorner = value;}
    public boolean topRight() {return topRight;}
    public void topRight(boolean value) {topRight = value;}
    public boolean topLeft() {return topLeft;}
    public void topLeft(boolean value) {topLeft = value;}
    public boolean bottomLeft() {return bottomLeft;}
    public void bottomLeft(boolean value) {bottomLeft = value;}
    public boolean bottomRight() {return bottomRight;}
    public void bottomRight(boolean value) {bottomRight = value;}

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
            canvas.drawWithShadow(texture, Color.WHITE, origin.x, origin.y, getX()*drawScale.x, getY()*drawScale.y, getAngle(), 1, 1);
        }
    }
}
