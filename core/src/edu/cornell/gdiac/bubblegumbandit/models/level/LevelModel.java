/*
 * LevelMode.java
 *
 * This stores all of the information to define a level in our simple platform game.
 * We have an avatar, some walls, some platforms, and an exit.  This is a refactoring
 * of WorldController in Lab 4 that separates the level data from the level control.
 *
 * Note that most of the methods are getters and setters, as is common with models.
 * The gameplay behavior is defined by GameController.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * JSON version, 3/2/2016
 */

package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.AIController;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.MovingEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.gum.FloatingGum;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.util.PooledList;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.bubblegumbandit.controllers.PlayerController;

import java.util.Iterator;

import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.*;

/**
 * Represents a single level in our game
 * <p>
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.  To reset a level, dispose it and reread the JSON.
 * <p>
 * The level contains its own Box2d World, as the World settings are defined by the
 * JSON file.  However, there is absolutely no controller code in this class, as
 * the majority of the methods are getters and setters.  The getters allow the
 * GameController class to modify the level elements.
 */
public class LevelModel {

    /**
     * The gap between each dot in the trajectory diagram (for raytraced trajectory.)
     * TODO: Move to another class?
     */
    private final float trajectoryGap = 0.5f;

    /**
     * The scale of each dot in the trajectory diagram (for raytraced trajectory.)
     * TODO: Move to another class?
     */
    private final float trajectoryScale = 0.5f;

    /**
     * The Box2D world
     */
    protected World world;
    /**
     * The boundary of the world
     */
    protected Rectangle bounds;
    /**
     * The world scale
     */
    protected Vector2 scale;

    // Physics objects for the game
    /**
     * Reference to the character avatar
     */
    private BanditModel bandit;
    /**
     * Reference to the goalDoor (for collision detection)
     */
    private ExitModel goalDoor;

    /**
     * Reference to floating gum in the game, to be collected
     */
    private FloatingGum[] floatingGum;

    /**
     * Whether or not the level is in debug more (showing off physics)
     */
    private boolean debug;

    /**
     * The full background of the level
     */
    private Texture backgroundText;

    /**
     * The background of the level, cropped if necessary
     */
    private TextureRegion backgroundRegion;


    /**
     * All the objects in the world.
     */
    protected PooledList<Obstacle> objects = new PooledList<Obstacle>();

    private AIController[] aiControllers;

    public AIController[] getEnemyControllers() {
        return aiControllers;
    }

    private Board board;


    /**
     * Returns the bounding rectangle for the physics world
     * <p>
     * The size of the rectangle is in physics, coordinates, not screen coordinates
     *
     * @return the bounding rectangle for the physics world
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Returns the scaling factor to convert physics coordinates to screen coordinates
     *
     * @return the scaling factor to convert physics coordinates to screen coordinates
     */
    public Vector2 getScale() {
        return scale;
    }

    /**
     * Returns a reference to the Box2D World
     *
     * @return a reference to the Box2D World
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns a reference to the player avatar
     *
     * @return a reference to the player avatar
     */
    public BanditModel getBandit() {
        return bandit;
    }

    /**
     * Returns a reference to the exit door
     *
     * @return a reference to the exit door
     */
    public ExitModel getExit() {
        return goalDoor;
    }

    /**
     * Returns whether this level is currently in debug node
     * <p>
     * If the level is in debug mode, then the physics bodies will all be drawn as
     * wireframes onscreen
     *
     * @return whether this level is currently in debug node
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Sets whether this level is currently in debug node
     * <p>
     * If the level is in debug mode, then the physics bodies will all be drawn as
     * wireframes onscreen
     *
     * @param value whether this level is currently in debug node
     */
    public void setDebug(boolean value) {
        debug = value;
    }

    /**
     * Creates a new LevelModel
     * <p>
     * The level is empty and there is no active physics world.  You must read
     * the JSON file to initialize the level
     */
    public LevelModel() {
        world = null;
        bounds = new Rectangle(0, 0, 1, 1);
        scale = new Vector2(1, 1);
        debug = false;

    }

    public Board getBoard() {
        return board;
    }

    /**
     * Lays out the game geography from the given JSON file
     *
     * @param directory   the asset manager
     * @param levelFormat the JSON file defining the level
     */
    public void populate(AssetDirectory directory, JsonValue levelFormat) {
        initializeWorld(directory, levelFormat);
        initializeBandit(directory, levelFormat);
        initializeFloatingGum(directory, levelFormat);
        initializeEnemies(directory, levelFormat);
    }

    public void dispose() {
        for (Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        if (world != null) {
            world.dispose();
            world = null;
        }
    }

    private void initializeFloatingGum(AssetDirectory directory, JsonValue levelFormat) {
        // get number of floating gums
        int numGums = levelFormat.get("floatingGums").get("numGums").asInt();
        JsonValue position = levelFormat.get("floatingGums").get("positions").child();
        floatingGum = new FloatingGum[numGums];

        // json of gums
        JsonValue gums = levelFormat.get("floatingGums");

        for (int i = 0; i < numGums; i++) {
            FloatingGum gum = new FloatingGum();
            gum.initialize(directory, gums);
            gum.setPosition(position);
            gum.setDrawScale(scale);
            activate(gum);
            floatingGum[i] = gum;
            position = position.next();
        }

    }

    private void initializeBandit(AssetDirectory directory, JsonValue levelFormat) {
        // Create bandit
        bandit = new BanditModel(world);
        bandit.initialize(directory, levelFormat.get("avatar"));
        bandit.setDrawScale(scale);
        activate(bandit);
        bandit.setFilter(CATEGORY_PLAYER, MASK_PLAYER);
    }

    /**
     * Initializes the game world based on the provided level format.
     * Reads world data from the level format JSON and creates instances of
     * the appropriate world objects, such as
     * goal doors, background, walls, and platforms.
     *
     * @param directory   The AssetDirectory containing the assets required for
     *                    world object initialization.
     * @param levelFormat A JsonValue containing the level format data,
     *                    including world object definitions.
     */
    private void initializeWorld(AssetDirectory directory, JsonValue levelFormat) {
        float gravity = levelFormat.getFloat("gravity");
        float[] pSize = levelFormat.get("physicsSize").asFloatArray();

        board = new Board(levelFormat.get("board"), scale);

        world = new World(new Vector2(0, gravity), false);
        bounds = new Rectangle(0, 0, board.getWidth(), board.getHeight());

        scale.x = pSize[0];
        scale.y = pSize[1];


        // Add level goal
        goalDoor = new ExitModel();
        goalDoor.initialize(directory, levelFormat.get("exit"));
        goalDoor.setDrawScale(scale);
        activate(goalDoor);

        String key2 = levelFormat.get("background").asString();
        backgroundText = directory.getEntry(key2, Texture.class);
        backgroundRegion = new TextureRegion(backgroundText);

        JsonValue wall = levelFormat.get("walls").child();
        while (wall != null) {
            WallModel obj = new WallModel();
            obj.initialize(directory, wall);
            obj.setDrawScale(scale);
            activate(obj);
            obj.setFilter(CATEGORY_TERRAIN, MASK_TERRAIN);
            wall = wall.next();
        }

        JsonValue floor = levelFormat.get("platforms").child();
        while (floor != null) {
            PlatformModel obj = new PlatformModel();
            obj.initialize(directory, floor);
            obj.setDrawScale(scale);
            activate(obj);
            obj.setFilter(CATEGORY_TERRAIN, MASK_TERRAIN);
            floor = floor.next();
        }
    }

    /**
     * Initializes the enemies in the game based on the provided level format.
     * Reads enemy data from the level format JSON and creates instances
     * of the appropriate enemy models.
     * Also initializes AIControllers for each enemy.
     *
     * @param directory   The AssetDirectory containing the
     *                    assets required for enemy initialization.
     * @param levelFormat A JsonValue containing the level format data,
     *                    including enemy definitions.
     */
    private void initializeEnemies(AssetDirectory directory, JsonValue levelFormat) {

        //Make AIControllers from JSON data.
        // get number of enemies
        int numEnemies = levelFormat.get("enemies").get("numenemies").asInt();

        aiControllers = new AIController[numEnemies];
        JsonValue enemyJson = levelFormat.get("enemies").get("enemylist").child();
        for (int i = 0; i < numEnemies; i++) {
            EnemyModel enemy;
            if (enemyJson.get("type").asString().equals("moving")) {
                enemy = new MovingEnemyModel(world, i);
                enemy.initialize(directory, enemyJson);
                enemy.setDrawScale(scale);
                activate(enemy);
                enemy.setFilter(CATEGORY_ENEMY, MASK_ENEMY);
                enemyJson = enemyJson.next();
                aiControllers[i] = new AIController(enemy, bandit, board);
            }
        }
    }

    /**
     * Immediately adds the object to the physics world
     *
     * @param obj The object to add
     */
    public void activate(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        objects.add(obj);
        obj.activatePhysics(world);
    }

    /**
     * Returns true if the object is in bounds.
     * <p>
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     * @return true if the object is in bounds.
     */
    private boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x + bounds.width);
        boolean vert = (bounds.y <= obj.getY() && obj.getY() <= bounds.y + bounds.height);
        return horiz && vert;
    }

    /**
     * Updates the level objects' physics state (NOT GAME LOGIC).
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        // Garbage collect the deleted objects.
        Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<Obstacle>.Entry entry = iterator.next();
            Obstacle obj = entry.getValue();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
                entry.remove();
            } else {
                obj.update(dt);
            }
        }
    }

    /**
     * Returns the origin of the gum when fired by the player.
     *
     * @param gumJV the JSON Value representing the gum projectile.
     * @return The origin of the projectile of the gum when fired.
     */
    public Vector2 getProjOrigin(JsonValue gumJV, GameCanvas canvas) {
        //  TODO: The logic for this should be in Gum Controller.

        Vector2 cross = canvas.unproject(PlayerController.getInstance().getCrossHair());
        cross.scl(1 / scale.x, 1 / scale.y);

        cross.x = Math.max(bounds.x, Math.min(bounds.x + bounds.width, cross.x));
        cross.y = Math.max(bounds.y, Math.min(bounds.y + bounds.height, cross.y));


        Vector2 target = cross;

        float offsetX = gumJV.getFloat("offsetX", 0);
        float offsetY = gumJV.getFloat("offsetY", 0);
        offsetY *= bandit.getYScale();

        Vector2 origin = new Vector2(bandit.getX(), bandit.getY() + offsetY);
        Vector2 dir = new Vector2((target.x - origin.x), (target.y - origin.y));
        dir.nor();
        dir.scl(offsetX);

        // Adjust origin of shot based on target pos
        // Rotate around top half of player for gravity pulling down, bottom half for gravity pulling up
        if (dir.y * world.getGravity().y < 0) {
            origin.x += dir.x;
        } else {
            origin.x += (target.x > bandit.getX() ? offsetX : -offsetX);
        }
        origin.y += dir.y;
        return origin;
    }


    public Vector2 getProjTarget(GameCanvas canvas) {
        Vector2 cross = canvas.unproject(PlayerController.getInstance().getCrossHair());
        cross.scl(1 / scale.x, 1 / scale.y);

        cross.x = Math.max(bounds.x, Math.min(bounds.x + bounds.width, cross.x));
        cross.y = Math.max(bounds.y, Math.min(bounds.y + bounds.height, cross.y));
        Vector2 target = cross;
        return cross;

    }

    public float getXTrajectory(float ox, float vx, float t) {
        return ox + vx * t;
    }

    public float getYTrajectory(float oy, float vy, float t, float g) {
        return oy + vy * t + .5f * g * t * t;
    }

    public void drawProjectile(JsonValue levelFormat, float gumSpeed, float gumGravity, TextureRegion
            gumProjectile, GameCanvas canvas) {
        Vector2 target = PlayerController.getInstance().getCrossHair();
        JsonValue gumJV = levelFormat.get("gumProjectile");

        Vector2 origin = getProjOrigin(gumJV, canvas);

        Vector2 gumVel = new Vector2(target.x - origin.x, target.y - origin.y);
        gumVel.nor();
        if (gumSpeed == 0) { // Use default gum speed
            gumVel.scl(gumJV.getFloat("speed", 0));
        } else { // Use slider gum speed
            gumVel.scl(gumSpeed);
        }
        float x, y;
        for (int i = 1; i < 10; i++) {
            x = getXTrajectory(origin.x, gumVel.x, i / 10f);
            y = getYTrajectory(origin.y, gumVel.y, i / 10f, gumGravity * world.getGravity().y);
            canvas.draw(gumProjectile, Color.PINK, gumProjectile.getRegionWidth() / 2f, gumProjectile.getRegionHeight() / 2f,
                    x * 50, y * 50, gumProjectile.getRegionWidth() * trajectoryScale, gumProjectile.getRegionHeight() * trajectoryScale);
        }
    }

    /**
     * Draws the path of the projectile using a raycast. Only works for shooting in a straight line (gravity scale of 0).
     *
     * @param levelFormat The JSON value defining the level
     * @param asset       The asset used to draw the trajectory. Must be round for good results.
     * @param canvas      The GameCanvas to draw the trajectory on.
     */
    public void drawProjectileRay(JsonValue levelFormat, TextureRegion asset, GameCanvas canvas) {
        Vector2 target = PlayerController.getInstance().getCrossHair();
        JsonValue gumJV = levelFormat.get("gumProjectile");
        Vector2 origin = getProjOrigin(gumJV, canvas);
        Vector2 dir = new Vector2((target.x - origin.x), (target.y - origin.y));
        dir.nor();
        dir.scl(bounds.width * 2); // Make sure ray will cover the whole screen
        Vector2 end = new Vector2(origin.x + dir.x, origin.y + dir.y); // Find end point of the ray cast

        final Vector2 intersect = new Vector2();

        RayCastCallback ray = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point,
                                          Vector2 normal, float fraction) {
                Obstacle ob = (Obstacle) fixture.getBody().getUserData();
                if (!ob.getName().equals("gumProjectile")) {
                    intersect.set(point);
                    return fraction;
                }
                return -1;
            }
        };
        world.rayCast(ray, origin, end);

        float x;
        float y;
        dir = new Vector2(intersect.x - origin.x, intersect.y - origin.y);
        int numSegments = (int) (dir.len() / trajectoryGap); // Truncate to find number before colliding
        dir.nor();
        Color[] colors = new Color[]{new Color(255, 158, 158, 255),
                                     new Color(255, 187, 187, 255),
                                     new Color(255, 206, 206, 255),
                                     new Color(255,219,219, 255),
                                     new Color(255,231,231, 255)};
        int range = numSegments + 1;
        if (range > 5) range = 5;
        for (int i = 0; i < range; i++) {
            x = origin.x + (dir.x * i * trajectoryGap);
            y = origin.y + (dir.y * i * trajectoryGap);
            canvas.draw(asset, colors[i], asset.getRegionWidth() / 2f, asset.getRegionHeight() / 2f, x * scale.x,
                    y * scale.y, asset.getRegionWidth() * trajectoryScale, asset.getRegionHeight() * trajectoryScale);
        }
    }


    public void drawGrid(GameCanvas canvas) {
        PolygonShape s = new PolygonShape();
        int halfWidth = (int) (scale.x / 2);
        int halfHeight = (int) (scale.y / 2);
        s.setAsBox(.5f * scale.x, .5f * scale.y);
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                canvas.drawPhysics(s, Color.RED, i * scale.x + halfWidth, j * scale.y + halfHeight);
            }
        }
    }

    /**
     * Draws the level to the given game canvas
     * <p>
     * If debug mode is true, it will outline all physics bodies as wireframes. Otherwise
     * it will only draw the sprite representations.
     *
     * @param canvas the drawing context
     */
    public void draw(GameCanvas canvas, JsonValue levelFormat, float gumSpeed, float gumGravity, TextureRegion
            gumProjectile) {
        canvas.clear();

        canvas.begin();

        if (backgroundRegion != null) {
            drawBackground(canvas);
        }
        for (Obstacle obj : objects) {
            obj.draw(canvas);
        }
        if (gumGravity != 0) {
            drawProjectile(levelFormat, gumSpeed, gumGravity, gumProjectile, canvas);
        } else {
            drawProjectileRay(levelFormat, gumProjectile, canvas);
        }

        canvas.end();

        if (debug) {
            canvas.beginDebug();
            for (Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            drawGrid(canvas);
            board.drawBoard(canvas);
            canvas.endDebug();

        }
    }

    /**
     * Draws a repeating background, and crops off any overhangs outside the level
     * to maintain resolution and aspect ratio.
     *
     * @param canvas the current canvas
     */
    private void drawBackground(GameCanvas canvas) {
        for (int i = 0; i < board.getWidth() * scale.x; i += backgroundRegion.getRegionWidth()) {
            for (int j = 0; j < board.getHeight() * scale.x; j += backgroundRegion.getRegionHeight()) {
                if (j + backgroundRegion.getRegionHeight() > board.getHeight() * scale.x) {
                    backgroundRegion.setRegionY((int) (backgroundText.getHeight()
                            - (board.getHeight() * scale.x - j)));
                }
                if (i + backgroundRegion.getRegionWidth() > board.getWidth() * scale.x) {
                    backgroundRegion.setRegionWidth((int) (board.getWidth() * scale.x - i));
                }
                canvas.draw(backgroundRegion, i, j);
                backgroundRegion.setRegionX(0);
                backgroundRegion.setRegionY(0);
                backgroundRegion.setRegionHeight(backgroundText.getHeight());
                backgroundRegion.setRegionWidth(backgroundText.getWidth());

            }
        }
    }
}