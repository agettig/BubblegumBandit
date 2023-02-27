package edu.cornell.gdiac.json.gum;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.physics.obstacle.Obstacle;

public class CollisionController {

    /** Caching object for computing normal */
    private Vector2 normal;

    /** Caching object for intermediate calculations */
    private Vector2 temp;

    /** Caching object for computing net velocity */
    private Vector2 velocity;


    /**
     * Contruct a new controller.
     *
     * This constructor initializes all the caching objects so that
     * there is no heap allocation during collision detection.
     */
    public CollisionController(){
        normal = new Vector2();
        temp = new Vector2();
        velocity = new Vector2();
    }

    public void checkForGumCollision(Bubblegum gum, Obstacle other){

    }

}
