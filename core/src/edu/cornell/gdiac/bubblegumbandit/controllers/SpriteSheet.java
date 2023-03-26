package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import java.util.HashMap;

//my solution to film strip
/**
 * An Animation Controller to store and control the animations for a single object.
 */
public class SpriteSheet {

  /** The width of an animation frame on the sprite sheet */
  private int width;

  /** The height of an animation frame on the sprite sheet */
  private int height;

  /** The full sprite sheet */
  private Texture spriteSheet;

  /** The region representing the current sprite/animation frame */
  private TextureRegion frame;

  /** Stores every animation for access by name during state changes in parent objects */
  private HashMap<String, Animation> animations;

  /** The current animation */
  private Animation current;

  /** The draw origin for this sprite sheet */
  private Vector2 origin;

  /** The FPS of every animation in this controller
   * Open to changing to be more adaptable later.
   * */
  private int FPS;

  /** The time since the last change in frame */
  private float timeSinceLast = 0;

  /**
   * Creates an Animator Controller from a sprite sheet
   * @param directory
   */
  public SpriteSheet(AssetDirectory directory) { //may need to move some of this logic to a loader
    // and parser..

    //setting default values
    frame.setRegionHeight(height);
    frame.setRegionWidth(width);
    JsonValue spriteSheetJSON = directory.getEntry("animations", JsonValue.class);
    String key = spriteSheetJSON.get("sheet").asString();
    this.spriteSheet = directory.getEntry(key, Texture.class);
    switch (spriteSheetJSON.get("origin").asString()) {
      case "left":
        origin = new Vector2(0, height/2);
        break;
      case "right":
        origin = new Vector2(width, height/2);
        break;
      case "bottom":
        origin = new Vector2(width/2, height);
        break;
      case "top" :
        origin = new Vector2(width/2, 0);
        break;
      case "bottom left":
        origin = new Vector2(0, height);
        break;
      case "bottom right":
        origin = new Vector2(width, height);
        break;
      case "top left":
        origin = Vector2.Zero;
        break;
      case "top right":
        origin = new Vector2(width, 0);
        break;
      case "center":
        origin = new Vector2(width/2, height/2);
      default:
        System.err.println("No valid animation origin for sprite sheet "+spriteSheetJSON.name());



    }
    this.FPS = spriteSheetJSON.get("fps").asInt();

    //creating animations
    JsonValue animationsJSON = spriteSheetJSON.get("animations");
    int index = 0;
    JsonValue currJSON;
    while((currJSON = animationsJSON.get(index))!=null) {
      String name = currJSON.name(); //unsure if this works for single values...
      int frames = currJSON.asInt();
      Animation animation = new Animation(frames, index, name);
      animations.put(name, animation);
    }

  }

  /**
   * @return the name of the current animation
   */
  public String getAnimation() {
    return current.name;
  }

  /**
   * Sets the current animation to a new animation
   * @param name the name of the new animation
   */
  public void setAnimation(String name) {
    if (animations.containsKey(name)) current = animations.get(name);
    current.frameNum = 0;
  }

  /**
   * Sets the controller's FPS
   * @param fps the new FPS
   */
  public void setFPS(int fps) {
    FPS = fps;
  }

  /**
   * @return the draw origin for the sprite sheet
   */
  public Vector2 getOrigin() {
    return origin;
  }

  /**
   * @return the current frame, should be called once per draw update.
   * Note: the frame is not necessarily altered by calling this method
   * if too little time has passed since the last call.
   * Likewise, some calls will move the frame along multiple steps
   * if the frame rate of the overall game is slow.
   */
  public TextureRegion getFrame() {
    current.setNextFrame();
    return frame;
  }

  /**
   * Private class for storing information about a single animation
   */
  private class Animation {
    int frameCount;
    int frameNum;
    /** the animation ID represents which animation it is when the animations on the sprite
     * sheet are numbered top to bottom, starting from 0. Animations should be specified in the
     * JSON in this order.
     */
    int animationId;
    String name;

    Animation(int frameCount, int animationId, String name) {
      this.frameCount = frameCount;
      this.animationId = animationId;
      this.name = name;
    }

    void setNextFrame() {
      timeSinceLast += Gdx.graphics.getDeltaTime(); //to be independent of frame rate..
      if(timeSinceLast<1/FPS) return;
      frameNum = (frameNum + (int) timeSinceLast*FPS)%frameCount; //hmm
      frame.setRegionX(width*frameNum);
      frame.setRegionY(spriteSheet.getHeight()-animationId*height);
      timeSinceLast = 0;
    }

  }
}
