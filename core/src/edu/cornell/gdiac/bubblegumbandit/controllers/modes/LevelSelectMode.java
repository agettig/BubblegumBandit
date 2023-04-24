package edu.cornell.gdiac.bubblegumbandit.controllers.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.PlayerController;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.models.LevelIconModel;
import edu.cornell.gdiac.bubblegumbandit.models.SunfishModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCamera;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.bubblegumbandit.view.HUDController;
import edu.cornell.gdiac.util.Controllers;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.util.XBoxController;

import javax.xml.parsers.SAXParser;

/**
 * Class that provides the level select screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class LevelSelectMode implements Screen, InputProcessor, ControllerListener {

    // technical attributes

    /**
     * Need an ongoing reference to the asset directory
     */
    protected AssetDirectory directory;

    /**
     * The font for giving messages to the player
     */
    protected BitmapFont displayFont;

    /**
     * The JSON defining game constants
     */
    private JsonValue constantsJson;


    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    // level select specific attributes

    /**
     * The Box2D world
     */
    protected World world;

    /** Background Space texture */
    private TextureRegion background;

    /** The bandit's ship, The Sunfish, that follows the players cursor */
    private SunfishModel sunfish;

    // level icon attributes

    /** array of all level icons */
    private Array<LevelIconModel> levels;

    /** Whether this player mode is still active */
    private boolean active;

    /** whether the player has chosen a level to play */
    private boolean ready;

    /** the level chosen by the player */
    private int selectedLevel;

    /** the gap between level icons */
    private final static float LEVEL_GAP = 500;

    /** the gap between level icons and the bounds of space */
    private final static float SPACE_GAP = 800;

    //space boundaries
    /** the width of the sunfish movement bounds */
    private final static float SPACE_WIDTH = 5000;
    /** height of sunfish movement bounds */
    private final static float SPACE_HEIGHT = 5000;

    //the camera dimensions
    private float camWidth;
    private float camHeight;

    /** Camera zoom out */
    private final static float ZOOM = 1.5f;



    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Creates a LoadingMode with the default size and position.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     */
    public LevelSelectMode() {
    }
    /**
     * Gather the assets for this controller.
     * <p>
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        // Access the assets used directly by this controller
        active = true;
        this.directory = directory;
        // Some assets may have not finished loading so this is a catch-all for those.
        directory.finishLoading();
        displayFont = directory.getEntry("projectSpace", BitmapFont.class);

        constantsJson = directory.getEntry("constants", JsonValue.class);

        Gdx.input.setInputProcessor( this );

        world = new World(new Vector2(0, 0), false);

        TextureRegion sunfish_texture = new TextureRegion (directory.getEntry("sunfish", Texture.class));
        TextureRegion fire = new TextureRegion (directory.getEntry("fire", Texture.class));
        TextureRegion boost = new TextureRegion (directory.getEntry("boost", Texture.class));

        sunfish = new SunfishModel(sunfish_texture, fire, boost, SPACE_GAP, SPACE_HEIGHT - SPACE_GAP);
        sunfish.activatePhysics(world);

        background = new TextureRegion(directory.getEntry("space_bg", Texture.class));

        createIcons(directory);

    }

    private void createIcons(AssetDirectory directory){

        TextureRegion ship1 = new TextureRegion(directory.getEntry("ship1", Texture.class));
        TextureRegion ship2 = new TextureRegion(directory.getEntry("ship2", Texture.class));

        levels = new Array<>();
        levels.add(new LevelIconModel(ship1, 1, 100, SPACE_HEIGHT - SPACE_GAP));
        levels.add(new LevelIconModel(ship2, 2, ship1.getRegionWidth() + LEVEL_GAP , SPACE_HEIGHT - SPACE_GAP));
    }

    /**
     * Sets the canvas associated with this controller
     * <p>
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        canvas.getCamera().setFixedX(false);
        canvas.getCamera().setFixedY(false);
        canvas.getCamera().setZoom(ZOOM);

        camWidth = canvas.getCamera().viewportWidth;
        camHeight = canvas.getCamera().viewportHeight;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        canvas = null;
        sunfish = null;
        world = null;
        levels = null;
    }

    /**
     * Update the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param dt Number of seconds since last animation frame
     */
    private void update(float dt) {
        world.step(dt, 8, 3);

        Vector2 mousePos = canvas.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        for (LevelIconModel level : levels){

            if (level.getState() != 2) {
                if (level.onIcon(mousePos.x, mousePos.y)) {
                    level.setPressState(1);
                } else {
                    level.setPressState(0);
                }
            }

            level.update();
        }

        sunfish.setMovement(mousePos);
        sunfish.update(dt);

//        for (LevelIconModel level : levels){
//            level.update();
//        }


        //move camera, while keeping view in bounds
        canvas.getCamera().setTarget(sunfish.getPosition());
//        System.out.println(sunfish.getPosition());

         //x bounds
        if (sunfish.getX() < camWidth) {
            canvas.getCamera().setTargetX(camWidth);
        }
//        if (sunfish.getX() > SPACE_WIDTH - camWidth){
//            canvas.getCamera().setTargetX(SPACE_WIDTH - camWidth);
//
//        }
         //y bounds
        if (sunfish.getY() < camHeight) {
            canvas.getCamera().setTargetY(camHeight);

        }
        if (sunfish.getY() > SPACE_HEIGHT - camHeight) {
            canvas.getCamera().setTargetY(SPACE_HEIGHT- camHeight);
        }

        canvas.getCamera().update(dt);
    }

    /** returns the level chosen by the player */
    public int getSelectedLevel() {
        return selectedLevel;
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.clear();
        canvas.begin();

        drawBackground(canvas);

        for (LevelIconModel level : levels){
            level.draw(canvas, displayFont);
        }

        sunfish.draw(canvas);
        canvas.end();
    }

    // ADDITIONAL SCREEN METHODS
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

            // We are ready, notify our listener
            if (isReady() && listener != null) {
                listener.exitScreen(this, 0);
            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);
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

        if (active){
            Vector2 target = canvas.unproject(new Vector2(screenX, screenY));

            for (LevelIconModel level : levels){
                if (level.onIcon(target.x, target.y)){
                    level.setPressState(2);
                }
            }

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

        if (active) {
            Vector2 target = canvas.unproject(new Vector2(screenX, screenY));

            for (LevelIconModel level : levels){

                if (level.onIcon(target.x, target.y) && level.getState() == 2){
                    ready = true;
                    selectedLevel = level.getLevel();
                }
                else{
                    level.setPressState(0);
                }
            }
        }
        return true;
    }


    /**
     * Called when the mouse was moved without any buttons being pressed.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
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
    public boolean buttonDown (Controller controller, int buttonCode) {

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
    public boolean buttonUp (Controller controller, int buttonCode) {
        return true;
    }


    /**
     * Called when a key is pressed
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.SPACE){
            sunfish.setBoosting(true);
        }
        return true;

    }

    /**
     * Called when a key is typed
     *
     * @param character the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.SPACE){
            sunfish.setBoosting(false);
        }
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
    public void connected (Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (Controller controller) {}

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
    public boolean axisMoved (Controller controller, int axisCode, float value) {
        return true;
    }


    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
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
}