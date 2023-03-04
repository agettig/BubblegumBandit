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
package edu.cornell.gdiac.json;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.json.enemies.Enemy;
import edu.cornell.gdiac.util.*;

import edu.cornell.gdiac.physics.obstacle.*;
import edu.cornell.gdiac.json.gum.Bubblegum;

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
public class GameController implements Screen, ContactListener {
    // ASSETS
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
     * Number of velocity iterations for the constrain solvers
     */
    public static final int WORLD_VELOC = 6;
    /**
     * Number of position iterations for the constrain solvers
     */
    public static final int WORLD_POSIT = 2;

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

    private TextureRegion gumTexture;

    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;


    protected Queue<Bubblegum> gumQueue = new Queue<Bubblegum>();

    /**
     * Queue of Obstacles involved in a gum collision to make static
     */
    protected Queue<Obstacle> stickyQueue = new Queue<Obstacle>();

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
        level = new LevelModel();
        complete = false;
        failed = false;
        active = false;
        countdown = -1;

        setComplete(false);
        setFailure(false);
        sensorFixtures = new ObjectSet<Fixture>();
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
        gumTexture = new TextureRegion(directory.getEntry("gum", Texture.class));

        // This represents the level but does not BUILD it
        levelFormat = directory.getEntry("level1", JsonValue.class);
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the level and creates a new one. It will
     * reread from the JSON file, allowing us to make changes on the fly.
     */
    public void reset() {
        level.dispose();

        setComplete(false);
        setFailure(false);
        countdown = -1;

        // Reload the json each time
        level.populate(directory, levelFormat);
        level.getWorld().setContactListener(this);
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
        input.readInput(level.getBounds(), level.getScale());
        if (listener == null) {
            return true;
        }

        // Toggle debug
        if (input.didDebug()) {
            level.setDebug(!level.getDebug());
        }

        // Handle resets
        if (input.didReset()) {
            reset();
        }

        // Now it is time to maybe switch screens.
        if (input.didExit()) {
            listener.exitScreen(this, EXIT_QUIT);
            return false;
        } else if (countdown > 0) {
            countdown--;
        } else if (countdown == 0) {
            reset();
        }

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
        // Process actions in object model
        DudeModel avatar = level.getAvatar();
        avatar.setMovement(
                InputController.getInstance().getHorizontal() * avatar.getForce());
        avatar.setJumping(InputController.getInstance().didPrimary());
        avatar.applyForce();

        // remove jump
//		if (avatar.isJumping()) {
//			jumpId = playSound( jumpSound, jumpId );
//		}

        if (InputController.getInstance().getSwitchGravity() && avatar.isGrounded()) {
            Vector2 currentGravity = level.getWorld().getGravity();
            currentGravity.y = -currentGravity.y;
            jumpId = playSound(jumpSound, jumpId);
            level.getWorld().setGravity(currentGravity);
            avatar.flippedGravity();

            for (Enemy e : level.getEnemies()) e.flippedGravity();
        }

        for (Enemy e : level.getEnemies()) e.update();

        if (InputController.getInstance().didShoot()) {
            // TODO: Visible crosshair?
            createGumProjectile(InputController.getInstance().getCrossHair());
        }

        level.update(dt);

        // Make everything in the sticky queue static.
        immobilizeStickyQueue();

        // Turn the physics engine crank.
        level.getWorld().

        step(WORLD_STEP, WORLD_VELOC, WORLD_POSIT);
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

        level.draw(canvas);

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
     * Callback method for the start of a collision
     * <p>
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     * <p>
     * This is where we check for gum collisions
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle) body1.getUserData();
            Obstacle bd2 = (Obstacle) body2.getUserData();

            DudeModel avatar = level.getAvatar();
            BoxObstacle door = level.getExit();

            // See if we have landed on the ground.
            if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                    (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
                avatar.setGrounded(true);
                sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }

            // Check for win condition
            if ((bd1 == avatar && bd2 == door) ||
                    (bd1 == door && bd2 == avatar)) {
                setComplete(true);
            }

            // Check for gum collision
            handleGumCollision(bd1, bd2);

            // TODO: Gum interactions

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        DudeModel avatar = level.getAvatar();
        if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                avatar.setGrounded(false);
            }
        }
    }

    /**
     * Unused ContactListener method
     */
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    /**
     * Unused ContactListener method
     */
    public void preSolve(Contact contact, Manifold oldManifold) {
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
        JsonValue gumJV = levelFormat.get("gumProjectile");
        DudeModel avatar = level.getAvatar();
        float offset = gumJV.getFloat("offset", 0);
        offset *= (target.x > avatar.getX() ? 1 : -1);
        float startX = avatar.getX() + offset;
        float startY = avatar.getY();

        String key = gumJV.get("texture").asString();
        TextureRegion gumTexture = new TextureRegion(directory.getEntry(key, Texture.class));
        float radius = gumTexture.getRegionWidth() / (2.0f * level.getScale().x);

        WheelObstacle gum = new WheelObstacle(startX, startY, radius);

        // Physics properties
        gum.setName(gumJV.name());
        gum.setDensity(gumJV.getFloat("density", 0));
        gum.setDrawScale(level.getScale());
        gum.setTexture(gumTexture);
        gum.setBullet(true);
        gum.setGravityScale(gumJV.getFloat("gravityScale", 0));
        // TODO: For different trajectories: change gravity scale, add various forces

        // Compute position and velocity
        float speed = gumJV.getFloat("speed", 0);
        Vector2 gumVel = new Vector2(target.x - startX, target.y - startY);
        gumVel.nor();
        gumVel.scl(speed);
        gum.setVX(gumVel.x);
        gum.setVY(gumVel.y);
        level.activate(gum);
    }

    /**
     * Remove a gum from the world.
     *
     * @param gum the gum to remove
     */
    public void removeGum(Obstacle gum) {
        gum.markRemoved(true);
    }

    /**
     * Adds an Obstacle to the end of the Sticky Queue.
     *
     * @param o the Obstacle to add.
     */
    private void enqueueSticky(Obstacle o) {
        if (o == null) return;
        stickyQueue.addLast(o);
    }

    /**
     * Returns true if an Obstacle is a gum projectile.
     * <p>
     * An Obstacle is a gum projectile if its name equals
     * "gumProjectile".
     *
     * @param o the Obstacle to check
     * @returns true if the Obstacle is a gum projectile
     */
    private boolean isGumProjectile(Obstacle o) {
        return o.getName().equals("gumProjectile");
    }

    /**
     * Handles a gum projectile's collision in the Box2D world.
     * <p>
     * Examines two Obstacles in a collision. If either is a
     * gum projectile, adds it to the Sticky Queue.
     *
     * @param bd1 The first Obstacle in the collision.
     * @param bd2 The second Obstacle in the collision.
     */
    private void handleGumCollision(Obstacle bd1, Obstacle bd2) {

        //Safety check.
        if (bd1 == null || bd2 == null) return;

        if (isGumProjectile(bd1)) {
            bd1.setName("stickyGum");
            enqueueSticky(bd1);
            enqueueSticky(bd2);
        }
        if (isGumProjectile(bd2)) {
            bd2.setName("stickyGum");
            enqueueSticky(bd1);
            enqueueSticky(bd2);
        }
        if (bd1.getName().equals("stickyGum")) enqueueSticky(bd2);
        if (bd2.getName().equals("stickyGum")) enqueueSticky(bd1);
    }

    /**
     * Makes every Obstacle's body in the Sticky Queue a StaticBody
     * before clearing the queue.
     */
    private void immobilizeStickyQueue() {
        if (stickyQueue == null) return;
        if (stickyQueue.isEmpty()) return;
        for (Obstacle ob : stickyQueue) {
            if (ob != null) ob.setBodyType(BodyDef.BodyType.StaticBody);
        }
        stickyQueue.clear();
    }

}