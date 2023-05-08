package edu.cornell.gdiac.bubblegumbandit.view;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.BubblegumController;
import edu.cornell.gdiac.bubblegumbandit.models.level.LevelModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;

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
  private Label orbCountdown;
  private Label fpsLabel;
  private Array<Image> captiveIcons;

  /** Position that the timer "shakes" to */
  private Vector2 shakeAdjust;

  private Texture captiveIcon;

  private Texture emptyIcon;
  private int maxCaptives = 6;


  private Image[] gumCount = new Image[6];
  private Image[] reloadGumCount = new Image[6];
  private Image[] emptyGumCount = new Image[6];


  public HUDController(AssetDirectory directory) {

    font = directory.getEntry("codygoonRegular", BitmapFont.class);
    stage = new Stage();

    table = new Table();
    table.align(Align.topLeft);
    stage.addActor(table);
    table.setFillParent(true);

    healthBar = new Image(directory.getEntry("healthBar", Texture.class));
    healthFillText = directory.getEntry("healthFill", Texture.class);
    healthIcon = new Image(directory.getEntry("healthIcon", Texture.class));
    bubbleIcon = new Image(directory.getEntry("bubblegumIcon", Texture.class));
    captiveIcon = directory.getEntry("captiveIcon", Texture.class);
    emptyIcon = directory.getEntry("captiveIconOutline", Texture.class);


    healthFillRegion = new TextureRegion(healthFillText, 0, 0,
            healthFillText.getWidth(), healthFillText.getHeight());
    healthFill = new Image(new SpriteDrawable(new Sprite(healthFillRegion)) {
    });

    health = new WidgetGroup(healthBar, healthFill, healthIcon);
    table.add(health).align(Align.left);
    healthBar.setY(healthIcon.getHeight() / 2 - healthBar.getHeight() / 2);
    healthBar.setX(healthIcon.getWidth() / 2);
    healthFill.setX(healthBar.getX() + healthBar.getHeight() * HEALTH_MARGIN);
    healthFill.setY(healthBar.getY() + healthBar.getHeight() * HEALTH_MARGIN);

    table.row();
    for (int i = 0; i < 6; i++) {
      Image emptyGum = new Image(directory.getEntry("emptyGum", Texture.class));
      emptyGum.setPosition(32 + i*72, stage.getHeight() - 103);
      stage.addActor(emptyGum);

      Image reloadGumIcon = new Image(directory.getEntry("bubblegumIcon", Texture.class));
      reloadGumIcon.setPosition(32 + i*72, stage.getHeight() - 103);
      gumCount[i] = reloadGumIcon;
      stage.addActor(reloadGumIcon);
    }
//    Actor blank = new Actor();
//    blank.setHeight(64);
    table.add(new Actor()).padTop(50);

    table.row();

    captiveIcons = new Array<>(maxCaptives);

    for(int i =0; i< maxCaptives; i++) {
      Image icon = new Image(emptyIcon);
      captiveIcons.add(icon);
      table.add(icon).padRight(12).padBottom(5);
    }

    table.padLeft(10).padTop(60);


    orbCountdown = new Label("00", new Label.LabelStyle(font, Color.WHITE));
    orbCountdown.setFontScale(1f);
    orbCountdown.setPosition(stage.getWidth() / 2, stage.getHeight() / 8, Align.center);
    stage.addActor(orbCountdown);


    for (int i = 0; i < 6; i++) {
      Image reloadGumIcon = new Image(directory.getEntry("bubblegumIcon", Texture.class));
      Image emptyGumIcon = new Image(directory.getEntry("emptyGum", Texture.class));
      emptyGumIcon.setPosition(stage.getWidth() / 2 - 125 + i*72, stage.getHeight() / 4, Align.center);
      reloadGumIcon.setPosition(stage.getWidth() / 2 - 125 + i*72, stage.getHeight() / 4, Align.center);

      emptyGumCount[i] = emptyGumIcon;
      reloadGumCount[i] = reloadGumIcon;
      emptyGumIcon.setVisible(false);
      reloadGumIcon.setVisible(false);
      stage.addActor(emptyGumIcon);
      stage.addActor(reloadGumIcon);
    }

    fpsLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
    fpsLabel.setFontScale(0.5f);
    table.row();
    table.add(fpsLabel).padTop(10);

    table.padLeft(30).padTop(60);
  }

  public void setCaptives(int currentCaptives, int totalCaptives){
    //ahh

    for(int i = 0; i< maxCaptives; i++) {
      Image curr = captiveIcons.get(i);
      if(i<currentCaptives) {
        curr.setVisible(true);
        curr.setDrawable(new SpriteDrawable(new Sprite(captiveIcon)));
      } else if (i<totalCaptives){
        curr.setVisible(true);
        curr.setDrawable(new SpriteDrawable(new Sprite(emptyIcon)));
      } else {
        curr.setVisible(false);
      }
    }
  }





  public boolean hasViewport() {
    return stage.getViewport() != null;
  }

  public void setViewport(Viewport view) {
    stage.setViewport(view);
    view.apply(true);
  }

  public void draw(LevelModel level,
                   BubblegumController bubblegumController,
                   int timer,
                   int fps,
                   boolean showFPS,
                   boolean reloadingGum,
                   float dt,
                   GameCamera camera,
                   BanditModel bandit) {
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
    int numCaptives = level.getBandit().getNumStars();
    int totalCaptives = level.getTotalCaptives();
    setCaptives(numCaptives, totalCaptives);

    healthFill.setWidth(healthFillRegion.getRegionWidth()-HEALTH_MARGIN*healthBar.getHeight());
    healthFill.setHeight(healthBar.getHeight()-2*(healthBar.getHeight()*HEALTH_MARGIN));

    int currentAmmo = bubblegumController.getAmmo();
    for (int i = 0; i < currentAmmo; i++) {
      Image gumImage = gumCount[i];
      gumImage.setVisible(true);
    }
    for (int i = currentAmmo; i < 6; i++) {
      Image gumImage = gumCount[i];
      gumImage.setVisible(false);
    }

    if(timer >= 0){
      shakeTimer(dt, timer, camera, bandit);
      int orbCountdownValue = orbCountdown.getText().toString().equals("") ? -1 :
              Integer.parseInt(orbCountdown.getText().toString());

      //If we need to "tick"
      if(orbCountdownValue != timer){
        if(timer > 10){
          orbCountdown.setColor(Color.WHITE);
          orbCountdown.setFontScale(1f);
        }
        else if(timer > 5){
          orbCountdown.setColor(Color.YELLOW);
          orbCountdown.setFontScale(1.4f);
        }
        else if(timer > 1){
          orbCountdown.setColor(Color.ORANGE);
        }
        else{
          orbCountdown.setColor(Color.RED);
          orbCountdown.setFontScale(1.9f);
        }


        if(timer == 3){
          camera.addTrauma(
                  bandit.getX() * bandit.getDrawScale().x,
                  bandit.getY() * bandit.getDrawScale().y,
                  .75f);
        }
        if(timer == 2){
          camera.addTrauma(
                  bandit.getX() * bandit.getDrawScale().x,
                  bandit.getY() * bandit.getDrawScale().y,
                  1f);
        }

        if(timer == 1){
          camera.addTrauma(
                  bandit.getX() * bandit.getDrawScale().x,
                  bandit.getY() * bandit.getDrawScale().y,
                  2f);
        }

        if(timer == 0){
          camera.addTrauma(
                  bandit.getX() * bandit.getDrawScale().x,
                  bandit.getY() * bandit.getDrawScale().y,
                  2f);
        }
        orbCountdown.setText(timer);
      }
    } else orbCountdown.setText("");



    if (reloadingGum) {
      for (int i = 0; i < 6; i++) {
        Image emptyGumImage = emptyGumCount[i];
        emptyGumImage.setVisible(true);
      }
      for (int i = 0; i < currentAmmo; i++) {
        Image gumImage = reloadGumCount[i];
        gumImage.setVisible(true);
      }
    }
    else {
      for (int i = 0; i < reloadGumCount.length; i++) {
        Image gumImage = reloadGumCount[i];
        gumImage.setVisible(false);
      }
      for (int i = 0; i < 6; i++) {
        Image emptyGumImage = emptyGumCount[i];
        emptyGumImage.setVisible(false);
      }
    }

    if (showFPS) {
      fpsLabel.setText("FPS: " + fps);
    } else {
      fpsLabel.setText("");
    }


    stage.draw();
  }

  /** Shakes the timer, lerping quickly it to a random position.*/
  private void shakeTimer(float dt, float timer, GameCamera camera, BanditModel bandit){
    float xShakeRange;
    float yShakeRange;
    float transitionSpeed;
    final float centerTimerX = stage.getWidth() / 2;
    final float centerTimerY = stage.getHeight() / 8;

    if(timer > 10){
      orbCountdown.setX(centerTimerX);
      orbCountdown.setY(centerTimerY);
      return;
    }



    if(timer >= 3){
      xShakeRange = 10f;
      yShakeRange = 2f;
      transitionSpeed = 7f;
    }
    else if (timer == 2){
      xShakeRange = 20f;
      yShakeRange = 10f;
      transitionSpeed = 7f;
    }
    else{
      xShakeRange = 70f;
      yShakeRange = 30f;
      transitionSpeed = 7f;
    }

    shakeAdjust = new Vector2(
            centerTimerX + MathUtils.random(-xShakeRange, xShakeRange),
            centerTimerY + MathUtils.random(-yShakeRange, yShakeRange)
    );



    orbCountdown.setX(MathUtils.lerp(orbCountdown.getX(), shakeAdjust.x, transitionSpeed * dt));
    orbCountdown.setY(MathUtils.lerp(orbCountdown.getY(), shakeAdjust.y, transitionSpeed * dt));

  }
}