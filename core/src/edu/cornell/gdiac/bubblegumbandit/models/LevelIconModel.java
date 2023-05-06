package edu.cornell.gdiac.bubblegumbandit.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import edu.cornell.gdiac.bubblegumbandit.helpers.SaveData;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import org.w3c.dom.Text;

import static java.lang.String.valueOf;

public class LevelIconModel {

    /** texture of the ship */
    private TextureRegion texture;

    /** texture for the level number to sit in*/
    private TextureRegion marker;

    /** texture to mark a successful npc rescue*/
    private TextureRegion success;

    /** texture to mark a unsuccessful or incomplete npc rescue*/
    private TextureRegion fail;

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

    /** the state of the button
     *
     * 0 - regular
     * 1 - hovered over
     * 2 - clicked
     *
     * */
    private int pressState;

    private static final float HOVER_SPEED = 0.1f;
    private static final float HOVER_DIFF = 10f;

    private float max_hover;

    private float min_hover;

    private boolean goingDown;

    /** The number of collected stars in the level, updated based on save data*/
    private int stars;

    public LevelIconModel (TextureRegion texture, TextureRegion marker, TextureRegion success, TextureRegion fail, int level, float x, float y) {
        this.texture = texture;
        this.marker = marker;
        this.success = success;
        this.fail = fail;
        this.level = level;
        this.x = x;
        this.y = y;
        tint = Color.WHITE;
        max_hover = y + HOVER_DIFF;
        min_hover = y - HOVER_DIFF;
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

        //update stats from save data
//        stars = SaveData.getStars(level);

    }


    public void draw(GameCanvas canvas, BitmapFont font){
        Vector2 pos = getCenter();
        Color numTint = Color.WHITE;

        //draw ship icon
        if (SaveData.unlocked(level)){
            canvas.draw(texture, tint, x, y, texture.getRegionWidth(), texture.getRegionHeight());

            //draw npc status
            drawNPCs(canvas, pos);
        } else {
            canvas.draw(texture, Color.DARK_GRAY, x, y, texture.getRegionWidth(), texture.getRegionHeight());
            numTint = Color.DARK_GRAY;
        }

        //draw number icons
        canvas.draw(marker, numTint, marker.getRegionWidth()/2, 0, pos.x, pos.y + texture.getRegionHeight()/2f + MARKER_OFFSET, 0, 1, 1);
        canvas.drawText(valueOf(level), font,numTint, pos.x, pos.y + texture.getRegionHeight()/2f +marker.getRegionHeight() * 0.7f + MARKER_OFFSET, 1, Align.center, false);
    }

    public void drawNPCs(GameCanvas canvas, Vector2 center){
        //TODO: replace with calls to SaveData
        //3 total saves, 2 success, 1 failure
        int total = 3;
        int successes = 2;

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
            canvas.draw(icon, Color.WHITE, success.getRegionWidth()/2, success.getRegionHeight(),
                    start + space * i, center.y - texture.getRegionHeight()/2f - MARKER_OFFSET, 0,
                    1, 1);

        }
    }


}
