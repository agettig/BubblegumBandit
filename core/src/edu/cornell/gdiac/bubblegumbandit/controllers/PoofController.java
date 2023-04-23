package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.AnimationController;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;

public class PoofController {

  //constructor takes in animation from json
  //methods to make new poofs, draw poofs
  //poofs each have an animation controller
  //internal class poof
  //poofs instantiated at locations in world, with y scale


  private Array<Poof> poofs;
  private Array<Poof> trash;
  private String animationKey;
  private AssetDirectory assets;


  public PoofController (String animationKey, AssetDirectory assets) {
    this.animationKey = animationKey;
    this.assets = assets;
    trash = new Array<>();
    poofs = new Array<>();


  }

  public void clear() {
    poofs.clear();
  }

  public void makePoof(float x, float y, Vector2 scale, boolean reflect) {
    poofs.add(new Poof(x, y, scale, reflect));
  }


  public void draw(GameCanvas canvas) {
    if(poofs.size==0) return;
    for(int i = 0; i<poofs.size; i++) {
      if(poofs.get(i).dispose) trash.add(poofs.get(i));
    }
    for(Poof poof: trash) {
      poofs.removeValue(poof, false);
    }


    for(Poof poof: poofs) {
      poof.draw(canvas);
    }

  }


  private class Poof {
    AnimationController ac;
    boolean dispose;
    float x;
    float y;
    boolean reflect;
    Vector2 scale;

    private Poof(float x, float y, Vector2 scale, boolean reflect) {
      this.ac = new AnimationController(assets, animationKey);
      ac.setAnimation("poof", false);
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
      canvas.draw(region, Color.WHITE, region.getRegionWidth()/2f,
          0, x*scale.x, y*scale.y, 0, 1, (reflect? -1 : 1));

    }

  }

}
