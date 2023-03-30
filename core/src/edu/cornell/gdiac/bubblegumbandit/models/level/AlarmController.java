package edu.cornell.gdiac.bubblegumbandit.models.level;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
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
  Color active = Color.RED;
  /** The color of the inactive alarms */
  Color inactive = Color.TEAL;
  /** The maximum distance of an alarm while active and the distance while inactive */
  int MAX_DST = 6;
  /** The minimum distance of an active alarm */
  int MIN_DST = 0;
  /** The number of rays used by each box2dlights light */
  int rayCount = 20;
  /** The degree of change in the distance of a light per physics update */
  float change = .1f;

  /**
   * Creates an alarm system.
   * @param locations As integer pairs, the physics x and y positions of each light
   * @param directory The asset directory containing the alarm textures
   * @param world The physics world
   */
  public AlarmController(int[][] locations, AssetDirectory directory, World world) {
    this.rays = new RayHandler(world);
    rays.setAmbientLight(.7f);
    rays.setShadows(true);
    this.lights = new PointLight[locations.length];
    this.onTexture = new TextureRegion(directory.getEntry("alarm_on", Texture.class));
    this.offTexture = new TextureRegion(directory.getEntry("alarm_off", Texture.class));
    for(int i = 0; i<locations.length; i++) {
      int[] loc = locations[i];
      if(loc.length!=2) System.err.println("Light location not formatted as [x,y].");
      lights[i] = new PointLight(rays, rayCount, inactive, MAX_DST, loc[0]+.5f, loc[1]+.5f);
    }
    setAlarms(true); //comment out once controlled by events in game controller
  }

  /**
   * Draws the box2dlight component of each alarm. Not to be called with a canvas's begin/end block,
   * but strictly after.
   * @param camera The current game camera
   * @param scale The physics to world scale (should be 64x64 in BGB)
   */
  public void drawLights(GameCamera camera, Vector2 scale) {
    rays.setCombinedMatrix(camera.combined.scl(scale.x), camera.position.x / scale.x,
        camera.position.y / scale.y, camera.viewportWidth * camera.zoom / scale.x,
        camera.viewportHeight * camera.zoom / scale.y); //how to scale down to physics?
    rays.render();
  }

  /**
   * Draws the alarm textures. Called before drawLights, within the canvas's begin/end block.
   * @param canvas The game canvas
   * @param scale The physics to world scale (should be 64x64 in BGB)
   */
  public void drawAlarms(GameCanvas canvas, Vector2 scale) {
    for(PointLight light : lights) {
      TextureRegion draw = alarming? onTexture : offTexture;
      canvas.drawWithShadow(draw, Color.WHITE, draw.getRegionWidth()/2,
          draw.getRegionHeight()/2, light.getX()*scale.x,
          light.getY()*scale.y,
          0, 1, 1);
    }
  }

  /**
   * Effect: Updates the box2dlights, oscillates distance if alarming.
   */
  public void update() {
    if(alarming) {
      for(PointLight light : lights) {
        if(light.getDistance()+change>MAX_DST||
            light.getDistance()+change<0) change = change *-1;
        light.setDistance(light.getDistance()+change);
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
      for(PointLight light : lights) {
        light.setColor(active);
      }
    } else if (!set&&alarming) {
      for(PointLight light : lights) {
        light.setColor(inactive);
        light.setDistance(MAX_DST);
      }
    }
    alarming = set;

  }


  }
