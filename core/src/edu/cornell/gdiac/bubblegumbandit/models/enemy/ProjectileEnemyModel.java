package edu.cornell.gdiac.bubblegumbandit.models.enemy;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.*;
import static edu.cornell.gdiac.bubblegumbandit.controllers.InputController.CONTROL_MOVE_DOWN;

public class ProjectileEnemyModel extends MovingEnemyModel {

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
    public ProjectileEnemyModel(World world, int id){
        super(world, id);
    }

    /**Initializes this MovingEnemy in the game. Sets its vision radius.
     *
     * @param directory The BubblegumBandit asset directory
     * @param x the x position of this enemy
     * @param y the y position of this enemy
     * @param constantsJson the constants json
     * */
    public void initialize(AssetDirectory directory, float x, float y, JsonValue constantsJson){
        super.initialize(directory, x, y, constantsJson);
        setName("smallrobot");
        setVisionRadius(constantsJson.get("visionradius").asFloat());
    }

    /**
     * Main update loop for a ProjectileEnemyModel. Takes a control code
     * and performs the corresponding action.
     *
     * @param controlCode The code that tells this ProjectileEnemyModel
     *                    what to do.
     * @param dt          Time since last frame.
     */
    @Override
    public void update(int controlCode, float dt) {
        super.update(controlCode);

        // Determine how we are moving.
        boolean movingLeft = (controlCode & CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (controlCode & CONTROL_MOVE_RIGHT) != 0;
        boolean movingUp = (controlCode & CONTROL_MOVE_UP) != 0;
        boolean movingDown = (controlCode & CONTROL_MOVE_DOWN) != 0;

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
            if (!isFlipped) {
                setVY(4f);
                body.applyForceToCenter(0, 5, true);
            } else setVY(0);
            setVX(0);
        } else if (movingDown) {
            if (isFlipped) {
                setVY(-4f);
                body.applyForceToCenter(0, -5, true);
            } else setVY(0);
            setVX(0);
        } else setVX(0);
    }


    /**
     * Returns true if the enemy's velocity needs to be clamped. If
     * so, clamps the horizontal velocity of this enemy so that it does
     * not exceed the maximum speed.
     *
     * @returns true if this method clamped the enemy's velocity.
     * */
    private boolean clampVelocity(){
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
        if(!clampVelocity()) getBody().applyForceToCenter(-MOVEMENT_FORCE, 0, true);
    }

    /**
     * Moves the Enemy right, or clamps its velocity if it is over
     * the maximum speed.
     * */
    private void moveRight(){
        //adjust force
        this.vision.setDirection((float) Math.PI);

        //if we didn't need to clamp velocity, increase it.
        if(!clampVelocity()) getBody().applyForceToCenter(MOVEMENT_FORCE, 0, true);
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        return false;
    }
}
