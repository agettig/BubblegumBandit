package edu.cornell.gdiac.bubblegumbandit.models.projectiles;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

import java.util.ArrayList;

/**
* Class to represent a "stuck" GumModel. This GumModel
* is not a projectile; instead, it is instantiated when
* a gum projectile hits an Obstacle.
*/
public class GumModel extends WheelObstacle {

    /**Upper limit on GumModel objects */
    private static final int MAX_GUM = 2;

    /**All active joints created by gum */
    private static ArrayList<WeldJoint> activeGumJoints;

    /**All active gum objects created by gum */
    private static ArrayList<GumModel> activeGum;

    /**Diameter of a GumModel projectile */
    private float diameter;

    /**X-coordinate of GumModel velocity */
    public float vx;

    /**Y-coordinate of GumModel velocity */
    public float vy;

    /** Reference to the character avatar */
    private BanditModel avatar;

    /**
     * Creates a GumModel projectile.
     * */
    public GumModel(float x, float y, float radius){
        super(x, y, radius);
        if(activeGum == null) activeGum = new ArrayList<GumModel>();
        activeGum.add(this);
    }

    /**
     * Returns true if the player has shot the maximum amount of gum allowed.
     *
     * @return true if the player has shot the maximum amount of gum allowed.
     * */
    public static boolean atGumLimit(){
        if(activeGum == null) activeGum = new ArrayList<GumModel>();
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
        for(GumModel b : activeGum){
            Body gumBody = b.getBody();
            world.destroyBody(gumBody);

        }
        activeGum.clear();
        activeGumJoints.clear();
    }


    /**
     * Returns the diameter of this GumModel projectile.
     *
     * @return the diameter of this GumModel
     * */
    public float getDiameter(){
        return diameter;
    }

    /**
     * Returns the radius of this GumModel.
     *
     * @returns the radius of this GumModel (its diameter halved)
     * */
    public float getRadius(){
        return diameter / 2;
    }

    /**
     * Returns the velocity of this GumModel.
     *
     * @return the velocity of this GumModel
     * */
    public Vector2 getVelocity(){
        //Return a new object to avoid corruption
        return new Vector2(vx, vy);
    }

    /**
     * Sets the X-coordinate of this GumModel's velocity.
     *
     * @param vx the new X-velocity of this GumModel
     */
    public void setVx(float vx) { this.vx = vx; }


    /**
     * Sets the Y-coordinate of this GumModel's velocity.
     *
     * @param vy the new Y-velocity of this GumModel
     */
    public void setVy(float vy) { this.vy = vy; }

}
