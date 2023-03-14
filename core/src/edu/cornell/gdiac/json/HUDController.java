package edu.cornell.gdiac.json;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import edu.cornell.gdiac.assets.AssetDirectory;
import java.awt.Font;

public class HUDController {


  private Font font;
  private Stage stage;

  private Image healthBar;
  private Image healthFill;
  private WidgetGroup health;

  public HUDController(AssetDirectory directory) {

    font = directory.getEntry("display", Font.class);
    stage = new Stage();//Stage(canvas.getUIviewport());
    healthBar = new Image(directory.getEntry( "health_bar", Texture.class ));
    healthFill = new Image(directory.getEntry( "health_fill", Texture.class ));
    //set scale?
    health = new Stack(healthBar, healthFill);
    stage.addActor(health);

  }

  public void draw(LevelModel level, GameCanvas canvas) {
    if(stage.getViewport()==null) stage.setViewport(canvas.getUIviewport());
    float healthFraction = level.getAvatar().getHealth()/ level.getAvatar().getMaxHealth();
    healthFill.setScaleX(healthFraction);
    stage.draw();
  }




}
