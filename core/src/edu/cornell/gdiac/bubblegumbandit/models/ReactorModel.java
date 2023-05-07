package edu.cornell.gdiac.bubblegumbandit.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;

/** class to represent the dual reactors around an orb */
public class ReactorModel {
    private TextureRegion texture;
    private Vector2 reactor1;
    private Vector2 reactor2;
    public ReactorModel(TextureRegion texture, Vector2 pos) {
        this.texture = texture;
    }
    public void draw(GameCanvas canvas){

    }
}
