package edu.cornell.gdiac.json;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;

import com.badlogic.gdx.scenes.scene2d.ui.*;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import edu.cornell.gdiac.assets.AssetDirectory;
import java.awt.Font;

public class HUDController {


  /** The font used in the HUD */
  private Font font;
  /** The stage, contains all HUD elements */
  private Stage stage;

  /** The backing of the health bar, drawn as empty, as a UI element */
  private Image healthBar;
  /** The filling of the health bar, as a UI element */
  private Image healthFill;
  /** The filling of the health bar, as a texture */
  private Texture healthFillText;
  /** The region of the health bar currently being displayed */
  //We can't just scale a fixed size texture because then the gradient effect would be
  //lost, and the edges would not fit properly in the health bar
  private TextureRegion healthFillRegion;
  /** Allows the health filling to be displayed on top of the health bar, contains both */
  private WidgetGroup health;

  /** The container for laying out all HUD elements */
  private Table table;
  /** The margin of the current health bar, depends on the current design */
  private float HEALTH_MARGIN = .1f;

  /** The last health the health bar filling was cropped to, saved as a reference so the
   * texture region healthFillRegion is not redefined on frames where the health has not changed.
   */
  private float lastFrac = 1f;


  public HUDController(AssetDirectory directory) {

    font = directory.getEntry("display", Font.class);
    stage = new Stage();


    table = new Table();
    table.align(Align.topLeft);
    stage.addActor(table);
    table.setFillParent(true);


    healthBar = new Image(directory.getEntry( "health_bar", Texture.class ));
    healthFillText = directory.getEntry( "health_fill", Texture.class );
    healthFillRegion = new TextureRegion(healthFillText, 0, 0,
        healthFillText.getWidth(), healthFillText.getHeight());
    healthFill = new Image(new SpriteDrawable(new Sprite(healthFillRegion)) {
    });

    health = new Stack(healthBar, healthFill);
    stage.addActor(health);
    health.setSize(healthBar.getWidth(), healthBar.getHeight());
    table.add(health).pad(10);

    healthFill.setScaleY(1-2*HEALTH_MARGIN);

  }

  public void draw(LevelModel level) {

    //drawing the health bar, draws no fill if health is 0
    float healthFraction = level.getAvatar().getHealth()/ level.getAvatar().getMaxHealth();
    float margin = health.getHeight()*HEALTH_MARGIN;
    healthFill.setY(margin);
    healthFill.setX(margin);
    if(healthFraction!=lastFrac) {
      if(healthFraction==0) {
        healthFill.setDrawable(null);
      } else {
        healthFillRegion.setRegionWidth((int) (healthFillText.getWidth()*healthFraction));
        healthFill.setDrawable(new SpriteDrawable(new Sprite(healthFillRegion)) {
        });
      }
      lastFrac = healthFraction;
    }
    healthFill.setWidth((health.getWidth()-2*margin)*healthFraction);


    stage.draw();
  }




}
