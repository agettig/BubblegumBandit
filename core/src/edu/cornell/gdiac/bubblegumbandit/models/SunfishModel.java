package edu.cornell.gdiac.bubblegumbandit.models;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;
import org.w3c.dom.Text;

/** Class to represent the moving ship on the level select screen */
public class SunfishModel extends WheelObstacle {

    /** texture of the sunfish */
    private TextureRegion texture;

    /** texture of the fire */
    private TextureRegion fire_texture;

    /** Rotate image for front of ship to point */
    private static final float ANGLE_OFFSET =(float) (90 * (Math.PI / 180));

    /** current movement of the ship*/
    private Vector2 movement;

    /** Cache for internal force calculations */
    private Vector2 forceCache;

    /** radius of the physics body */
    private static final float RADIUS = 1f;

    /** last location of the cursor */
    private Vector2 lastPos;

    /** The ship's speed */
    private static final float SPEED = 10f;


    /** desired angle of rotation */
    private float angle;

    /** the rate at which to update angle */
    private static final float ANGLE_RATE = 0.1f;

    /**
     * The amount to slow the ship down
     */
    private static final float DAMPING = 5f;

    /**
     * The amount to speed the ship up
     */
    private static final float THRUST = 20f;


    /** an array of fire coming out of the sunfish */
    private Array<Fire> exhaust;

    /** How long between shots */
    private static final int COOLDOWN_TIME  = 8;

    /** How long before ship can fire again */
    private int cooldown;

    /** offset of texture position where ship should be drawn */
    private static Vector2 ship_offset;

    /** offset of ship position where exhaust should be drawn */
    private static Vector2 exhaust_offset;

    public SunfishModel (TextureRegion texture, TextureRegion fire_texture, float x, float y){
        super(x, y, RADIUS);
        this.texture = texture;
        this.fire_texture = fire_texture;
        movement = new Vector2(SPEED, SPEED);
        forceCache = new Vector2();
        lastPos = new Vector2();
        setMass(0.01f);
        exhaust = new Array<Fire>();
        cooldown = 0;
        ship_offset = new Vector2(texture.getRegionWidth()/ 2, texture.getRegionHeight() / 2);
        exhaust_offset = new Vector2(0, ship_offset.y * 1.5f);

    }


    /**
     * Sets movement of the ship.
     *
     * @param value movement of this character.
     */
    public void setMovement(Vector2 value) {

        lastPos = new Vector2(value);

        // Take the vector that goes from body origin to mouse in camera space
        Vector2 a = body.getPosition();
        Vector2 d = value.sub(a);

        angle = d.angleRad();// - ANGLE_OFFSET;

        movement = new Vector2(SPEED, 0);
        movement.rotateRad(angle);

//        System.out.println(movement);
    }

    public void update(float dt){
//        System.out.println(getVX());
        setMovement(lastPos);
        float dst = getPosition().dst(lastPos);

        //thrust distance
        if (dst > 500){
            forceCache.set(movement).scl(THRUST);
            body.setTransform(body.getPosition().add(movement), 0);
        }
        //normal speed distance
        else if (dst > 100){
            forceCache.set(movement);
            body.setTransform(body.getPosition().add(movement), 0);
        }
        // damping distance
        else{
            forceCache.set(-DAMPING * getVX(), -DAMPING * getVY());

        }
        body.applyForce(forceCache, getPosition(), true);
//            body.applyLinearImpulse(movement, getPosition(), true);
        body.setTransform(body.getPosition(), angle);

        //add exhaust
        if (cooldown <= 0 ) {

            Vector2 offset = new Vector2(exhaust_offset);
            offset.rotateRad(angle + ANGLE_OFFSET);

            exhaust.add(new Fire(getX() + offset.x, getY() +offset.y));
            cooldown = COOLDOWN_TIME;
        }
        else{
            cooldown --;
        }

        //remove dead fires
        for (Fire fire : exhaust){
            if (fire.isAlive()){
                fire.update(dt);
            }
            else{
                exhaust.removeValue(fire, true);
            }
        }
    }



    public void draw(GameCanvas canvas){

        for (Fire fire : exhaust){
            fire.draw(canvas);
        }

//        float x_offset = (texture.getRegionWidth() / 2);
//        float y_offset = (texture.getRegionHeight() / 2);

        canvas.draw(texture, Color.WHITE, origin.x + ship_offset.x, origin.y + ship_offset.y, getX(), getY(), getAngle() - ANGLE_OFFSET, 1, 1);
    }

    /** an inner class that represents the fire coming out of the sunfish */

    private class Fire {
        /** MAX age of a fire object */
        private static final float MAX_AGE = 50;

        /** X-coordinate of fire position */
        public float x;
        /** Y-coordinate of fire position */
        public float y;

        /** Age for the fire in frames (for decay) */
        public float age;

        /** Amount to scale the fire size */
        private float scale;

        /**
         * Creates a fire object by setting its position and velocity.
         *
         * A newly allocated photon starts with age 0.
         *
         * @param x  The x-coordinate of the position
         * @param y  The y-coordinate of the position
         */
        public Fire(float x, float y) {
            this.x  = x;  this.y  = y;
            this.age = 0;
            this.scale = 1;
        }

        public void update(float dt) {
            age++;
            scale = 1 - age/MAX_AGE;
        }

        /** Returns true if this fire object should still be drawn */
        public boolean isAlive() {
            return age < MAX_AGE;
        }

        public void draw (GameCanvas canvas){
            canvas.draw(fire_texture, Color.WHITE, origin.x, origin.y, x, y, 0, scale, scale);
        }

    }


}
