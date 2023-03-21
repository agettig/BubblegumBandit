package edu.cornell.gdiac.json.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.json.controllers.InputController;

/**Represents an Enemy robot that can move.*/
public class MovingEnemy  extends Enemy{

    /**Maximum speed/velocity of a moving enemy. */
    private static float MAX_SPEED = 1f;

    /**Force to apply to MovingEnemies for movement.*/
    private final float MOVEMENT_FORCE = 6.3f;

    /**Impulse to apply to MovingEnemies for jumping.*/
    private final float JUMPING_IMPULSE = 3f;

    /**Vision radius of a MovingEnemy. */
    private float visionRadius;

    /**[CONTROVERSIAL] Ticks to manage game time.*/
    private long ticks;

    /**Sets the vision radius of a MovingEnemy.
     *
     * @param visionRadius The new vision radius */
    public void setVisionRadius(float visionRadius) {
        this.visionRadius = visionRadius;
    }

    /**Creates a MovingEnemy.
     *
     * @param world The box2d world
     * @param id the id of this Enemy
     * */
    public MovingEnemy(World world, int id){
        super(world, id);
    }

    /**Initializes this MovingEnemy in the game. Sets its vision radius.
     *
     * @param directory The BubblegumBandit asset directory
     * @param json the json to parse
     * */
    public void initialize(AssetDirectory directory, JsonValue json){
        super.initialize(directory, json);
        setVisionRadius(json.get("visionradius").asFloat());
    }


    /**
     * Returns true if the enemy's velocity needs to be clamped. If
     * so, clamps the horizontal velocity of this enemy so that it does
     * not exceed the maximum speed.
     *
     * @returns true if this method clamped the enemy's velocity.
     * */
    private boolean ClampVelocity(){
        Body enemyBody = getBody();

        //If over the max velocity, adjust.
        if(enemyBody.getLinearVelocity().x > MAX_SPEED){
            enemyBody.getLinearVelocity().set(MAX_SPEED,
                    enemyBody.getLinearVelocity().y);
            return true;
        }

        //Repeat for negatives.
        if(Math.abs(enemyBody.getLinearVelocity().x) > MAX_SPEED){
            enemyBody.getLinearVelocity().set(-MAX_SPEED,
                    enemyBody.getLinearVelocity().y);
            return true;
        }

        return false;
    }

    /**
     * Primary update method for a MovingEnemy.
     *
     * Takes a control code and performs the corresponding action. Updates
     * the tick count.
     */

    @Override
    public void update(int controlCode) {
        super.update(controlCode);

        ticks++;
        // Determine how we are moving.
        boolean movingLeft  = (controlCode & InputController.CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (controlCode & InputController.CONTROL_MOVE_RIGHT) != 0;
        boolean movingUp    = (controlCode & InputController.CONTROL_MOVE_UP) != 0;
        boolean movingDown  = (controlCode & InputController.CONTROL_MOVE_DOWN) != 0;

        // Process movement command.
        if (movingLeft) {
            setVX(-4f);
            setVY(0);
            setFaceRight(false);
        } else if (movingRight) {
            setVX(4f);
            setVY(0);
            setFaceRight(true);
        } else if (movingUp) {
            if (!isFlipped){
                setVY(4f);
                body.applyForceToCenter(0, 5,true);
            }
            else{
                setVY(0);
            }
            setVX(0);
        } else if (movingDown) {
            if (isFlipped){
                setVY(-4f);
                body.applyForceToCenter(0, -5,true);

            }
            else{
                setVY(0);
            }
            setVX(0);
        } else {
//            // NOT MOVING, SO SLOW DOWN
//            velocity.x *= SPEED_DAMPNING;
//            velocity.y *= SPEED_DAMPNING;
//            if (Math.abs(velocity.x) < EPSILON_CLAMP) {
//                velocity.x = 0.0f;
//            }
//            if (Math.abs(velocity.y) < EPSILON_CLAMP) {
//                velocity.y = 0.0f;
//            }
            setVX(0);

        }
//        switch(controlCode){
//            case InputController.CONTROL_MOVE_LEFT: //chase left
////                moveLeft();
//                //moveRight();
//                //jump();
//                setVX(-1);
//                break;
//            case InputController.CONTROL_MOVE_RIGHT: //chase right
//                setVX(1.2f);
//                break;
//            case InputController.CONTROL_FIRE: //shoot
//                break;
//            case InputController.CONTROL_MOVE_UP: //jump
//                setVY(2);
//                setVX(0);
//                break;
//            default: break;
//        }

    }

    /**
     * Makes the Enemy jump if it has not done so in 60 ticks.
     */
    private void jump(){
//        System.out.println(ticks);
        if(ticks % 20 != 0) return;

        this.vision.setDirection(.5f);
        Vector2 imp = new Vector2(0, 3);
        getBody().applyLinearImpulse(imp, getPosition(), true);
    }

    /**
     * Moves the Enemy left, or clamps its velocity if it is over
     * the maximum speed.
     * */
    private void moveLeft(){
        //adjust force
        this.vision.setDirection(0);

        //if we didn't need to clamp velocity, increase it.
        if(!ClampVelocity()) getBody().applyForceToCenter(-MOVEMENT_FORCE, 0, true);
    }

    /**
     * Moves the Enemy right, or clamps its velocity if it is over
     * the maximum speed.
     * */
    private void moveRight(){
        //adjust force
        this.vision.setDirection((float) Math.PI);

        //if we didn't need to clamp velocity, increase it.
        if(!ClampVelocity()) getBody().applyForceToCenter(MOVEMENT_FORCE, 0, true);
    }
}
