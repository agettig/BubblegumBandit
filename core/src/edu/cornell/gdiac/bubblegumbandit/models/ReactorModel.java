package edu.cornell.gdiac.bubblegumbandit.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;

/** class to represent the dual reactors around an orb */
public class ReactorModel {
    private TextureRegion texture;

    private TextureRegion beam;
    private TextureRegion casing;

    private TextureRegion computer;
    private Vector2 reactor1 = new Vector2(0,0);
    private Vector2 reactor2 = new Vector2(0,0);

    private PolygonRegion beamReg;

    public ReactorModel(Array<Vector2> pos, Vector2 orbPos, AssetDirectory directory) {
        texture = new TextureRegion (directory.getEntry("reactorCore", Texture.class));
        beam = new TextureRegion (directory.getEntry("beam", Texture.class));
        casing = new TextureRegion (directory.getEntry("case", Texture.class));
        computer = new TextureRegion (directory.getEntry("computer", Texture.class));

        reactor1.set(pos.get(0));
        reactor2.set(pos.get(1));

        //vertically aligned around orb
        if (reactor1.x == reactor2.x) {
            reactor1.x = reactor2.x = orbPos.x;
        }
        //horizontally aligned around orb
        else {
            reactor1.y = reactor2.y = orbPos.y;
        }

        System.out.println(beam.getRegionHeight());

        beam.setRegionHeight((int) ((reactor2.y + 1 - reactor1.y) *64));
        System.out.println(beam.getRegionHeight());
    }
    public void draw(GameCanvas canvas){
        canvas.draw(beam, Color.WHITE, beam.getRegionWidth()/2f, 0,reactor1.x * 64, reactor1.y * 64,  0, 1, 1);
//        canvas.drawWithShadow(computer, Color.WHITE, 0, 0,(reactor1.x - 10 )* 64, reactor1.y * 64,  0, 1, 1);
//        canvas.draw(casing, Color.WHITE, casing.getRegionWidth()/2f, 0,reactor1.x * 64, reactor1.y * 64,  0, 1, 1);
        canvas.drawWithShadow(texture, Color.WHITE, texture.getRegionWidth()/2f, 0,reactor1.x * 64, reactor1.y * 64,  0, 1, 1);
        canvas.drawWithShadow(texture, Color.WHITE, texture.getRegionWidth()/2f,0,reactor2.x * 64, reactor2.y * 64 + texture.getRegionHeight(),  0, 1, -1f);
    }
}
