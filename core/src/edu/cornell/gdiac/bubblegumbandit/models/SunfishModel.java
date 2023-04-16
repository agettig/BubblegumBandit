package edu.cornell.gdiac.bubblegumbandit.models;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;
import org.w3c.dom.Text;

/** Class to represent the moving ship on the level select screen */
public class SunfishModel extends WheelObstacle {

    /** texture of the sunfish */
    private TextureRegion texture;

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
    private static final float SPEED = 50f;


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


    public SunfishModel (TextureRegion texture, float x, float y){
        super(x, y, RADIUS);
        this.texture = texture;
        movement = new Vector2(SPEED, SPEED);
        forceCache = new Vector2();
        lastPos = new Vector2();
        setMass(0.01f);
    }


    /**
     * Sets movement of the ship.
     */
    public void setMovement(float x, float y){
        Vector2 vec = new Vector2(x, y);
        setMovement(vec);
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
        }
        //normal speed distance
        else if (dst > 100){
//            body.setTransform(body.getPosition().add(movement), 0);
            forceCache.set(movement);
        }
        // damping distance
        else{
            forceCache.set(-DAMPING * getVX(), 0);
        }

        body.applyForce(forceCache, getPosition(), true);
//            body.applyLinearImpulse(movement, getPosition(), true);
        body.setTransform(body.getPosition(), angle);
    }



    public void draw(GameCanvas canvas){

        float x_offset = (texture.getRegionWidth() / 2);
        float y_offset = (texture.getRegionHeight() / 2);

        canvas.draw(texture, Color.WHITE, origin.x + x_offset, origin.y + y_offset, getX(), getY(), getAngle() - ANGLE_OFFSET, 1, 1);
//        canvas.drawPhysicsLevel(shape, Color.WHITE, getX(), getY(), drawScale.x, drawScale.y);
    }

}
