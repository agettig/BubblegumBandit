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

  AlarmLight[] lights;
  RayHandler rays;
  TextureRegion onTexture;
  TextureRegion offTexture;
  boolean alarming;
  Color active = Color.RED;
  Color inactive = Color.TEAL;
  int MAX_DST = 6;
  int MIN_DST = 0;
  int rayCount = 20;
  float change = .1f; //rate of change of dst values

//basically array of 2d vectors, pulled from level JSON
  public AlarmController(int[][] locations, AssetDirectory directory, World world) {
    this.rays = new RayHandler(world);
    rays.setAmbientLight(.7f);
    rays.setShadows(true);
    this.lights = new AlarmLight[locations.length];
    this.onTexture = new TextureRegion(directory.getEntry("alarm_on", Texture.class));
    this.offTexture = new TextureRegion(directory.getEntry("alarm_off", Texture.class));
    for(int i = 0; i<locations.length; i++) {
      int[] loc = locations[i];
      if(loc.length!=2) System.err.println("Light location not formatted as [x,y].");
      lights[i] = new AlarmLight(rays, loc[0]+.5f, loc[1]+.5f);
    }
    setAlarms(true);
  }

  public void drawLights(GameCamera camera, Vector2 scale) {
    rays.setCombinedMatrix(camera.combined.scl(scale.x), camera.position.x / scale.x,
        camera.position.y / scale.y, camera.viewportWidth * camera.zoom / scale.x,
        camera.viewportHeight * camera.zoom / scale.y); //how to scale down to physics?
    rays.render();
  }

  public void drawAlarms(GameCanvas canvas, Vector2 scale) {
    for(AlarmLight light : lights) {
      TextureRegion draw = alarming? onTexture : offTexture;
      canvas.drawWithShadow(draw, Color.WHITE, draw.getRegionWidth()/2,
          draw.getRegionHeight()/2, light.light.getX()*scale.x,
          light.light.getY()*scale.y,
          0, 1, 1);
    }
  }

  public void update() {
    if(alarming) {
      for(AlarmLight light : lights) {
        if(light.light.getDistance()+change>MAX_DST||
            light.light.getDistance()+change<0) change = change *-1;
        light.light.setDistance(light.light.getDistance()+change);
      }
    }
    rays.update();
  }

  public void dispose() {
    rays.dispose();
  }

  public void setAlarms(boolean set) {
    //change alarm colors
    if(set&&!alarming) {
      for(AlarmLight light : lights) {
        light.light.setColor(active);
      }
    } else if (!set&&alarming) {
      for(AlarmLight light : lights) {
        light.light.setColor(inactive);
      }
    }
    alarming = set;

  }

  private class AlarmLight {

    PointLight light;

    public AlarmLight(RayHandler rays, float x, float y) {
      this.light = new PointLight(rays, rayCount, inactive, MAX_DST, x, y);
    }
  }



  }
