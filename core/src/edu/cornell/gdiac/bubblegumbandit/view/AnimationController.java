package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.FilmStrip;
import java.util.HashMap;

public class AnimationController {
  int FPS = 8;
  int tempFPS = 8;
  HashMap<String, FilmStrip> animations;
  HashMap<String, Integer> fps;
  FilmStrip current;
  FilmStrip temp;
  boolean finished;
  float timeSinceLastFrame = 0f;
  String currentName;
  String tempName;



  public AnimationController(AssetDirectory directory, String key) {
    animations = new HashMap<>();
    fps = new HashMap<>();

    System.out.println(key);
    JsonValue controllerJSON = directory.getEntry("animations", JsonValue.class).get(key);

    for(JsonValue value : controllerJSON) {
      String name = value.name();
      FilmStrip strip = directory.getEntry(value.get("strip").asString(), FilmStrip.class);
      strip = strip.copy(); // Each needs own filmstrip to make this not dependent on # enemies
      animations.put(name,strip);
      fps.put(name, value.get("fps").asInt());
      if(current==null) {
        current = strip;
        currentName = name;
        FPS =  value.get("fps").asInt();
      }
      if(strip==null) System.err.println("ohno "+name);
    }

  }


  public FilmStrip getFrame() {
    timeSinceLastFrame += Gdx.graphics.getDeltaTime();

    if(finished) {
      if (timeSinceLastFrame >= 1f / tempFPS) {
        finished = false;
        temp = null;
        timeSinceLastFrame = 0;
      } else {
        return temp;
      }

    }

    FilmStrip strip = temp==null ? current : temp;
    int fps = temp==null ? FPS : tempFPS-1;


    if(timeSinceLastFrame<1f/fps) return strip;
    int frame = strip.getFrame();
    frame = (int) ((frame + timeSinceLastFrame/(1f/fps))% strip.getSize());
    strip.setFrame(frame);
    timeSinceLastFrame = 0;

    if(temp!=null&&temp.getFrame()==temp.getSize()-1) {
      finished = true; //discard after one loop

    }

    return strip;

  }

  public boolean hasTemp() {
    return temp != null;
  }

  public boolean hasAnimation() {
    return temp!=null&&current!=null;
  }

  public String getCurrentAnimation() {
    return temp==null ? currentName : tempName;
  }



  public void setAnimation(String name, boolean loop) {
    if(animations.containsKey(name)&&(currentName!=name)) {
      timeSinceLastFrame = 0f;
      if(loop) {
        current = animations.get(name);
        currentName = name;
        FPS = fps.get(name);
      } else {
        temp = animations.get(name);
        tempName = name;
        tempFPS = fps.get(name);
      }
    } else {
      if(!animations.containsKey(name)) System.err.println("Animation "+name+" does not exist in this context.");
    }
  }

  public void clearAnimations() {
    this.current = null;
    this.currentName = "No animation playing.";
    this.FPS = -1;
    this.temp = null;
    this.tempFPS = -1;
    this.tempName = "No animation playing.";
  }


}
