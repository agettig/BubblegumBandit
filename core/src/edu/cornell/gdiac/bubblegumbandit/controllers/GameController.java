/*
 * GameController.java
 *
 * This combines the WorldController with the mini-game specific PlatformController
 * in the last lab.  With that said, some of the work is now offloaded to the new
 * LevelModel class, which allows us to serialize and deserialize a level.
 *
 * This is a refactored version of WorldController from Lab 4.  It separate the
 * level out into a new class called LevelModel.  This model is, in turn, read
 * from a JSON file.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * JSON version, 3/2/2016
 */
package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.LevelModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.ProjectileModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.gum.GumModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCamera;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.bubblegumbandit.view.HUDController;
import edu.cornell.gdiac.util.ScreenListener;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.*;

import javax.swing.*;

/**
 * Gameplay controller for the game.
 * <p>
 * This class does not have the Box2d world.  That is stored inside of the
 * LevelModel object, as the world settings are determined by the JSON
 * file.  However, the class does have all of the controller functionality,
 * including collision listeners for the active level.
 * <p>
 * You will notice that asset loading is very different.  It relies on the
 * singleton asset manager to manage the various assets.
 */
public class GameController implements Screen {
    // ASSETS

    // TODO remove
    private boolean enableGUI = false;

    /** How close to the center of the tile we need to be to stop drifting */
    private static final float DRIFT_TOLER = .2f;
    /** How fast we drift to the tile center when paused */
    private static final float DRIFT_SPEED = 0.325f;

    /**
     * Need an ongoing reference to the asset directory
     */
    protected AssetDirectory directory;
    /**
     * The font for giving messages to the player
     */
    protected BitmapFont displayFont;
    /**
     * The JSON defining the level model
     */
    private JsonValue levelFormat;

    /**
     * The JSON defining game constants
     */
    private JsonValue constantsJson;

    /** The JSON defining the tileset */
    private JsonValue tilesetJson;

    private HUDController hud;
    /**
     * The jump sound.  We only want to play once.
     */
    private SoundEffect jumpSound;

    /**Id for jump. */
    private long jumpId = -1;

    /**
     * Exit code for quitting the game
     */
    public static final int EXIT_QUIT = 0;
    /**
     * How many frames after winning/losing do we continue?
     */
    public static final int EXIT_COUNT = 120;

    // THESE ARE CONSTANTS BECAUSE WE NEED THEM BEFORE THE LEVEL IS LOADED
    /**
     * The amount of time for a physics engine step.
     */
    public static final float WORLD_STEP = 1 / 60.0f;
    /**
     * Number of velocity iterations for the constraint solvers
     */
    public static final int WORLD_VELOC = 8;
    /**
     * Number of position iterations for the constraint solvers
     */
    public static final int WORLD_POSIT = 3;

    /**
     * Reference to the game canvas
     */
    protected GameCanvas canvas;
    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;

    private CollisionController collisionController;

    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;

    /**
     * Reference to the game level
     */
    protected LevelModel level;

    /**
     * Whether or not this is an active controller
     */
    private boolean active;
    /**
     * Whether we have completed this level
     */
    private boolean complete;
    /**
     * Whether we have failed at this world (and need a reset)
     */
    private boolean failed;
    /**
     * Countdown active for winning or losing
     */
    private int countdown;

    /**
     * Reference to the GumModel controller instance
     */
    private BubblegumController bubblegumController;


    /** A collection of the active projectiles on screen */
    private ProjectileController projectileController;

    /**
     * The texture of the trajectory projectile
     */
    private TextureRegion trajectoryProjectile;

    private TextureRegion stuckGum;

    /** The gravity control mode for the player controller */
    private boolean gravityToggle = false;

    /** The number of the current level. */
    private int levelNum;

    /** The number of levels in the game. */
    private final int NUM_LEVELS = 2;

    /** Whether the orb has been collected. */
    private boolean orbCollected;

    /** Countdown timer after collecting the orb. */
    private float orbCountdown;

    /**
     * Returns true if the level is completed.
     * <p>
     * If true, the level will advance after a countdown
     *
     * @return true if the level is completed.
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Sets whether the level is completed.
     * <p>
     * If true, the level will advance after a countdown
     *
     * @param value whether the level is completed.
     */
    public void setComplete(boolean value) {
        if (value) {
            countdown = EXIT_COUNT;
        }
        complete = value;
    }

    /**
     * Returns true if the level is failed.
     * <p>
     * If true, the level will reset after a countdown
     *
     * @return true if the level is failed.
     */
    public boolean getFailure() {return failed;}

    /**
     * Sets whether the level is failed.
     * <p>
     * If true, the level will reset after a countdown
     *
     * @param value whether the level is failed.
     */
    public void setFailure(boolean value) {
        if (value) {
            countdown = EXIT_COUNT;
        }
        failed = value;
    }


    /**
     * Returns true if this is the active screen
     *
     * @return true if this is the active screen
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns the canvas associated with this controller
     * <p>
     * The canvas is shared across all controllers
     *
     * @return the GameCanvas associated with this controller
     */
    public GameCanvas getCanvas() {
        return canvas;
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
    }

    /**
     * Creates a new game world
     * <p>
     * The physics bounds and drawing scale are now stored in the LevelModel and
     * defined by the appropriate JSON file.
     */
    public GameController() {

        Pixmap pixmap = new Pixmap(Gdx.files.internal("textures/crosshair2.png"));
// Set hotspot to the middle of it (0,0 would be the top-left corner)
        int xHotspot = 16, yHotspot = 16;
        Cursor cursor = Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot);
        pixmap.dispose(); // We don't need the pixmap anymore
        Gdx.graphics.setCursor(cursor);


        //Technicals
        complete = false;
        failed = false;
        active = false;
        countdown = -1;
        orbCountdown = -1;
        levelNum = 1;
        setComplete(false);
        setFailure(false);

        //Data Structures && Classes
        level = new LevelModel();
        sensorFixtures = new ObjectSet<Fixture>();

        setComplete(false);
        setFailure(false);
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        bubblegumController = new BubblegumController();
        collisionController = new CollisionController(level, bubblegumController);
        projectileController = new ProjectileController();

        if (enableGUI){
            //TODO remove gui
//            javax.swing.SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    createAndShowGUI(new SliderListener());
//                }
//            });
        }
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.

    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        level.dispose();
        level = null;
        canvas = null;
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
        this.directory = directory;
        // Some assets may have not finished loading so this is a catch-all for those.
        directory.finishLoading();
        displayFont = directory.getEntry("display", BitmapFont.class);
        jumpSound = directory.getEntry("jump", SoundEffect.class);

        // This represents the level but does not BUILD it
        levelFormat = directory.getEntry("level" + levelNum, JsonValue.class);
        constantsJson = directory.getEntry("constants", JsonValue.class);
        tilesetJson = directory.getEntry("tileset", JsonValue.class);

        bubblegumController.initialize(directory, constantsJson.get("gumProjectile"));

        trajectoryProjectile = new TextureRegion(directory.getEntry("trajectoryProjectile", Texture.class));
        stuckGum = new TextureRegion(directory.getEntry("gum", Texture.class));
        hud = new HUDController(directory);
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the level and creates a new one. It will
     * reread from the JSON file, allowing us to make changes on the fly.
     */
    public void reset() {

        bubblegumController.resetAllBubblegum();
        projectileController.reset();
        collisionController.reset();

        level.dispose();

        setComplete(false);
        setFailure(false);
        countdown = -1;
        orbCountdown = -1;
        orbCollected = false;
        bubblegumController.resetAmmo();
        collisionController.resetRobotJoints();
        levelFormat = directory.getEntry("level" + levelNum, JsonValue.class);
        canvas.getCamera().setFixedX(false);
        canvas.getCamera().setFixedY(false);
        canvas.getCamera().setZoom(1);

        // Reload the json each time
        level.populate(directory, levelFormat, constantsJson, tilesetJson);
        level.getWorld().setContactListener(collisionController);
        projectileController.initialize(constantsJson.get("projectile"), directory, level.getScale().x, level.getScale().y);
        collisionController.initialize(canvas.getCamera());
        canvas.getCamera().setLevelSize(level.getBounds().width * level.getScale().x, level.getBounds().height * level.getScale().y);
    }

    /**
     * Returns whether to process the update loop
     * <p>
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt Number of seconds since last animation frame
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {

        PlayerController input = PlayerController.getInstance();
        input.readInput();
        if (listener == null) {return true;}

        // Toggle debug and handle resets.
        if (input.didDebug()) {level.setDebug(!level.getDebug());}
        if (input.didReset()) {reset();}
        if (input.didCameraSwap()) {
            canvas.getCamera().toggleDebug();
        }
        if (input.didControlsSwap()) { gravityToggle = !gravityToggle; }
        if (input.didAdvance()) {
            levelNum++;
            if (levelNum > NUM_LEVELS) {
                levelNum = 1;
            }
            reset();
        }
        if (input.didRetreat()) {
            levelNum--;
            if (levelNum < 1) {
                levelNum = NUM_LEVELS;
            }
            reset();
        }

        // Switch screens if necessary.
        if (input.didExit()) {
            listener.exitScreen(this, EXIT_QUIT);
            return false;
        }
        else if (countdown > 0) {countdown--;}
        else if (countdown == 0) {
            reset();
        }

        if (orbCountdown > 0 && !complete) { orbCountdown -= dt; }

        else if (orbCollected && orbCountdown <= 0) {
            level.getBandit().hitPlayer(level.getBandit().getHealth());
        }

        //Check for failure.
        if (!getFailure() && level.getBandit().getHealth() <= 0) {
            setFailure(true);
            return false;
        }
        return true;
    }

    /**
     * The core gameplay loop of this world.
     * <p>
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        if(collisionController.isWinConditionMet() && !isComplete()) {
            levelNum++;
            if (levelNum > NUM_LEVELS) {
                levelNum = 1;
            }
            setComplete(true);
        }

        if (!orbCollected && level.getBandit().isOrbCollected()) {
            orbCollected = true;
            orbCountdown = level.getOrbCountdown();
        }

        PlayerController inputResults = PlayerController.getInstance();

        //Update Controllers.
        BanditModel bandit = level.getBandit();

        //move bandit
        float movement = inputResults.getHorizontal() * bandit.getForce();
        bandit.setMovement(movement);
        bandit.applyForce();


        float grav =  level.getWorld().getGravity().y;
        if (bandit.isGrounded() && ((gravityToggle && PlayerController.getInstance().getGravityUp()) ||
                (!gravityToggle && PlayerController.getInstance().getGravityUp() && grav < 0) ||
                (!gravityToggle && PlayerController.getInstance().getGravityDown() && grav > 0))
        ) {
            Vector2 currentGravity = level.getWorld().getGravity();
            currentGravity.y = -currentGravity.y;
            jumpId = playSound(jumpSound, jumpId);
            level.getWorld().setGravity(currentGravity);
            bandit.flippedGravity();
            bandit.setGrounded(false);
            collisionController.clearSensorFixtures();

            if (level.getEnemyControllers() != null) {
                for (AIController ai : level.getEnemyControllers()) ai.flipEnemy();
            }
        }


        if (inputResults.didShoot() && bubblegumController.getAmmo() > 0) {
            Vector2 cross = level.getProjTarget(canvas);
            JsonValue gumJV = constantsJson.get("gumProjectile");
            BanditModel avatar = level.getBandit();
            Vector2 origin = level.getProjOrigin(gumJV, canvas);
            String key = gumJV.get("texture").asString();
            Vector2 scale = level.getScale();
            TextureRegion gumTexture = new TextureRegion(directory.getEntry(key, Texture.class));
            GumModel gum = bubblegumController.createGumProjectile(cross, gumJV, avatar, origin, scale, gumTexture);
            if (gum != null) {
                bubblegumController.fireGum();
                level.activate(gum);
                gum.setFilter(CATEGORY_GUM, MASK_GUM);
            }
        }
        if (inputResults.didUnstick()) {
            Vector2 cross = level.getProjTarget(canvas);
            JsonValue gumJV = constantsJson.get("unstickProjectile");
            BanditModel avatar = level.getBandit();
            Vector2 origin = level.getProjOrigin(gumJV, canvas);
            String key = gumJV.get("texture").asString();
            Vector2 scale = level.getScale();
            TextureRegion gumTexture = new TextureRegion(directory.getEntry(key, Texture.class));
            GumModel gum = bubblegumController.createGumProjectile(cross, gumJV, avatar, origin, scale, gumTexture);
            if (gum != null) {
                bubblegumController.fireGum();
                level.activate(gum);
                gum.setFilter(CollisionController.CATEGORY_UNSTICK, CollisionController.MASK_UNSTICK);
            }
        }

       for (AIController controller: level.getEnemyControllers()){

           //TODO fix adjust for drift

//            adjustForDrift(controller.getEnemy());

            //get action from controller
            int action = controller.getAction();

            if ((action & AIController.CONTROL_FIRE) == AIController.CONTROL_FIRE) {
                ProjectileModel newProj = projectileController.fireWeapon(controller, level.getBandit().getX(), level.getBandit().getY());
                level.activate(newProj);
                newProj.setFilter(CATEGORY_PROJECTILE, MASK_PROJECTILE);
            } else {
                controller.coolDown(true);
            }

            //pass to enemy, update the enemy with that action
           // TODO this probably means enemies are updated twice per frame, once with this update method
           // TODO and once with their parent. Switching to sense-think-act should fix this
           controller.getEnemy().update(action);
       }

        level.update(dt);
        projectileController.update();

        // Update the camera
        GameCamera cam = canvas.getCamera();
        Vector2 target = canvas.unproject(PlayerController.getInstance().getCrossHair());
        if (!cam.isFixedX()) {
            cam.setTargetX(bandit.getCameraTarget().x);
            cam.setSecondaryTargetX(target.x);
        } if (!cam.isFixedY()) {
            cam.setTargetY(bandit.getCameraTarget().y);
            cam.setSecondaryTargetY(target.y);
        }
        canvas.getCamera().update(dt);

        // Turn the physics engine crank.
        level.getWorld().step(WORLD_STEP, WORLD_VELOC, WORLD_POSIT);

        bubblegumController.updateJoints(level);
        collisionController.addRobotJoints(level);
    }


    /**
     * Draw the physics objects to the canvas
     * <p>
     * For simple worlds, this method is enough by itself.  It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     * <p>
     * The method draws all objects in the order that they were added.
     *
     * @param delta The drawing context
     */
    public void draw(float delta) {
        canvas.clear();

        level.draw(canvas, constantsJson, trajectoryProjectile);
        hud.draw(level, bubblegumController, (int) orbCountdown);

        // Final message
        if (complete && !failed) {
            displayFont.setColor(Color.YELLOW);
            canvas.begin(); // DO NOT SCALE
            //TODO fix drawing text to center
            canvas.drawTextCentered("VICTORY!", displayFont, 150);
            canvas.end();
        } else if (failed) {
            displayFont.setColor(Color.RED);
            canvas.begin(); // DO NOT SCALE
            canvas.drawTextCentered("FAILURE!", displayFont, 150);
            canvas.end();
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
        // IGNORE FOR NOW
    }

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
            if (preUpdate(delta)) {
                update(delta);
            }
            draw(delta);
        }
    }

    /**
     * Called when the Screen is paused.
     * <p>
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // We need this method to stop all sounds when we pause.
        if (jumpSound.isPlaying(jumpId)) {
            jumpSound.stop(jumpId);
        }
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
     * <p>
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

 /**
     * Method to ensure that a sound asset is only played once.
     * <p>
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound   The sound asset to play
     * @param soundId The previously playing sound instance
     * @return the new sound instance for this asset.
     */
    public long playSound(SoundEffect sound, long soundId) {
        return playSound(sound, soundId, 1.0f);
    }

    /**
     * Method to ensure that a sound asset is only played once.
     * <p>
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound   The sound asset to play
     * @param soundId The previously playing sound instance
     * @param volume  The sound volume
     * @return the new sound instance for this asset.
     */
    public long playSound(SoundEffect sound, long soundId, float volume) {
        if (soundId != -1 && sound.isPlaying(soundId)) {
            sound.stop(soundId);
        }
        return sound.play(volume);
    }

    public void setGravity(float gravity) {
        float g = gravity;
        if (level.getWorld().getGravity().y < 0) {
            g = -g;
        }
        level.getWorld().setGravity(new Vector2(0, g));
    }

    /**
     * Nudges the ship back to the center of a tile if it is not moving.
     *
     * @param enemy The Enemy to adjust
     */
    private void adjustForDrift(EnemyModel enemy) {
        // Drift to line up vertically with the grid.

        if (enemy.getVX() == 0.0f) {
            float offset = level.getBoard().centerOffset(enemy.getX());
            if (offset < -DRIFT_TOLER) {
                enemy.setX(enemy.getX()+DRIFT_SPEED);
            } else if (offset > DRIFT_TOLER) {
                enemy.setX(enemy.getX()-DRIFT_SPEED);
            }
        }

        // Drift to line up horizontally with the grid.
        if (enemy.getVY() == 0.0f) {
            float y = enemy.getY();
            if (enemy.getId()==0){y -= 1;}
            float offset = level.getBoard().centerOffset(y);
            if (offset < -DRIFT_TOLER) {
                enemy.setY(enemy.getY()+DRIFT_SPEED);
            } else if (offset > DRIFT_TOLER) {
                enemy.setY(enemy.getY()-DRIFT_SPEED);
            }
        }
    }
}