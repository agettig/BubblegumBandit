package edu.cornell.gdiac.json.gum;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;
import edu.cornell.gdiac.json.PlayerModel;

import java.util.ArrayList;

/**
* Class to represent a "stuck" Bubblegum. This Bubblegum
* is not a projectile; instead, it is instantiated when
* a gum projectile hits an Obstacle.
*/
public class Bubblegum extends WheelObstacle {

    /**Upper limit on Bubblegum objects */
    private static final int MAX_GUM = 2;

    /**All active joints created by gum */
    private static ArrayList<WeldJoint> activeGumJoints;

    /**All active gum objects created by gum */
    private static ArrayList<Bubblegum> activeGum;

    /**Diameter of a Bubblegum projectile */
    private float diameter;

    /**X-coordinate of Bubblegum velocity */
    public float vx;

    /**Y-coordinate of Bubblegum velocity */
    public float vy;

    /**
     * Creates a Bubblegum projectile.
     * */
    public Bubblegum(float x, float y, float radius){
        super(x, y, radius);
        if(activeGum == null) activeGum = new ArrayList<Bubblegum>();
        activeGum.add(this);
    }

    /**
     * Returns true if the player has shot the maximum amount of gum allowed.
     *
     * @return true if the player has shot the maximum amount of gum allowed.
     * */
    public static boolean atGumLimit(){
        if(activeGum == null) activeGum = new ArrayList<Bubblegum>();
        return activeGum.size() >= MAX_GUM;
    }

    public static void addGumJoint(WeldJoint wj){
        if(activeGumJoints == null) activeGumJoints = new ArrayList<WeldJoint>();
        activeGumJoints.add(wj);
    }

    public static void collectGum(World world){
        for(WeldJoint j : activeGumJoints){
            world.destroyJoint(j);
        }
        for(Bubblegum b : activeGum){
            Body gumBody = b.getBody();
            world.destroyBody(gumBody);

        }
        activeGum.clear();
        activeGumJoints.clear();
    }


    /**
     * Returns the diameter of this Bubblegum projectile.
     *
     * @return the diameter of this Bubblegum
     * */
    public float getDiameter(){
        return diameter;
    }

    /**
     * Returns the radius of this Bubblegum.
     *
     * @returns the radius of this Bubblegum (its diameter halved)
     * */
    public float getRadius(){
        return diameter / 2;
    }

    /**
     * Returns the velocity of this Bubblegum.
     *
     * @return the velocity of this Bubblegum
     * */
    public Vector2 getVelocity(){
        //Return a new object to avoid corruption
        return new Vector2(vx, vy);
    }

    /**
     * Sets the X-coordinate of this Bubblegum's velocity.
     *
     * @param vx the new X-velocity of this Bubblegum
     */
    public void setVx(float vx) { this.vx = vx; }


    /**
     * Sets the Y-coordinate of this Bubblegum's velocity.
     *
     * @param vy the new Y-velocity of this Bubblegum
     */
    public void setVy(float vy) { this.vy = vy; }

}
