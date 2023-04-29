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


  /** Holds all active effects */
  private Array<Effect> effects;

  /** Holds all effects to be discarded */
  private Array<Effect> trash;

  /** The key for the effect's animation */
  private String animationKey;

  /** The name of the effect's animation */
  private String animationName;

  /** Whether this effect is drawn with it's x origin at it's center */
  private boolean centerX;

  /** Whether this effect is drawn with it's y origin at it's center */
  private boolean centerY;

  /** The time enforced between instantiation of effects in this controller */
  private float delay;

  /** Timer to track the delay */
  private long delayTimer;

  /** The asset directory */
  private AssetDirectory assets;


  /**
   * Creates an effect controller
   * @param animationKey the key for the effect's animation
   * @param animationName the animation's name
   * @param assets the asset directory
   * @param centerX whether the x draw origin is centered
   * @param centerY whether the y draw origin is centered
   * @param delay the delay between instantiations of this effect
   */
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

  /**
   * Discards all effects in this controller
   */
  public void clear() {
    effects.clear();
  }

  /**
   * Adds a new effect to the controller
   * @param x the x position of the effect
   * @param y the f position of the effect
   * @param scale the physics to world scalar
   * @param reflect whether this effect
   *               should be reflected across the x axis
   */
  public void makeEffect(float x, float y, Vector2 scale, boolean reflect) {
    if(TimeUtils.timeSinceMillis(delayTimer)>delay*1000||delay==0) {
      effects.add(new Effect(x, y, scale, reflect, animationName));
      delayTimer = TimeUtils.millis();
    }

  }

/** Draws to the game canvas */
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

/** A single instance of an effect */
  private class Effect {
    /** The effect's animation controller */
    AnimationController ac;
    /** Flag to mark this effect for disposal */
    boolean dispose;
    /** The x pos */
    float x;
    /** The y pos */
    float y;
    /** Whether this effect should be reflected across the x axis */
    boolean reflect;
    /** The physics to world scale */
    Vector2 scale;

    private Effect(float x, float y, Vector2 scale, boolean reflect, String animationName) {
      this.ac = new AnimationController(assets, animationKey);
      ac.setAnimation(animationName, false);
      this.x = x;
      this.y = y;
      this.reflect = reflect;
      this.scale = scale;
      this.dispose = false;

    }

    /** Draws this effect to the game canvas */
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
