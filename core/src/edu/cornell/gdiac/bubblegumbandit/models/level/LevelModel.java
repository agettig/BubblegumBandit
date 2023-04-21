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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.LaserController;
import edu.cornell.gdiac.bubblegumbandit.controllers.PlayerController;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.AIController;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledGraph;
import edu.cornell.gdiac.bubblegumbandit.helpers.Gummable;
import edu.cornell.gdiac.bubblegumbandit.helpers.TiledParser;
import edu.cornell.gdiac.bubblegumbandit.helpers.Unstickable;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.ProjectileEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.LaserEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.RollingEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.util.PooledList;

import java.util.*;

import edu.cornell.gdiac.bubblegumbandit.models.BackObjModel;

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
     * How close to the center of the tile we need to be to stop drifting
     */
    private static final float DRIFT_TOLER = .2f;
    /**
     * How fast we drift to the tile center when paused
     */
    private static final float DRIFT_SPEED = 0.325f;

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
     * The amount of time counted down after the orb is collected.
     */
    private float timer = 60;


    /**
     * All the objects in the world.
     */
    protected PooledList<Obstacle> objects = new PooledList<Obstacle>();

    private Array<AIController> enemyControllers;

    public Array<AIController> aiControllers() {
        return enemyControllers;
    }

    private TiledGraph tiledGraphGravityDown;
    private TiledGraph tiledGraphGravityUp;

    /** EnemyModels to spawn after the orb gets picked up. */
    private HashSet<EnemyModel> postOrbEnemies;

    /**
     * The width of the level.
     */
    private int levelWidth;
    /**
     * The height of the level.
     */
    private int levelHeight;

    /**
     * Lighting system
     */
    private AlarmController alarms;

    /**
     * Reference to the aim model
     */
    private AimModel aim;

    /**
     * Reference to background elements in the game
     */
    private Array<BackObjModel> backgroundObjects;

    /**
     * Reference to support tile objects
     */
    private Array<BackgroundTileModel> supportTiles;

    /**
     * Returns the aim in this level.
     */
    public AimModel getAim() {
        return aim;
    }

    ;


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
     * Start the alarms in the level.
     */
    public void startAlarms() {
        alarms.setAlarms(true);
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
        aim = new AimModel();
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
        aim.initialize(directory, constants);

        JsonValue boardGravityDownLayer = null;
        JsonValue boardGravityUpLayer = null;

        JsonValue tileLayer = null;
        JsonValue objects = null;
        JsonValue supports = null;
        JsonValue postOrb = null;

        JsonValue layer = levelFormat.get("layers").child();
        while (layer != null) {
            String layerName = layer.getString("name");
            switch (layerName) {
                case "BoardGravityDown":
                    boardGravityDownLayer = layer;
                    break;
                case "BoardGravityUp":
                    boardGravityUpLayer = layer;
                    break;
                case "Terrain":
                    tileLayer = layer;
                    break;
                case "Objects":
                    objects = layer.get("Objects");
                    break;
                case "Supports":
                    supports = layer;
                    break;
                case "PostOrb":
                    postOrb = layer;
                    break;
                default:
                    throw new RuntimeException("Invalid layer name");
            }
            layer = layer.next();
        }

        if (boardGravityDownLayer == null || boardGravityUpLayer == null || tileLayer == null || objects == null) {
            throw new RuntimeException("Missing layer data");
        }

        int[] worldData = tileLayer.get("data").asIntArray();
        float gravity = 0;

        JsonValue property = levelFormat.get("properties").child();
        while (property != null) {
            String propName = property.get("name").asString();
            if (propName.equals("gravity")) {
                gravity = property.getFloat("value");
            }
            if (propName.equals("timer")) {
                timer = property.getFloat("value");
            }
            property = property.next();
        }


        //------------------------------------------------

        float[] pSize = constants.get("physicsSize").asFloatArray();

        levelWidth = levelFormat.getInt("width");
        levelHeight = levelFormat.getInt("height");
        world = new World(new Vector2(0, gravity), false);
        bounds = new Rectangle(0, 0, levelWidth, levelHeight);

        scale.x = pSize[0];
        scale.y = pSize[1];
        JsonValue tileset = levelFormat.get("tilesets").child();
        while (!tileset.get("name").asString().equals("board")) {
            tileset = tileset.next();
        }
        int boardIdOffset = tileset.getInt("firstgid");

        tiledGraphGravityUp = new TiledGraph(boardGravityUpLayer, boardIdOffset, scale, 3f / 8);
        tiledGraphGravityDown = new TiledGraph(boardGravityDownLayer, boardIdOffset, scale, 2f / 8);

        String key2 = constants.get("background").asString();
        backgroundText = directory.getEntry(key2, Texture.class);
        backgroundRegion = new TextureRegion(backgroundText);


        HashMap<Integer, TextureRegion> textures = TiledParser.createTileset(directory, levelFormat);
        HashMap<Vector2, TileModel> tiles = new HashMap<>();
        supportTiles = new Array<BackgroundTileModel>();
        enemyControllers = new Array<>();
        backgroundObjects = new Array<>();

        // Iterate over each tile in the world and create if it exists
        for (int i = 0; i < worldData.length; i++) {
            int tileVal = worldData[i];
            if (tileVal != 0) {
                TileModel newTile = new TileModel();
                float x = (i % levelWidth) + 0.5f;
                float y = levelHeight - (i / levelWidth) - 0.5f;
                newTile.initialize(textures.get(tileVal), x, y, constants.get("tiles"));
                tiles.put(new Vector2(x, y), newTile);
                newTile.setDrawScale(scale);
                activate(newTile);
                newTile.setFilter(CATEGORY_TERRAIN, MASK_TERRAIN);
            }
        }

        if (supports != null) {
            int[] supportData = supports.get("data").asIntArray();
            // Iterate over each support in the world and create if it exists
            for (int i = 0; i < supportData.length; i++) {
                int tileVal = supportData[i];
                if (tileVal != 0) {
                    BackgroundTileModel newTile = new BackgroundTileModel();
                    float x = (i % levelWidth) + 0.5f;
                    float y = levelHeight - (i / levelWidth) - 0.5f;
                    newTile.initialize(textures.get(tileVal), x, y, scale);
                    supportTiles.add(newTile);

                }
            }
        }

        // Iterate over each tile in the world, find and mark open corners of tiles that have them
        for (Map.Entry<Vector2, TileModel> entry : tiles.entrySet()) {
            Vector2 c = entry.getKey();
            TileModel tile = entry.getValue();
            Vector2 top = new Vector2(c.x, c.y + 1);
            Vector2 right = new Vector2(c.x + 1, c.y);
            Vector2 left = new Vector2(c.x - 1, c.y);
            Vector2 bottom = new Vector2(c.x, c.y - 1);

            if (!tiles.containsKey(top) && !tiles.containsKey(right)) {
                tile.hasCorner(true);
                tile.topRight(true);
            }
            if (!tiles.containsKey(top) && !tiles.containsKey(left)) {
                tile.hasCorner(true);
                tile.topLeft(true);
            }
            if (!tiles.containsKey(bottom) && !tiles.containsKey(left)) {
                tile.hasCorner(true);
                tile.bottomLeft(true);
            }
            if (!tiles.containsKey(bottom) && !tiles.containsKey(right)) {
                tile.hasCorner(true);
                tile.bottomRight(true);
            }
        }
        tiles.clear();

        bandit = null;
        goalDoor = null;
        boolean orbPlaced = false;

        // Create objects
        Array<Vector2> alarmPos = new Array<>();
        JsonValue object = objects.child();
        JsonValue enemyConstants;
        EnemyModel enemy;
        int enemyCount = 0;
        while (object != null) {
            String objType = object.get("type").asString();
            float x = (object.getFloat("x") + (object.getFloat("width") / 2)) / scale.x;
            float y = levelHeight - ((object.getFloat("y") - (object.getFloat("height") / 2)) / scale.y);


            switch (objType) {
                case "box":
                case "chair":

                    //the gid is a huge negative integer when flipped horizontally.
                    // add a specific attribute for this from Tiled?
                    boolean facingRight = (object.getInt("gid") > 0);

                    JsonValue bgoConstants = constants.get(objType);
                    BackObjModel o = new BackObjModel();
                    o.initialize(directory, x, y, facingRight, bgoConstants);
                    o.setDrawScale(scale);
                    activate(o);
                    o.setFilter(CATEGORY_BACK, MASK_BACK);
                    backgroundObjects.add(o);
                    break;

                case "bandit":
                    bandit = new BanditModel(world);
                    bandit.initialize(directory, x, y, constants.get(objType));
                    bandit.setDrawScale(scale);
                    break;
                case "exit":
                    goalDoor = new ExitModel();
                    goalDoor.initialize(directory, x, y, constants.get(objType));
                    goalDoor.setDrawScale(scale);
                    break;
                case "smallEnemy":
                    enemyConstants = constants.get(objType);
                    x = (float) ((int) x + .5);
                    enemy = new ProjectileEnemyModel(world, enemyCount);
                    enemy.initialize(directory, x, y, enemyConstants);
                    enemy.setDrawScale(scale);
                    activate(enemy);
                    enemy.setFilter(CATEGORY_ENEMY, MASK_ENEMY);
                    enemyControllers.add(new AIController(enemy, bandit, tiledGraphGravityUp, tiledGraphGravityDown));
                    enemyCount++;
                    break;
                case "mediumEnemy":
                    enemyConstants = constants.get(objType);
                    x = (float) ((int) x + .5);
                    enemy = new RollingEnemyModel(world, enemyCount);
                    enemy.initialize(directory, x, y, enemyConstants);
                    enemy.setDrawScale(scale);
                    activate(enemy);
                    enemy.setFilter(CATEGORY_ENEMY, MASK_ENEMY);

                    enemyControllers.add(new AIController(enemy, bandit, tiledGraphGravityUp, tiledGraphGravityDown));
                    enemyCount++;

                    break;
                case "largeEnemy":
                    enemyConstants = constants.get(objType);
                    enemy = new LaserEnemyModel(world, enemyCount);
                    enemy.initialize(directory, x, y, enemyConstants);
                    enemy.setDrawScale(scale);
                    activate(enemy);
                    enemy.setFilter(CATEGORY_ENEMY, MASK_ENEMY);
                    enemyControllers.add(new AIController(enemy, bandit, tiledGraphGravityUp, tiledGraphGravityDown));
                    enemyCount++;
                    break;
                case "orb":
                    orbPlaced = true;
                case "star":
                case "floatingGum":
                    Collectible coll = new Collectible();
                    coll.initialize(directory, x, y, scale, constants.get(objType));
                    activate(coll);
                    coll.setFilter(CATEGORY_COLLECTIBLE, MASK_COLLECTIBLE);
                    coll.getFilterData().categoryBits = CATEGORY_COLLECTIBLE; // Do this for ID purposes
                    break;
                case "cameraV":
                case "cameraH":
                    CameraTileModel cam = new CameraTileModel();
                    cam.initialize(x, y, scale, levelHeight, object, constants.get("cameratile"));
                    activate(cam);
                    cam.setFilter(CATEGORY_EVENTTILE, MASK_EVENTTILE);
                    break;
                case "alarm":
                    alarmPos.add(new Vector2(x, y));
                    break;
                default:
                    enemyConstants = null;
                    throw new UnsupportedOperationException(objType + " is not a valid object");
            }
            object = object.next();
        }

        while (postOrb != null){
            String objType = postOrb.get("type").asString();
            float x = (postOrb.getFloat("x") + (postOrb.getFloat("width") / 2)) / scale.x;
            float y = levelHeight - ((postOrb.getFloat("y") - (postOrb.getFloat("height") / 2)) / scale.y);


            switch (objType) {
                case "smallEnemy":
                    enemy = new ProjectileEnemyModel(world, enemyCount);
                    postOrbEnemies.add(enemy);
                    break;
                case "mediumEnemy":
                    enemy = new RollingEnemyModel(world, enemyCount);
                    postOrbEnemies.add(enemy);
                    break;
                case "largeEnemy":
                    enemy = new LaserEnemyModel(world, enemyCount);
                    postOrbEnemies.add(enemy);
                    break;
                default:
                    throw new UnsupportedOperationException(objType + " is not a valid post orb object");
            }
            postOrb = postOrb.next();
        }

        if (goalDoor == null) {
            throw new RuntimeException("Level missing exit");
        }
        if (bandit == null) {
            throw new RuntimeException("Level missing bandit");
        }
        if (!orbPlaced) {
            throw new RuntimeException("Level missing orb");
        }

        activate(goalDoor);
        goalDoor.setFilter(CATEGORY_EVENTTILE, MASK_EVENTTILE);
        // Add bandit at the end because this affects draw order
        activate(bandit);
        bandit.setFilter(CATEGORY_PLAYER, MASK_PLAYER);

        alarms = new AlarmController(alarmPos, directory, world);
    }

    /**
     * Spawns all EnemyModels that should drop in after the Bandit picks
     * up the orb.
     *
     * @param enemyData JSON file containing data of Enemies to spawn
     * */
    public void spawnPostOrbEnemies(JsonValue enemyData){

    }

    public void dispose() {
        for (Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        if (world != null) {
            world.dispose();
            world = null;
            alarms.dispose();
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
        for (AIController controller : enemyControllers) {
//            adjustForDrift(controller.getEnemy());
            controller.getEnemyStateMachine().update();
        }
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
        alarms.update();
    }

    public float getXTrajectory(float ox, float vx, float t) {
        return ox + vx * t;
    }

    public float getYTrajectory(float oy, float vy, float t, float g) {
        return oy + vy * t + .5f * g * t * t;
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
    public void draw(GameCanvas canvas, JsonValue levelFormat, TextureRegion
            gumProjectile, TextureRegion laserBeam, TextureRegion laserBeamEnd) {
        canvas.begin();
        if (backgroundRegion != null) {
            drawBackground(canvas);
        }

        alarms.drawAlarms(canvas, scale);

        for(BackgroundTileModel tile: supportTiles) {
            tile.draw(canvas);
        }

        Set<Obstacle> postLaserDraw = new HashSet<>();



        for (Obstacle obj : objects) {
            if (obj.equals(aim.highlighted)) { // Probably inefficient, but the draw order needs to be maintained.
                aim.highlighted.drawWithOutline(canvas);
            } else {
                obj.draw(canvas);
            }
        }
        drawChargeLasers(laserBeam, laserBeamEnd, canvas);




        aim.drawProjectileRay(canvas);



        canvas.end();

        if (debug) {
            canvas.beginDebug();
            for (Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            // drawGrid(canvas);
            if (tiledGraphGravityUp != null && tiledGraphGravityDown != null) {
                tiledGraphGravityDown.drawGraph(canvas);
                tiledGraphGravityUp.drawGraph(canvas);
            }
            canvas.endDebug();
        }
        alarms.drawLights(canvas, scale);
    }

    public void drawChargeLasers(TextureRegion beam, TextureRegion beamEnd, GameCanvas canvas) {

        //Local variables to scale our laser depending on its phase.
        final float chargeLaserScale = .5f;
        final float lockedLaserScale = 1f;
        final float firingLaserScale = 1.5f;
        for (AIController ai : enemyControllers) {
            if (ai.getEnemy() instanceof LaserEnemyModel) {

                //Don't draw inactive lasers.
                LaserEnemyModel enemy = (LaserEnemyModel) ai.getEnemy();
                if(enemy.inactiveLaser()) continue;


                //Determine properties based on our laser phase.
                Color laserColor;
                float laserThickness;

                if(enemy.chargingLaser()){
                    laserColor = Color.YELLOW;
                    laserThickness = chargeLaserScale;
                }
                else if(enemy.lockingLaser()){
                    laserColor = Color.ORANGE;
                    laserThickness = lockedLaserScale;
                }
                else{
                    laserColor = Color.WHITE;
                    laserThickness = firingLaserScale;
                }

                //Math calculations for the laser.
                Vector2 intersect = enemy.getBeamIntersect();
                Vector2 enemyPos = enemy.getPosition();
                Vector2 dir = new Vector2(
                        intersect.x - enemyPos.x,
                        intersect.y - enemyPos.y
                );

                beam.setRegionWidth(2);
                int numSegments = (int)((dir.len() * scale.x) / beam.getRegionWidth());
                if(enemy.getGummed()) numSegments -= (((enemy.getWidth()/2)*scale.x))/beam.getRegionWidth();
                dir.nor();

                //Draw her up!
                for(int i = 0; i < numSegments; i++){



                    //Calculate the positions and angle of the charging laser.
                    float enemyOffsetX = enemy.getFaceRight() ? (enemy.getWidth()/2): 0;
                    float enemyOffsetY = enemy.getHeight()*10;

                    float x = enemyPos.x * scale.x + (i * dir.x * beam.getRegionWidth());
                    float y = enemyPos.y * scale.y + (i * dir.y * beam.getRegionWidth());

                    float slope = (intersect.y - enemyPos.y)/(intersect.x - enemyPos.x);
                    float ang = (float) Math.atan(slope);

                    if(enemy.getGummed()) enemyOffsetX -= (enemy.getWidth()/2)*scale.x;

                    canvas.draw(
                            beam,
                            laserColor,
                            beam.getRegionWidth(),
                            beam.getRegionHeight(),
                            x + enemyOffsetX,
                            y + enemyOffsetY* enemy.getYScale(),
                            ang,
                            1f,
                            1 * laserThickness);
                }
            }
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

    /**
     * Nudges the ship back to the center of a tile if it is not moving.
     *
     * @param enemy The Enemy to adjust
     */
    private void adjustForDrift(EnemyModel enemy) {
        // Drift to line up vertically with the grid.

        if (enemy.getVX() == 0.0f) {
            float offset = enemy.getX() - (int) enemy.getX();
            if (offset > .5) {
                offset -= .5;
            } else {
                offset = .5f - offset;
            }
            if (offset < -DRIFT_TOLER) {
                enemy.setX(enemy.getX() + DRIFT_SPEED);
            } else if (offset > DRIFT_TOLER) {
                enemy.setX(enemy.getX() - DRIFT_SPEED);
            }
        }

        // Drift to line up horizontally with the grid.
        if (enemy.getVY() == 0.0f) {
            float offset = enemy.getY() - (int) enemy.getY();
            if (offset > .5) {
                offset -= .5;
            } else {
                offset = .5f - offset;
            }
            if (offset < -DRIFT_TOLER) {
                enemy.setY(enemy.getY() + DRIFT_SPEED);
            } else if (offset > DRIFT_TOLER) {
                enemy.setY(enemy.getY() - DRIFT_SPEED);
            }
        }
    }

    /**
     * Returns the amount of time the player has to escape.
     */
    public float getOrbCountdown() {
        return timer;
    }

    public class AimModel {

        /**
         * The colors used in the aim render
         */
        private final Color[] COLORS = new Color[]{new Color(1, .619f, .62f, 1),
                                                   new Color(1, .73f, .73f, .9f),
                                                   new Color(1, .81f, .81f, .8f),
                                                   new Color(1, .86f, .86f, .7f),
                                                   new Color(1, .905f, .905f, .6f),
                                                   new Color(1, 1, 1, .5f)};

        /**
         * The max number of dots in the trajectory
         */
        private final int MAX_DOTS = 6;

        /**
         * The gap between each dot in the trajectory diagram (for raytraced trajectory.)
         */
        private final float trajectoryGap = 0.5f;

        /**
         * The scale of each dot in the trajectory diagram (for raytraced trajectory.)
         */
        private final float trajectoryScale = 0.5f;

        private TextureRegion trajectoryTexture;

        private JsonValue gumJV;

        /**
         * The current number of dots
         */
        private int range;

        /**
         * Array of dot positions
         */
        private float[] dotPos;

        /**
         * The highlighted obstacle, if it exists
         */
        protected Unstickable highlighted;

        /**
         * Cache for the start position of the raycast
         */
        private Vector2 originCache;

        /**
         * Cache for the direction vector of the raycast
         */
        private Vector2 directionCache;

        /**
         * Cache for the end position of the raycast
         */
        private Vector2 endCache;

        /**
         * The distance of the gum offset.
         */
        private float offsetDist;

        /**
         * The intersected point of the trajectory raycast.
         */
        private final Vector2 intersect = new Vector2();

        /**
         * The raycast callback used for the trajectory raycast
         */
        private final RayCastCallback trajectoryRay = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point,
                                          Vector2 normal, float fraction) {
                Obstacle ob = (Obstacle) fixture.getBody().getUserData();
                if (!ob.getName().equals("gumProjectile") && !ob.getName().equals("cameratile") && !ob.equals(bandit)) {
                    intersect.set(point);
                    return fraction;
                }
                return -1;
            }
        };

        /**
         * The last intersected obstacle in the unsticking raycast.
         */
        private final Obstacle[] lastCollision = new Obstacle[1];

        /**
         * The raycast callback used for the unsticking raycast.
         * This has a different raycast so the origin can be within the player.
         */
        private final RayCastCallback unstickRay = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point,
                                          Vector2 normal, float fraction) {
                Obstacle ob = (Obstacle) fixture.getBody().getUserData();
                if (!ob.getName().equals("gumProjectile") && !ob.getName().equals("cameratile") &&
                        ob.getFilterData().categoryBits != CATEGORY_COLLECTIBLE && !ob.equals(bandit)) {
                    lastCollision[0] = ob;
                    return fraction;
                }
                return -1;
            }
        };


        /**
         * Get the highlighted unstickable.
         */
        public Unstickable getSelected() {
            return highlighted;
        }

        public AimModel() {
            dotPos = new float[MAX_DOTS * 2];
            directionCache = new Vector2();
            endCache = new Vector2();
            originCache = new Vector2();
        }

        /**
         * Initialize the Aim Model.
         */
        public void initialize(AssetDirectory directory, JsonValue constants) {
            trajectoryTexture = new TextureRegion(directory.getEntry("trajectoryProjectile", Texture.class));
            gumJV = constants.get("gumProjectile");
            offsetDist = (float) Math.sqrt(Math.pow(gumJV.getFloat("offsetX"), 2) + Math.pow(gumJV.getFloat("offsetY"), 2));
        }

        /**
         * Returns the origin of the gum when fired by the player.
         *
         * @param gumJV the JSON Value representing the gum projectile.
         * @return The origin of the projectile of the gum when fired.
         */
        public Vector2 getProjOrigin(JsonValue gumJV, GameCanvas canvas) {
            Vector2 cross = canvas.unproject(PlayerController.getInstance().getCrossHair());
            cross.scl(1 / scale.x, 1 / scale.y);

            cross.x = Math.max(bounds.x, Math.min(bounds.x + bounds.width, cross.x));
            cross.y = Math.max(bounds.y, Math.min(bounds.y + bounds.height, cross.y));

            Vector2 target = cross;

            float offsetX = gumJV.getFloat("offsetX", 0);
            float offsetY = gumJV.getFloat("offsetY", 0);
            offsetY *= bandit.getYScale();

            originCache.set(bandit.getX(), bandit.getY() + offsetY);
            directionCache.set((target.x - originCache.x), (target.y - originCache.y));
            directionCache.nor();
            directionCache.scl(offsetX);

            // Adjust origin of shot based on target pos
            // Rotate around top half of player for gravity pulling down, bottom half for gravity pulling up
            if (directionCache.y * world.getGravity().y < 0) {
                originCache.x += directionCache.x;
            } else {
                originCache.x += (target.x > bandit.getX() ? offsetX : -offsetX);
            }
            originCache.y += directionCache.y;
            return originCache;
        }

        public Vector2 getProjTarget(GameCanvas canvas) {
            Vector2 cross = canvas.unproject(PlayerController.getInstance().getCrossHair());
            cross.scl(1 / scale.x, 1 / scale.y);

            cross.x = Math.max(bounds.x, Math.min(bounds.x + bounds.width, cross.x));
            cross.y = Math.max(bounds.y, Math.min(bounds.y + bounds.height, cross.y));
            return cross;
        }

        /**
         * Update the trajectory
         */
        public void update(GameCanvas canvas, float dt) {
            Vector2 target = PlayerController.getInstance().getCrossHair();
            originCache.set(getProjOrigin(gumJV, canvas)); // Redundant, but just to keep the logic sorted
            directionCache.set((target.x - originCache.x), (target.y - originCache.y));
            directionCache.nor();
            directionCache.scl(bounds.width * 2); // Make sure ray will cover the whole screen
            endCache.set(originCache.x + directionCache.x, originCache.y + directionCache.y); // Find end point of the ray cast

            world.rayCast(trajectoryRay, originCache, endCache);

            directionCache.set(intersect.x - originCache.x, intersect.y - originCache.y);
            int numSegments = (int) (directionCache.len() / trajectoryGap); // Truncate to find number before colliding
            directionCache.nor();
            range = numSegments + 1;
            if (range > MAX_DOTS) range = MAX_DOTS;
            for (int i = 0; i < range; i++) {
                dotPos[2 * i] = originCache.x + (directionCache.x * i * trajectoryGap);
                dotPos[2 * i + 1] = originCache.y + (directionCache.y * i * trajectoryGap);
            }

            // Unsticking raycast
            directionCache.nor();
            // Scoot the origin back inside the bandit (in the direction of the aim).
            originCache.sub(directionCache.x * offsetDist, directionCache.y * offsetDist);

            world.rayCast(unstickRay, originCache, endCache);

            highlighted = null;
            if (lastCollision[0] instanceof Unstickable) {
                if (!(lastCollision[0] instanceof Gummable) || lastCollision[0].getGummed()) {
                    highlighted = (Unstickable) lastCollision[0]; // Only highlight stuck gum or gummables
                }
            }
        }

        /**
         * Draws the path of the projectile using the result of a raycast. Only works for shooting in a straight line (gravity scale of 0).
         *
         * @param canvas The GameCanvas to draw the trajectory on.
         */
        public void drawProjectileRay(GameCanvas canvas) {
            for (int i = 0; i < range; i++) {
                canvas.draw(trajectoryTexture, COLORS[i], trajectoryTexture.getRegionWidth() / 2f, trajectoryTexture.getRegionHeight() / 2f, dotPos[2 * i] * scale.x,
                        dotPos[2 * i + 1] * scale.y, trajectoryTexture.getRegionWidth() * trajectoryScale, trajectoryTexture.getRegionHeight() * trajectoryScale);
            }
        }
    }


    /**
     * Return a reference to all the background objects
     */
    public Array<BackObjModel> getBackgroundObjects() {
        return backgroundObjects;
    }
}