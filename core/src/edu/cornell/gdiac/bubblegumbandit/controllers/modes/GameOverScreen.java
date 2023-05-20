package edu.cornell.gdiac.bubblegumbandit.controllers.modes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.GameController;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.helpers.SaveData;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.util.ScreenListener;
import org.w3c.dom.Text;

public class GameOverScreen implements Screen, InputProcessor {

    /**
     * The current state of the play button
     * <p>
     * 0 = nothing pressed
     * 1 = Try Again or Retry Level down
     * 2 = level select or Continue down
     * 3 = return to title down
     * 5 = try again or retry level up, ready to go
     * 6 = level select or continue up, should open level select
     * 7 = return to title up, should open title screen
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

    private final static float SPACE_WIDTH = 3000;
    private final static float SPACE_HEIGHT = 3000;

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
     * Pointer to take player back to title Screen.
     */
    private Texture titleScreenButton;

    /**
     * The x-coordinate of the center of the start button.
     */
    private int startButtonPositionX;

    /**
     * The y-coordinate of the center of the start button.
     */
    private int startButtonPositionY;

    private boolean isLastLevel;

    /**
     * The x-coordinate of the center of the level select button.
     */
    private int levelSelectButtonPositionX;

    /**
     * The y-coordinate of the center of the level select button.
     */
    private int levelSelectButtonPositionY;

    /**
     * The x-coordinate of the center of the return to title button.
     */
    private int titleScreenButtonPositionX;

    /**
     * The y-coordinate of the center of the return to title button.
     */
    private int titleScreenButtonPositionY;

    /**
     * Scale of continue game, back to level select buttons.
     */
    private final float BUTTON_SCALE = .25f;

    /**
     * true if the player is hovering over the start button
     */
    private boolean hoveringStart;

    /**
     * true if the player is hovering over the level select button
     */
    private boolean hoveringLevelSelect;

    /**
     * true if the player is hovering over the title button
     */
    private boolean hoveringReturnTitleScreen;

    /**
     * icon for captives
     */
    private Texture captiveIcon;
    private Texture emptyIcon;

    /** How much of screen is shown*/
    private float fadeFraction;

    /** How fast the screen fades in */
    private float fadeRate;

    /**
     * The height of the canvas window (necessary since sprite origin != screen origin)
     */
    private int heightY;

    private Array<Image> captiveIcons;

    private int caughtCaptives;

    private int totalCaptives;

    private BitmapFont subHeadingFont;

    private float backgroundHeight;

    private float backgroundWidth;

    private boolean levelWon;

    private Color green = new Color(10f/255f, 199f/255f, 152f/255f, 1f);
    private Color red = new Color(252f/255f, 97f/255f, 89/255f, 1f);
    private Color yellow = new Color(255f/255f, 178f/255f, 23f/255f, 1f);


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

        background = new TextureRegion(directory.getEntry("settingsBackground", Texture.class));
        displayFont = directory.getEntry("projectSpaceLarge", BitmapFont.class).newFontCache().getFont();
        subHeadingFont = directory.getEntry("codygoonRegular", BitmapFont.class).newFontCache().getFont();
        hoverPointer = directory.getEntry("hoverPointer", Texture.class);
        continueGameButton = directory.getEntry("continueGameButton", Texture.class);
        levelSelectButton = directory.getEntry("levelSelectButton", Texture.class);
        titleScreenButton = directory.getEntry("titleScreenButton", Texture.class);
        captiveIcon = directory.getEntry("captiveIcon", Texture.class);
        emptyIcon = directory.getEntry("captiveIconOutline", Texture.class);
        backgroundHeight = background.getRegionHeight();
        backgroundWidth = background.getRegionWidth();
        isLastLevel = false;
        fadeFraction = 0;
        fadeRate = 0.05f;
        levelWon = false;
    }

    public void setIsLastLevel(boolean b){
        isLastLevel = b;
    }

    public void gameWon(AssetDirectory directory) {
        gameOverMessage = "VICTORY";
        displayFont.setColor(green);
        subHeadingFont.setColor(yellow);
        continueGameButton = directory.getEntry("continueGameButton", Texture.class);
        levelSelectButton = directory.getEntry("retryLevelButton", Texture.class);
        SoundController.pauseMusic();
        SoundController.stopSound("banditJingle");
        if (isLastLevel) {
            SoundController.playSound("hubbaVictory", 1);
        } else {
            SoundController.playSound("victory", 1);
        }
        levelWon = true;
    }

    public boolean gameWon() {return levelWon;}

    public void gameLost(AssetDirectory directory) {
        gameOverMessage = "HEIST FAILED";
        displayFont.setColor(red);
        continueGameButton = directory.getEntry("tryAgainButton", Texture.class);
        SoundController.pauseMusic();
        SoundController.playSound("failure", 1);
        levelWon = false;
    }

    public void setCaptive(int caught, int total) {
        caughtCaptives = caught;
        totalCaptives = total;
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
            update(delta);
            draw();
            if (continueGame() && listener != null) {
                listener.exitScreen(this, Screens.CONTROLLER);
            }
            if (levelSelect() && listener != null) {
                if(isLastLevel){
                    listener.exitScreen(this, Screens.CREDITS);
                }
                else listener.exitScreen(this, Screens.LEVEL_SELECT);
            }
            if (returnToTitle() && listener != null) {
                listener.exitScreen(this, Screens.LOADING_SCREEN);
            }
        }
        if (false) {
            listener.exitScreen(this, 0);
        }
    }

    private void update(float dt) {
        fadeFraction += fadeRate;
    }

    private void draw() {
        canvas.begin();

//        if(levelWon && SaveData.getContinueLevel() >= GameController.NUM_LEVELS){
//            listener.exitScreen(this, Screens.CREDITS);
//            canvas.end();
//            return;
//        }

        if (fadeFraction < 1) {
            background.setRegionHeight((int) ((fadeFraction) * backgroundHeight));
            canvas.draw(background, 0, backgroundHeight - background.getRegionHeight());
        }
        else {
            canvas.draw(background, Color.WHITE, 0, 0, canvas.getCamera().viewportWidth,
                canvas.getCamera().viewportHeight);

            float x = (canvas.getCamera().viewportWidth) / 2.0f;
            float y = (canvas.getCamera().viewportHeight)/ 2.0f;

            float highestButtonY = y;
            float middleButtonY = y - 60;

        if (levelWon && totalCaptives > 0) {
            canvas.drawTextCentered(gameOverMessage, displayFont, 150);
            canvas.drawTextCentered("CAPTIVES SAVED", subHeadingFont, 65);
            int[] oddOffset = new int[]{ -115, -35, 45};
            int[] evenOffset = new int[]{-75, 5};
            if (totalCaptives == 1) {
                canvas.draw(emptyIcon, x + oddOffset[1], y - 15);
            }
            else {
                for (int i = 0; i < totalCaptives; i++) {
                    if (totalCaptives % 2 == 1) {
                        canvas.draw(emptyIcon, x + oddOffset[i], y - 15);
                    }
                    else {
                        canvas.draw(emptyIcon, x + evenOffset[i], y - 15);
                    }
                }
            }

            if (totalCaptives == 1 && caughtCaptives == 1) {
                canvas.draw(captiveIcon, x + oddOffset[1], y - 15);
            }
            else {
                for (int i = 0; i < caughtCaptives; i++) {
                    if (totalCaptives % 2 == 1) {
                        canvas.draw(captiveIcon, x + oddOffset[i], y - 15);
                    }
                    else {
                        canvas.draw(captiveIcon, x + evenOffset[i], y - 15);
                    }
                }
            }

            highestButtonY = y - 60;
            middleButtonY = y - 120;
        }
        else {
            canvas.drawTextCentered(gameOverMessage, displayFont, 100);
        }


            startButtonPositionX = (int) x;
            startButtonPositionY = (int) highestButtonY;

            levelSelectButtonPositionX = (int) x;
            levelSelectButtonPositionY = (int) middleButtonY;

            titleScreenButtonPositionX = (int) x;
            titleScreenButtonPositionY = (int) middleButtonY - 60;


            //Draw continue game options
            canvas.draw(
                    continueGameButton,
                    getButtonTint("continue"),
                    continueGameButton.getWidth() / 2f,
                    continueGameButton.getHeight() / 2f,
                    startButtonPositionX,
                    startButtonPositionY,
                    0,
                     BUTTON_SCALE,
                     BUTTON_SCALE
            );


            //Draw Level Select
            canvas.draw(
                    levelSelectButton,
                    getButtonTint("level"),
                    levelSelectButton.getWidth() / 2f,
                    levelSelectButton.getHeight() / 2f,
                    levelSelectButtonPositionX,
                    levelSelectButtonPositionY,
                    0,
                     BUTTON_SCALE,
                     BUTTON_SCALE
            );

            //Draw Continue Game
            canvas.draw(
                    titleScreenButton,
                    getButtonTint("title"),
                    titleScreenButton.getWidth() / 2f,
                    titleScreenButton.getHeight() / 2f,
                    titleScreenButtonPositionX,
                    titleScreenButtonPositionY,
                    0,
                    BUTTON_SCALE,
                    BUTTON_SCALE
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

        if (buttonName.equals("title")) {
            if (hoveringReturnTitleScreen && pressState == 3) return pressTint;
            else if (hoveringReturnTitleScreen) return hoverTint;
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

        if (continueGameButton == null || levelSelectButton == null || titleScreenButton == null) return false;
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
        topY = levelSelectButtonPositionY - rectHeight /2.0f;
        bottomY = levelSelectButtonPositionY + rectHeight / 2.0f;
        hoveringLevelSelect = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

        //Detect hovers on the return to titlebutton
        rectWidth = scale * BUTTON_SCALE * titleScreenButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * titleScreenButton.getHeight();
        leftX = titleScreenButtonPositionX - rectWidth / 2.0f;
        rightX = titleScreenButtonPositionX + rectWidth / 2.0f;
        topY = titleScreenButtonPositionY - rectHeight / 2.0f;
        bottomY = titleScreenButtonPositionY + rectHeight / 2.0f;
        hoveringReturnTitleScreen = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

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
        if (continueGameButton == null || levelSelectButton == null || titleScreenButton == null) return false;

        //Detect clicks on the top button
        float rectWidth = scale * BUTTON_SCALE * continueGameButton.getWidth();
        float rectHeight = scale * BUTTON_SCALE * continueGameButton.getHeight();
        float leftX = startButtonPositionX - rectWidth / 2.0f;
        float rightX = startButtonPositionX + rectWidth / 2.0f;
        float topY = startButtonPositionY - rectHeight / 2.0f;
        float bottomY = startButtonPositionY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            if (levelWon) {
                pressState = 2; //continue should go to level select
            }
            else {
                pressState = 1;
            }
        }

        //Detect clicks on the middle button
        rectWidth = scale * BUTTON_SCALE * levelSelectButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * levelSelectButton.getHeight();
        leftX = levelSelectButtonPositionX - rectWidth / 2.0f;
        rightX = levelSelectButtonPositionX + rectWidth / 2.0f;
        topY = levelSelectButtonPositionY - rectHeight / 2.0f;
        bottomY = levelSelectButtonPositionY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            if (levelWon) {
                pressState = 1;
            }
            else {
                pressState = 2;
            }
        }

        //Detect clicks on the bottom button
        rectWidth = scale * BUTTON_SCALE * titleScreenButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * titleScreenButton.getHeight();
        leftX = titleScreenButtonPositionX - rectWidth / 2.0f;
        rightX = titleScreenButtonPositionX + rectWidth / 2.0f;
        topY = titleScreenButtonPositionY - rectHeight / 2.0f;
        bottomY = titleScreenButtonPositionY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 3;
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

        //return to title
        if (pressState == 3) {
            pressState = 7;
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
    public boolean returnToTitle() {return pressState == 7;}

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

    public void dispose() {

    }

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
