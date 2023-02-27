package edu.cornell.gdiac.json.gum;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

public class Bubblegum {

    /**Diameter of a Bubblegum projectile */
    private float diameter;

    /**X-coordinate of Bubblegum projectile */
    public float x;

    /**Y-coordinate of Bubblegum projectile */
    public float y;

    /**X-coordinate of Bubblegum velocity */
    public float vx;

    /**Y-coordinate of Bubblegum velocity */
    public float vy;

    /**
     * Creates a Bubblegum projectile.
     * */
    public Bubblegum(){

    }


    /**
     * Returns the (X,Y) position of this Bubblegum projectile.
     *
     * @returns a Vector2 representing this Bubblegum's screen position
     * */
    public Vector2 getPosition(){
        return new Vector2(x, y);
    }


}
