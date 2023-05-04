package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.GameController;
import edu.cornell.gdiac.util.ScreenListener;
import org.w3c.dom.Text;

public class GameOverScreen implements Screen, InputProcessor {

    /**
     * The current state of the play button
     * <p>
     * 0 = nothing pressed
     * 1 = play down
     * 2 = level select down
     * 5 = play up, ready to go
     * 6 = level select up, should open level select
     */
    private int pressState;

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

    private String gameOverMessage;

    /**
     * Pointer to what is being hovered.
     */
    private Texture hoverPointer;

    /**
     * Texture to continue the game.
     */
    private Texture continueGameButton;

    /**
     * Pointer to take player back to level select.
     */
    private Texture levelSelectButton;

    /**
     * The x-coordinate of the center of the start button.
     */
    private int startButtonPositionX;

    /**
     * The y-coordinate of the center of the start button.
     */
    private int startButtonPositionY;

    /**
     * The x-coordinate of the center of the level select button.
     */
    private int levelSelectButtonPositionX;

    /**
     * The y-coordinate of the center of the level select button.
     */
    private int levelSelectButtonPositionY;

    /**
     * Scale of continue game, back to level select buttons.
     */
    private final float BUTTON_SCALE = .3f;

    /**
     * true if the player is hovering over the start button
     */
    private boolean hoveringStart;

    /**
     * true if the player is hovering over the level select button
     */
    private boolean hoveringLevelSelect;

    /**
     * The height of the canvas window (necessary since sprite origin != screen origin)
     */
    private int heightY;


    public GameOverScreen() {}

    /**
     * Creates a GameOverScreen for when the player wins, with the default size and position.
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

        hoverPointer = directory.getEntry("hoverPointer", Texture.class);
        continueGameButton = directory.getEntry("continueGameButton", Texture.class);
        levelSelectButton = directory.getEntry("levelSelectButton", Texture.class);

    }

    public void gameWon() {
        gameOverMessage = "You Won!";
    }

    public void gameLost() {
        gameOverMessage = "You Lost!";
    }

    @Override
    public void show() {
        // Useless if called in outside animation loop
        active = true;
        Gdx.input.setInputProcessor(this);
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
            if (continueGame() && listener != null) {
                listener.exitScreen(this, 1);
            }
            if (levelSelect() && listener != null) {
                listener.exitScreen(this, 6);
            }
        }
        if (false) {
            listener.exitScreen(this, 0);
        }
    }

    private void draw() {
        canvas.clear();
        canvas.begin();
        resize((int)canvas.getCamera().viewportWidth, (int)canvas.getCamera().viewportHeight);
        canvas.draw(background, Color.WHITE, 0, 0, canvas.getCamera().viewportWidth, canvas.getCamera().viewportHeight);
        canvas.draw(background, 0, 25);
        canvas.drawTextCentered(gameOverMessage, displayFont, 150);

        float highestButtonY = canvas.getCamera().viewportHeight / 2;
        float lowestButtonY = canvas.getCamera().viewportHeight / 6;
        float buttonSpace = highestButtonY - lowestButtonY;
        float gap = buttonSpace / 4;


        startButtonPositionX = (int) canvas.getCamera().viewportWidth / 5;
        startButtonPositionY = (int) highestButtonY;

        levelSelectButtonPositionX = (int) canvas.getCamera().viewportWidth / 5;
        levelSelectButtonPositionY = (int) (highestButtonY - gap);

        float pointerX = startButtonPositionX / 4f;

        //Draw Continue Game
        canvas.draw(
                continueGameButton,
                getButtonTint("continue"),
                continueGameButton.getWidth() / 2f,
                continueGameButton.getHeight() / 2f,
                startButtonPositionX,
                startButtonPositionY,
                0,
                scale * BUTTON_SCALE,
                scale * BUTTON_SCALE
        );
        if (hoveringStart) {
            canvas.draw(
                    hoverPointer,
                    Color.WHITE,
                    hoverPointer.getWidth() / 2f,
                    hoverPointer.getHeight() / 2f,
                    pointerX,
                    startButtonPositionY,
                    0,
                    scale,
                    scale
            );
        }

        //Draw Level Select
        canvas.draw(
                levelSelectButton,
                getButtonTint("level"),
                levelSelectButton.getWidth() / 2f,
                levelSelectButton.getHeight() / 2f,
                levelSelectButtonPositionX,
                levelSelectButtonPositionY,
                0,
                scale * BUTTON_SCALE,
                scale * BUTTON_SCALE
        );
        if (hoveringLevelSelect) {
            canvas.draw(
                    hoverPointer,
                    Color.WHITE,
                    hoverPointer.getWidth() / 2f,
                    hoverPointer.getHeight() / 2f,
                    pointerX,
                    levelSelectButtonPositionY,
                    0,
                    scale,
                    scale
            );
        }

        canvas.end();
    }

    private Color getButtonTint(String buttonName) {
        int hoverR = 241;
        int hoverG = 154;
        int hoverB = 142;
        int hoverA = 255;
        int hoverRgba8888 = (hoverR << 24) | (hoverG << 16) | (hoverB << 8) | hoverA;
        Color hoverTint = new Color(hoverRgba8888);

        int pressR = 70;
        int pressG = 153;
        int pressB = 167;
        int pressA = 255;
        int pressRgba8888 = (pressR << 24) | (pressG << 16) | (pressB << 8) | pressA;
        Color pressTint = new Color(pressRgba8888);

        Color defaultTint = Color.WHITE;

        if (buttonName.equals("continue")) {
            if (hoveringStart && pressState == 1) return pressTint;
            else if (hoveringStart) return hoverTint;
            else return defaultTint;
        }

        if (buttonName.equals("level")) {
            if (hoveringLevelSelect && pressState == 2) return pressTint;
            else if (hoveringLevelSelect) return hoverTint;
            else return defaultTint;
        }

        return null;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {

        Vector2 pixelMouse = canvas.unproject(new Vector2(screenX, screenY));

        float pixelX = pixelMouse.x;
        float pixelY = pixelMouse.y;

        if (continueGameButton == null || levelSelectButton == null) return false;
        // Flip to match graphics coordinates

        //Detect hovers on the start button
        float rectWidth = scale * BUTTON_SCALE * continueGameButton.getWidth();
        float rectHeight = scale * BUTTON_SCALE * continueGameButton.getHeight();
        float leftX = startButtonPositionX - rectWidth / 2.0f;
        float rightX = startButtonPositionX + rectWidth / 2.0f;
        float topY = (startButtonPositionY - (rectHeight) / 2.0f);
        float bottomY = (startButtonPositionY + (rectHeight) / 2.0f);
        hoveringStart = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

        //Detect hovers on the level select button
        rectWidth = scale * BUTTON_SCALE * levelSelectButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * levelSelectButton.getHeight();
        leftX = levelSelectButtonPositionX - rectWidth / 2.0f;
        rightX = levelSelectButtonPositionX + rectWidth / 2.0f;
        topY = levelSelectButtonPositionY - rectHeight / 2.0f;
        bottomY = levelSelectButtonPositionY + rectHeight / 2.0f;
        hoveringLevelSelect = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

        return true;
    }

    /**
     * Called when the screen was touched or a mouse button was pressed.
     * <p>
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (pressState == 2) {
            return true;
        }

        Vector2 pixelMouse = canvas.unproject(new Vector2(screenX, screenY));

        float pixelX = pixelMouse.x;
        float pixelY = pixelMouse.y;

        // Flip to match graphics coordinates
        screenY = heightY - screenY;


        // if loading has not started
        if (continueGameButton == null || levelSelectButton == null) return false;

        //Detect clicks on the start button
        float rectWidth = scale * BUTTON_SCALE * continueGameButton.getWidth();
        float rectHeight = scale * BUTTON_SCALE * continueGameButton.getHeight();
        float leftX = startButtonPositionX - rectWidth / 2.0f;
        float rightX = startButtonPositionX + rectWidth / 2.0f;
        float topY = startButtonPositionY - rectHeight / 2.0f;
        float bottomY = startButtonPositionY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 1;
        }

        //Detect clicks on the level select button
        rectWidth = scale * BUTTON_SCALE * levelSelectButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * levelSelectButton.getHeight();
        leftX = levelSelectButtonPositionX - rectWidth / 2.0f;
        rightX = levelSelectButtonPositionX + rectWidth / 2.0f;
        topY = levelSelectButtonPositionY - rectHeight / 2.0f;
        bottomY = levelSelectButtonPositionY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 2;
        }
        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     * <p>
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        //Start
        if (pressState == 1) {
            pressState = 5;
            return false;
        }

        //Level Select
        if (pressState == 2) {
            pressState = 6;
            return false;
        }
        return true;
    }


        /**
         * Called when a key is pressed (UNSUPPORTED)
         *
         * @param keycode the key pressed
         * @return whether to hand the event to other listeners.
         */
    public boolean keyDown(int keycode) {
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param character the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released (UNSUPPORTED)
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        return true;
    }

    public boolean continueGame() { return pressState == 5;}
    public boolean levelSelect() {return pressState == 6;}

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param dx the amount of horizontal scroll
     * @param dy the amount of vertical scroll
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(float dx, float dy) {
        return true;
    }


    public void pause() {}

    public void resume() {}

    @Override
    public void hide() {
        active = false;
        pressState = 0;
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
        heightY = height;
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
