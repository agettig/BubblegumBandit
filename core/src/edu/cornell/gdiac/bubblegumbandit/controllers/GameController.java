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

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.bubblegumbandit.models.level.PlatformModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.bubblegumbandit.models.projectiles.GumModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.helpers.GumJointPair;
import edu.cornell.gdiac.bubblegumbandit.models.level.LevelModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.MovingEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.util.*;

import edu.cornell.gdiac.physics.obstacle.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import static edu.cornell.gdiac.util.SliderGui.createAndShowGUI;

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


    /**
     * Reference to the Collision controller instance
     */
    private CollisionController collisionController;

    /**
     * Queue of gum joints
     */
    protected Queue<JointDef> jointsQueue;

    /** Gum gravity scale when creating gum */
    private float gumGravity;

    /** Gum speed when creating gum */
    private float gumSpeed;

    /** The texture of the trajectory projectile */
    private TextureRegion trajectoryProjectile;

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
    public boolean isFailure() {
        return failed;
    }

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

        //Technicals
        complete = false;
        failed = false;
        active = false;
        countdown = -1;
        setComplete(false);
        setFailure(false);

        //Data Structures && Classes
        level = new LevelModel();
        jointsQueue = new Queue<JointDef>();
        bubblegumController = new BubblegumController();
        collisionController = new CollisionController(level);

        //GUI
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(new SliderListener());
            }
        });
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
        TextureRegion gumTexture = new TextureRegion(directory.getEntry("gum", Texture.class));
        TextureRegion stuckGumTexture = new TextureRegion(directory.getEntry("chewedGum", Texture.class));

        // This represents the level but does not BUILD it
        levelFormat = directory.getEntry("level1", JsonValue.class);
        trajectoryProjectile = new TextureRegion(directory.getEntry("trajectoryProjectile", Texture.class));
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the level and creates a new one. It will
     * reread from the JSON file, allowing us to make changes on the fly.
     */
    public void reset() {

        bubblegumController.resetAllBubblegum();
        level.dispose();

        setComplete(false);
        setFailure(false);
        countdown = -1;

        // Reload the json each time
        level.populate(directory, levelFormat);
        level.getWorld().setContactListener(collisionController);
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

        InputController input = InputController.getInstance();
        input.readInput();
        if (listener == null) {return true;}

        // Toggle debug and handle resets.
        if (input.didDebug()) {level.setDebug(!level.getDebug());}
        if (input.didReset()) {reset();}

        // Switch screens if necessary.
        if (input.didExit()) {
            listener.exitScreen(this, EXIT_QUIT);
            return false;
        }
        else if (countdown > 0) {countdown--;}
        else if (countdown == 0) {reset(); }

        //Check for failure.
        if (!isFailure() && level.getAvatar().getY() < -1) {
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

        if(collisionController.isWinConditionMet()) setComplete(true);

        InputController inputResults = InputController.getInstance();

        //Update Controllers.
        BanditModel bandit = level.getAvatar();
        for(AIController ai : level.getEnemies()) ai.update();

        //move bandit
        float movement = inputResults.getHorizontal() * bandit.getForce();
        bandit.setMovement(movement);
        bandit.applyForce();

        //Perform actions when the player flips gravity.
        if (inputResults.getSwitchGravity() && bandit.isGrounded()) {
            Vector2 currentGravity = level.getWorld().getGravity();
            currentGravity.y = -currentGravity.y;
            jumpId = playSound(jumpSound, jumpId);
            level.getWorld().setGravity(currentGravity);
            bandit.flippedGravity();
            bandit.setGrounded(false);
            collisionController.clearSensorFixtures();

            for (AIController ai : level.getEnemies()) ai.flipEnemy();
        }


        if (inputResults.didShoot()) {

            Vector2 cross = level.getProjTarget(canvas);

            createGumProjectile(cross);
        }

        level.update(dt);
        level.getWorld().step(WORLD_STEP, WORLD_VELOC, WORLD_POSIT);

        addJointsToWorld();
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

        level.draw(canvas, levelFormat, gumSpeed, gumGravity, trajectoryProjectile);

        // Final message
        if (complete && !failed) {
            displayFont.setColor(Color.YELLOW);
            canvas.begin(); // DO NOT SCALE
            canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
            canvas.end();
        } else if (failed) {
            displayFont.setColor(Color.RED);
            canvas.begin(); // DO NOT SCALE
            canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
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

    /**
     * Add a new gum projectile to the world and send it in the right direction.
     */
    private void createGumProjectile(Vector2 target) {

        if(bubblegumController.gumLimitReached()) return;

        JsonValue gumJV = levelFormat.get("gumProjectile");
        BanditModel avatar = level.getAvatar();

        Vector2 origin = level.getProjOrigin(gumJV, canvas);
        Vector2 gumVel = new Vector2(target.x - origin.x, target.y - origin.y);
        gumVel.nor();

        // Prevent player from shooting themselves by clicking on player
        // TODO: Should be tied in with raycast in LevelModel, check if raycast hits player
        if (origin.x > avatar.getX() && gumVel.x < 0) { //  && gumVel.angleDeg() > 110 && gumVel.angleDeg() < 250)) {
            return;
        } else if (origin.x < avatar.getX() && gumVel.x > 0) { //&& (gumVel.angleDeg() < 70 || gumVel.angleDeg() > 290)) {
            return;
        }

        String key = gumJV.get("texture").asString();
        TextureRegion gumTexture = new TextureRegion(directory.getEntry(key, Texture.class));
        float radius = gumTexture.getRegionWidth() / (2.0f * level.getScale().x);

        //Create a new GumModel and assign it to the BubblegumController.
        GumModel gum = new GumModel(origin.x, origin.y, radius);
        gum.setName(gumJV.name());
        gum.setDensity(gumJV.getFloat("density", 0));
        gum.setDrawScale(level.getScale());
        gum.setTexture(gumTexture);
        gum.setBullet(true);
        gum.setGravityScale(gumGravity);
        bubblegumController.addNewBubblegum(gum);

        // Compute position and velocity
        if (gumSpeed == 0) { // Use default gum speed
            gumVel.scl(gumJV.getFloat("speed", 0));
        } else { // Use slider gum speed
            gumVel.scl(gumSpeed);
        }
        gum.setVX(gumVel.x);
        gum.setVY(gumVel.y);
        level.activate(gum);
    }

    /**
     * Adds every joint in the joint queue to the world before clearing the queue.
     */
    private void addJointsToWorld() {
        for(int i = 0; i < bubblegumController.numActivePairsToAssemble(); i++){
            GumJointPair pairToAssemble = bubblegumController.dequeueAssembly();
            WeldJointDef weldJointDef = pairToAssemble.getJointDef();
            WeldJoint createdWeldJoint = (WeldJoint) level.getWorld().createJoint(weldJointDef);
            GumJointPair activePair = new GumJointPair(pairToAssemble.getGum(), createdWeldJoint);
            bubblegumController.addToStuckBubblegum(activePair);
        }
    }

    public void setGravity(float gravity) {
        float g = gravity;
        if (level.getWorld().getGravity().y < 0) {
            g = -g;
        }
        level.getWorld().setGravity(new Vector2(0, g));
    }

    class SliderListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                int val = source.getValue();
                if (source.getName().equals("gravity")) {
                    setGravity(val);
                } else if (source.getName().equals("radius")) {
                    for (AIController ai : level.getEnemies()) {
                        ai.setVisionRadius(val);
                    }
                } else if (source.getName().equals("range")) {
                    for (AIController ai : level.getEnemies()) {
                        ai.setVisionRange((float) (val * (Math.PI / 180f)));
                    }
                } else if (source.getName().equals("gum gravity scale")) {
                    gumGravity = val;
                } else if (source.getName().equals("gum speed")) {
                    gumSpeed = val;
                } else if (source.getName().equals("move speed")) {
                    for (AIController ai : level.getEnemies()) {
                        ai.setMovementSpeed((float) val /100);
                    }
                }
            }

        }

    }
}