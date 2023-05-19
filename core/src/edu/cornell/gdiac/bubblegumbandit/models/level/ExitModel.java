/*
 * ExitModel.java
 *
 * This is a refactored version of the exit door from Lab 4.  We have made it a specialized
 * class so that we can import its properties from a JSON file.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * JSON version, 3/2/2016
 */

package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import java.lang.reflect.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * A sensor obstacle representing the end of the level
 *
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.
 */
public class ExitModel extends BoxObstacle {

  /** The left-hand side of the door in full */
  private Texture left;

  /** Current crop of the left side of the door */
  private TextureRegion leftCurr;

  /** The right-hand side of the door */
  private Texture right;

  /** Current crop of the right side of the door */
  private TextureRegion rightCurr;

  /** The door frame, always drawn on top */
  private TextureRegion frame;

  /** The 'back' of the door, a textured black rectangle, drawn first */
  private TextureRegion back;

  /** Whether the door is open or not */
  private boolean open;

  /** Whether the door sliding or not */
  private boolean transitioning;

  private int closedMargin = 8;

  /** the x coord of the left door region */
  int lx;
  /** the width of the left door region */
  int lw;
  /** the width of the right door region */
  int rw;

  /**
   * The speed at which the door moves to close, in distance covered by each
   * side per frame
   */
  int speed = 3;

  /** The size an open door starts as during the transition, texture regions cannot have 0 width */
  private int startSize = 1;

  /**
   * Create a new ExitModel with degenerate settings
   */
  public ExitModel() {
    super(0, 0, 1, 1);
    setSensor(true);
    open = true;
    transitioning = false;

  }

  /**
   * Initializes the exit door via the given JSON value
   *
   * The JSON value has been parsed and is part of a bigger level file.  However,
   * this JSON value is limited to the exit subtree
   *
   * @param directory the asset manager
   * @param x         the x position of the exit
   * @param y         the y position of the exit
   * @param constants the JSON subtree defining the exit constants
   */
  public void initialize(AssetDirectory directory, float x, float y, JsonValue constants) {
    setName(constants.name());
    float[] size = constants.get("size").asFloatArray();
    setPosition(x, y);
    setDimension(size[0], size[1]);

    // Technically, we should do error checking here.
    // A JSON field might accidentally be missing
    setBodyType(
        constants.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody :
            BodyDef.BodyType.DynamicBody);
    setDensity(constants.get("density").asFloat());
    setFriction(constants.get("friction").asFloat());
    setRestitution(constants.get("restitution").asFloat());

    // Reflection is best way to convert name to color
    Color debugColor;
    try {
      String cname = constants.get("debugcolor").asString().toUpperCase();
      Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
      debugColor = new Color((Color) field.get(null));
    } catch (Exception e) {
      debugColor = null; // Not defined
    }
    int opacity = constants.get("debugopacity").asInt();
    debugColor.mul(opacity / 255.0f);
    setDebugColor(debugColor);

    // Now get the texture from the AssetManager singleton
    String key = constants.get("frame").asString();
    frame = new TextureRegion(directory.getEntry(key, Texture.class));
    setTexture(frame);

    key = constants.get("back").asString();
    back = new TextureRegion(directory.getEntry(key, Texture.class));

    key = constants.get("left").asString();
    left = directory.getEntry(key, Texture.class);

    key = constants.get("right").asString();
    right = directory.getEntry(key, Texture.class);

    leftCurr = new TextureRegion(left);
    rightCurr = new TextureRegion(right);
    setOpen(false);


  }


  /**
   * Opens and closes the door, this is a view-only process and does not impact any real state
   *
   * @param set whether the door should be open or not
   */
  public void setOpen(boolean set) {
    if (set != this.open) {
      transitioning = true;
      if (set) {
        leftCurr = new TextureRegion(left);
        rightCurr = new TextureRegion(right);
        lx = 0;
        lw = left.getWidth();
        rw = right.getWidth();
        SoundController.playSound("shipDoor", 1);
        SoundController.lastPlayed(-29);
        SoundController.playSound("banditJingle", 0.5f);
      } else {
        lx = left.getWidth() - startSize;
        lw = startSize;
        rw = startSize;
        leftCurr = new TextureRegion(left, left.getWidth() - startSize, 0, startSize,
            this.left.getHeight());
        rightCurr = new TextureRegion(right, 0, 0, startSize, this.left.getHeight());
        SoundController.playSound("shipDoor", 1);
        SoundController.lastPlayed(-29);
      }
    }
    this.open = set;

  }


  @Override
  public void draw(GameCanvas canvas) {
    canvas.draw(back, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.x,
        getAngle(), 1, 1);
    if (!open && !transitioning) {
      canvas.draw(left, Color.WHITE, origin.x, origin.y, getX() * drawScale.x + closedMargin,
          getY() * drawScale.x, getAngle(), 1, 1);
      canvas.draw(right, Color.WHITE, origin.x, origin.y,
          getX() * drawScale.x + frame.getRegionWidth() - right.getWidth() - closedMargin,
          getY() * drawScale.x, getAngle(), 1, 1);
    } else if (transitioning) {
      int dir = open ? -1 : 1;
      lx = lx - dir * speed;
      lw = (lw + dir * speed);

      if (lw >= left.getWidth() - startSize || lw <= startSize) {
        transitioning = false;
      }

      leftCurr.setRegion(lx, 0, lw, left.getHeight());

      rw = (rw + dir * speed);
      if (rw >= right.getWidth() - startSize || rw <= startSize) {
        transitioning = false;
      }

      rightCurr.setRegion(0, 0, rw, right.getHeight());

      canvas.draw(leftCurr, Color.WHITE, origin.x, origin.y,
          getX() * drawScale.x + closedMargin,
          getY() * drawScale.x, getAngle(), 1, 1);

      canvas.draw(rightCurr, Color.WHITE, origin.x, origin.y,
          getX() * drawScale.x + frame.getRegionWidth() - rightCurr.getRegionWidth() - closedMargin,
          getY() * drawScale.x, getAngle(), 1, 1);

    }
    canvas.draw(frame, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.x,
        getAngle(), 1, 1);


  }
}
