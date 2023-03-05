package edu.cornell.gdiac.json.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.json.DudeModel;


public class MovingEnemy  extends Enemy {

    private enum EnemyState {
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

    /** The radius of how far in front of them the robot can see*/
    private float visionRadius;

    /** How far the robot is allowed to grab*/
    private float grabRadius;
    /** Time passed since the controller was started*/
    private long ticks;
    /** State of the robot*/
    private EnemyState state;
    /**Represents the player aka Bubblegum Bandit*/
    private DudeModel target;

    public void setVisionRadius(float visionRadius) {
        this.visionRadius = visionRadius;
    }

    public void setGrabRadius(float grabRadius) {this.grabRadius = grabRadius;}

    public void setEnemyState(EnemyState state) {
        this.state = state;
    }

    public MovingEnemy() {
        super(0, 0, 1, 1);
    }

    public void initialize(AssetDirectory directory, JsonValue json, DudeModel target) {
        super.initialize(directory, json);
        setVisionRadius(json.get("vision\n" +
                "    public void setVisionRadius(float visionradius").asFloat());
        setGrabRadius(json.get("grabradius").asFloat());
        setEnemyState(EnemyState.valueOf(json.get("enemystate").asString()));
        this.target = target;
    }

    // TODO
    @Override
    public void applyForce() {
//        body.applyForce(new Vector2(5, 0),getPosition(),true);
    }

    // TODO
    @Override
    public void update() {
        applyForce();
    }

    // TODO checkGrabRange
    public boolean checkGrabRange(Vector2 player, Vector2 robot) {
        return false;
    }

    // TODO checkVisionRange
    public boolean checkVisionRange(Vector2 player, Vector2 robot) {
        return false;
    }

    // TODO changeStateIfApplicable
    public void changeStateIfApplicable(){
        // TODO add initialization

        switch(state){
            case STATIONARY:
                this.state = EnemyState.WANDER;
                break;

            case WANDER:
                if (checkVisionRange(this.target.getPosition(), this.getPosition())) {
                    this.state = EnemyState.CHASE;
                }
                break;

            case CHASE:
                if (checkGrabRange(this.target.getPosition(), this.getPosition())) {
                    this.state = EnemyState.ATTACK;
                }
                else if (target.getWidth() + ticks % 10 == 0) {
                    this.state = EnemyState.WANDER;
                }
                break;

            case ATTACK:
                if (checkGrabRange(this.target.getPosition(), this.getPosition())) {
                    this.state = EnemyState.CHASE;
                }
                else {
                    //game over???
                }
                break;

            default:
                Gdx.app.error("EnemyState", "Illegal enemy state", new IllegalStateException());
        }

    }
}
