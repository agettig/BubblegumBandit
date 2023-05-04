package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.GameController;
import edu.cornell.gdiac.util.ScreenListener;

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

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    private final static float SPACE_WIDTH = 5000;
    private final static float SPACE_HEIGHT = 5000;

    public GameOverScreen() {}

    /**
     * Creates a GameOverScreen with the default size and position.
     *
     * @param directory  The asset directory to load in the background
     */

    public void initialize(AssetDirectory directory, GameCanvas canvas) {
        this.canvas = canvas;
        active = true;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(), canvas.getHeight());

        background = new TextureRegion(directory.getEntry("spaceBg", Texture.class));
        displayFont = directory.getEntry("projectSpace", BitmapFont.class);
    }

    @Override
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    @Override
    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            draw();
        }
        if (false) {
            listener.exitScreen(this, 0);
        }
    }

    private void draw() {
        canvas.clear();
        canvas.begin();
        drawBackground(canvas);
        canvas.end();
    }

    /**
     * Draws a repeating background, and crops off any overhangs outside the level
     * to maintain resolution and aspect ratio.
     *
     * @param canvas the current canvas
     */
    private void drawBackground(GameCanvas canvas) {
        for (int i = 0; i < SPACE_WIDTH; i += background.getRegionWidth()) {
            for (int j = 0; j < SPACE_HEIGHT; j += background.getRegionHeight()) {
                canvas.draw(background, i, j);
            }
        }
    }

    public void pause() {}

    public void resume() {}

    @Override
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

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }
}
