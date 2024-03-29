/*
 * LoadingMode.java
 *
 * Asset loading is a really tricky problem.  If you have a lot of sound or images,
 * it can take a long time to decompress them and load them into memory.  If you just
 * have code at the start to load all your assets, your game will look like it is hung
 * at the start.
 *
 * The alternative is asynchronous asset loading.  In asynchronous loading, you load a
 * little bit of the assets at a time, but still animate the game while you are loading.
 * This way the player knows the game is not hung, even though he or she cannot do
 * anything until loading is complete. You know those loading screens with the inane tips
 * that want to be helpful?  That is asynchronous loading.
 *
 * This player mode provides a basic loading screen.  While you could adapt it for
 * between level loading, it is currently designed for loading all assets at the
 * start of the game.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.bubblegumbandit.controllers.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.helpers.SaveData;
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
public class LoadingMode implements Screen, InputProcessor, ControllerListener {
    // There are TWO asset managers.  One to load the loading screen.  The other to load the assets
    /**
     * Internal assets for this loading screen
     */
    private AssetDirectory internal;
    /**
     * The actual assets to be loaded
     */
    private AssetDirectory assets;

    /**
     * Background texture for start-up
     */
    private Texture background;

    /**
     * Background while loading
     */
    private Texture loadingBackground;

    /**
     * Button to start the game
     */
    private Texture startButton;

    /**
     * Button to reset save
     */
    private Texture newSaveButton;

    /**
     * Button to enter level select
     */
    private Texture creditsButton;

    /**
     * Button to open settings
     */
    private Texture settingsButton;

    /**
     * Button to quit
     */
    private Texture exitButton;

    private Texture skipButton;

    /**
     * Pointer to what is being hovered.
     */
    private Texture hoverPointer;

    /**
     * The background for the progress bar
     */
    private final Texture progressBackground;

    /**
     * The fill for the progress bar
     */
    private final Texture progressFill;


    /**
     * The current fill for the progress bar
     */
    private TextureRegion progressCurrent;

    private Texture page1;
    private Texture page2;
    private Texture page3;
    private Texture currentPage;


    /**
     * The margin for the progress bar fill against the background
     */
    private final int FILL_MARGIN = 4;

    /**
     * The sunfish!
     */
    private final Texture sunfish;

    /**
     * The texture for the jet behind the sunfish
     */
    private final Texture jet;

    /**
     * Container for the scales and scaling directions of the jets
     */
    private float[][] sizes = new float[][] {{.33f,1},{ .66f,1},{1, 1f}};

    /**
     * The font for the "loading" text displayed
     */
    private final BitmapFont font;

    /**
     * The texture for the big ship illustration
     */
    private final Texture ship;



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
    private static float BAR_HEIGHT_RATIO = 0.4f;

    private static float BUTTONS_PUSH_DOWN = 50f;

    /**
     * Reference to GameCanvas created by the root
     */
    private GameCanvas canvas;
    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;

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
     * The x-coordinate of the center of the settings button.
     */
    private int settingsButtonPositionX;

    /**
     * The y-coordinate of the center of the settings button.
     */
    private int settingsButtonPositionY;

    /**
     * The x-coordinate of the center of the exit button.
     */
    private int exitButtonPositionX;

    /**
     * The y-coordinate of the center of the exit button.
     */
    private int exitButtonPositionY;

    private int skipPositionX;

    private int skipPositionY;

    /**
     * true if the player is hovering over the start button
     */
    private boolean hoveringStart;

    /**
     * true if the player is hovering over the level select button
     */
    private boolean hoveringCredits;

    /**
     * true if the player is hovering over the settings button
     */
    private boolean hoveringSettings;

    /**
     * true if the player is hovering over the exit button
     */
    private boolean hoveringExit;

    private boolean hoveringSkip;

    private boolean displayNewSave;

    private boolean hoveringNewSave;

    private int newSavePositionX;

    private int newSavePositionY;


    private float scale;

     public final Color hoverTint = new Color(1, 149 / 255f, 138 / 255f, 1);

     public final Color pressTint = new Color(70/255f, 153/255f, 167/255f,1);

     public final Color defaultTint = Color.WHITE;

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
     * 1 = play down
     * 2 = settings down
     * 3 = credits down
     * 4 = exit down
     * 5 = play up, ready to go
     * 6 = level select up, should open level select
     * 7 = settings up, should open settings
     * 8 = credits up, should open credits
     * 9 = exit up, should quit.
     * 10 = skip down
     * 11 = skip up, should skip the video
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

    private float shipTime;

    private boolean skipClicked;
    private Color pageColor = Color.CLEAR.cpy();
    float pageTimer = -1;

    /**
     * Returns the budget for the asset loader.
     * <p>
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @return the budget in milliseconds
     */
    public int getBudget() {
        return budget;
    }

    /**
     * Sets the budget for the asset loader.
     * <p>
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param millis the budget in milliseconds
     */
    public void setBudget(int millis) {
        budget = millis;
    }

    /**
     * Returns true if the player touched up on the level select button.
     *
     * @return true if the player clicked level select
     * */
    public boolean isReady() {
        return pressState == 5;
    }

    public boolean isNewSave() {
        return pressState == 13;
    }

    /**
     * Returns true if the player touched up on the settings switch button.
     *
     * @return true if the player clicked to switch settings.
     * */
    public boolean isSettings(){
        return pressState == 6;
    }

    /**
     * Returns true if the player touched up on the credits button.
     *
     * @return true if the player clicked on the credits button.
     * */
    public boolean isCredits() {return pressState == 7;}

    /**
     * Returns true if the player clicked the quit button.
     *
     * @return true if the player wants to quit.
     */
    public boolean shouldQuit() {
        return pressState == 8;
    }



    /**
     * Returns the asset directory produced by this loading screen
     * <p>
     * This asset loader is NOT owned by this loading scene, so it persists even
     * after the scene is disposed.  It is your responsbility to unload the
     * assets in this directory.
     *
     * @return the asset directory produced by this loading screen
     */
    public AssetDirectory getAssets() {
        return assets;
    }

    /**
     * Creates a LoadingMode with the default budget, size and position.
     *
     * @param file   The asset directory to load in the background
     * @param canvas The game canvas to draw to
     */
    public LoadingMode(String file, GameCanvas canvas) {
        this(file, canvas, DEFAULT_BUDGET);
    }

    /**
     * Creates a LoadingMode with the default size and position.
     * <p>
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param file   The asset directory to load in the background
     * @param canvas The game canvas to draw to
     * @param millis The loading budget in milliseconds
     */
    public LoadingMode(String file, GameCanvas canvas, int millis) {
        this.canvas = canvas;
        budget = millis;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(), canvas.getHeight());

        // We need these files loaded immediately
        internal = new AssetDirectory("jsons/loading.json");
        internal.loadAssets();
        internal.finishLoading();

        //We need these NOW!
        startButton = null;
        creditsButton = null;
        settingsButton = null;
        exitButton = null;
        skipButton = null;
        hoverPointer = null;
        skipClicked = false;

        background = internal.getEntry("background", Texture.class);
        background.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        loadingBackground= internal.getEntry("loadingBackground", Texture.class);
        loadingBackground.setFilter(TextureFilter.Linear, TextureFilter.Linear);

         progressBackground = internal.getEntry("progressBackground", Texture.class);
         progressFill = internal.getEntry("progressFill", Texture.class);
         progressCurrent = new TextureRegion(progressFill);

         sunfish = internal.getEntry("spaceship", Texture.class);
         jet = internal.getEntry("jet", Texture.class);

         font = internal.getEntry("projectSpace", BitmapFont.class);
         ship = internal.getEntry("bigShip", Texture.class);
         ship.setFilter(TextureFilter.Linear, TextureFilter.Linear);

         page1 = internal.getEntry("page1", Texture.class);
         page2 = internal.getEntry("page2", Texture.class);
        page3 = internal.getEntry("page3", Texture.class);
         currentPage = page1;


       // No progress so far.
        progress = 0;
        pressState = 0;

        Gdx.input.setInputProcessor(this);

        // Let ANY connected controller start the game.
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener(this);
        }

        // Start loading the real assets
        assets = new AssetDirectory(file);
        assets.loadAssets();

        active = true;

    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
        assets.unloadAssets();
        assets.dispose();
    }

    private boolean dataMade = false;

    /**
     * Update the status of this player mode.
     * <p>
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        if (startButton == null) {
            assets.update(budget);
            this.progress = assets.getProgress();
            if (progress >= 1.0f) {
                SoundController.getInstance().initialize(assets);
                this.progress = 1.0f;
                hoverPointer = internal.getEntry("hoverPointer", Texture.class);
                startButton = internal.getEntry("startButton", Texture.class);
                creditsButton = internal.getEntry("creditsButton", Texture.class);
                settingsButton = internal.getEntry("settingsButton", Texture.class);
                exitButton = internal.getEntry("exitButton", Texture.class);
                skipButton = internal.getEntry("skipButton", Texture.class);
                newSaveButton = internal.getEntry("newSaveButton", Texture.class);
            }
        }
        resize(canvas.getWidth(), canvas.getHeight());
        if (assets.isFinished()) {
            SoundController.playMusic("menu");
            if (!dataMade) {
                dataMade = true;
                boolean hasSave = SaveData.saveExists(assets);
                displayNewSave = true;
                if (!hasSave) {
                    SaveData.makeData(assets);
                    displayNewSave = false;
                }
            }
            if (pageTimer == -1) {
                //boolean hasSave = SaveData.saveExists(assets);
                pageTimer = 12f;
                currentPage = page1;
            } else if (assets.isFinished() && pageTimer > 0) {
                pageTimer -= delta;
                if (pageTimer < 8 && currentPage == page1) {
                    currentPage = page2;
                    SoundController.playSound("pageTurn", 1);
                } else if (pageTimer < 4 && currentPage == page2) {
                    currentPage = page3;
                    SoundController.playSound("pageTurn", 1);
                }
                if (pageTimer > 11.5) pageColor = new Color(1, 1, 1, pageColor.a += .1f);
                else if (pageTimer < 8.5 && pageTimer >= 8) pageColor = new Color(1, 1, 1, pageColor.a -= .1f);
                else if (pageTimer < 8 && pageTimer > 7.5) pageColor = new Color(1, 1, 1, pageColor.a += .1f);
                else if (pageTimer < 4.5 && pageTimer >= 4) pageColor = new Color(1, 1, 1, pageColor.a -= .1f);
                else if (pageTimer < 4 && pageTimer > 3.5) pageColor = new Color(1, 1, 1, pageColor.a += .1f);
                else if (pageTimer < .5) pageColor = new Color(1, 1, 1, pageColor.a -= .1f);
                else pageColor = Color.WHITE.cpy();

            }
        }
    }


    /**
     * Draw the status of this player mode.
     * <p>
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw(){
        canvas.begin();
        resize((int)canvas.getCamera().viewportWidth, (int)canvas.getCamera().viewportHeight);

        if (startButton == null || settingsButton == null || exitButton == null
            || hoverPointer == null|| newSaveButton == null) {
            drawProgress(canvas);
        } else {
            if(pageTimer>0) {
                canvas.clear();
                //canvas.draw(loadingBackground, Color.WHITE, 0, 0,
                   // canvas.getCamera().viewportWidth, canvas.getCamera().viewportHeight);
                canvas.draw(currentPage, pageColor,
                    canvas.getCamera().viewportWidth/2-currentPage.getWidth()/2, canvas.getCamera().viewportHeight/2-currentPage.getHeight()/2, currentPage.getWidth(), currentPage.getHeight());

//                skipPositionX = (int) canvas.getCamera().viewportWidth/10;
//                skipPositionY = (int) (canvas.getCamera().viewportHeight - skipButton.getHeight() * 4);
                skipPositionX = (int) canvas.getCamera().viewportWidth - 85;
                skipPositionY = (int) (canvas.getCamera().viewportHeight - skipButton.getHeight() * 4.5);

                canvas.draw(
                        skipButton,
                        getButtonTint("skip"),
                        skipButton.getWidth() / 2f,
                        skipButton.getHeight() / 2f,
                        skipPositionX,
                        skipPositionY,
                        0,
                        scale * BUTTON_SCALE,
                        scale * BUTTON_SCALE
                );
            } else {
                canvas.draw(background, Color.WHITE, 0, 0, canvas.getCamera().viewportWidth, canvas.getCamera().viewportHeight);

                float highestButtonY = canvas.getCamera().viewportHeight / 2 - BUTTONS_PUSH_DOWN;
                float lowestButtonY = canvas.getCamera().viewportHeight / 6;
                float buttonSpace = highestButtonY+BUTTONS_PUSH_DOWN - lowestButtonY;
                float gap = displayNewSave? buttonSpace/5.35f : buttonSpace / 4;


                int gapMulti = 0;

                startButtonPositionX = (int) canvas.getCamera().viewportWidth / 5;
                startButtonPositionY = (int) ((int) highestButtonY-gap*gapMulti);

                gapMulti++;

                if(displayNewSave) {
                    newSavePositionX = (int) canvas.getCamera().viewportWidth / 5;
                    newSavePositionY = (int) ((int) highestButtonY-gap*gapMulti);
                    gapMulti++;
                }

                settingsButtonPositionX = (int) canvas.getCamera().viewportWidth / 5;
                settingsButtonPositionY = (int) (highestButtonY - gap*gapMulti);

                gapMulti++;

                levelSelectButtonPositionX = (int) canvas.getCamera().viewportWidth / 5;
                levelSelectButtonPositionY = (int) (highestButtonY - gap * gapMulti);

                gapMulti++;

                exitButtonPositionX = (int) canvas.getCamera().viewportWidth / 5;
                exitButtonPositionY = (int) (highestButtonY - gap * gapMulti);


                float pointerX = startButtonPositionX / 4f;


                //Draw Continue Game
                canvas.draw(
                    startButton,
                    getButtonTint("start"),
                    startButton.getWidth() / 2f,
                    startButton.getHeight() / 2f,
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

                if(displayNewSave) {
                    canvas.draw(
                        newSaveButton,
                        getButtonTint("newSave"),
                        newSaveButton.getWidth() / 2f,
                        newSaveButton.getHeight() / 2f,
                        newSavePositionX,
                        newSavePositionY,
                        0,
                        scale * BUTTON_SCALE,
                        scale * BUTTON_SCALE
                    );
                    if (hoveringNewSave) {
                        canvas.draw(
                            hoverPointer,
                            Color.WHITE,
                            hoverPointer.getWidth() / 2f,
                            hoverPointer.getHeight() / 2f,
                            pointerX,
                            newSavePositionY,
                            0,
                            scale,
                            scale
                        );
                    }
                }



                //Draw Level Select
                canvas.draw(
                    creditsButton,
                    getButtonTint("credits"),
                    creditsButton.getWidth() / 2f,
                    creditsButton.getHeight() / 2f,
                    levelSelectButtonPositionX,
                    levelSelectButtonPositionY,
                    0,
                    scale * BUTTON_SCALE,
                    scale * BUTTON_SCALE
                );
                if (hoveringCredits) {
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

                //Draw Settings
                canvas.draw(
                    settingsButton,
                    getButtonTint("settings"),
                    settingsButton.getWidth() / 2f,
                    settingsButton.getHeight() / 2f,
                    settingsButtonPositionX,
                    settingsButtonPositionY,
                    0,
                    scale * BUTTON_SCALE,
                    scale * BUTTON_SCALE
                );
                if (hoveringSettings) {
                    canvas.draw(
                        hoverPointer,
                        Color.WHITE,
                        hoverPointer.getWidth() / 2f,
                        hoverPointer.getHeight() / 2f,
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
                    exitButton.getWidth() / 2f,
                    exitButton.getHeight() / 2f,
                    exitButtonPositionX,
                    exitButtonPositionY,
                    0,
                    scale * BUTTON_SCALE,
                    scale * BUTTON_SCALE
                );

                if (hoveringExit) {
                    canvas.draw(
                        hoverPointer,
                        Color.WHITE,
                        hoverPointer.getWidth() / 2f,
                        hoverPointer.getHeight() / 2f,
                        pointerX,
                        exitButtonPositionY,
                        0,
                        scale,
                        scale
                    );
                }

                float offsetY = (float) (Math.sin(shipTime) * 10 )+20;

                canvas.draw(ship, Color.WHITE, 0, 0,
                    canvas.getCamera().viewportWidth-ship.getWidth(),
                    canvas.getCamera().viewportHeight-ship.getHeight()+offsetY,
                    ship.getWidth(), ship.getHeight());

            }
            }

        canvas.end();
    }


    private Color getButtonTint(String buttonName) { //why is this not a switch?

        if (buttonName.equals("start")) {
            if (hoveringStart && pressState == 1) return pressTint;
            else if (hoveringStart) return hoverTint;
            else return defaultTint;
        }

        if (buttonName.equals("settings")) {
            if (hoveringSettings && pressState == 2) return pressTint;
            else if (hoveringSettings) return hoverTint;
            else return defaultTint;
        }
        if(buttonName.equals("newSave")) {
            if(hoveringNewSave&&pressState == 12) return pressTint;
            else if(hoveringNewSave) return hoverTint;
            else return defaultTint;
        }

        if(buttonName.equals("credits")) {
            if (hoveringCredits && pressState == 3) return pressTint;
            else if (hoveringCredits) return hoverTint;
            else return defaultTint;        }

        if (buttonName.equals("exit")) {
            if (hoveringExit && pressState == 4) return pressTint;
            else if (hoveringExit) return hoverTint;
            else return defaultTint;
        }

        if (buttonName.equals("skip")) {
            if (hoveringSkip && pressState == 10) return pressTint;
            else if (hoveringSkip) return hoverTint;
            else return defaultTint;
        }

        //Should never reach here, keeps Java happy
        return null;
    }




    /**
     * Updates the progress bar according to loading progress
     * <p>
     * The progress bar is composed of parts: two rounded caps on the end,
     * and a rectangle in a middle.  We adjust the size of the rectangle in
     * the middle to represent the amount of progress.
     *
     * @param canvas The drawing context
     */
    private void drawProgress(GameCanvas canvas) {
        canvas.draw(loadingBackground, Color.WHITE, 0, 0,
            canvas.getCamera().viewportWidth, canvas.getCamera().viewportHeight);
        canvas.draw(progressBackground, Color.WHITE,
            centerX-progressBackground.getWidth()/2f, centerY,
            scale*progressBackground.getWidth(), scale*progressBackground.getHeight());
      progressCurrent = new TextureRegion(progressFill, 0, 0,
          (int) (progressFill.getWidth()*progress), progressFill.getHeight());
      canvas.draw(progressCurrent, Color.WHITE, 0, 0,
          centerX-progressFill.getWidth()/2f, centerY+FILL_MARGIN,
          progressCurrent.getRegionWidth(), progressCurrent.getRegionHeight());
      canvas.draw(sunfish, Color.WHITE, 0f, 0f, (float) centerX+progressBackground.getWidth()/2f-
              sunfish.getHeight(),
          (float) centerY+progressBackground.getHeight()*3.2f, (float) (-Math.PI/2f), 1f, 1f);
      for(int i = 0; i<3; i++) {
          sizes[i][0] +=.02*sizes[i][1];
          if(sizes[i][0]>1) {
              sizes[i][0] = 1;
              sizes[i][1] = sizes[i][1]*-1;
          } else if (sizes[i][0]<.2) {
              sizes[i][0] = .2f;
              sizes[i][1] = sizes[i][1]*-1;
          }
          canvas.draw(jet, Color.WHITE, jet.getWidth()*sizes[i][0]/2, jet.getHeight()*sizes[i][0]/2,
              (float) centerX+progressBackground.getWidth()/2f-
                  sunfish.getHeight()-(jet.getWidth()+8)*(i+.6f),
              (float) centerY+progressBackground.getHeight()*2.3f, (float) (-Math.PI/2f), sizes[i][0],  sizes[i][0]);
      }
      canvas.drawText("LOADING", font, centerX-progressBackground.getWidth()/2f, centerY+progressBackground.getHeight()*2.3f);

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
            shipTime+=delta;
            update(delta);
            draw();
            // If the player hits the start/play button
            // We are ready, notify our listener
            if (isReady() && listener != null) {
                SoundController.playSound("keyClick", 1);
                listener.exitScreen(this, Screens.LEVEL_SELECT);

            }
            if (isSettings() && listener != null){
                SoundController.playSound("keyClick", 1);
                listener.exitScreen(this, Screens.SETTINGS);
            }

            if (isNewSave() && listener != null){
                SoundController.playSound("keyClick", 1);
                displayNewSave = false;
                SaveData.makeData(assets);
                pressState = 0;
                return;
            }

            if (isCredits() && listener != null){
                SoundController.playSound("keyClick", 1);
                listener.exitScreen(this, Screens.CREDITS);
            }

            // If the player hits the quit button
            if (shouldQuit()) {
                SoundController.playSound("keyClick", 1);
                listener.exitScreen(this, Screens.EXIT_CODE);
            }

            if (skipClicked) {
                SoundController.playSound("keyClick", 1);
                pageTimer = 0;
                skipClicked = false;
                pressState = 0;
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

    /**
     * Sets the ScreenListener for this mode
     * <p>
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
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
        if (startButton == null || creditsButton == null
                || settingsButton == null || exitButton == null || skipButton == null) return false;

        //Detect clicks on the start button
        float rectWidth = scale * BUTTON_SCALE * startButton.getWidth();
        float rectHeight = scale * BUTTON_SCALE * startButton.getHeight();
        float leftX = startButtonPositionX - rectWidth / 2.0f;
        float rightX = startButtonPositionX + rectWidth / 2.0f;
        float topY = startButtonPositionY - rectHeight / 2.0f;
        float bottomY = startButtonPositionY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 1;
        }

        //Detect clicks on the settings button
        rectWidth = scale * BUTTON_SCALE * settingsButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * settingsButton.getHeight();
        leftX = settingsButtonPositionX - rectWidth / 2.0f;
        rightX = settingsButtonPositionX + rectWidth / 2.0f;
        topY = settingsButtonPositionY - rectHeight / 2.0f;
        bottomY = settingsButtonPositionY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 2;
        }

        //Detect clicks on new save button
        if(displayNewSave) {
            rectWidth = scale * BUTTON_SCALE * newSaveButton.getWidth();
            rectHeight = scale * BUTTON_SCALE * newSaveButton.getHeight();
            leftX = newSavePositionX - rectWidth / 2.0f;
            rightX = newSavePositionX + rectWidth / 2.0f;
            topY = newSavePositionY - rectHeight / 2.0f;
            bottomY = newSavePositionY + rectHeight / 2.0f;
            if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
                pressState = 12;
            }
        }


        //Detect clicks on the credits button
        rectWidth = scale * BUTTON_SCALE * creditsButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * creditsButton.getHeight();
        leftX = levelSelectButtonPositionX - rectWidth / 2.0f;
        rightX = levelSelectButtonPositionX + rectWidth / 2.0f;
        topY = levelSelectButtonPositionY - rectHeight / 2.0f;
        bottomY = levelSelectButtonPositionY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 3;
        }

        //Detect clicks on the exit button
        rectWidth = scale * BUTTON_SCALE * exitButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * exitButton.getHeight();
        leftX = exitButtonPositionX - rectWidth / 2.0f;
        rightX = exitButtonPositionX + rectWidth / 2.0f;
        topY = exitButtonPositionY - rectHeight / 2.0f;
        bottomY = exitButtonPositionY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 4;
        }

        rectWidth = scale * BUTTON_SCALE * skipButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * skipButton.getHeight();
        leftX = skipPositionX - rectWidth / 2.0f;
        rightX = skipPositionX + rectWidth / 2.0f;
        topY = skipPositionY - rectHeight / 2.0f;
        bottomY = skipPositionY + rectHeight / 2.0f;
        if (pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY) {
            pressState = 10;
            skipClicked = true;
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

        //Settings
        if (pressState == 3) {
            pressState = 7;
            return false;
        }

        //New Save
        if(pressState == 12) {
            pressState = 13;
            return false;
        }

        //Credits
        if (pressState == 4) {
            pressState = 8;
            return false;
        }

        //Exit
        if (pressState == 5) {
            pressState = 9;
            return false;
        }

        if (pressState == 10) {
            pressState = 11;
            return false;
        }

        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     * <p>
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (pressState == 0) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart) {
                pressState = 1;
                return false;
            }
        }
        return true;
    }

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
                pressState = 2;
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

        if (startButton == null || creditsButton == null
                || settingsButton == null || exitButton == null) return false;
        // Flip to match graphics coordinates

        //Detect hovers on the start button
        float rectWidth = scale * BUTTON_SCALE * startButton.getWidth();
        float rectHeight = scale * BUTTON_SCALE * startButton.getHeight();
        float leftX = startButtonPositionX - rectWidth / 2.0f;
        float rightX = startButtonPositionX + rectWidth / 2.0f;
        float topY = (startButtonPositionY - (rectHeight) / 2.0f);
        float bottomY = (startButtonPositionY + (rectHeight) / 2.0f);
        hoveringStart = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

        //Detect hovers on the settings button
        rectWidth = scale * BUTTON_SCALE * settingsButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * settingsButton.getHeight();
        leftX = settingsButtonPositionX - rectWidth / 2.0f;
        rightX = settingsButtonPositionX + rectWidth / 2.0f;
        topY = settingsButtonPositionY - rectHeight / 2.0f;
        bottomY = settingsButtonPositionY + rectHeight / 2.0f;
        hoveringSettings = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

        //Detect hovers on the credits
        rectWidth = scale * BUTTON_SCALE * creditsButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * creditsButton.getHeight();
        leftX = levelSelectButtonPositionX - rectWidth / 2.0f;
        rightX = levelSelectButtonPositionX + rectWidth / 2.0f;
        topY = levelSelectButtonPositionY - rectHeight / 2.0f;
        bottomY = levelSelectButtonPositionY + rectHeight / 2.0f;
        hoveringCredits = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

        //Detect hovers on the exit button
        rectWidth = scale * BUTTON_SCALE * exitButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * exitButton.getHeight();
        leftX = exitButtonPositionX - rectWidth / 2.0f;
        rightX = exitButtonPositionX + rectWidth / 2.0f;
        topY = exitButtonPositionY - rectHeight / 2.0f;
        bottomY = exitButtonPositionY + rectHeight / 2.0f;
        hoveringExit = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

        rectWidth = scale * BUTTON_SCALE * newSaveButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * newSaveButton.getHeight();
        leftX = newSavePositionX - rectWidth / 2.0f;
        rightX = newSavePositionX + rectWidth / 2.0f;
        topY = newSavePositionY - rectHeight / 2.0f;
        bottomY = newSavePositionY + rectHeight / 2.0f;
        hoveringNewSave = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

        rectWidth = scale * BUTTON_SCALE * skipButton.getWidth();
        rectHeight = scale * BUTTON_SCALE * skipButton.getHeight();
        leftX = skipPositionX - rectWidth / 2.0f;
        rightX = skipPositionX + rectWidth / 2.0f;
        topY = skipPositionY - rectHeight / 2.0f;
        bottomY = skipPositionY + rectHeight / 2.0f;
        hoveringSkip = pixelX >= leftX && pixelX <= rightX && pixelY >= topY && pixelY <= bottomY;

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