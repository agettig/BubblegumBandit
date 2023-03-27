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
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.AIController;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.EnemyController;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledGraph;
import edu.cornell.gdiac.bubblegumbandit.helpers.TiledParser;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.MovingEnemyModel;
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

    private EnemyController[] enemyControllers;

    public AIController[] getEnemyControllers() {
        return aiControllers;
    }

    private Board board;

    private TiledGraph tiledGraph;

    /** The width of the level. */
    private int levelWidth;
    /** The height of the level. */
    private int levelHeight;


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
     * @param constants   the JSON file defining the constants
     * @param tilesetJson the JSON file defining the tileset
     */
    public void populate(AssetDirectory directory, JsonValue levelFormat, JsonValue constants, JsonValue tilesetJson) {
        JsonValue boardLayer = levelFormat.get("layers").child();

        JsonValue tileLayer = boardLayer.next();
        JsonValue objects = tileLayer.next().get("Objects");

        int[] worldData = tileLayer.get("data").asIntArray();
        float gravity = 0;
        int numEnemies = 0;

        JsonValue property = levelFormat.get("properties").child();
        while (property != null) {
            String propName = property.get("name").asString();
            if (propName.equals("gravity")) {
                gravity = property.getFloat("value");
            } else if (propName.equals("numenemies")) {
                numEnemies = property.getInt("value");
            }
            property = property.next();
        }

        float[] pSize = constants.get("physicsSize").asFloatArray();

        levelWidth = levelFormat.getInt("width");
        levelHeight = levelFormat.getInt("height");
        world = new World(new Vector2(0, gravity), false);
        bounds = new Rectangle(0, 0, levelWidth, levelHeight);

        scale.x = pSize[0];
        scale.y = pSize[1];
        int boardIdOffset = 0;
        JsonValue tileset = levelFormat.get("tilesets").child();
        boardIdOffset = tileset.next().getInt("firstgid");
//        while (tileset != null) {
//            if (tileset.get("source").asString().equals("..\\/..\\/Tiled\\/board.tsx")) {
//                boardIdOffset = tileset.getInt("firstgid");
//            }
//            tileset = tileset.next();
//        }

        board = new Board(boardLayer, boardIdOffset, scale);

        tiledGraph = new TiledGraph(boardLayer, boardIdOffset, scale);

        String key2 = constants.get("background").asString();
        backgroundText = directory.getEntry(key2, Texture.class);
        backgroundRegion = new TextureRegion(backgroundText);

        TextureRegion[] textures = TiledParser.createTileset(directory, tilesetJson);
        aiControllers = new AIController[numEnemies];
        enemyControllers = new EnemyController[numEnemies];

        // Iterate over each tile in the world and create if it exists
        for (int i = 0; i < worldData.length; i++) {
            int tileVal = worldData[i];
            if (tileVal != 0) {
                TileModel newTile = new TileModel();
                float x = (i % levelWidth) + 0.5f;
                float y = levelHeight - (i / levelWidth) - 0.5f;
                newTile.initialize(textures[tileVal], x, y, constants.get("tiles"));
                newTile.setDrawScale(scale);
                activate(newTile);
                newTile.setFilter(CATEGORY_TERRAIN, MASK_TERRAIN);
            }
        }

        // Create objects
        JsonValue object = objects.child();
        int enemyCount = 0;
        while (object != null) {
            String objName = object.get("name").asString();
            float x = (object.getFloat("x") + (object.getFloat("width") / 2)) / scale.x;
            float y = levelHeight - ((object.getFloat("y") - (object.getFloat("height") / 2)) / scale.y);
            switch (objName) {
                case "Player":
                    bandit = new BanditModel(world);
                    bandit.initialize(directory, x, y, constants.get("avatar"));
                    bandit.setDrawScale(scale);
                    break;
                case "Exit":
                    goalDoor = new ExitModel();
                    goalDoor.initialize(directory, x, y, constants.get("exit"));
                    goalDoor.setDrawScale(scale);
                    break;
                case "Enemy":
                    JsonValue enemyConstants = constants.get(object.get("type").asString());
                    if (enemyConstants.get("type").asString().equals("moving")) {
                        EnemyModel enemy = new MovingEnemyModel(world, enemyCount);
                        enemy.initialize(directory, x, y, enemyConstants);
                        enemy.setDrawScale(scale);
                        activate(enemy);
                        enemy.setFilter(CATEGORY_ENEMY, MASK_ENEMY);
                        aiControllers[enemyCount] = new AIController(enemy, bandit, board);
                        enemyControllers[enemyCount] = new EnemyController(enemy, bandit, tiledGraph);
                        enemyCount++;
                    }
                    break;
                case "Gum":
                    Collectible gum = new Collectible();
                    gum.initialize(directory, x, y, scale, constants.get("floatingGums"));
                    activate(gum);
                    break;
                case "Camera":
                    CameraTileModel cam = new CameraTileModel();
                    cam.initialize(x, y, levelHeight, object, constants.get("cameratile"));
                    cam.setDrawScale(scale);
                    activate(cam);
                    break;
                default:
                    throw new UnsupportedOperationException(objName + " is not a valid object");

            }
            object = object.next();
        }
        activate(goalDoor);
        // Add bandit at the end because this affects draw order
        activate(bandit);
        bandit.setFilter(CATEGORY_PLAYER, MASK_PLAYER);

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

        for (EnemyController controller : enemyControllers){
            controller.getEnemyStateMachine().update();
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
        Color[] colors = new Color[]{new Color(1, .619f, .62f, 1),
                                     new Color(1, .73f, .73f, .9f),
                                     new Color(1, .81f, .81f, .8f),
                                     new Color(1,.86f,.86f, .7f),
                                     new Color(1,.905f,.905f, .6f),
                                     new Color(1,1,1,.5f)};
        int range = numSegments + 1;
        if (range > 6) range = 6;
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
        for (int i = 0; i < levelWidth; i++) {
            for (int j = 0; j < levelHeight; j++) {
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
            if (board != null) {
                board.drawBoard(canvas);
                tiledGraph.drawGraph(canvas);
            }
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
        for (int i = 0; i < levelWidth * scale.x; i += backgroundRegion.getRegionWidth()) {
            for (int j = 0; j < levelHeight * scale.x; j += backgroundRegion.getRegionHeight()) {
                if (j + backgroundRegion.getRegionHeight() > levelHeight * scale.x) {
                    backgroundRegion.setRegionY((int) (backgroundText.getHeight()
                            - (levelHeight * scale.x - j)));
                }
                if (i + backgroundRegion.getRegionWidth() > levelWidth * scale.x) {
                    backgroundRegion.setRegionWidth((int) (levelWidth * scale.x - i));
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