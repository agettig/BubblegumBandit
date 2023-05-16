/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter.
 * There must be some undocumented OpenGL code in setScreen.
 *
 * This class differs slightly from the labs in that the AssetManager is now a
 * singleton and is not constructed by this class.
 *
 * Author: Walker M. White
 * Version: 3/2/2016
 */
package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.modes.*;
import edu.cornell.gdiac.bubblegumbandit.view.GameCamera;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.bubblegumbandit.controllers.modes.GameOverScreen;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.assets.*;

import static edu.cornell.gdiac.bubblegumbandit.controllers.GameController.disableGumMaxLevel;

/**
 * Root class for a LibGDX.
 * <p>
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However,
 * those classes are unique to each platform, while this class is the same across all
 * plaforms. In addition, this functions as the root class all intents and purposes,
 * and you would draw it as a root class in an architecture specification.
 */
public class GDXRoot extends Game implements ScreenListener {
    /**
     * AssetManager to load game assets (textures, sounds, etc.)
     */
    AssetDirectory directory;
    /**
     * Drawing context to display graphics (VIEW CLASS)
     */
    private GameCanvas canvas;
    /**
     * Player mode for the asset loading screen (CONTROLLER CLASS)
     */
    private LoadingMode loading;
    /**
     * Player mode for the level select screen (CONTROLLER CLASS)
     */
    private LevelSelectMode levels;

    /**
     * Player mode for the credits screen (VIEW CLASS)
     * */
    private CreditsMode credits;

    /**
     * Player mode for the game proper (CONTROLLER CLASS)
     */
    private GameController controller;

    /**
     * Won/Lost screen
     */
    private GameOverScreen gameOver;

    private SettingsMode settingsMode;

    private Cursor mouseCursor;

    private Cursor crosshairCursor;

    private GameCamera oldCamera;

    /**
     * Creates a new game from the configuration settings.
     */
    public GDXRoot() {
    }

    /**
     * Called when the Application is first created.
     * <p>
     * This is method immediately loads assets for the loading screen, and prepares
     * the asynchronous loader for all other assets.
     */
    public void create() {

        Pixmap mouse = new Pixmap(Gdx.files.internal("textures/UI/pinkMouse.png"));
        Pixmap crosshair = new Pixmap(Gdx.files.internal("textures/UI/crosshair2.png"));
        mouseCursor = Gdx.graphics.newCursor(mouse, 9, 2);
        crosshairCursor = Gdx.graphics.newCursor(crosshair, 15, 15);
        mouse.dispose();
        crosshair.dispose();
        Gdx.graphics.setCursor(mouseCursor);

        canvas = new GameCanvas();
        loading = new LoadingMode("jsons/assets.json", canvas, 1);

        levels = new LevelSelectMode();
        settingsMode = new SettingsMode();
        settingsMode.setViewport(canvas.getUIViewport());
        gameOver = new GameOverScreen();
        credits = new CreditsMode();


        // Initialize the three game worlds
        controller = new GameController();
        controller.setPauseViewport(canvas.getUIViewport());
        loading.setScreenListener(this);
        setScreen(loading);
    }

    /**
     * Called when the Application is destroyed.
     * <p>
     * This is preceded by a call to pause().
     */
    public void dispose() {
        // Call dispose on our children
        setScreen(null);
        controller.dispose();

        canvas.dispose();
        canvas = null;

        // Unload all of the resources
        if (directory != null) {
            directory.unloadAssets();
            directory.dispose();
            directory = null;
        }
        super.dispose();
    }

    /**
     * Called when the Application is resized.
     * <p>
     * This can happen at any point during a non-paused state but will never happen
     * before a call to create().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        Gdx.gl.glViewport(0, 0, width,
                height);
        canvas.resize();
        super.resize(width, height);
    }

    /**
     * The given screen has made a request to exit its player mode.
     * <p>
     * The value exitCode can be used to implement menu options.
     *
     * @param screen   The screen requesting to exit
     * @param exitCode The state of the screen upon exit
     */
    public void exitScreen(Screen screen, int exitCode) {

        if ((exitCode == Screens.RESUME_CONTROLLER || exitCode == Screens.CONTROLLER ) && controller.getLevelNum() > disableGumMaxLevel){
            Gdx.graphics.setCursor(crosshairCursor);
        } else{
            Gdx.graphics.setCursor(mouseCursor);
        }

        if (exitCode == Screens.EXIT_CODE) {
            Gdx.app.exit();
            System.exit(0);
        } else if (exitCode == Screens.LOADING_SCREEN) {
            canvas.resetCamera();
            loading.setScreenListener(this);
            setScreen(loading);
        } else if (exitCode == Screens.SETTINGS) {
            settingsMode.setAccessedFromMain(screen == loading);
            oldCamera = canvas.getCamera();
            settingsMode.setScreenListener(this);
            BitmapFont codygoonRegular = loading.getAssets().getEntry("codygoonRegular", BitmapFont.class);
            BitmapFont projectSpace = loading.getAssets().getEntry("projectSpace", BitmapFont.class);
            settingsMode.initialize(codygoonRegular, projectSpace);
            setScreen(settingsMode);
            canvas.resetCamera();
        } else if (exitCode == Screens.LEVEL_SELECT) {
            directory = loading.getAssets();
            controller.gatherAssets(directory);
            levels.gatherAssets(directory);
            levels.setCanvas(canvas);
            levels.setScreenListener(this);
            setScreen(levels);
        } else if (exitCode == Screens.CONTROLLER) {
            if (screen == settingsMode) {
                canvas.setCamera(oldCamera);
                controller.setPaused(true);
                controller.update(0);
            } else {
                directory = loading.getAssets();
                controller.gatherAssets(directory);
                controller.setScreenListener(this);
                controller.setCanvas(canvas);
                if (screen == levels) controller.setLevelNum(levels.getSelectedLevel());
                if (screen == gameOver && gameOver.gameWon()) controller.previousLevel();
                controller.reset();
            }
            setScreen(controller);
        } else if (exitCode == Screens.GAME_WON) {
            directory = loading.getAssets();
            gameOver.initialize(directory, canvas);
            gameOver.gameWon(directory);
            gameOver.setScreenListener(this);
            gameOver.setCaptive(controller.getCaughtCaptives(), controller.getTotalCaptives());
            canvas.resetCamera();
            setScreen(gameOver);
        } else if (exitCode == Screens.GAME_LOST) {
            directory = loading.getAssets();
            gameOver.initialize(directory, canvas);
            gameOver.gameLost(directory);
            gameOver.setScreenListener(this);
            canvas.resetCamera();
            setScreen(gameOver);
        } else if (exitCode == Screens.RESUME_CONTROLLER) {
            controller.setPaused(false);
            controller.setScreenListener(this);
            setScreen(controller);
        } else if (exitCode == Screens.CREDITS){
            directory = loading.getAssets();
            BitmapFont codygoonRegular = loading.getAssets().getEntry("codygoonRegular", BitmapFont.class);
            BitmapFont projectSpace = loading.getAssets().getEntry("projectSpace", BitmapFont.class);
            controller.gatherAssets(directory);
            credits.gatherAssets(directory, projectSpace, codygoonRegular);
            credits.setCanvas(canvas);
            credits.setScreenListener(this);
            setScreen(credits);
        }
        else {
            throw new UnsupportedOperationException("Invalid screen");
        }
    }
}