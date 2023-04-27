package edu.cornell.gdiac.bubblegumbandit.controllers.modes;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.GameController;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.util.Controllers;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.util.XBoxController;

/**
 * Class that provides a loading screen for the state of the game.
 * <p>
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 * <p>
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class PauseMode implements Screen, InputProcessor, ControllerListener {
    // There are TWO asset managers.  One to load the loading screen.  The other to load the assets
    private Stage stage;
    /**
     * Internal assets for this loading screen
     */
    private AssetDirectory internal;

    /**
     * Background texture for start-up
     */
    private Texture background;

    /**
     * Button to resume
     */
    private TextButton resumeButton;

    /**
     * Button to retry level
     */
    private TextButton restartButton;

    /**
     * Button to open level select
     */
    private TextButton levelSelectButton;

    /**
     * Button to quit
     */
    private TextButton settingsButton;

    /**
     * Button to quit
     */
    private TextButton quitButton;

    /**
     * Pointer to what is being hovered.
     */
    private TextureRegion pointer;


    /**
     * Default budget for asset loader (do nothing but load 60 fps)
     */
    private static int DEFAULT_BUDGET = 15;
    /**
     * Standard window size (for scaling)
     */
    private static int STANDARD_WIDTH = 800;
    /**
     * Standard window height (for scaling)
     */
    private static int STANDARD_HEIGHT = 700;
    /**
     * Ratio of the bar width to the screen
     */
    private static float BAR_WIDTH_RATIO = 0.66f;
    /**
     * Ration of the bar height to the screen
     */
    private static float BAR_HEIGHT_RATIO = 0.25f;

    /**
     * Reference to GameCanvas created by the root
     */
    private GameCanvas canvas;
    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;

    /**The font used*/
    private BitmapFont font;

    /** The style used for the labels*/
    private Label.LabelStyle style;

    /**
     * Bubblegum Pink color
     * used for hover
     */
    public final Color bubblegumPink = new Color(1, 149 / 255f, 138 / 255f, 1);


    /**
     * The width of the progress bar
     */
    private int width;
    /**
     * The y-coordinate of the center of the progress bar
     */
    private int centerY;
    /**
     * The x-coordinate of the center of the progress bar
     */
    private int centerX;
    /**
     * The height of the canvas window (necessary since sprite origin != screen origin)
     */
    private int heightY;

    /**
     * The x-coordinate of the center of the resume button.
     */
    private int resumeX;

    /**
     * The y-coordinate of the center of the resume button.
     */
    private int resumeY;

    /**
     * The x-coordinate of the center of the restart button
     */
    private int restartX;

    /**
     * The y-coordinate of the center of the restart button
     */
    private int restartY;

    /**
     * The x-coordinate of the center of the level select
     */
    private int levelSelectX;

    /**
     * The y-coordinate of the center of the level select.
     */
    private int levelSelectY;

    /**
     * The x-coordinate of the center of the settings button.
     */
    private int settingsX;

    /**
     * The y-coordinate of the center of the settings button.
     */
    private int settingsY;

    /**
     * The x-coordinate of the center of the quit button.
     */
    private int quitX;

    /**
     * The y-coordinate of the center of the quit button.
     */
    private int quitY;

    /**
     * true if the player is hovering over the resume button
     */
    private boolean hoveringResume;

    /**
     * true if the player is hovering over the restart button
     */
    private boolean hoveringRestart;

    /**
     * true if the player is hovering over the level select button
     */
    private boolean hoveringLevelSelect;

    /**
     * true if the player is hovering over the settings button
     */
    private boolean hoveringSettings;

    /**
     * true if the player is hovering over the quit button
     */
    private boolean hoveringQuit;


    private float scale;

    /**
     * Scale of Start, Settings, and Exit buttons.
     */
    private final float BUTTON_SCALE = .3f;

    /**
     * Current progress (0 to 1) of the asset manager
     */
    private float progress;
    /**
     * The current state of the play button
     * <p>
     * 0 = nothing pressed
     * 1 = resume down
     * 2 = restart down
     * 3 = level select down
     * 4 = settings down
     * 5 = quit down
     * 6 = resume up, ready to go
     * 7 = restart up, ready to go
     * 8 = level select up, go to level select
     * 9 = settings up, go to settings
     * 10 = quit up, quit level
     */
    private int pressState;
    /**
     * The amount of time to devote to loading assets (as opposed to on screen hints, etc.)
     */
    private int budget;

    /**
     * Whether or not this player mode is still active
     */
    private boolean active;

    private Table pauseTable;


    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 6;
    }

    public boolean isRestart() {
        return pressState == 7;
    }

    public boolean isLevelSelect() {
        return pressState == 8;
    }

    public boolean switchSettings(){
        return pressState == 9;
    }


    /**
     * Returns true if the player clicked the quit button.
     *
     * @return true if the player wants to quit.
     */
    public boolean shouldQuit() {
        return pressState == 10;
    }


    /**
     * Constructor for making a Pause Mode
     *
     * @param canvas The game canvas to draw to
     */
    public PauseMode(GameCanvas canvas) {
        this.canvas = canvas;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(), canvas.getHeight());

        // We need these files loaded immediately
        internal = new AssetDirectory("jsons/pause.json");
        internal.loadAssets();
        internal.finishLoading();

        //We need these NOW!
        resumeButton = null;
        restartButton = null;
        levelSelectButton = null;
        settingsButton = null;
        quitButton = null;
        pointer = null;

        background = internal.getEntry("background", Texture.class);

        // No progress so far.
        progress = 0;
        pressState = 0;

        Gdx.input.setInputProcessor(this);

        // Let ANY connected controller start the game.
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener(this);
        }

        pauseTable.row();
        pauseTable.add(resumeButton).pad(0, 100, 0, 0);
        pauseTable.row();
        pauseTable.add(restartButton).pad(0, 100, 0, 0);
        pauseTable.row();
        pauseTable.add(levelSelectButton).pad(0, 100, 0, 0);
        pauseTable.row();
        pauseTable.add(settingsButton).pad(0, 100, 0, 0);
        pauseTable.row();
        pauseTable.add(quitButton).pad(0, 100, 0, 0);
        pauseTable.row();

        for (Cell cell : pauseTable.getCells()) {
            cell.align(Align.left);
        }
        pauseTable.columnDefaults(1).setActorWidth(400);
        pauseTable.columnDefaults(1).fillX();


        active = true;


    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
    }

    public void makePauseTable() {
        //style = new Label.LabelStyle(this.font, Color.WHITE);
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(null, null, null, this.font);

        resumeButton = new TextButton("Resume", style);
        resumeButton.getLabel().setFontScale(.5f);
        restartButton = new TextButton("Retry", style);
        restartButton.getLabel().setFontScale(.5f);
        levelSelectButton = new TextButton("Level Select", style);
        levelSelectButton.getLabel().setFontScale(.5f);
        settingsButton = new TextButton("Settings", style);
        settingsButton.getLabel().setFontScale(.5f);
        quitButton = new TextButton("Quit", style);
        quitButton.getLabel().setFontScale(.5f);

        resumeButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                pressState = 6;
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                resumeButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                resumeButton.getLabel().setColor(Color.WHITE);
            }
        });

        restartButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                    pressState = 7;
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                restartButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                restartButton.getLabel().setColor(Color.WHITE);
            }
        });
        levelSelectButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                pressState = 8;
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                levelSelectButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                levelSelectButton.getLabel().setColor(Color.WHITE);
            }
        });
        settingsButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                pressState = 9;
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                settingsButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                settingsButton.getLabel().setColor(Color.WHITE);
            }
        });
        quitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                pressState = 10;
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                quitButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                quitButton.getLabel().setColor(Color.WHITE);
            }
        });

        pauseTable.row();
        pauseTable.add(resumeButton).pad(0, 100, 0, 0);
        pauseTable.row();
        pauseTable.add(restartButton).pad(0, 100, 0, 0);
        pauseTable.row();
        pauseTable.add(levelSelectButton).pad(0, 100, 0, 0);
        pauseTable.row();
        pauseTable.add(settingsButton).pad(0, 100, 0, 0);
        pauseTable.row();
        pauseTable.add(quitButton).pad(0, 100, 0, 0);
        pauseTable.row();

        for (Cell cell : pauseTable.getCells()) {
            cell.align(Align.left);
        }
        pauseTable.columnDefaults(1).setActorWidth(400);
        pauseTable.columnDefaults(1).fillX();
    }

    public void initialize(BitmapFont font) {
        if (this.font != null) {
            return;
        }

        this.font = font;
        makePauseTable();
    }

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }
    /**
     * Draw the status of this player mode.
     * <p>
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        stage.getBatch().begin();
        resize((int)canvas.getCamera().viewportWidth, (int)canvas.getCamera().viewportHeight);
        canvas.draw(background, Color.WHITE, 0, 0, canvas.getCamera().viewportWidth, canvas.getCamera().viewportHeight);


        int spacing = 15;
//        stage.getBatch().draw(pointer,
//                resumeButton.getX() + resumeButton.getWidth() + spacing,
//                resumeButton.getY() + resumeButton.getWidth() + spacing,
//                pointer.getRegionHeight()/2,
//                pointer.getRegionWidth(),
//                pointer.getRegionHeight(), 1, 1, 180);
        stage.getBatch().draw(pointer,
                resumeButton.getX() - pointer.getRegionWidth() - spacing,
                resumeButton.getY(),
                pointer.getRegionWidth() / 2,
                pointer.getRegionHeight() / 2,
                pointer.getRegionWidth(),
                pointer.getRegionHeight(), 1, 1, 0);

//        stage.getBatch().draw(pointer,
//                restartButton.getX() + restartButton.getWidth() + spacing,
//                restartButton.getY() + restartButton.getWidth() + spacing,
//                pointer.getRegionHeight()/2,
//                pointer.getRegionWidth(),
//                pointer.getRegionHeight(), 1, 1, 180);
        stage.getBatch().draw(pointer,
                restartButton.getX() - pointer.getRegionWidth() - spacing,
                restartButton.getY(),
                pointer.getRegionWidth() / 2,
                pointer.getRegionHeight() / 2,
                pointer.getRegionWidth(),
                pointer.getRegionHeight(), 1, 1, 0);

//        stage.getBatch().draw(pointer,
//                levelSelectButton.getX() + levelSelectButton.getWidth() + spacing,
//                levelSelectButton.getY() + levelSelectButton.getWidth() + spacing,
//                pointer.getRegionHeight()/2,
//                pointer.getRegionWidth(),
//                pointer.getRegionHeight(), 1, 1, 180);
        stage.getBatch().draw(pointer,
                levelSelectButton.getX() - pointer.getRegionWidth() - spacing,
                levelSelectButton.getY(),
                pointer.getRegionWidth() / 2,
                pointer.getRegionHeight() / 2,
                pointer.getRegionWidth(),
                pointer.getRegionHeight(), 1, 1, 0);

//        stage.getBatch().draw(pointer,
//                settingsButton.getX() + settingsButton.getWidth() + spacing,
//                settingsButton.getY() + settingsButton.getWidth() + spacing,
//                pointer.getRegionHeight()/2,
//                pointer.getRegionWidth(),
//                pointer.getRegionHeight(), 1, 1, 180);
        stage.getBatch().draw(pointer,
                settingsButton.getX() - pointer.getRegionWidth() - spacing,
                settingsButton.getY(),
                pointer.getRegionWidth() / 2,
                pointer.getRegionHeight() / 2,
                pointer.getRegionWidth(),
                pointer.getRegionHeight(), 1, 1, 0);

//        stage.getBatch().draw(pointer,
//                quitButton.getX() + quitButton.getWidth() + spacing,
//                quitButton.getY() + quitButton.getWidth() + spacing,
//                pointer.getRegionHeight()/2,
//                pointer.getRegionWidth(),
//                pointer.getRegionHeight(), 1, 1, 180);
        stage.getBatch().draw(pointer,
                quitButton.getX() - pointer.getRegionWidth() - spacing,
                quitButton.getY(),
                pointer.getRegionWidth() / 2,
                pointer.getRegionHeight() / 2,
                pointer.getRegionWidth(),
                pointer.getRegionHeight(), 1, 1, 0);

        canvas.end();
    }

    // ADDITIONAL SCREEN METHODS

    /**
     * Called when the Screen should render itself.
     * <p>
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            draw();
            // If the player hits the start/play button
            // We are ready, notify our listener
            if (isReady() && listener != null) {
                listener.exitScreen(this, 1);
            }

            if (isRestart() && listener != null) {
                listener.exitScreen(this, 2);
            }

            if (isLevelSelect() && listener != null) {
                listener.exitScreen(this, 3);
            }

            if (switchSettings() && listener != null){
                listener.exitScreen(this, 4);
            }
            // If the player hits the quit button
            if (shouldQuit()) {
                listener.exitScreen(this, GameController.EXIT_QUIT);
            }


        }
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

        this.width = (int) (BAR_WIDTH_RATIO * width);
        centerY = (int) (BAR_HEIGHT_RATIO * height);
        centerX = width / 2;
        heightY = height;
    }

    /**
     * Called when the Screen is paused.
     * <p>
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     * <p>
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
        pressState = 0;
        Gdx.input.setInputProcessor(this);
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
        pressState = 0;
    }


    // PROCESSING PLAYER INPUT

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
        if (resumeButton == null || restartButton == null
                || levelSelectButton == null || settingsButton == null || quitButton == null) return false;

        //Detect clicks on the start button
        float rectWidth = scale * BUTTON_SCALE * resumeButton.getWidth();
        float rectHeight = scale * BUTTON_SCALE * resumeButton.getHeight();
        float leftX = resumeX - rectWidth / 2.0f;
        float rightX = resumeX + rectWidth / 2.0f;
        float topY = resumeY - rectHeight / 2.0f;
        float bottomY = resumeY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 1;
        }

        //Detect clicks on the level select button
        rectWidth = scale * BUTTON_SCALE * restartButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * restartButton.getHeight();
        leftX = restartX - rectWidth / 2.0f;
        rightX = restartX + rectWidth / 2.0f;
        topY = restartY - rectHeight / 2.0f;
        bottomY = restartY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 2;
        }

        //Detect clicks on the settings button
        rectWidth = scale * BUTTON_SCALE * levelSelectButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * levelSelectButton.getHeight();
        leftX = levelSelectX - rectWidth / 2.0f;
        rightX = levelSelectX + rectWidth / 2.0f;
        topY = levelSelectY - rectHeight / 2.0f;
        bottomY = levelSelectY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 3;
        }

        //Detect clicks on the exit button
        rectWidth = scale * BUTTON_SCALE * settingsButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * settingsButton.getHeight();
        leftX = settingsX - rectWidth / 2.0f;
        rightX = settingsX + rectWidth / 2.0f;
        topY = settingsY - rectHeight / 2.0f;
        bottomY = settingsY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 4;
        }

        rectWidth = scale * BUTTON_SCALE * quitButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * quitButton.getHeight();
        leftX = quitX - rectWidth / 2.0f;
        rightX = quitX + rectWidth / 2.0f;
        topY = quitY - rectHeight / 2.0f;
        bottomY = quitY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 5;
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
            pressState = 6;
            return false;
        }

        //Level Select
        if (pressState == 2) {
            pressState = 7;
            return false;
        }

        //Settings
        if (pressState == 3) {
            pressState = 8;
            return false;
        }

        //Exit
        if (pressState == 4) {
            pressState = 9;
            return false;
        }

        if (pressState == 5) {
            pressState = 10;
            return false;
        }

        return true;
    }

//    /**
//     * Called when a button on the Controller was pressed.
//     * <p>
//     * The buttonCode is controller specific. This listener only supports the start
//     * button on an X-Box controller.  This outcome of this method is identical to
//     * pressing (but not releasing) the play button.
//     *
//     * @param controller The game controller
//     * @param buttonCode The button pressed
//     * @return whether to hand the event to other listeners.
//     */
//    public boolean buttonDown(Controller controller, int buttonCode) {
//        if (pressState == 0) {
//            ControllerMapping mapping = controller.getMapping();
//            if (mapping != null && buttonCode == mapping.buttonStart) {
//                pressState = 1;
//                return false;
//            }
//        }
//        return true;
//    }

    /**
     * Called when a button on the Controller was released.
     * <p>
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp(Controller controller, int buttonCode) {
        if (pressState == 1) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart) {
                pressState = 6;
                return false;
            }
        }
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

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

        if (resumeButton == null || restartButton == null
                || levelSelectButton == null || settingsButton == null || quitButton == null) return false;
        // Flip to match graphics coordinates

        //Detect hovers on the start button
        float rectWidth = scale * BUTTON_SCALE * resumeButton.getWidth();
        float rectHeight = scale * BUTTON_SCALE * resumeButton.getHeight();
        float leftX = resumeX - rectWidth / 2.0f;
        float rightX = resumeX + rectWidth / 2.0f;
        float topY = (resumeY - (rectHeight) / 2.0f);
        float bottomY = (resumeY + (rectHeight) / 2.0f);
        hoveringResume = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

        //Detect hovers on the level select button
        rectWidth = scale * BUTTON_SCALE * restartButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * restartButton.getHeight();
        leftX = restartX - rectWidth / 2.0f;
        rightX = restartX + rectWidth / 2.0f;
        topY = restartY - rectHeight / 2.0f;
        bottomY = restartY + rectHeight / 2.0f;
        hoveringRestart = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

        //Detect hovers on the settings button
        rectWidth = scale * BUTTON_SCALE * levelSelectButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * levelSelectButton.getHeight();
        leftX = levelSelectButton.getX() - rectWidth / 2.0f;
        rightX = levelSelectButton.getX() + rectWidth / 2.0f;
        topY = levelSelectButton.getY() - rectHeight / 2.0f;
        bottomY = levelSelectButton.getY() + rectHeight / 2.0f;
        hoveringLevelSelect = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

        //Detect hovers on the exit button
        rectWidth = scale * BUTTON_SCALE * settingsButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * settingsButton.getHeight();
        leftX = settingsX - rectWidth / 2.0f;
        rightX = settingsX + rectWidth / 2.0f;
        topY = settingsY - rectHeight / 2.0f;
        bottomY = settingsY + rectHeight / 2.0f;
        hoveringSettings = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

        //Detect hovers on the exit button
        rectWidth = scale * BUTTON_SCALE * quitButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * quitButton.getHeight();
        leftX = quitX - rectWidth / 2.0f;
        rightX = quitX + rectWidth / 2.0f;
        topY = quitY - rectHeight / 2.0f;
        bottomY = quitY + rectHeight / 2.0f;
        hoveringQuit = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

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
    public void connected(Controller controller) {
    }

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected(Controller controller) {
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        return false;
    }

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     * <p>
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode   The axis moved
     * @param value      The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return true;
    }

}
