package edu.cornell.gdiac.bubblegumbandit.controllers;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.bubblegumbandit.models.projectiles.GumModel;
import edu.cornell.gdiac.physics.obstacle.Obstacle;

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

    //TODO: Implement.
    /**
     *  Handles collisions between GumModel and Obstacles, causing the GumModel to
     *  stop at the Obstacle.
     *
     *  @param gum The GumModel in the collision
     *  @param other The other obstacle in the collision
     */
    public void resolveGumCollision(GumModel gum, Obstacle other){}




}
