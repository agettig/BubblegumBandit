package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.FilmStrip;
import java.util.HashMap;

public class AnimationController {
  int FPS = 8;
  HashMap<String, FilmStrip> animations;
  FilmStrip current;
  float timeSinceLastFrame = 0f;


  public AnimationController(AssetDirectory directory, String key) {
    animations = new HashMap<>();

    JsonValue controllerJSON = directory.getEntry("animations", JsonValue.class).get(key);

    for(JsonValue value : controllerJSON) {
      String name = value.name();
      FilmStrip strip = directory.getEntry(value.asString(), FilmStrip.class);
      animations.put(name,strip);
      if(current==null) current = strip;
    }

  }

  public FilmStrip getFrame() {
    timeSinceLastFrame += Gdx.graphics.getDeltaTime();
    System.out.println(timeSinceLastFrame);
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
    } else {
      System.err.println("Animation "+name+" does not exist in this context.");
    }
  }


}
