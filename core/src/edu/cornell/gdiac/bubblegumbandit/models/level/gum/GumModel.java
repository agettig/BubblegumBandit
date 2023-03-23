package edu.cornell.gdiac.bubblegumbandit.models.level.gum;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

import java.util.ArrayList;

/**
 * Class to represent a "stuck" Bubblegum. This Bubblegum
 * is not a projectile; instead, it is instantiated when
 * a gum projectile hits an Obstacle.
 */
public class GumModel extends WheelObstacle {

    private int objectsStuck;


    /**
     * Creates a Bubblegum projectile.
     * */
    public GumModel(float x, float y, float radius){
        super(x, y, radius);
        objectsStuck = 0;
    }

    public void incrementStuckObjects(){
        objectsStuck += 1;
    }

    public int getObjectsStuck(){return objectsStuck;}

}