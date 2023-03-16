package edu.cornell.gdiac.json.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.json.controllers.InputController;

public class MovingEnemy  extends Enemy{

    /** How far forward this ship can move in a single turn */
    private static float MOVE_SPEED = 0.01f;
    /** How much this ship can turn in a single turn */
    private static final float TURN_SPEED = 15.0f;
    /** Ship velocity */
    private Vector2 velocity;

    private final float FORCE = 5f;
    private float visionRadius;
    private long ticks;


    public void setVisionRadius(float visionRadius) {
        this.visionRadius = visionRadius;
    }


    public MovingEnemy(World world, int i){
        super(world, i);
    }

    public void initialize(AssetDirectory directory, JsonValue json){
        super.initialize(directory, json);
        setVisionRadius(json.get("visionradius").asFloat());
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
    public void update(int controlCode) {
        super.update(controlCode);

        switch(controlCode){
            case InputController.CONTROL_MOVE_LEFT:
                moveLeft();
                applyForce();
                break;
            case InputController.CONTROL_MOVE_RIGHT:
                moveRight();
                applyForce();
                break;
            case InputController.CONTROL_FIRE:
                break;
            case InputController.CONTROL_MOVE_UP: //jump
                break;
            default: break;
        }
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

    private void moveLeft(){
        //adjust force
        this.vision.setDirection(-1);
    }

    private void moveRight(){
        //adjust force
        this.vision.setDirection(-1);
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
