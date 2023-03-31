package edu.cornell.gdiac.bubblegumbandit.view;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

import com.badlogic.gdx.scenes.scene2d.ui.*;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.BubblegumController;
import edu.cornell.gdiac.bubblegumbandit.models.level.LevelModel;

public class HUDController {


  /** The font used in the HUD */
  private BitmapFont font;
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
  private float HEALTH_MARGIN = .125f;

  /** The last health the health bar filling was cropped to, saved as a reference so the
   * texture region healthFillRegion is not redefined on frames where the health has not changed.
   */
  private float lastFrac = 1f;

  private Image healthIcon;
  private Image bubbleIcon;
  private Label gumCount;
  private Label orbCountdown;


  public HUDController(AssetDirectory directory) {

    font = directory.getEntry("display", BitmapFont.class);
    stage = new Stage();

    table = new Table();
    table.align(Align.topLeft);
    stage.addActor(table);
    table.setFillParent(true);


    healthBar = new Image(directory.getEntry( "health_bar", Texture.class ));
    healthFillText = directory.getEntry( "health_fill", Texture.class );
    healthIcon = new Image(directory.getEntry( "health_icon", Texture.class ));
    bubbleIcon = new Image(directory.getEntry( "bubblegum_icon", Texture.class ));

    healthFillRegion = new TextureRegion(healthFillText, 0, 0,
            healthFillText.getWidth(), healthFillText.getHeight());
    healthFill = new Image(new SpriteDrawable(new Sprite(healthFillRegion)) {
    });

    health = new WidgetGroup(healthBar, healthFill, healthIcon);
    table.add(health).align(Align.left);
    healthBar.setY(healthIcon.getHeight()/2-healthBar.getHeight()/2);
    healthBar.setX(healthIcon.getWidth()/2);
    healthFill.setX(healthBar.getX()+healthBar.getHeight()*HEALTH_MARGIN);
    healthFill.setY(healthBar.getY()+healthBar.getHeight()*HEALTH_MARGIN);

    table.row();
    table.add(bubbleIcon);

    gumCount = new Label("x0", new Label.LabelStyle(font, Color.WHITE));
    gumCount.setFontScale(0.5f);

    table.add(gumCount).padLeft(10);

    table.padLeft(30).padTop(60);

    orbCountdown = new Label("00", new Label.LabelStyle(font, Color.WHITE));
    orbCountdown.setFontScale(1f);
    orbCountdown.setPosition(stage.getWidth() / 2, stage.getHeight() / 8, Align.center);
    stage.addActor(orbCountdown);


  }

  public boolean hasViewport() {
    return stage.getViewport() != null;
  }

  public void setViewport(Viewport view) {
    stage.setViewport(view);
    view.apply(true);
  }

  public void draw(LevelModel level, BubblegumController bubblegumController, int timer) {
    //drawing the health bar, draws no fill if health is 0
    float healthFraction = level.getBandit().getHealth()/ level.getBandit().getMaxHealth();

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

    healthFill.setWidth(healthFillRegion.getRegionWidth()-HEALTH_MARGIN*healthBar.getHeight());
    healthFill.setHeight(healthBar.getHeight()-2*(healthBar.getHeight()*HEALTH_MARGIN));
    gumCount.setText("x" + bubblegumController.getAmmo() );

    if (timer >= 9) {
      orbCountdown.setText(timer);
    } else if (timer >= 0) {
      orbCountdown.setText("0" + timer);
    }else {
      orbCountdown.setText("");
    }

    stage.draw();

  }


}