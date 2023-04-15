package edu.cornell.gdiac.bubblegumbandit.models.level;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.GameCamera;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;

public class AlarmController {

  /** Every point light */
  PointLight[] lights;
  /** The ray handler for box2dlights */ //will need to be moved if we want other lights
  RayHandler rays;
  /** The texture for an active alarm */
  TextureRegion onTexture;
  /** The texture for an inactive alarm */
  TextureRegion offTexture;
  /** Whether the alarms are currently going off */
  boolean alarming;
  /** The color of the active alarms */
  Color active = new Color(1, 0, 0, 1f);
  /** The color of the inactive alarms */
  Color inactive = new Color(1, 1, 1, 0.6f); // Color.TEAL;
  /** The number of rays used by each box2dlights light */
  int rayCount = 80;

  /** The inactive ambient light amount */
  private final float NORMAL_AMBIENT = 0.8f;
  /** The active ambient light amount */
  private final float ALARM_AMBIENT = 0.6f;
  /** The  distance of an alarm while inactive */
  private final int INACTIVE_DIST = 8;
  /** The  distance of an alarm while active */
  private final int ACTIVE_DIST = 9;

  /**
   * Tracks start of last pulse cycle
   */
  private long timeStamp;
  /**
   * Length of pulse cycle, including rests, in milliseconds.
   */
  private float pulseTime = 3000;


  /**
   * Creates an alarm system.
   * @param locations As integer pairs, the physics x and y positions of each light
   * @param directory The asset directory containing the alarm textures
   * @param world The physics world
   */
  public AlarmController(Array<Vector2> locations, AssetDirectory directory, World world) {
    this.rays = new RayHandler(world);
    rays.setAmbientLight(0.95f);
    rays.setShadows(true);
    this.lights = new PointLight[locations.size];
    this.onTexture = new TextureRegion(directory.getEntry("alarm_on", Texture.class));
    this.offTexture = new TextureRegion(directory.getEntry("alarm_off", Texture.class));
    for(int i = 0; i<locations.size; i++) {
      lights[i] = new PointLight(rays, rayCount, inactive, INACTIVE_DIST, locations.get(i).x+.5f, locations.get(i).y+.5f);
    }
    timeStamp = TimeUtils.millis();

  }

  /**
   * Draws the box2dlight component of each alarm. Not to be called with a canvas's begin/end block,
   * but strictly after.
   * @param canvas The current game canvas
   * @param scale The physics to world scale (should be 64x64 in BGB)
   */
  public void drawLights(GameCanvas canvas, Vector2 scale) {

    FitViewport view = canvas.getUIViewport();
    GameCamera camera = canvas.getCamera();
    rays.useCustomViewport(view.getScreenX()*2, view.getScreenY()*2,
        view.getScreenWidth()*2, view.getScreenHeight()*2);
    rays.setCombinedMatrix(camera.combined.scl(scale.x), camera.position.x / scale.x,
        camera.position.y / scale.y, camera.viewportWidth * camera.zoom / scale.x,
        camera.viewportHeight * camera.zoom / scale.y);
    camera.combined.scl(1/scale.x);

    rays.render();

  }

  /**
   * Draws the alarm textures. Called before drawLights, within the canvas's begin/end block.
   * @param canvas The game canvas
   * @param scale The physics to world scale (should be 64x64 in BGB)
   */
  public void drawAlarms(GameCanvas canvas, Vector2 scale) {
    for(PointLight light : lights) {
      TextureRegion draw = alarming ? onTexture : offTexture;
      canvas.drawWithShadow(draw, Color.WHITE, draw.getRegionWidth()/2f, draw.getRegionHeight()/2f, (light.getX())*scale.x, (light.getY())*scale.y, 0, 1, 1);
    }
  }

  /**
   * Effect: Updates the box2dlights, oscillates distance if alarming.
   */
  public void update() {
    if(alarming) {

      float time = TimeUtils.timeSinceMillis(timeStamp);
      if(time>pulseTime/3f&&time<pulseTime) {

        for(PointLight light : lights) {
          light.setDistance(time<= pulseTime*(2f/3f) ? light.getDistance()+.1f
              : light.getDistance()-.1f);
        }

      } else if (time>pulseTime) { {
       timeStamp = TimeUtils.millis();
        for(PointLight light : lights) {
          light.setDistance(0);
        }
      }

      }


    }
    rays.update();
  }

  /**
   * Cleanup for when the world is discarded
   */
  public void dispose() {
    rays.dispose();
  }

  /**
   * Sets off or disarms the alarm system.
   * @param set whether to set off the alarms, turns off alarms when false
   */
  public void setAlarms(boolean set) {
    //change alarm colors
    if(set&&!alarming) {
      rays.setAmbientLight(ALARM_AMBIENT);
      for(PointLight light : lights) {
        light.setColor(active);
        light.setDistance(ACTIVE_DIST);
        light.setSoftnessLength(ACTIVE_DIST);
      }
      timeStamp = TimeUtils.millis();
    } else if (!set&&alarming) {
      rays.setAmbientLight(NORMAL_AMBIENT);
      for(PointLight light : lights) {
        light.setColor(inactive);
        light.setDistance(INACTIVE_DIST);
      }
    }
    alarming = set;
  }
}
