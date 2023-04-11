package edu.cornell.gdiac.bubblegumbandit.controllers.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.GameController;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.util.ScreenListener;

public class PauseMode implements Screen, InputProcessor, ControllerListener {
    /** Internal assets for the pause scrren*/
    private AssetDirectory directory;

    /** The JSON for game constants*/
    private JsonValue constantsJson;

    /** Background texture*/
    private Texture background;

    /** Resume button*/
    private Texture resumeButton;

    /** Retry button*/
    private Texture levelSelectButton;

    /** Settings Button*/
    private Texture settingsButton;

    /**Save and Quit button*/
    private Texture saveAndQuitButton;

    /** Standard window width*/
    private static int STANDARD_WIDTH = 800;

    /** Standard window height*/
    private static int STANDARD_HEIGHT = 700;

    /** Ratio of the bar width to the screen */
    private static float BAR_WIDTH_RATIO  = 0.66f;

    /** Ration of the bar height to the screen */
    private static float BAR_HEIGHT_RATIO = 0.25f;

    /** Reference to the GameCanvas*/
    private GameCanvas canvas;

    /** Listener that updates the player mode*/
    private ScreenListener listener;

    /** The width of the progress bar */
    private int width;

    /** The y-coordinate of the center of the progress bar */
    private int centerY;

    /** The x-coordinate of the center of the progress bar */
    private int centerX;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;

    /** The x-coordinate of the center of the resume button*/
    private int resumeX;

    /** The y-coordinate of the center of the resume button*/
    private int resumeY;

    /** The x-coordinate of the center of the level select button*/
    private int levelSelectX;

    /** The y-coordinate of the center of the level select button*/
    private int levelSelectY;

    /** The x-coordinate of the center of the settings button*/
    private int settingsX;

    /** The y-coordinate of the center of the settings button*/
    private int settingsY;

    /** The x-coordinate of the center of the save and quit button*/
    private int saveAndQuitX;

    /** The y-coordinate of the center of the save and quit button*/
    private int saveAndQuitY;

    /** True if the player is hovering over the resume button*/
    private boolean hoverResume;

    /** True if the player is hovering over the level select button*/
    private boolean hoverLevelSelect;

    /** True if the player is hovering over the settings button*/
    private boolean hoverSettings;

    /** True if the player is hovering over the save and quit button*/
    private boolean hoverSaveAndQuit;

    /** Scale of Start, Settings, and Exit buttons.  */
    private final float BUTTON_SCALE = .3f;

    /** The scale of the screen*/
    private float scale;

    /** The progress of the asset manager*/
    private float progress;

    /**
     * 0 = nothing pressed
     * 1 = resume down
     * 2 = level select down
     * 3 = setting down
     * 4 = save and quit down
     * 5 = resume up
     * 6 = level select up
     * 7 = settings up
     * 8 = save and quit up
     * */
    private int pressState;

    /** True if the player is ready to go after clicking resume*/
    public boolean isReady() {
        return pressState == 5;
    }

    /** True if the player wants to quit*/
    public boolean quit() {
        return pressState == 8;
    }

    /** True if this mode is still active*/
    private boolean active;

    private BitmapFont displayFont;

    /** The box2D world*/
    private World world;

    /** Creates a PauseMode with the default size and position
     *
     * @oaram file The asset directory
     * @param canvas The game canvas to draw to
     * */
    public PauseMode(AssetDirectory directory, GameCanvas canvas) {
        active = true;
        this.canvas = canvas;
        canvas.getCamera().setFixedX(false);
        canvas.getCamera().setFixedY(false);
        canvas.getCamera().setZoom(1);

        this.directory = directory;
        directory.finishLoading();
        displayFont = directory.getEntry("display", BitmapFont.class);
        SoundController.initialize(directory);
        constantsJson = directory.getEntry("constants", JsonValue.class);
        Gdx.input.setInputProcessor( this );
        background = directory.getEntry("background", Texture.class);

        resize(canvas.getWidth(), canvas.getHeight());

        resumeButton = null;
        levelSelectButton = null;
        settingsButton = null;
        saveAndQuitButton = null;

        progress = 0;
        pressState = 0;
    }

//    /** Gathers the assets for this controllers*/
//    public void gatherAssets(AssetDirectory directory) {
//
//    }
//
//    /** Creates the textures */
//    public void createIcons(AssetDirectory directory) {
//
//    }

    /** Called when this mode releases all resources*/
    public void dispose() {
        canvas = null;
        world = null;
        directory.unloadAssets();
        directory.dispose();
    }

    /** Updates the status of this mode*/
    public void update(float dt) {
        world.step(dt, 8, 3);
    }

    /** Returns the menu option selected by the player*/
    public int getSelectedMenuItem() {
        return 0;
    }

    /** Draws the status of this mode*/
    private void draw() {
        //canvas.clear();
        canvas.begin();

        canvas.end();
    }

    /** Called when the screen should render itself*/
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (isReady() && listener != null) {
                listener.exitScreen(this, 0);
            }
        }
    }

    /** Called when the screen is resized*/
    public void resize(int width, int height) {
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        this.width = (int)(BAR_WIDTH_RATIO*width);
        centerY = (int)(BAR_HEIGHT_RATIO*height);
        centerX = width/2;
        heightY = height;
    }

    /**
            * Called when the Screen is paused.
            *
            * This is usually when it's not active or visible on screen. An Application is
            * also paused before it is destroyed.
            */
    public void pause() {

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }


    // PROCESSING PLAYER INPUT
    /**
     * Called when the screen was touched or a mouse button was pressed.
     *
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

        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        //Detect clicks on the resume button
        float rectWidth = scale * BUTTON_SCALE * resumeButton.getWidth();
        float rectHeight = scale * BUTTON_SCALE * resumeButton.getHeight();
        float leftX = resumeX - rectWidth / 2.0f;
        float rightX = resumeX + rectWidth / 2.0f;
        float topY = resumeY - rectHeight / 2.0f;
        float bottomY = resumeY + rectHeight / 2.0f;
        if (screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY) {
            pressState = 1;
        }

        //Detect clicks on the level select button
        rectWidth = scale * BUTTON_SCALE * levelSelectButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * levelSelectButton.getHeight();
        leftX = levelSelectX - rectWidth / 2.0f;
        rightX = levelSelectX + rectWidth / 2.0f;
        topY = levelSelectY - rectHeight / 2.0f;
        bottomY = levelSelectY + rectHeight / 2.0f;
        if (screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY) {
            pressState = 2;
        }

        //Detect clicks on the settings button
        rectWidth = scale * BUTTON_SCALE * settingsButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * settingsButton.getHeight();
        leftX = settingsX - rectWidth / 2.0f;
        rightX = settingsX + rectWidth / 2.0f;
        topY = settingsY - rectHeight / 2.0f;
        bottomY = settingsY + rectHeight / 2.0f;
        if (screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY) {
            pressState = 3;
        }

        //Detect clicks on the exit button
        rectWidth = scale * BUTTON_SCALE * saveAndQuitButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * saveAndQuitButton.getHeight();
        leftX = saveAndQuitX - rectWidth / 2.0f;
        rightX = saveAndQuitX + rectWidth / 2.0f;
        topY = saveAndQuitY - rectHeight / 2.0f;
        bottomY = saveAndQuitY + rectHeight / 2.0f;
        if (screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY) {
            pressState = 4;
        }
        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     *
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

        //Settings
        if (pressState == 3) {
            pressState = 7;
            return false;
        }

        //Exit
        if (pressState == 4) {
            pressState = 8;
            return false;
        }

        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (GameController controller, int buttonCode) {
//        if (pressState == 0) {
//            ControllerMapping mapping = controller.getMapping();
//            if (mapping != null && buttonCode == mapping.buttonStart ) {
//                pressState = 1;
//                return false;
//            }
//        }
        return true;
    }

    /**
     * Called when a button on the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (GameController controller, int buttonCode) {
//        if (pressState == 1) {
//            ControllerMapping mapping = controller.getMapping();
//            if (mapping != null && buttonCode == mapping.buttonStart ) {
//                pressState = 2;
//                return false;
//            }
//        }
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {

        if(resumeButton == null || levelSelectButton == null
                || settingsButton == null || saveAndQuitButton == null) return false;
// Flip to match graphics coordinates
        screenY = heightY-screenY;

        //Detect hovers on the start button
        float rectWidth = scale * BUTTON_SCALE * resumeButton.getWidth();
        float rectHeight = scale * BUTTON_SCALE * resumeButton.getHeight();
        float leftX = resumeX - rectWidth / 2.0f;
        float rightX = resumeX + rectWidth / 2.0f;
        float topY = resumeY - rectHeight / 2.0f;
        float bottomY = resumeY + rectHeight / 2.0f;
        hoverResume = screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY;

        //Detect hovers on the level select button
        rectWidth = scale * BUTTON_SCALE * levelSelectButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * levelSelectButton.getHeight();
        leftX = levelSelectX - rectWidth / 2.0f;
        rightX = levelSelectX + rectWidth / 2.0f;
        topY = levelSelectY - rectHeight / 2.0f;
        bottomY = levelSelectY + rectHeight / 2.0f;
        hoverLevelSelect = screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY;

        //Detect hovers on the settings button
        rectWidth = scale * BUTTON_SCALE * settingsButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * settingsButton.getHeight();
        leftX = settingsX - rectWidth / 2.0f;
        rightX = settingsX + rectWidth / 2.0f;
        topY = settingsY - rectHeight / 2.0f;
        bottomY = settingsY + rectHeight / 2.0f;
        hoverSettings = screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY;

        //Detect hovers on the exit button
        rectWidth = scale * BUTTON_SCALE * saveAndQuitButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * saveAndQuitButton.getHeight();
        leftX = saveAndQuitX - rectWidth / 2.0f;
        rightX = saveAndQuitX + rectWidth / 2.0f;
        topY = saveAndQuitY - rectHeight / 2.0f;
        bottomY = saveAndQuitY+ rectHeight / 2.0f;
        hoverSaveAndQuit = screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY;

        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param dx the amount of horizontal scroll
     * @param dy the amount of vertical scroll
     *
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(float dx, float dy) {
        return true;
    }

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

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected (GameController controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (GameController controller) {}

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     *
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode 	The axis moved
     * @param value 	The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved (GameController controller, int axisCode, float value) {
        return true;
    }




}
