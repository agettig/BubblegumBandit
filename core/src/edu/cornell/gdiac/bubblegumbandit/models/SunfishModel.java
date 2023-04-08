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

    private static final float RADIUS = 10f;

    public SunfishModel (TextureRegion texture, float x, float y){
        super(x, y, RADIUS);
        this.texture = texture;
    }
    public void draw(GameCanvas canvas){
        canvas.draw(texture, getX(), getY());
    }
}
