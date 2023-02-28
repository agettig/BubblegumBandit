package edu.cornell.gdiac.json.gum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.json.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.SimpleObstacle;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;
import edu.cornell.gdiac.json.DudeModel;
import edu.cornell.gdiac.physics.obstacle.Obstacle;


public class Bubblegum extends WheelObstacle {

    /**Diameter of a Bubblegum projectile */
    private float diameter;

    /**X-coordinate of Bubblegum velocity */
    public float vx;

    /**Y-coordinate of Bubblegum velocity */
    public float vy;

    /** Reference to the character avatar */
	private DudeModel avatar;

    /**
     * Creates a Bubblegum projectile.
     *
     * */
    public Bubblegum(float x, float y){
        super(x, y, 2);
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
