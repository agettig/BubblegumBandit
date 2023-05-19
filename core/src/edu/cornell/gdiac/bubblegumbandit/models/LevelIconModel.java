package edu.cornell.gdiac.bubblegumbandit.models;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.EffectController;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.helpers.SaveData;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import org.w3c.dom.Text;

import java.util.Random;

import static java.lang.String.valueOf;

public class LevelIconModel {

    /** texture of the ship */
    private TextureRegion texture;

    /** texture for the level number to sit in*/
    private static TextureRegion marker;

    /** texture to mark a successful npc rescue*/
    private static TextureRegion success;

    /** texture to mark a unsuccessful or incomplete npc rescue*/
    private static TextureRegion fail;

    /** the distance between the bottom of the marker and the top of the ship, in pixels*/
    private static final float MARKER_OFFSET = 30;

    /** the distance between each icon for a save, in pixels*/
    private static final float NPC_SPACE = 20;

    /** The level number this icon represents */
    private int level;

    /** x position of the texture */
    public float x;

    /** y position of the texture */
    public float y;

    /** color to tint the level texture, determined by press state */
    private Color tint;

    /** the tint of an exploded, dead ship */
    private static final Color DEAD = new Color(255/255f, 100/255f, 120/255f, 1);

    /** the state of the button
     *
     * 0 - regular
     * 1 - hovered over
     * 2 - clicked
     *
     * */
    private int pressState;

    // variables for ship vertical hover
    private static final float HOVER_SPEED = 0.1f;
    private static final float HOVER_DIFF = 10f;

    private float max_hover;

    private float min_hover;

    private boolean goingDown;

    //Explosion variables

    /** the final rotation angle of an exploded ship*/
    private static final float ANGLE = (float) (Math.PI/10);

    /** whether this ship has exploded yet */
    private boolean exploded = false;

    /** Explosion effect drawn when a level is completed, repeated completions trigger the effect again */
    private EffectController explosionEffectController;

    /** Random number generator, used to set the placement of explosions */
    private Random rand = new Random();

    /** the current rotation angle of the ship */
    private float angle;

    /** the rotation rate of an exploding ship */
    private static final float ROTATION_RATE = 0.002f;

    //endRegion



    /** sets the static attributes of the class */
    public static void setTextures(AssetDirectory directory){
        marker = new TextureRegion (directory.getEntry("marker", Texture.class));
        success = new TextureRegion (directory.getEntry("o", Texture.class));
        fail = new TextureRegion (directory.getEntry("x", Texture.class));

    }

    public LevelIconModel (AssetDirectory directory, AssetDirectory internal, int level, float x, float y) {

        Texture entry = internal.getEntry("ship" + valueOf(level), Texture.class);
        if (entry == null){
            entry = internal.getEntry("ship1", Texture.class);
        }

        this.texture = new TextureRegion(entry);


        this.level = level;
        this.x = x;
        this.y = y;
        tint = Color.WHITE;
        max_hover = y + HOVER_DIFF;
        min_hover = y - HOVER_DIFF;

        explosionEffectController = new EffectController("explosion", "explosion",
                directory, true, true, 0.03f);

    }

    public void setPressState(int value) {

        if (value == 1){
            pressState = 1;
            tint = new Color(.38f, .78f, .81f, 1f);
        }
        else if (value == 2){
            pressState = 2;
            tint = new Color(.18f, .58f, .61f, 1);
        }
        else{
            pressState = 0;
            tint = Color.WHITE;
        }
    }

    public int getState() {
        return pressState;
    }

    /** returns true if cursor is in bounds of the texture */
    public boolean onIcon(float sx, float sy){
        return (sx >= x && sx <= x + texture.getRegionWidth() && sy >= y && sy <= y + texture.getRegionHeight());
    }

    public int getLevel() {
        return level;
    }

    /** returns the x,y coordinates of the center of the icon*/
    public Vector2 getCenter(){
        return new Vector2(x + 0.5f * texture.getRegionWidth(), y + 0.5f * texture.getRegionHeight());
    }

    /** whether this level is unlocked or not, locked levels are inaccessible by the player */
    public boolean isUnlocked() {
        return SaveData.unlocked(level);
    }
    public void update() {

        explosionEffectController.update();

        //update position based on hover
        if (y >= max_hover) {
            y = max_hover;
            goingDown = true;
        }
        else if ( y <= min_hover) {
            y = min_hover;
            goingDown = false;
        }

        if (y < max_hover && !goingDown) {
            y += HOVER_SPEED;
        }
        else if (y > min_hover && goingDown){
            y -= HOVER_SPEED;
        }

        if (SaveData.completed(level) && !exploded) {
            makeExplosion(x + rand.nextInt(texture.getRegionWidth()),y + rand.nextInt(texture.getRegionHeight()));
            angle += ROTATION_RATE;
        }
        if (angle >= ANGLE){
            angle = ANGLE;
            exploded = true;
        }

    }


    public void draw(GameCanvas canvas, BitmapFont font){
        Vector2 pos = getCenter();
        Color numTint = Color.WHITE;

        //draw ship icon
        if (SaveData.completed(level)){
            canvas.draw(texture, tint.cpy().mul(DEAD), texture.getRegionWidth()/2f, texture.getRegionHeight()/2f,
                    x + texture.getRegionWidth()/2f,y + texture.getRegionHeight()/2f,angle, 1, 1);
            drawNPCs(canvas, pos);

        }
        else if (SaveData.unlocked(level)){
            canvas.draw(texture, tint, x, y, texture.getRegionWidth(), texture.getRegionHeight());
            //draw npc status
            drawNPCs(canvas, pos);
        } else {
            //locked levels are grayed out
            canvas.draw(texture, Color.DARK_GRAY, x, y, texture.getRegionWidth(), texture.getRegionHeight());
            numTint = Color.DARK_GRAY;
        }

        //draw number icons
        canvas.draw(marker, numTint, marker.getRegionWidth()/2f, 0, pos.x, pos.y + texture.getRegionHeight()/2f + MARKER_OFFSET, 0, 1, 1);
        canvas.drawText(valueOf(level), font,numTint, pos.x, pos.y + texture.getRegionHeight()/2f +marker.getRegionHeight() * 0.7f + MARKER_OFFSET, 1, Align.center, false);

        explosionEffectController.draw(canvas);
    }

    /** Draw npc indicator icons under the level */
    private void drawNPCs(GameCanvas canvas, Vector2 center){
        int total = SaveData.getCaptiveCount(level);
        int successes = SaveData.completed(level) ? SaveData.getStars(level) : 0;

        //the start x, relative to the center: center.x - the size the icons will take up / 2
        float start = center.x - (((total-1) * (success.getRegionWidth() + NPC_SPACE))/ 2f);
        float space = NPC_SPACE + success.getRegionWidth();
        //note: success and fail icons are the same size
        TextureRegion icon = success;

        for (int i = 0; i < total; i++){

            if (i == successes){
                //once drawn all the successes, the rest are failures
                icon = fail;
            }
            canvas.draw(icon, Color.WHITE, success.getRegionWidth()/2f, success.getRegionHeight(),
                    start + space * i, center.y - texture.getRegionHeight()/2f - MARKER_OFFSET, 0,
                    1, 1);

        }
    }


    /** sets an explosion */
    private void makeExplosion(float x, float y){
        explosionEffectController.makeEffect(x, y, new Vector2(1, 1), false);
        SoundController.playSound("shipExplosion",0.5f);
        SoundController.lastPlayed(-26);
    }


}
