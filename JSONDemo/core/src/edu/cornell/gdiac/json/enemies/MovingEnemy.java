package edu.cornell.gdiac.json.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

public class MovingEnemy  extends Enemy{

    private enum EnemyState{
        /**
         * The enemy is stationary
         */
        STATIONARY,
        /**
         * The enemy is wandering back and forth
         * Switches directions when hitting a wall
         */
        WANDER,
        /**
         * The ship has a target, but must get closer
         */
        CHASE,
        /**
         * The ship has a target and is attacking it
         */
        ATTACK
    }

    private float visionRadius;

    private EnemyState state;

    public void setVisionRadius(float visionRadius) {
        this.visionRadius = visionRadius;
    }

    public void setEnemyState(EnemyState state){
        this.state = state;
    }

    public MovingEnemy(){
        super(0,0,1,1);
    }

    public void initialize(AssetDirectory directory, JsonValue json){
        super.initialize(directory, json);
        setVisionRadius(json.get("visionradius").asFloat());
        setEnemyState(EnemyState.valueOf(json.get("enemystate").asString()));
    }

    @Override
    public void applyForce() {

    }

    @Override
    public void update() {

    }

    // TODO changeStateIfApplicable
    public void changeStateIfApplicable(){
        // TODO add initialization

        switch(state){
            case STATIONARY:

                break;

            case WANDER:


                break;

            case CHASE:


                break;

            case ATTACK:



                break;

            default:
                Gdx.app.error("EnemyState", "Illegal enemy state", new IllegalStateException());
        }

    }
}
