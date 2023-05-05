package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.AnimationController;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;

/** A class representing the NPCS which may be freed during larger levels, mostly support for animations */
public class Captive extends Collectible {
  private AnimationController animationController;
  private boolean freed;

  private TextureRegion drawn;



  public Captive() {
    super(2);
  }

  /** The y-scale for the shrinking effect when prisoners are freed. Will add animation before effect */
  private float ys = 1f;


  @Override
  public void initialize(AssetDirectory directory, float x, float y, Vector2 scale, JsonValue json) {
    super.initialize(directory, x, y, scale, json);
    animationController = new AnimationController(directory, "captive");

  }

  @Override
  public void draw(GameCanvas canvas) {

    if (getCollected() && !freed) {
      freed = true;
      animationController.setAnimation("free", false);
    }

    if (freed && animationController.onLastFrame()) {
      //need shrink here
      if (ys > 0) {
          ys-=.1f;
      } else {
        markRemoved(true);
        return;
      }
    } else {
      drawn = animationController.getFrame();
    }

    canvas.drawWithShadow(drawn,
        Color.WHITE,texture.getRegionWidth() / 2f,texture.getRegionHeight() /2f ,
        getX()*drawScale.x-10,getY()*drawScale.y-20,getAngle(),1,ys);
  }


}
