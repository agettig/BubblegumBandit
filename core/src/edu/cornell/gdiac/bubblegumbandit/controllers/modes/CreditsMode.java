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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
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
    private final static float BACK_SCALAR_X = .087f;

    /** How far up, in terms of the background, to draw the back button*/
    private final static float BACK_SCALAR_Y = .05f;

    /** Standard window size (for scaling)  */
    private final static int STANDARD_WIDTH = 800;

    /**  Standard window height (for scaling) */
    private final static int STANDARD_HEIGHT = 700;

    /** Scale of buttons. */
    private final float BUTTON_SCALE = .15f;

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

    /** Left table for the credits. */
    private Table creditsTableLeft;

    /** Right table for the credits. */
    private Table creditsTableRight;

    /** Texture to represent the CreditsMode background.*/
    private Texture background;

    /** Texture to represent the back button. */
    private Texture backButton;

    /** Texture to represent the credits title. */
    private Texture creditsTitle;

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

    /** Font used for names. */
    private BitmapFont nameFont;

    /** Font used for roles. */
    private BitmapFont roleFont;

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
     * @param nameFont the font for names
     * @param roleFont the font for roles
     */
    public void gatherAssets(AssetDirectory directory, BitmapFont nameFont, BitmapFont roleFont) {
        this.directory = directory;
        this.directory.finishLoading();
        Gdx.input.setInputProcessor( this );
        world = new World(new Vector2(0, 0), false);
        SoundController.playMusic("menu");

        background = directory.getEntry("creditsBackground", Texture.class);
        backButton = directory.getEntry("back", Texture.class);
        creditsTitle = directory.getEntry("creditsTitle", Texture.class);
        this.nameFont = nameFont;
        this.roleFont = roleFont;

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

        makeCreditsTable();


    }

    /**
     * Creates the table of names and roles.
     * */
    private void makeCreditsTable(){
        stage = new Stage();


        creditsTableLeft = new Table();
        creditsTableLeft.align(Align.left);
        creditsTableLeft.setFillParent(true);
        creditsTableLeft.padTop(80f);

        creditsTableRight = new Table();
        creditsTableRight.align(Align.left);
        creditsTableRight.setFillParent(true);
        creditsTableRight.padTop(80f);
        creditsTableRight.padLeft(710f);


        stage.addActor(creditsTableLeft);
        stage.addActor(creditsTableRight);

        Label.LabelStyle nameStyle = new Label.LabelStyle(this.nameFont, Color.WHITE);
        final Color roleOrange = new Color(
                1f,
                178/255f,
                23/255f,
                1f
        );
        Label.LabelStyle roleStyle = new Label.LabelStyle(this.roleFont, roleOrange);

        //Ruike Liang
        creditsTableLeft.row();
        Label ruike = new Label("Ruike Liang", nameStyle);
        ruike.setAlignment(Align.left);
        ruike.setFontScale(.45f);
        ruike.setFontScaleX(.5f);
        creditsTableLeft.add(ruike).padBottom(20f).padRight(30f);
        creditsTableLeft.row();
        Label ruikeRole = new Label("Project Lead", roleStyle);
        ruikeRole.setAlignment(Align.left);
        ruikeRole.setFontScale(.45f);
        creditsTableLeft.add(ruikeRole).padRight(169f);

        //Ariela Gettig
        creditsTableLeft.row();
        Label ariela = new Label("Ariela Gettig", nameStyle);
        ariela.setAlignment(Align.left);
        ariela.setFontScale(.45f);
        ariela.setFontScaleX(.5f);
        creditsTableLeft.add(ariela).padBottom(20f).padTop(40f).padLeft(33f);
        creditsTableLeft.row();
        Label arielaRole = new Label("Programming Lead", roleStyle);
        arielaRole.setAlignment(Align.left);
        arielaRole.setFontScale(.45f);
        creditsTableLeft.add(arielaRole).padRight(126f);

        //Bella Besuud
        creditsTableLeft.row();
        Label bella = new Label("Bella Besuud", nameStyle);
        bella.setAlignment(Align.left);
        bella.setFontScale(.45f);
        bella.setFontScaleX(.5f);
        creditsTableLeft.add(bella).padBottom(20f).padTop(40f).padLeft(47f);
        creditsTableLeft.row();
        Label bellaRole = new Label("Design Lead", roleStyle);
        bellaRole.setAlignment(Align.left);
        bellaRole.setFontScale(.45f);
        creditsTableLeft.add(bellaRole).padRight(181f);

        //Caroline Hohner
        creditsTableLeft.row();
        Label caroline = new Label("Caroline Hohner", nameStyle);
        caroline.setAlignment(Align.left);
        caroline.setFontScale(.45f);
        caroline.setFontScaleX(.5f);
        creditsTableLeft.add(caroline).padBottom(20f).padTop(40f).padLeft(110f);
        creditsTableLeft.row();
        Label carolineRole = new Label("Programmer, Designer", roleStyle);
        carolineRole.setAlignment(Align.left);
        carolineRole.setFontScale(.45f);
        creditsTableLeft.add(carolineRole).padRight(82f);

        //Right Side

        //Benjamin Neuwirth
        creditsTableRight.row();
        Label ben = new Label("Benjamin Neuwirth", nameStyle);
        ben.setAlignment(Align.left);
        ben.setFontScale(.45f);
        ben.setFontScaleX(.5f);
        creditsTableRight.add(ben).padBottom(20f).padRight(30f);
        creditsTableRight.row();
        Label benRole = new Label("Programmer", roleStyle);
        benRole.setAlignment(Align.left);
        benRole.setFontScale(.45f);
        creditsTableRight.add(benRole).padRight(369f);

        //Emily Penna
        creditsTableRight.row();
        Label emily = new Label("Emily Penna", nameStyle);
        emily.setAlignment(Align.left);
        emily.setFontScale(.45f);
        emily.setFontScaleX(.5f);
        creditsTableRight.add(emily).padBottom(20f).padTop(40f).padRight(187f);
        creditsTableRight.row();
        Label emilyRole = new Label("Programmer, Designer, Musician", roleStyle);
        emilyRole.setAlignment(Align.left);
        emilyRole.setFontScale(.45f);
        creditsTableRight.add(emilyRole).padRight(155f);


        //Teddy Siker
        creditsTableRight.row();
        Label teddy = new Label("Teddy Siker", nameStyle);
        teddy.setAlignment(Align.left);
        teddy.setFontScale(.45f);
        teddy.setFontScaleX(.5f);
        creditsTableRight.add(teddy).padBottom(20f).padTop(40f).padRight(190f);
        creditsTableRight.row();
        Label teddyRole = new Label("Programmer", roleStyle);
        teddyRole.setAlignment(Align.left);
        teddyRole.setFontScale(.45f);
        creditsTableRight.add(teddyRole).padRight(369f);


        //Sophia Xu
        creditsTableRight.row();
        Label sophia = new Label("Sophia Xu", nameStyle);
        sophia.setAlignment(Align.left);
        sophia.setFontScale(.45f);
        sophia.setFontScaleX(.5f);
        creditsTableRight.add(sophia).padBottom(20f).padTop(40f).padRight(243f);
        creditsTableRight.row();
        Label sophiaRole = new Label("Programmer", roleStyle);
        sophiaRole.setAlignment(Align.left);
        sophiaRole.setFontScale(.45f);
        creditsTableRight.add(sophiaRole).padRight(369f);

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

        //Draw the credits title

        final float creditsScale = .4f;
        float creditsX = -(creditsTitle.getWidth()/2f);
        float creditsY = creditsTitle.getHeight() * 1.45f;

        canvas.draw(
                creditsTitle,
                PRESS_BLUE,
                creditsX * creditsScale,
                creditsY,
                creditsTitle.getWidth() * creditsScale,
                creditsTitle.getHeight() * creditsScale
        );


        canvas.end();

        stage.draw();
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
            backPressedDown = false;
            backHovered = false;
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
