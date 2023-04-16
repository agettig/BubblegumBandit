package edu.cornell.gdiac.bubblegumbandit.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import org.w3c.dom.Text;

import static java.lang.String.valueOf;

public class LevelIconModel {

    /** texture of the ship */
    private TextureRegion texture;

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

    private static float max_hover;

    private static float min_hover;

    private boolean goingDown;

    public LevelIconModel (TextureRegion texture, int level, float x, float y) {
        this.texture = texture;
        this.level = level;
        this.x = x;
        this.y = y;
        tint = Color.WHITE;
        max_hover = y + HOVER_DIFF;
        min_hover = y - HOVER_DIFF;
    }

    public void setPressState(int value) {

        if (value == 1){
            tint = new Color(.38f, .78f, .81f, 1f);
        }
        else if (value == 2){
            tint = new Color(.18f, .58f, .61f, 1);
        }
        else{
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


    public void update() {

        if (y >= max_hover) goingDown = true;
        if ( y <= min_hover) goingDown = false;

        if (y < max_hover && !goingDown) {
            y += HOVER_SPEED;
        }
        if (y > min_hover && goingDown){
            y -= HOVER_SPEED;
        }
    }

    public void draw(GameCanvas canvas, BitmapFont font){
        canvas.draw(texture, tint, x, y, texture.getRegionWidth(), texture.getRegionHeight());
        canvas.drawText(valueOf(level), font, (float) (x + 0.5 * texture.getRegionWidth()), (float) (y + 0.5 * texture.getRegionHeight()));
    }


}
