package edu.cornell.gdiac.json.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
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

    /** How far forward this ship can move in a single turn */
    private static float MOVE_SPEED = 0.01f;
    /** How much this ship can turn in a single turn */
    private static final float TURN_SPEED = 15.0f;
    /** Ship velocity */
    private Vector2 velocity;
    private float visionRadius;
    private long ticks;

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
        velocity = new Vector2();
        ticks = 5;
    }

    public void setMoveSpeed(float moveSpeed) {
        MOVE_SPEED = moveSpeed;
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

        //determine how the enemy is moving
        int direction = this.enemyMovementDirection();

        //movement
        if (direction == -1) {
            velocity.x = -MOVE_SPEED;
            velocity.y = 0;
            this.vision.setDirection(-1);
            this.moveBot();
        } else if (direction == 1) {
            velocity.x = MOVE_SPEED;
            velocity.y = 0;
            this.moveBot();
        }
        else {
            velocity.x = 0;
            velocity.y = 0;
        }
        if (ticks % 400 == 0) {
            flipBot();
        }
        applyForce();
    }

    private void flipBot() {
        if (this.enemyMovementDirection() == 1) {
            this.vision.setDirection(-1);
            this.setFaceRight(false);
        }else if (this.enemyMovementDirection() == -1) {
            this.vision.setDirection(1);
            this.setFaceRight(true);
        }
    }

    private void moveBot() {
        Vector2 tmp = new Vector2();
        tmp.set(this.getPosition());

        tmp.add(this.velocity.x, this.velocity.y);

        if (this.isGrounded()) {
            this.setPosition(tmp);
            this.ticks += 1;
        }
    }


        // TODO changeStateIfApplicable
        public void changeStateIfApplicable () {
            // TODO add initialization

            switch (state) {
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

        /** Returns the direction the robot should move
         *
         * @return the direction the robot is moving -1 for left, 1 for right*/
        public int enemyMovementDirection () {
            int direction = 0;

            //initialize direction based on the direction the bot is facing
            if (this.isGrounded()) {
                if (this.getFaceRight()) {
                    direction = 1;
                }
                else direction = -1;
            }

            //every five seconds change the direction
            return direction;
        }
}
