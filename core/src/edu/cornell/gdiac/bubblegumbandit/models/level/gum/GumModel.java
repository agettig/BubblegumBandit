package edu.cornell.gdiac.bubblegumbandit.models.level.gum;

import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

import java.util.ArrayList;

import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.CATEGORY_GUM;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.MASK_GUM_LIMIT;

/**
 * Class to represent a "stuck" Bubblegum. This Bubblegum
 * is not a projectile; instead, it is instantiated when
 * a gum projectile hits an Obstacle.
 */
public class GumModel extends WheelObstacle {

    /**
     * Set of obstacles the gum is stuck to
     * */
    private ObjectSet<Obstacle> obstacles;

    /**
     * The maximum number of obstacles a gum can stick to
     * */
    private final int MAX_OBSTACLES = 2;


    /**
     * Creates a Bubblegum projectile.
     * */
    public GumModel(float x, float y, float radius){
        super(x, y, radius);
        obstacles = new ObjectSet<>();
    }

    /**
     * Checks if obstacle is not already stuck to gum and gum has not reached max stuck obstacles
     *
     * @param o Obstacle to add to obstacles
     * */
    public boolean canAddObstacle(Obstacle o){
        return obstacles.size < MAX_OBSTACLES && !obstacles.contains(o);
    }

    /**
     * Adds an obstacle to the obstacle set
     *
     * @param o Obstacle added to the obstacle set
     * */
    public void addObstacle(Obstacle o){
        obstacles.add(o);
    }

    /**
     * Checks if gum is at obstacle capacity and sets collision filter
     * to stop collisions between gum and enemies
     * */
    public void setCollisionFilters(){
        if (obstacles.size == 2){
            setFilter(CATEGORY_GUM, MASK_GUM_LIMIT);
        }
    }

}