package edu.cornell.gdiac.bubblegumbandit.models;


import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import org.w3c.dom.Text;

/** Class to represent the moving ship on the level select screen */
public class SunfishModel {

    /** texture of the sunfish */
    private TextureRegion texture;
    public SunfishModel (TextureRegion texture){
        this.texture = texture;
    }
    public void draw(GameCanvas canvas){
        canvas.draw(texture, 100, 100);
    }
}
