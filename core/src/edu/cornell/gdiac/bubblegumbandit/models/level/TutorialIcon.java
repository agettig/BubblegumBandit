package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
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
  private float textOffset = 4f;
  private Color bubblePink = new Color(246f/255f, 148f/255f, 139f/255f, 1);
  private String description;
  protected BitmapFont fontSmall;


  public TutorialIcon(AssetDirectory directory, float x, float y,
                      int keyCode, Vector2 scale) {
    this.font = directory.getEntry("sedgwickAve", BitmapFont.class);
    this.x = x;
    this.y = y;
    this.keyCode = keyCode;
    this.icon =getTexture(directory);
    this.scale = scale;
    this.description = getDescription();
    this.fontSmall = directory.getEntry("sedgwickAveSmall", BitmapFont.class);

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

  private String getDescription() {
    switch(keyCode) {
      case 0: {
        return "run left";
      }
      case 1: {
        return "run right";
      }
      case 2: {
        return "flip gravity up";
      }
      case 3: {
        return "flip gravity down";
      }
      case 4: {
        return "hold to open map";
      }
      case 5: {
        return "hold to reload";
      }
      case 6: {
        return "shoot gum";
      }
      case 7: {
        return "unstick gum";
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
    canvas.draw(icon, x * scale.x, y * scale.y);
    canvas.drawText(getKeyText(), font, bubblePink,
        x * scale.x,
        y * scale.y - margin, icon.getRegionWidth(), Align.center, true);
    canvas.drawText(getDescription().toUpperCase(), fontSmall, bubblePink,
        x * scale.x,
        y * scale.y - margin*4, icon.getRegionWidth(), Align.center, true);

  }


}
