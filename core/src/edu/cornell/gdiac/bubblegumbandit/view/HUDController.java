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

  private int timerStart = -1;

  private Image healthIcon;
  private Image bubbleIcon;
  private Label orbCountdown;
  private Label fpsLabel;
  private Array<Image> captiveIcons;

  /** Texture for the Escape icon after the Bandit picks up the orb. */
  private Image escapeIcon;

  /** Text that tells the player to get OUT! */
  private Label escapeText;

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


    escapeText = new Label("ESCAPE", new Label.LabelStyle(font, Color.RED));
    escapeIcon = new Image(directory.getEntry("escapeIcon", Texture.class));
    escapeText.setFontScale(1.75f);
    escapeText.setAlignment(Align.center, Align.center);
    escapeText.setVisible(false);
    escapeText.setPosition(stage.getWidth()/2 - escapeText.getWidth()/2, stage.getHeight() * .6f);
    escapeIcon.setPosition(stage.getWidth()/2 - escapeIcon.getWidth()/2, stage.getHeight() / 8);
    timerStart = -1;
    stage.addActor(escapeIcon);
    stage.addActor(escapeText);


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

  public void drawCountdownText(int timer, float dt, GameCamera camera, BanditModel bandit){

    if(timer < 0 && orbCountdown != null) orbCountdown.setText("");
    if(timer < 0) timerStart = -1;
    if(timer < 0) return; //We aren't ticking down.

    //We need to instantiate the label here in order to draw the text over the UI
    if(orbCountdown == null){
      orbCountdown = new Label("", new Label.LabelStyle(font, Color.WHITE));
      orbCountdown.setFontScale(1f);
      orbCountdown.setColor(Color.WHITE);
      orbCountdown.setPosition(escapeIcon.getWidth() - escapeIcon.getWidth()/2,
              escapeIcon.getY() + escapeIcon.getHeight()/2);
      orbCountdown.setText(timer);
      stage.addActor(orbCountdown);
    }

    shakeTimer(dt, timer, camera, bandit);

    //Should we draw "ESCAPE!" ?
    final int escapeTextDuration = 3;
    if(timerStart < 0) timerStart = timer;
    escapeText.setVisible(timerStart - timer < escapeTextDuration);


    //Did we tick down one second?
    String countdownText = orbCountdown.getText().toString();
    if(countdownText.equals("") || Integer.parseInt(countdownText) != timer){
      orbCountdown.setText(timer);

      //Should we SHAKE??
      int shakeChance = 45;
      boolean shouldShake = MathUtils.random(1, 100) <= shakeChance;
      if(shouldShake && timer > 3){
        float randomTrauma = MathUtils.random(.25f, 1f);
        camera.addTrauma(
                bandit.getX() * bandit.getDrawScale().x,
                bandit.getY() * bandit.getDrawScale().y,
                randomTrauma
        );
      }

      //Final shakes
      if(timer <= 3){
        camera.addTrauma(
                bandit.getX() * bandit.getDrawScale().x,
                bandit.getY() * bandit.getDrawScale().y,
                4 - timer
        );
      }
    }
  }

  public void draw(LevelModel level,
                   int fps,
                   int timer,
                   boolean showFPS,
                   boolean reloadingGum) {
    int ammo = level.getBandit().getAmmo();
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

    for (int i = 0; i < ammo; i++) {
      Image gumImage = gumCount[i];
      gumImage.setVisible(true);
    }
    for (int i = ammo; i < 6; i++) {
      Image gumImage = gumCount[i];
      gumImage.setVisible(false);
    }


    if (reloadingGum) {
      for (int i = 0; i < 6; i++) {
        Image emptyGumImage = emptyGumCount[i];
        emptyGumImage.setVisible(true);
      }
      for (int i = 0; i < ammo; i++) {
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

    if(timer >= 0){
      escapeIcon.setVisible(true);
    }
    else escapeIcon.setVisible(false);


    stage.draw();
  }

  /** Shakes the timer, lerping quickly it to a random position.*/
  private void shakeTimer(float dt, float timer, GameCamera camera, BanditModel bandit){
    float xShakeRange;
    float yShakeRange;
    float transitionSpeed;
    final float centerTimerX = escapeIcon.getX() + escapeIcon.getWidth()/2;
    final float centerTimerY = escapeIcon.getY() + escapeIcon.getHeight()/2;
    orbCountdown.setX(centerTimerX);
    orbCountdown.setY(centerTimerY);
    escapeIcon.setX((stage.getWidth() / 2) - escapeIcon.getWidth()/2);
    escapeIcon.setY(stage.getHeight() / 8f);

    if(timer > 10) return;

    xShakeRange = 10f;
    yShakeRange = 2f;
    transitionSpeed = 7f;
    shakeAdjust = new Vector2(
            centerTimerX + MathUtils.random(-xShakeRange, xShakeRange),
            centerTimerY + MathUtils.random(-yShakeRange, yShakeRange)
    );
    orbCountdown.setX(MathUtils.lerp(orbCountdown.getX(), shakeAdjust.x, transitionSpeed * dt));
    orbCountdown.setY(MathUtils.lerp(orbCountdown.getY(), shakeAdjust.y, transitionSpeed * dt));

  }
}