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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.AIController;
import edu.cornell.gdiac.bubblegumbandit.helpers.Gummable;
import edu.cornell.gdiac.bubblegumbandit.helpers.Shield;
import edu.cornell.gdiac.bubblegumbandit.helpers.SaveData;
import edu.cornell.gdiac.bubblegumbandit.helpers.Unstickable;
import edu.cornell.gdiac.bubblegumbandit.models.BackObjModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.LaserEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.ProjectileEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.RollingEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.LevelModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.ProjectileModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.gum.GumModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCamera;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.bubblegumbandit.view.HUDController;
import edu.cornell.gdiac.bubblegumbandit.view.Minimap;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.ArrayList;

import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.*;

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

    // TODO move this
    private final int RELOAD_RATE = 30;
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
     * The JSON defining game constants
     */
    private JsonValue constantsJson;

    /**
     * The JSON defining the tileset
     */
    private JsonValue tilesetJson;

    private HUDController hud;

    private Minimap minimap;

    /**
     * The jump sound.  We only want to play once.
     */
    private SoundEffect jumpSound;

    /**
     * Id for jump.
     */
    private long jumpId = -1;

    /**
     * The small enemy shooting sound.  We only want to play once.
     */
    private SoundEffect smallEnemyShootingSound;
    /**
     * Id for small enemy shooting
     */
    private long smallEnemyShootingId = -2;
    /**
     * The gum splat sound.  We only want to play once.
     */
    private SoundEffect gumSplatSound;
    /**
     * Id for gum splat sound
     */
    private long gumSplatId = -3;
    /**
     * The sound when enemy is hit with gume.  We only want to play once.
     */
    private SoundEffect enemySplatSound;
    /**
     * Id for enemy splat sound
     */
    private long enemySplatId = -4;
    /**
     * The sound when an item is collected.  We only want to play once.
     */
    private SoundEffect collectItemSound;
    /**
     * Id for collectible item sound
     */
    private long collectItemId = -4;

    /**
     * Array holding all sounds
     */
    private SoundEffect[] soundEffects = new SoundEffect[]{jumpSound, smallEnemyShootingSound, gumSplatSound, enemySplatSound, collectItemSound};

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
     * A collection of the active projectiles on screen
     */
    private ProjectileController projectileController;

    /**
     * Reference to LaserController instance
     */
    private LaserController laserController;


    /**
     * The texture of the trajectory projectile
     */
    private TextureRegion trajectoryProjectile;

    /**
     * The texture of the laserBeam
     */
    private TextureRegion laserBeam;

    private TextureRegion laserBeamEnd;


    private TextureRegion stuckGum;

    /**
     * The number of the current level.
     */
    private int levelNum;

    /**
     * The number of levels in the game.
     */
    private final int NUM_LEVELS = 9;

    /**
     * Whether the orb has been collected.
     */
    private boolean orbCollected;

    /**
     * true if Enemies that should spawn after the orb gets picked up
     * have been created
     */
    private boolean spawnedPostOrbEnemies;

    /**
     * Countdown timer after collecting the orb.
     */
    private float orbCountdown;

    /**
     * Tick counter for gum reloading
     */
    private long ticks;
    /**
     * Whether the player is reloading gum
     */
    private boolean reloadingGum;


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
    public boolean getFailure() {
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

        Pixmap pixmap = new Pixmap(Gdx.files.internal("textures/UI/crosshair2.png"));
// Set hotspot to the middle of it (0,0 would be the top-left corner)
        int xHotspot = 16, yHotspot = 16;
        Cursor cursor = Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot);
        pixmap.dispose(); // We don't need the pixmap anymore
        Gdx.graphics.setCursor(cursor);


        //Technicals
        ticks = 0;
        complete = false;
        failed = false;
        active = false;
        countdown = -1;
        orbCountdown = -1;
        levelNum = 1;
        reloadingGum = false;
        setComplete(false);
        setFailure(false);

        //Data Structures && Classes
        level = new LevelModel();

        setComplete(false);
        setFailure(false);
        bubblegumController = new BubblegumController();
        laserController = new LaserController();
        collisionController = new CollisionController(level, bubblegumController);
        projectileController = new ProjectileController();
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


        // This represents the level but does not BUILD it
        levelFormat = directory.getEntry("level" + levelNum, JsonValue.class);
        constantsJson = directory.getEntry("constants", JsonValue.class);
        tilesetJson = directory.getEntry("tileset", JsonValue.class);

        bubblegumController.initialize(directory, constantsJson.get("gumProjectile"));

        trajectoryProjectile = new TextureRegion(directory.getEntry("trajectoryProjectile", Texture.class));
        laserBeam = new TextureRegion(directory.getEntry("laserBeam", Texture.class));
        laserBeamEnd = new TextureRegion(directory.getEntry("laserBeamEnd", Texture.class));
        stuckGum = new TextureRegion(directory.getEntry("splatGum", Texture.class));
        hud = new HUDController(directory);
        minimap = new Minimap();
    }


    /**
     * sets the level loaded by the game controller, set by level select
     */
    public void setLevelNum(int num) {
        levelNum = num;
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
        hud.resetStars();


        level.dispose();

        setComplete(false);
        setFailure(false);
        countdown = -1;
        orbCountdown = -1;
        orbCollected = false;
        spawnedPostOrbEnemies = false;
        bubblegumController.resetAmmo();
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
        int x = levelFormat.get("width").asInt();
        int y = levelFormat.get("height").asInt();
        minimap.initialize(directory, levelFormat, x, y);

        SoundController.playMusic("game");
    }

    public void respawn() {
        setComplete(false);
        setFailure(false);
        countdown = -1;
        orbCountdown = -1;
        orbCollected = false;
        level.endAlarms();
        for (EnemyModel enemy : level.getPostOrbEnemies()) {
            enemy.markRemoved(true);
        }
        level.remakeOrb(directory, constantsJson);
        bubblegumController.resetAmmo();
        spawnedPostOrbEnemies = false;
        level.getBandit().respawnPlayer();
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
        if (listener == null) {
            return true;
        }

        // Toggle debug and handle resets.
        if (input.didDebug()) {
            level.setDebug(!level.getDebug());
        }
        if (input.didReset()) {
            reset();
        }
        if (input.didCameraSwap()) {
            canvas.getCamera().toggleDebug();
        }
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
        } else if (countdown > 0) {
            countdown--;
        } else if (countdown == 0) {

            // TODO fix with post orb enemies
            if (level.getPostOrbEnemies().size() == 0) {
                if (orbCollected && !complete) {
                    respawn();
                } else {
                    reset();
                }
            } else {
                reset();
            }
        }

        if (orbCountdown > 0 && !complete) {
            orbCountdown -= dt;
        } else if (orbCollected && orbCountdown <= 0) {
            level.getBandit().kill();
        }

        //Check for failure.
        if (!getFailure() && level.getBandit().getHealth() <= 0) {
            setFailure(true);
            level.getBandit().kill();
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
        ticks++;
        if (collisionController.isWinConditionMet() && !isComplete()) {
            levelNum++;
            SaveData.setStatus(levelNum - 1, level.getBandit().getNumStars());
            SaveData.unlock(levelNum);

            if (levelNum > NUM_LEVELS) {
                levelNum = 1;
            }
            setComplete(true);
        }

        if (!orbCollected && level.getBandit().isOrbCollected()) {
            orbCollected = true;
            orbCountdown = level.getOrbCountdown();
            level.startAlarms();
            SoundController.playMusic("escape");
        }

        PlayerController inputResults = PlayerController.getInstance();

        //Update Controllers.
        BanditModel bandit = level.getBandit();

        //move bandit
        if (bandit.getHealth() > 0) {
            float movement = inputResults.getHorizontal() * bandit.getForce();
            bandit.setMovement(movement);
            bandit.applyForce();
        } else {
            bandit.setVX(0f);
            if (bandit.isGrounded()) bandit.setVY(0);
        }


        float grav = level.getWorld().getGravity().y;
        boolean shouldFlip = (bandit.isGrounded() || !bandit.hasFlipped()) &&
                ((PlayerController.getInstance().getGravityUp() && grav < 0) ||
                        (PlayerController.getInstance().getGravityDown() && grav > 0));
        shouldFlip = shouldFlip || (collisionController.shouldFlipGravity());
        if (shouldFlip) {
            Vector2 currentGravity = level.getWorld().getGravity();
            currentGravity.y = -currentGravity.y;
            jumpId = SoundController.playSound("jump", 0.25f);
            level.getWorld().setGravity(currentGravity);
            bandit.flippedGravity();
            collisionController.clearSensorFixtures();

            if (level.aiControllers() != null) {
                for (AIController ai : level.aiControllers()) ai.flipEnemy();
            }
            if (level.getBackgroundObjects() != null) {
                for (BackObjModel o : level.getBackgroundObjects()) o.flip();
            }
            for (Obstacle flippable : level.getFlippables()) {
                flippable.flipGravity();
            }
        }

        if (inputResults.didReload() && !bubblegumController.atMaxGum()) {
            if (ticks % RELOAD_RATE == 0) {
                bubblegumController.addAmmo(1);
                reloadingGum = true;
            }
        } else {
            reloadingGum = false;
        }


        if (inputResults.didShoot() && bubblegumController.getAmmo() > 0 && bandit.getHealth() > 0) {
            Vector2 cross = level.getAim().getProjTarget(canvas);
            JsonValue gumJV = constantsJson.get("gumProjectile");
            BanditModel avatar = level.getBandit();
            Vector2 origin = level.getAim().getProjOrigin(gumJV, canvas);
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
        if (inputResults.didUnstick() && bandit.getHealth() > 0) {
            Unstickable unstickable = level.getAim().getSelected();
            if (unstickable != null) {
                Obstacle unstickableOb = (Obstacle) unstickable;
                if (unstickableOb.getName().equals("stickyGum")) {
                    // Unstick it
                    bubblegumController.removeGum((GumModel) unstickable);
                    SoundController.playSound("enemySplat", 1f); // Temp sound
                } else if (unstickableOb instanceof Gummable) {
                    Gummable gummable = (Gummable) unstickableOb;
                    if (gummable.getGummed()) {
                        // Ungum it
                        bubblegumController.removeGummable(gummable);
                        SoundController.playSound("enemySplat", 1f); // Temp sound

                    }
                }
            }
        }

        level.update(dt);
        for (AIController controller : level.aiControllers()) {

            EnemyModel enemy = controller.getEnemy();
            // TODO: Make separate state for dead enemies
            if (enemy.isRemoved()) {
                continue;
            }
            boolean isLaserEnemy = enemy instanceof LaserEnemyModel;
            boolean isProjectileEnemy = enemy instanceof ProjectileEnemyModel;

            if (isProjectileEnemy) {
                if (controller.getEnemy().fired()) {
                    ProjectileModel newProj = projectileController.fireWeapon(controller, level.getBandit().getX(), level.getBandit().getY());
                    smallEnemyShootingId = SoundController.playSound("smallEnemyShooting", 1);
                    level.activate(newProj);
                    newProj.setFilter(CATEGORY_PROJECTILE, MASK_PROJECTILE);
                } else {
                    controller.coolDown(true);
                }
            } else if (isLaserEnemy) {
                LaserEnemyModel laserEnemy = (LaserEnemyModel) controller.getEnemy();
                if (laserEnemy.coolingDown()) laserEnemy.decrementCooldown(dt);
                else {
                    boolean sameSide = false;
                    if (enemy.getFaceRight() && enemy.getX() < bandit.getX())
                        sameSide = true;
                    if (!enemy.getFaceRight() && enemy.getX() > bandit.getX())
                        sameSide = true;
                    boolean canFire = laserEnemy.canSeeBandit(bandit) && laserEnemy.inactiveLaser() && sameSide;
                    if (canFire) {
                        if (enemy instanceof Shield) {
                            ((Shield) enemy).isShielded(false);
                        }
                        laserController.fireLaser(controller);
                    }
                }
            }


        }
        projectileController.update();
        minimap.updateMinimap(dt, inputResults.didExpandMinimap());
        level.getAim().update(canvas, dt);
        laserController.updateLasers(dt, level.getWorld(), level.getBandit());

        // Update the camera
        GameCamera cam = canvas.getCamera();
        Vector2 target = canvas.unproject(PlayerController.getInstance().getCrossHair());
        if (!cam.isFixedX()) {
            cam.setTargetX(bandit.getCameraTarget().x);
            cam.setSecondaryTargetX(target.x);
        }
        if (!cam.isFixedY()) {
            cam.setTargetY(bandit.getCameraTarget().y);
            cam.setSecondaryTargetY(target.y);
        }
        canvas.getCamera().update(dt);

        //Check to create post-orb enemies
        if (orbCollected && !spawnedPostOrbEnemies) {
            level.spawnPostOrbEnemies();
            spawnedPostOrbEnemies = true;
        }

        // Turn the physics engine crank.
        level.getWorld().step(WORLD_STEP, WORLD_VELOC, WORLD_POSIT);
        bubblegumController.updateJoints(level);
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
        level.draw(canvas, constantsJson, trajectoryProjectile, laserBeam, laserBeamEnd);

        if (!hud.hasViewport()) hud.setViewport(canvas.getUIViewport());
        canvas.getUIViewport().apply();
        hud.draw(level, bubblegumController, (int) orbCountdown, (int) (1 / delta), level.getDebug(), reloadingGum);

        Vector2 banditPosition = level.getBandit().getPosition();

        minimap.draw(banditPosition);

        // Final message
        if (complete && !failed) {
            displayFont.setColor(Color.YELLOW);
            canvas.begin(); // DO NOT SCALE
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
        SoundController.pause();
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


    public void setGravity(float gravity) {
        float g = gravity;
        if (level.getWorld().getGravity().y < 0) {
            g = -g;
        }
        level.getWorld().setGravity(new Vector2(0, g));
    }

}