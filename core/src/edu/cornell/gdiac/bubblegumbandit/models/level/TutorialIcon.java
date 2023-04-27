package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.helpers.SaveData;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;

public class TutorialIcon {
  private TextureRegion icon;
  private int keyCode;
  private float x;
  private float y;
  private static float margin = 10f;
  protected BitmapFont font;
  private Vector2 scale;


  public TutorialIcon(AssetDirectory directory, float x, float y, int keyCode, Vector2 scale) {
    this.font = directory.getEntry("projectSpace", BitmapFont.class);
    this.x = x;
    this.y = y;
    this.keyCode = keyCode;
    this.icon =getTexture(directory);
    this.scale = scale;

  }


  private TextureRegion getTexture(AssetDirectory directory) {
    switch(keyCode) {
      case 0: {
        return new TextureRegion(directory.getEntry("leftIcon", Texture.class));
      }
      case 1: {
        return new TextureRegion(directory.getEntry("rightIcon", Texture.class));
      }
      case 2: {
        return new TextureRegion(directory.getEntry("upIcon", Texture.class));
      }
      case 3: {
        return new TextureRegion(directory.getEntry("downIcon", Texture.class));
      }
      case 4: {
        return new TextureRegion(directory.getEntry("mapIcon", Texture.class));
      }
      case 5: {
        return new TextureRegion(directory.getEntry("reloadIcon", Texture.class));
      }
      case 6: {
        return new TextureRegion(directory.getEntry("shootIcon", Texture.class));
      }
      case 7: {
        return new TextureRegion(directory.getEntry("unstickIcon", Texture.class));
      }
      default : {
        System.err.println("No key associated with keyCode "+keyCode);
        return null;
      }


    }
  }

  private String getKeyText() {
    int key = SaveData.getKeyBindings()[keyCode];
    String keyText =  Input.Keys.toString(key);
    if(keyCode==6||keyCode==7) return key == Input.Buttons.LEFT ? "LEFT CLICK" : "RIGHT CLICK";
    return keyText;
  }

  public void draw(GameCanvas canvas) {
    canvas.draw(icon, x* scale.x, y*scale.y);
    canvas.drawText(getKeyText(), font, x*scale.x, y*scale.y-margin);

  }


}
