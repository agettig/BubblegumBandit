package edu.cornell.gdiac.bubblegumbandit.controllers.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerPowerLevel;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.GameController;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.models.LevelIconModel;
import edu.cornell.gdiac.bubblegumbandit.models.SunfishModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.util.ScreenListener;

/**
 * Represents a mode to display the credits: names,
 * roles, and Easter eggs.
 * */
public class CreditsMode implements Screen, InputProcessor, ControllerListener {

    /** How much to zoom the Camera out. */
    private final static float ZOOM = 1.5f;

    /** How far right, in terms of the background, to draw the back button*/
    private final static float BACK_SCALAR_X = .08f;

    /** How far up, in terms of the background, to draw the back button*/
    private final static float BACK_SCALAR_Y = .07f;

    /** Standard window size (for scaling)  */
    private final static int STANDARD_WIDTH = 800;

    /**  Standard window height (for scaling) */
    private final static int STANDARD_HEIGHT = 700;

    /** Scale of buttons. */
    private final float BUTTON_SCALE = .2f;

    /** Pink hover color. */
    public final Color HOVER_PINK = new Color(
            1, 149 / 255f, 138 / 255f, 1
    );

    /** Blue press color. */
    public final Color PRESS_BLUE = new Color(
            55 / 255f, 226 / 255f, 226 / 255f, 1
    );

    /** Current CreditsMode screen scale.*/
    private float scale;

    /** true if the CreditsMode is the current mode.*/
    private boolean active;

    /** Reference to the JSON file that stores asset information. */
    private AssetDirectory directory;

    /** Reference to the GameCanvas that draws onto the screen. */
    private GameCanvas canvas;

    /** Box2D world for the CreditsMode. */
    private World world;

    /** Stage to hold CreditsMode texture assets. */
    private Stage stage;

    /** Texture to represent the CreditsMode background.*/
    private Texture background;

    /** Texture to represent the back button. */
    private Texture backButton;

    /** X-Offset for drawing things. */
    private float offsetX;

    /** Y-Offset for drawing things. */
    private float offsetY;

    /** The X-Coordinate for the back button.*/
    private float backButtonX;

    /** The Y-Coordinate for the back button.*/
    private float backButtonY;

    /** true if player clicked the back button*/
    private boolean backPressedDown;

    /** true if player released the back button, and we should return to main*/
    private boolean backPressedUp;

    /** true if player hovered the back button*/
    private boolean backHovered;

    /** CreditsMode's reference to a screen listener */
    private ScreenListener listener;

    /** Current tint for the back button. */
    private Color backColor;



    /** Creates the CreditsMode. */
    public CreditsMode(){
        stage = new Stage();
    }

    /**
     * Gather the assets for the CreditsMode.
     * <p>
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        this.directory = directory;
        this.directory.finishLoading();
        Gdx.input.setInputProcessor( this );
        world = new World(new Vector2(0, 0), false);
        SoundController.playMusic("menu");

        background = directory.getEntry("spaceBg", Texture.class);
        backButton = directory.getEntry("back", Texture.class);

        backPressedDown = false;
        backPressedUp = false;
        backHovered = false;
        active = true;
    }

    /**
     * Sets the canvas associated with the CreditsMode.
     * <p>
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        resize(canvas.getWidth(), canvas.getHeight());
        canvas.resetCamera();
        canvas.getCamera().isCredits(true);


        offsetX = -canvas.getCamera().viewportWidth/2;
        offsetY = -canvas.getCamera().viewportHeight/2;

        backButtonX = canvas.getCamera().viewportWidth * BACK_SCALAR_X + offsetX;
        backButtonY = canvas.getCamera().viewportWidth * BACK_SCALAR_Y + offsetY;
    }

    /**
     * Draws the CreditsMode.
     */
    private void draw() {

        if(!active) return;

        resize(
                (int)canvas.getCamera().viewportWidth,
                (int)canvas.getCamera().viewportHeight
        );

        canvas.clear();
        canvas.begin();

        //Draw the background.
        canvas.draw(
                background,
                Color.WHITE,
                offsetX,
                offsetY,
                canvas.getCamera().viewportWidth,
                canvas.getCamera().viewportHeight
        );

        //Find the color for the back button.
        if(backPressedDown) backColor = PRESS_BLUE;
        else if(backHovered) backColor = HOVER_PINK;
        else backColor = Color.WHITE;

        //Draw the back button.
        canvas.draw(
                backButton,
                backColor,
                backButtonX,
                backButtonY,
                backButton.getWidth() * BUTTON_SCALE,
                backButton.getHeight() * BUTTON_SCALE
        );

        canvas.end();
    }

    /**
     * Called when the Screen should render itself.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            System.out.println("Hovered: " + backHovered);
            System.out.println("Pressed: " + backPressedDown);
            System.out.println("Up: " + backPressedUp);

            //Logic for returing to Main Menu below
            if (backPressedUp && listener!=null){
                canvas.getCamera().isLevelSelect(false);
                listener.exitScreen(this, Screens.LOADING_SCREEN);
            }
        }
    }

    /**
     * Update the status of the CreditsMode.
     *
     * @param dt Number of seconds since last animation frame
     */
    private void update(float dt) {
        world.step(dt, 8, 3);
        resize(canvas.getWidth(), canvas.getHeight());
        Vector2 mousePos = canvas.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        canvas.getCamera().update(dt);
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

        if(active){
            Vector2 mousePos = canvas.unproject(new Vector2(screenX, screenY));
            boolean onBack = true;
            if(mousePos.x < backButtonX || mousePos.x > backButtonX + backButton.getWidth() * BUTTON_SCALE)
                onBack = false;
            if(mousePos.y < backButtonY || mousePos.y > backButtonY + backButton.getHeight() * BUTTON_SCALE)
                onBack = false;
            backPressedUp = onBack;
        }

        return true;
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
    }


    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }


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

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {


        if(active){
            Vector2 mousePos = canvas.unproject(new Vector2(screenX, screenY));
            boolean onBack = true;
            if(mousePos.x < backButtonX || mousePos.x > backButtonX + backButton.getWidth() * BUTTON_SCALE)
                onBack = false;
            if(mousePos.y < backButtonY || mousePos.y > backButtonY + backButton.getHeight() * BUTTON_SCALE)
                onBack = false;
            backPressedDown = onBack;
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        if(active){
            Vector2 mousePos = canvas.unproject(new Vector2(screenX, screenY));
            boolean onBack = true;
            if(mousePos.x < backButtonX || mousePos.x > backButtonX + backButton.getWidth() * BUTTON_SCALE)
                onBack = false;
            if(mousePos.y < backButtonY || mousePos.y > backButtonY + backButton.getHeight() * BUTTON_SCALE)
                onBack = false;
            backHovered = onBack;
        }

        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public void show() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

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
