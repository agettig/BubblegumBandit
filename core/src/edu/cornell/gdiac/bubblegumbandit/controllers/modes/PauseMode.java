package edu.cornell.gdiac.bubblegumbandit.controllers.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
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

    /** Reset Button*/
    private Texture resetButton;

    /** Retry button*/
    private Texture levelSelectButton;

    /** Settings Button*/
    private Texture settingsButton;

    /**Save and Quit button*/
    private Texture saveAndQuitButton;

    /** Pointer to what is being hovered. */
    private Texture hoverPointer;

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

    /** The x-coordinate of the center of the reset button*/
    private int resetX;

    /** The y-coordinate of the center of the reset button*/
    private int resetY;

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

    /** True if the player is hovering over the reset button*/
    private boolean hoverReset;

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
     * 2 = rest down
     * 3 = level select down
     * 4 = setting down
     * 5 = save and quit down
     * 6 = resume up
     * 7 = reset up
     * 8 = level select up
     * 9 = settings up
     * 10 = save and quit up
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
        resetButton = null;
        levelSelectButton = null;
        settingsButton = null;
        saveAndQuitButton = null;
        hoverPointer = null;

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
        if (resumeButton == null) {
            //assets.update(budget);
            //this.progress = assets.getProgress()
            hoverPointer = directory.getEntry("hoverPointer", Texture.class);
            resumeButton = directory.getEntry("resumeButton", Texture.class);
            resetButton = directory.getEntry("resetButton", Texture.class);
            levelSelectButton = directory.getEntry("levelSelectButton", Texture.class);
            settingsButton = directory.getEntry("settingsButton", Texture.class);
            saveAndQuitButton = directory.getEntry("saveAndQuitButton", Texture.class);
        }
    }

    /** Returns the menu option selected by the player*/
    public int getSelectedMenuItem() {
        return 0;
    }

    /** Draws the status of this mode*/
    private void draw() {
        canvas.begin();
        canvas.draw(background, Color.WHITE, 0, 0, canvas.getCamera().viewportWidth, canvas.getCamera().viewportHeight);
//        if (resumeButton == null || resetButton == null || settingsButton == null || saveAndQuitButton == null || hoverPointer == null) {
//            drawProgress(canvas);
//        } else {

            float highestButtonY = canvas.getCamera().viewportHeight/2;
            float lowestButtonY = canvas.getCamera().viewportHeight/6;
            float buttonSpace = highestButtonY - lowestButtonY;
            float gap = buttonSpace / 4;

            resumeX = (int) canvas.getCamera().viewportWidth / 5;
            resumeY = (int) highestButtonY;

            resetX = (int) canvas.getCamera().viewportWidth / 5;
            resetY = (int) (highestButtonY - gap);

            levelSelectX = (int) canvas.getCamera().viewportWidth / 5;
            levelSelectY = (int) (highestButtonY - gap*2);

            settingsX = (int) canvas.getCamera().viewportWidth / 5;
            settingsY = (int) (highestButtonY - gap*3);

            saveAndQuitX = (int) canvas.getCamera().viewportWidth / 5;
            saveAndQuitY = (int) (highestButtonY - gap*4);


            float pointerX = resumeX / 4f;


            //Draw Continue Game
            canvas.draw(
                    startButton,
                    getButtonTint("start"),
                    startButton.getWidth()/2f,
                    startButton.getHeight()/2f,
                    startButtonPositionX,
                    startButtonPositionY,
                    0,
                    scale * BUTTON_SCALE,
                    scale * BUTTON_SCALE
            );
            if(hoveringStart){
                canvas.draw(
                        hoverPointer,
                        Color.WHITE,
                        hoverPointer.getWidth()/2f,
                        hoverPointer.getHeight()/2f,
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
                    levelSelectButton.getWidth()/2f,
                    levelSelectButton.getHeight()/2f,
                    levelSelectButtonPositionX,
                    levelSelectButtonPositionY,
                    0,
                    scale * BUTTON_SCALE,
                    scale * BUTTON_SCALE
            );
            if(hoveringLevelSelect){
                canvas.draw(
                        hoverPointer,
                        Color.WHITE,
                        hoverPointer.getWidth()/2f,
                        hoverPointer.getHeight()/2f,
                        pointerX,
                        levelSelectButtonPositionY,
                        0,
                        scale,
                        scale
                );
            }

            //Draw Settings
            canvas.draw(
                    settingsButton,
                    getButtonTint("settings"),
                    settingsButton.getWidth()/2f,
                    settingsButton.getHeight()/2f,
                    settingsButtonPositionX,
                    settingsButtonPositionY,
                    0,
                    scale * BUTTON_SCALE,
                    scale * BUTTON_SCALE
            );
            if(hoveringSettings){
                canvas.draw(
                        hoverPointer,
                        Color.WHITE,
                        hoverPointer.getWidth()/2f,
                        hoverPointer.getHeight()/2f,
                        pointerX,
                        settingsButtonPositionY,
                        0,
                        scale,
                        scale
                );
            }

            //Draw Exit
            canvas.draw(
                    exitButton,
                    getButtonTint("exit"),
                    exitButton.getWidth()/2f,
                    exitButton.getHeight()/2f,
                    exitButtonPositionX,
                    exitButtonPositionY,
                    0,
                    scale * BUTTON_SCALE,
                    scale * BUTTON_SCALE
            );
            if(hoveringExit){
                canvas.draw(
                        hoverPointer,
                        Color.WHITE,
                        hoverPointer.getWidth()/2f,
                        hoverPointer.getHeight()/2f,
                        pointerX,
                        exitButtonPositionY,
                        0,
                        scale,
                        scale
                );

        }
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

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

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

        //Detect clicks on the reset button
        rectWidth = scale * BUTTON_SCALE * resetButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * resetButton.getHeight();
        leftX = resetX - rectWidth / 2.0f;
        rightX = resetX + rectWidth / 2.0f;
        topY = resetY - rectHeight / 2.0f;
        bottomY = resetY + rectHeight / 2.0f;
        if (screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY) {
            pressState = 2;
        }

        //Detect clicks on the level select button
        rectWidth = scale * BUTTON_SCALE * levelSelectButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * levelSelectButton.getHeight();
        leftX = levelSelectX - rectWidth / 2.0f;
        rightX = levelSelectX + rectWidth / 2.0f;
        topY = levelSelectY - rectHeight / 2.0f;
        bottomY = levelSelectY + rectHeight / 2.0f;
        if (screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY) {
            pressState = 3;
        }

        //Detect clicks on the settings button
        rectWidth = scale * BUTTON_SCALE * settingsButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * settingsButton.getHeight();
        leftX = settingsX - rectWidth / 2.0f;
        rightX = settingsX + rectWidth / 2.0f;
        topY = settingsY - rectHeight / 2.0f;
        bottomY = settingsY + rectHeight / 2.0f;
        if (screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY) {
            pressState = 4;
        }

        //Detect clicks on the exit button
        rectWidth = scale * BUTTON_SCALE * saveAndQuitButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * saveAndQuitButton.getHeight();
        leftX = saveAndQuitX - rectWidth / 2.0f;
        rightX = saveAndQuitX + rectWidth / 2.0f;
        topY = saveAndQuitY - rectHeight / 2.0f;
        bottomY = saveAndQuitY + rectHeight / 2.0f;
        if (screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY) {
            pressState = 5;
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

        if (pressState == 2) {
            pressState = 6;
            return false;
        }

        //Level Select
        if (pressState == 3) {
            pressState = 7;
            return false;
        }

        //Settings
        if (pressState == 4) {
            pressState = 8;
            return false;
        }

        //Exit
        if (pressState == 5) {
            pressState = 9;
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

        //Detect hovers on the reset button
        rectWidth = scale * BUTTON_SCALE * resetButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * resetButton.getHeight();
        leftX = resetX - rectWidth / 2.0f;
        rightX = resetX + rectWidth / 2.0f;
        topY = resetY - rectHeight / 2.0f;
        bottomY = resetY + rectHeight / 2.0f;
        hoverReset = screenX >= leftX && screenX <= rightX && screenY >= topY && screenY <= bottomY;

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


    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return false;
    }
}
