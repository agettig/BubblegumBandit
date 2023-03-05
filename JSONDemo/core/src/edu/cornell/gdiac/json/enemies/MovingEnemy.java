package edu.cornell.gdiac.json.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.World;
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

    public MovingEnemy(World world){
        super(world);
    }

    public void initialize(AssetDirectory directory, JsonValue json){
        super.initialize(directory, json);
        setVisionRadius(json.get("visionradius").asFloat());
        setEnemyState(EnemyState.valueOf(json.get("enemystate").asString()));
    }

    // TODO
    @Override
    public void applyForce() {
//        body.applyForce(new Vector2(5, 0),getPosition(),true);
    }

    // TODO
    @Override
    public void update() {
        super.update();
        applyForce();
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
