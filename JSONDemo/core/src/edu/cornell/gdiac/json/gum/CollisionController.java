package edu.cornell.gdiac.json.gum;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.physics.box2d.*;


import javax.print.attribute.standard.JobImpressionsSupported;

public class CollisionController {

    /** Caching object for computing normal */
    private Vector2 normal;


    /**
     * Construct a new CollisionController.
     *
     * This constructor initializes all the caching objects so that
     * there is no heap allocation during collision detection.
     */
    public CollisionController(){
        normal = new Vector2();
    }

    /**
     *  Handles collisions between Bubblegum and Obstacles, causing the Bubblegum to
     *  stop at the Obstacle.
     *
     *  @param gum The Bubblegum in the collision
     *  @param other The other obstacle in the collision
     */
    public void checkForGumCollision(Bubblegum gum, Obstacle other){

        //Safety checks. Prevent crashing.
        if(gum == null || other == null) return;
        

        //If the gum hits an obstacle, stick it.
        normal.set(gum.getPosition()).sub(other.getPosition());
        float distance = normal.len();
        float impactDistance = gum.getDiameter();
        if(distance < impactDistance){
            //Stick logic.
        }
    }



}
