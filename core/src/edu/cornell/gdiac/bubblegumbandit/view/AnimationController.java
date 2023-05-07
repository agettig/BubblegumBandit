package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.FilmStrip;
import java.util.HashMap;

/** An animation controller to animate objects in the game */
public class AnimationController {

  /** The FPS of the looping looping animation */
  private float loopFPS = 8;

  /** The FPS of the looping temporary (non-looping) animation */
  private float tempFPS = 8;

  /** Frame number */
  private int frameNum;

  /** Maps animations to their names */

  private HashMap<String, FilmStrip> animations;

  /** Maps FPS to animation names */
  private HashMap<String, Float> fps;

  /** The looping looping animation */
  private FilmStrip looping;

  /** The looping temporary animation (will usually be null) */
  private FilmStrip temp;

  /** Whether the temporary animation has finished and is ready to be dropped */
  private boolean finished;

  /** The time elapsed since the last animation frame; */
  private float timeSinceLastFrame = 0f;

  /** The name of the looping looping animation */
  private String currentName;

  /** The name of the looping temporary animation */
  private String tempName;

  private TextureRegion lastFrame;


  /**
   * Creates an animation controller
   * @param directory the asset directory
   * @param key the key for the animation controller in animations.json
   */
  public AnimationController(AssetDirectory directory, String key) {
    animations = new HashMap<>();
    fps = new HashMap<>();


    JsonValue controllerJSON = directory.getEntry("animations", JsonValue.class).get(key);

    for(JsonValue value : controllerJSON) {
      String name = value.name();
      FilmStrip strip = directory.getEntry(value.get("strip").asString(), FilmStrip.class);
      strip = strip.copy(); // Each needs own filmstrip to make this not dependent on # enemies
      animations.put(name,strip);
      fps.put(name, value.get("fps").asFloat());
      if(looping ==null) {
        looping = strip;
        currentName = name;
        loopFPS =  value.get("fps").asFloat();
      }
      if(strip==null) System.err.println("ohno "+name);
    }
    finished = true;

  }

  public boolean onLastFrame() {
    FilmStrip strip = temp==null ? looping : temp;
    return strip.getSize() == strip.getFrame()+1;
  }


  /**
   * Returns the looping frame
   */
  public FilmStrip getFrame() {

    timeSinceLastFrame += Gdx.graphics.getDeltaTime();

    if(temp!=null&&finished&&timeSinceLastFrame>=1f/tempFPS) temp = null;

    FilmStrip strip = temp==null ? looping : temp;
    float fps = temp==null ? loopFPS : tempFPS-1;


    if(timeSinceLastFrame<1f/fps) return strip;
    int frame = strip.getFrame();
    frame = (int) ((frame + timeSinceLastFrame/(1f/fps))% strip.getSize());
    frameNum = frame;
    strip.setFrame(frame);
    timeSinceLastFrame = 0;

    if(temp!=null&&temp.getFrame()==temp.getSize()-1) finished = true;

    return strip;

  }

  /**
   * Returns whether the controller has a temporary animation
   */
  public boolean hasTemp() {
    return temp != null;
  }

  /**
   * Returns whether the controller has any animation
   */
  public boolean hasAnimation() {
    return temp!=null&& looping !=null;
  }

  /**
   * Returns the name of the animation playing
   */
  public String getCurrentAnimation() {
    return temp==null ? currentName : tempName;
  }

  /**
   * Returns the index of the frame that is currently playing.
   *
   * @return the index of the frame that is currently playing.
   * */
  public int getFrameNum(){
    return frameNum;
  }


  /**
   * Sets the animation
   * @param name the name of the animation
   * @param loop whether the animation is looping or temporary
   */
  public void setAnimation(String name, boolean loop) {
   if(animations.containsKey(name)&&(currentName!=name)) {
      timeSinceLastFrame = 0f;
      frameNum = 0;
      if(loop) {
        looping = animations.get(name);
        currentName = name;
        loopFPS = fps.get(name);
      } else {
        finished = false;
        temp = animations.get(name);
        tempName = name;
        tempFPS = fps.get(name);
      }
    } else {
      if(!animations.containsKey(name)) System.err.println("Animation "+name+" does not exist in this context.");
    }

  }

  /**
   * Clears all looping animations, looping and not.
   */
  public void clearAnimations() {
    this.looping = null;
    this.currentName = "No animation playing.";
    this.loopFPS = -1;
    this.temp = null;
    this.tempFPS = -1;
    this.tempName = "No animation playing.";


  }


}
