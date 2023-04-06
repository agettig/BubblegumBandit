package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.FilmStrip;
import java.util.HashMap;

public class AnimationController {
  int FPS = 8;
  HashMap<String, FilmStrip> animations;
  HashMap<String, Integer> fps;
  FilmStrip current;
  float timeSinceLastFrame = 0f;
  String currentName;



  public AnimationController(AssetDirectory directory, String key) {
    animations = new HashMap<>();
    fps = new HashMap<>();

    JsonValue controllerJSON = directory.getEntry("animations", JsonValue.class).get(key);

    for(JsonValue value : controllerJSON) {
      String name = value.name();
      FilmStrip strip = directory.getEntry(value.get("strip").asString(), FilmStrip.class);
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
    if(timeSinceLastFrame<1f/FPS) return current;
    int frame = current.getFrame();
    frame = (int) ((frame + timeSinceLastFrame/(1f/FPS))% current.getSize());
    current.setFrame(frame);
    timeSinceLastFrame = 0;
    return current;

  }


  public void setAnimation(String name) {
    if(animations.containsKey(name)) {
      current = animations.get(name);
      currentName = name;
      FPS = fps.get(name);
    } else {
      System.err.println("Animation "+name+" does not exist in this context.");
    }
  }


}
