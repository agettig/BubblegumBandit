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
    private TextureRegion reactor;

    private TextureRegion reactorOff;

    private TextureRegion beam;
    private TextureRegion casing;

    private TextureRegion computer;
    private Vector2 reactor1 = new Vector2(0,0);
    private Vector2 reactor2 = new Vector2(0,0);

    private PolygonRegion beamReg;

    /** How fast the lights on the reactor turn off after orb is collected, per tick */
    private static final float FADE_RATE = 0.005f;

    /** the number of ticks that have passed */
    private float ticks;

    private boolean orbCollected;

    /** World scale */
    private static final float SCALE = 64;

    /** the x drawing scale of the beam of light between the reactor cores, decreases after orb is collected */
    private float beamScale = 1;

    private Color fadeTint = Color.WHITE.cpy();

    private boolean vertical;



    public ReactorModel(Array<Vector2> pos, Vector2 orbPos, AssetDirectory directory) {
        reactor = new TextureRegion (directory.getEntry("reactorCore", Texture.class));
        reactorOff = new TextureRegion (directory.getEntry("reactorCoreOff", Texture.class));
        beam = new TextureRegion (directory.getEntry("beam", Texture.class));
        casing = new TextureRegion (directory.getEntry("case", Texture.class));
        computer = new TextureRegion (directory.getEntry("computer", Texture.class));

        reactor1.set(pos.get(0));
        reactor2.set(pos.get(1));

        //vertically aligned around orb
        if (reactor1.x == reactor2.x) {
            reactor1.x = reactor2.x = orbPos.x;
            vertical = true;
        }
        //horizontally aligned around orb
        else {
            reactor1.y = reactor2.y = orbPos.y;
            vertical = false;
        }

        System.out.println(beam.getRegionHeight());

        beam.setRegionHeight((int) ((reactor2.y + 1 - reactor1.y) *SCALE));
        System.out.println(beam.getRegionHeight());
    }

    public void orbCollected (boolean value){
        orbCollected = value;
        if (!value) beamScale = 1;
    }

    public void update (){
        if (orbCollected && beamScale > 0){
            beamScale -= 0.1f;
        }
        if (beamScale < 0) beamScale = 0;

        fadeTint.set(1, 1, 1, beamScale);
    }
    public void draw(GameCanvas canvas){

//        if(vertical) {
            canvas.draw(beam, Color.WHITE, beam.getRegionWidth() / 2f, 0, reactor1.x * SCALE, reactor1.y * SCALE, 0, beamScale, 1);
//        canvas.drawWithShadow(computer, Color.WHITE, 0, 0,(reactor1.x - 10 )* SCALE, reactor1.y * SCALE,  0, 1, 1);
//        canvas.draw(casing, Color.WHITE, casing.getRegionWidth()/2f, 0,reactor1.x * SCALE, reactor1.y * SCALE,  0, 1, 1);

            canvas.drawWithShadow(reactorOff, Color.WHITE, reactor.getRegionWidth() / 2f, 0, reactor1.x * SCALE, reactor1.y * SCALE, 0, 1, 1);
            canvas.drawWithShadow(reactorOff, Color.WHITE, reactor.getRegionWidth() / 2f, 0, reactor2.x * SCALE, reactor2.y * SCALE + reactor.getRegionHeight(), 0, 1, -1f);

            canvas.draw(reactor, fadeTint, reactor.getRegionWidth() / 2f, 0, reactor1.x * SCALE, reactor1.y * SCALE, 0, 1, 1);
            canvas.draw(reactor, fadeTint, reactor.getRegionWidth() / 2f, 0, reactor2.x * SCALE, reactor2.y * SCALE + reactor.getRegionHeight(), 0, 1, -1f);
//        }
//        else {
//            canvas.draw(beam, Color.WHITE, beam.getRegionWidth() / 2f, 0, reactor1.x * SCALE, reactor1.y * SCALE, 0, beamScale, 1);
//
//            canvas.draw(reactorOff, fadeTint, reactor.getRegionWidth() / 2f, 0, reactor1.x * SCALE, reactor1.y * SCALE, (float)-Math.PI/2, 1, 1);
//            canvas.draw(reactorOff, fadeTint, reactor.getRegionWidth() / 2f, 0, reactor2.x * SCALE + reactorOff.getRegionWidth(), reactor2.y * SCALE, (float)-Math.PI/2, 1, -1f);
//
//            canvas.draw(reactor, fadeTint, reactor.getRegionWidth() / 2f, 0, reactor1.x * SCALE, reactor1.y * SCALE, (float)-Math.PI/2, 1, 1);
//            canvas.draw(reactor, fadeTint, reactor.getRegionWidth() / 2f, 0, reactor2.x * SCALE + reactorOff.getRegionWidth(), reactor2.y * SCALE, (float)-Math.PI/2, 1, -1f);
//        }
    }
}
