package edu.cornell.gdiac.bubblegumbandit.models;


import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;
import org.w3c.dom.Text;

/** Class to represent the moving ship on the level select screen */
public class SunfishModel extends WheelObstacle {

    /** texture of the sunfish */
    private TextureRegion texture;

    /** current movement of the ship*/
    private Vector2 movement;

    /** Cache for internal force calculations */
    private Vector2 forceCache;
    private static final float RADIUS = 1f;

    public SunfishModel (TextureRegion texture, float x, float y){
        super(x, y, RADIUS);
        this.texture = texture;
        movement = new Vector2();
        forceCache = new Vector2();
    }

    /**
     * Sets movement of this character.
     *
     * @param value movement of this character.
     */
    public void setMovement(Vector2 value) {
        movement = value;
    }

    /**
     * Sets movement of this character.
     */
    public void setMovement(float x, float y){
        Vector2 vec = new Vector2(x, y);
        setMovement(vec);
    }

    public void applyForce(){

//        System.out.println(movement);
//        body.applyForce(movement,getPosition(),true);
//        System.out.println(getX());
        setPosition(movement);
    }
    public void draw(GameCanvas canvas){
        canvas.draw(texture, getX(), getY());
    }
}
