package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.assets.AssetDirectory;

public class GameOverScreen implements Screen {

    /**
     * Whether or not this game over screen is still active
     */
    private boolean active;

    /**
     * Standard window size (for scaling)
     */
    private static int STANDARD_WIDTH = 800;
    /**
     * Standard window height (for scaling)
     */
    private static int STANDARD_HEIGHT = 700;

    /**
     * Reference to GameCanvas created by the root
     */
    private GameCanvas canvas;

    private float scale;

    /** Background Space texture */
    private TextureRegion background;

    /**
     * The font for giving messages to the player
     */
    protected BitmapFont displayFont;

    /**
     * Creates a GameOverScreen with the default size and position.
     *
     * @param directory  The asset directory to load in the background
     * @param canvas The game canvas to draw to
     */
    public GameOverScreen(AssetDirectory directory, GameCanvas canvas) {
        this.canvas = canvas;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(), canvas.getHeight());

        background = new TextureRegion(directory.getEntry("spaceBg", Texture.class));
        displayFont = directory.getEntry("projectSpace", BitmapFont.class);
    }


    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    public void render(float delta) {}

    public void pause() {}

    public void resume() {}

    public void hide() {
        active = false;
    }

    public void dispose() {}

    /**
     * Called when the Screen is resized.
     * <p>
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);
    }
}
