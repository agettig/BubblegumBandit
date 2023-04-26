package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.AnimationController;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;

public class EffectController {



  //constructor takes in animation from json
  //methods to make new effects, draw effects
  //effects each have an animation controller
  //internal class poof
  //effects instantiated at locations in world, with y scale


  private Array<Effect> effects;
  private Array<Effect> trash;
  private String animationKey;
  private AssetDirectory assets;
  private String animationName;
  private boolean centerX;
  private boolean centerY;
  private float delay;
  private long delayTimer;



  public EffectController (String animationKey, String animationName,AssetDirectory assets,
                           boolean centerX, boolean centerY, float delay) {
    this.animationKey = animationKey;
    this.assets = assets;
    trash = new Array<>();
    effects = new Array<>();
    this.animationName = animationName;
    this.centerY = centerY;
    this.centerX = centerX;
    this.delay = delay;
    delayTimer = (long) (delay*1000+1);


  }

  public void clear() {
    effects.clear();
  }

  public void makeEffect(float x, float y, Vector2 scale, boolean reflect) {

    if(TimeUtils.timeSinceMillis(delayTimer)>delay*1000||delay==0) {
      effects.add(new Effect(x, y, scale, reflect, animationName));
      delayTimer = TimeUtils.millis();
    }

  }


  public void draw(GameCanvas canvas) {
    if(effects.size==0) return;
    for(int i = 0; i< effects.size; i++) {
      if(effects.get(i).dispose) trash.add(effects.get(i));
    }
    for(Effect effect: trash) {
      effects.removeValue(effect, false);
    }


    for(Effect effect: effects) {
      effect.draw(canvas);
    }

  }


  private class Effect {
    AnimationController ac;
    boolean dispose;
    float x;
    float y;
    boolean reflect;
    Vector2 scale;
    boolean centerx;

    private Effect(float x, float y, Vector2 scale, boolean reflect, String animationName) {
      this.ac = new AnimationController(assets, animationKey);
      ac.setAnimation(animationName, false);
      this.x = x;
      this.y = y;
      this.reflect = reflect;
      this.scale = scale;
      this.dispose = false;

    }
    private void draw(GameCanvas canvas) {
      if(!ac.hasTemp()) {
        this.dispose = true;
        return;
      }
      TextureRegion region = ac.getFrame();
      float ox = centerX? region.getRegionWidth()/2f: 0;
      float oy =  centerY? region.getRegionHeight()/2f: 0;
      canvas.draw(region, Color.WHITE, ox,
          oy, x*scale.x, y*scale.y, 0, 1, (reflect? -1 : 1));

    }

  }

}
