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

import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.FitViewport;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.EffectController;
import edu.cornell.gdiac.bubblegumbandit.controllers.InputController;
import edu.cornell.gdiac.bubblegumbandit.controllers.PlayerController;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.AIController;
import edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph.TiledGraph;
import edu.cornell.gdiac.bubblegumbandit.helpers.Gummable;
import edu.cornell.gdiac.bubblegumbandit.helpers.TiledParser;
import edu.cornell.gdiac.bubblegumbandit.helpers.TiledParser.TileRect;
import edu.cornell.gdiac.bubblegumbandit.helpers.Unstickable;
import edu.cornell.gdiac.bubblegumbandit.models.ReactorModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.*;
import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;
import edu.cornell.gdiac.bubblegumbandit.view.AnimationController;
import edu.cornell.gdiac.bubblegumbandit.view.GameCamera;
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

    // TODO Update this to something more sensible
    public static final float UPDATE_DIST = 15f;

    /** How close to the center of the tile we need to be to stop drifting */
    private static final float DRIFT_TOLER = .2f;

    /** How fast we drift to the tile center when paused */
    private static final float DRIFT_SPEED = 0.325f;

    /** The Box2D world  */
    protected World world;

    /** Map from tile id in worldData to tile in the world. */
    private HashMap<Integer, TileModel> worldTileMap;

    /** Array holding all the tiles in the world. */
    private Array<TileModel> worldTiles;

    /** The boundary of the world */
    protected Rectangle bounds;

    /** The world scale  */
    protected Vector2 scale;

    /** Reference to the character avatar  */
    private BanditModel bandit;

    /** Reference to the goal door */
    private ExitModel goalDoor;

    /** true if the level is in debug mode  */
    private boolean debug;


    /** All AIControllers in the level. */
    private Array<AIController> enemyControllers;

    /** Number of Enemies in the level, including ones that
     * spawn after the orb is collected.*/
    int enemyCount;

    /** Enemies to spawn after the orb gets picked up. */
    private HashSet<EnemyModel> postOrbEnemies;

    public HashSet<EnemyModel> getPostOrbEnemies() {
        return postOrbEnemies;
    }

    //private EffectController gumEffectController;
    private EffectController glassEffectController;
    private EffectController sparkEffectController;

    /** Decision graph for Enemies when gravity is normal. */
    private TiledGraph tiledGraphGravityDown;

    /** Decision graph for Enemies when gravity is flipped. */
    private TiledGraph tiledGraphGravityUp;

    /** The width of the level. */
    private int levelWidth;

    /** The height of the level. */
    private int levelHeight;

    /** The amount of time counted down after the orb is
     *  collected. */
    private float timer;

    /** Reference to the AlarmController. */
    private AlarmController alarms;

    /**  Reference to the aim model. */
    private final AimModel aim;

    /** All background elements in the level.  */
    private Array<BackObjModel> backgroundObjects;

    /** All support tile objects in the level.  */
    private Array<BackgroundTileModel> supportTiles;


    /** All background tile objects in the level */
    private Array<BackgroundTileModel> backgroundTiles;


    /** All objects in the world.  */
    protected PooledList<Obstacle> objects = new PooledList<>();

    /** All flippable objects in the world. */
    protected PooledList<Obstacle> flippableObjects = new PooledList<>();

    /** Position of the collectable orb. */
    private Vector2 orbPosition;

    /** Holds all tutorial wall decor. */
    private Array<TutorialIcon> icons;

    /** represents the reactor around the orb */
    private ReactorModel reactorModel;

    private boolean disableShooting;

    /** Cache for figuring out which tile is hit */
    private Vector2 tileCache = new Vector2();

    /** Holds a reference to all doors in the level. */
    private Array<DoorModel> doors;
    /** Number of total captives in the level */
    private int captiveCount;

    private GameCamera camera;

    private RayHandler rays;
    private HashMap<Light, Obstacle> objLights;

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
        icons = new Array<>();
        captiveCount = 0;
        objLights = new HashMap<>();

    }

    /**
     * Returns this level's AimModel reference.
     *
     * @return the reference to this LevelModel's AimModel
     *         reference.
     */
    public AimModel getAim() {
        return aim;
    }

    /** Returns the total amount of captives in the level */
    public int getCaptiveCount() {return captiveCount; }

    /**
     * Returns an Array of all AIControllers in this level.
     *
     * @return an Array of all AIControllers that exist in this
     * level.
     * */
    public Array<AIController> aiControllers() {
        return enemyControllers;
    }

    /**
     * Returns the bounding rectangle for the physics world.
     * <p>
     * The size of the rectangle is in physics, coordinates,
     * not screen coordinates.
     *
     * @return the bounding rectangle for the physics world.
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Returns the scaling factor to convert physics
     * coordinates to screen coordinates.
     *
     * @return the scaling factor to convert physics
     *         coordinates to screen coordinates.
     */
    public Vector2 getScale() {
        return scale;
    }

    /**
     * Returns a reference to the Box2D World.
     *
     * @return a reference to the Box2D World.
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns a reference to the Bandit's avatar.
     *
     * @return a reference to the Bandit's avatar.
     */
    public BanditModel getBandit() {
        return bandit;
    }

    /**
     * Returns a reference to the exit door.
     *
     * @return a reference to the exit door.
     */
    public ExitModel getExit() {
        return goalDoor;
    }

    /**
     * Returns a PooledList of all flippable objects in this level.
     *
     * @return a PooledList of all flippable objects in this level.
     */
    public PooledList<Obstacle> getFlippables() {
        return flippableObjects;
    }

    /**
     * Returns true if this level is currently in debug mode.
     * <p>
     * If the level is in debug mode, then the physics bodies
     * will all be drawn as wireframes onscreen.
     *
     * @return true if this level is currently in debug node;
     *         otherwise, false.
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Sets whether this level is currently in debug mode
     * <p>
     * If the level is in debug mode, then the physics bodies
     * will all be drawn as wireframes onscreen.
     *
     * @param value whether this level is currently in debug mode.
     */
    public void setDebug(boolean value) {
        debug = value;
    }

    /** Sets off every alarm in this level and deactivates reactor after orb is collected */
    public void startPostOrb(){
        alarms.setAlarms(true);
        if (reactorModel != null) reactorModel.orbCollected(true);
    }

    /**
     * Disables every alarm in this level and resets reactor.
     */
    public void endPostOrb(){
        alarms.setAlarms(false);
        if (reactorModel != null) reactorModel.orbCollected(false);
    }

    /**
     * Returns the TileModel that an object at (x, y) collided with when colliding with WallModel wall
     * @param x the x position of the object
     * @param y the y position of the object
     * @param contactX the x position of the contact
     * @param contactY the y position of the contact
     * @return the TileModel at the collision spot
     */
    public TileModel getTile(float x, float y, float contactX, float contactY) {
        tileCache.set(contactX - x, contactY - y);
        tileCache.scl(0.1f); // Scoot in direction of contact

        // Figure out which tile is there
        int tileX = (int) (contactX + tileCache.x);
        int tileY = (int) (contactY + tileCache.y);
        tileY = levelHeight - tileY - 1;
//        System.out.println("Tile X: " + tileX + " Tile Y: " + tileY + " Contact X: " + contactX + " Contact Y: " + contactY);

        TileModel selectedTile = worldTileMap.get(tileY * levelWidth + tileX);
//        System.out.println("Selected Tile X: " + selectedTile.getX() + " Selected Tile Y: " + (levelHeight - selectedTile.getY() - 1));
        if (selectedTile == null) {
            throw new RuntimeException("The tile was not found successfully. Please tell Ben about this and he will try to fix it");
        }
        return selectedTile;
    }

    /**
     * Lays out the game geography from the given JSON file. Spawns
     * tiles, objects, boards, and other assets that should appear
     * in a Bubblegum Bandit level.
     *
     * @param directory   the asset manager
     * @param levelFormat the JSON file defining the level
     * @param constants   the JSON file defining the constants
     * @param tilesetJson the JSON file defining the tileset
     * @param camera the current game camera
     */
    public void populate(AssetDirectory directory, JsonValue levelFormat, JsonValue constants, JsonValue tilesetJson, boolean disableShooting, GameCamera camera) {
        this.camera = camera;

        //Initializations & Logic
        aim.initialize(directory, constants);
        postOrbEnemies = new HashSet<>();
        HashMap<Vector2, TileModel> tiles = new HashMap<>();
        supportTiles = new Array<>();
        backgroundTiles = new Array<>();
        enemyControllers = new Array<>();
        backgroundObjects = new Array<>();
        this.disableShooting = disableShooting;
        doors = new Array<>();

        JsonValue boardGravityDownLayer = null;
        JsonValue boardGravityUpLayer = null;
        JsonValue terrainLayer = null;
        JsonValue objects = null;
        JsonValue supports = null;
        JsonValue backgroundLayer = null;
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
                    terrainLayer = layer;
                    break;
                case "Objects":
                    objects = layer.get("Objects");
                    break;
                case "Supports":
                    supports = layer;
                    break;
                case "Background":
                    backgroundLayer = layer;
                    break;
                case "PostOrb":
                    postOrb = layer.get("Objects");
                    break;
                case "Corners":
                    //for creating the background
                    break;
                default:
                    throw new RuntimeException("Invalid layer name. Valid names: BoardGravityDown, BoardGravityUp, Terrain, Supports, Background, Corners, and Objects.");
            }
            layer = layer.next();
        }

        if (boardGravityDownLayer == null || boardGravityUpLayer == null || terrainLayer == null || objects == null) {
            throw new RuntimeException("Missing layer data. Should have: BoardGravityDown, BoardGravityUp, Terrain, PostOrb, and Objects.");
        }
        if (postOrb == null) {
            throw new RuntimeException("Missing PostOrb layer.");
        }

        int[] worldData = terrainLayer.get("data").asIntArray();
        float gravity = 0;

        if (levelFormat.get("properties") == null) {
            throw new RuntimeException("Set the level properties [gravity] and [timer] in "
                    + "Map -> Map Properties.");
        }

        JsonValue property = levelFormat.get("properties").child();
        while (property != null) {
            String propName = property.get("name").asString();
            if (propName.equals("gravity")) {
                gravity = property.getFloat("value");
            }
            if (propName.equals("timer")) {
                timer = property.getFloat("value");
            }
            if( propName.equals("captives")) {
                captiveCount = property.getInt("value");
            }
            property = property.next();
        }


        float[] pSize = constants.get("physicsSize").asFloatArray();

        levelWidth = levelFormat.getInt("width");
        levelHeight = levelFormat.getInt("height");
        world = new World(new Vector2(0, gravity), false);
        this.rays = new RayHandler(world);
        bounds = new Rectangle(0, 0, levelWidth, levelHeight);

        scale.x = pSize[0];
        scale.y = pSize[1];

        HashMap<Integer, TextureRegion> textures = TiledParser.createTileset(directory, levelFormat);

        int boardIdOffset = TiledParser.boardIdOffset;

        tiledGraphGravityUp = new TiledGraph(boardGravityUpLayer, boardIdOffset, scale, 3f / 8);
        tiledGraphGravityDown = new TiledGraph(boardGravityDownLayer, boardIdOffset, scale, 2f / 8);

        worldTiles = new Array<>();
        worldTileMap = new HashMap<>();
        // Iterate over each tile in the world and create if it exists
        for (int i = 0; i < worldData.length; i++) {
            int tileVal = worldData[i];
            if (tileVal != 0) {
                TileModel newTile = new TileModel();
                float x = (i % levelWidth) + 0.5f;
                float y = levelHeight - (i / levelWidth) - 0.5f;
                if (textures.get(tileVal) == null)  {
                    throw new RuntimeException("Tile " + (tileVal) + " doesn't have a texture");
                }
                newTile.initialize(textures.get(tileVal), x, y, constants.get("tiles"));
                tiles.put(new Vector2(x, y), newTile);
                newTile.setDrawScale(scale);
                worldTileMap.put(i, newTile);
                worldTiles.add(newTile);
            }
        }

        // Aggregated tiles for seaming fixes.
        TiledParser parser = new TiledParser();
        Array<TileRect> rects = parser.mergeTiles(levelWidth, levelHeight, worldData);
        for (TileRect rect : rects) {
            WallModel newWall = new WallModel();
            newWall.initialize(rect.startX, levelHeight - rect.endY - 1, rect.endX, levelHeight - rect.startY - 1, constants.get("wall"));
            newWall.setDrawScale(scale);
            activate(newWall);
            newWall.setFilter(CATEGORY_TERRAIN, MASK_TERRAIN);
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

        if (backgroundLayer != null) {
            int[] backgroundData =backgroundLayer.get("data").asIntArray();
            // Iterate over each support in the world and create if it exists
            for (int i = 0; i < backgroundData.length; i++) {
                int tileVal = backgroundData[i];
                if (tileVal != 0) {
                    BackgroundTileModel newTile = new BackgroundTileModel();
                    float x = (i % levelWidth) + 0.5f;
                    float y = levelHeight - (i / levelWidth) - 0.5f;
                    newTile.initialize(textures.get(tileVal), x, y, scale);
                    backgroundTiles.add(newTile);

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
        Array<EnemyModel> newEnemies = new Array<>();

        Array<Vector2> reactorPos = new Array<>();

        HashMap<Integer, EnemyModel> enemyIds = new HashMap<>();

        while (object != null) {
            JsonValue objTypeJson = object.get("type");
            String objType;
            if (objTypeJson != null) {
                objType = object.get("type").asString();
            } else { // Template
                objType = object.get("template").asString();
                int substringStart = objType.lastIndexOf("/") + 1;
                int substringEnd = objType.lastIndexOf(".");
                objType = objType.substring(substringStart, substringEnd);
            }
            int objId = (object.getInt("id"));
            int objGid = (object.getInt("gid"));
            boolean isFacingRight = (objGid & (1 << 31)) == 0; // Check if bit 31 of gid is 1
            float x = (object.getFloat("x") + (object.getFloat("width") / 2)) / scale.x;
            float y = levelHeight - ((object.getFloat("y") - (object.getFloat("height") / 2)) / scale.y);
            float decorX = object.getFloat("x")/scale.x;
            float decorY = levelHeight - object.getFloat("y")/scale.y;

            switch (objType) {
                case "tutorial": {
                    int keyCode = object.get("properties").get(0).getInt("value");
                    if(keyCode>8) {
                        System.err.println("Invalid keycode "+keyCode+" accessed by tutorial icon.");
                        break;
                    }
                    if(keyCode>=0) icons.add(new TutorialIcon(directory, decorX, decorY, keyCode, scale));
                    else {
                        String icon = object.get("properties").get(2).getString("value");
                        String text = object.get("properties").get(1).getString("value");
                        icons.add(new TutorialIcon(directory, decorX, decorY, text, icon, scale));
                    }
                    break;
                }
                case "chair":
                    boolean facingRight = (object.getInt("gid") > 0); //??
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
                case "shieldedSmallEnemy":
                    enemyConstants = constants.get("smallEnemy");
                    x = (float) ((int) x + .5);
                    enemy = new ShockEnemyModel(world, enemyCount);
                    enemy.initialize(directory, x, y, enemyConstants, isFacingRight);
                    enemy.setDrawScale(scale);
                    //if shielded add shield - DISABLED for now
//                    if (objType.contains("shielded")) enemy.hasShield(true);
                    newEnemies.add(enemy);
                    enemyIds.put(objId, enemy);
                    break;
                case "mediumEnemy":
                case "shieldedMediumEnemy":
                    enemyConstants = constants.get("mediumEnemy");
                    x = (float) ((int) x + .5);
                    enemy = new RollingEnemyModel(world, enemyCount);
                    enemy.initialize(directory, x, y, enemyConstants, isFacingRight);
                    enemy.setDrawScale(scale);
                    //if shielded add shield
                    enemy.hasShield(true);
                    enemyIds.put(objId, enemy);
                    newEnemies.add(enemy);
                    break;
                case "shieldedLargeEnemy":
                case "largeEnemy":
                    enemyConstants = constants.get("largeEnemy");
                    enemy = new LaserEnemyModel(world, enemyCount);
                    enemy.initialize(directory, x, y, enemyConstants, isFacingRight);
                    enemy.setDrawScale(scale);
                    //if shielded add shield
//                    if (objType.contains("shielded")) enemy.hasShield(true);
                    enemyIds.put(objId, enemy);
                    newEnemies.add(enemy);
                    break;
                case "orb":
                    orbPlaced = true;
                    orbPosition = new Vector2(x,y);
                    Collectible orb = new Collectible();
                    orb.initialize(directory, x, y, scale, constants.get(objType));
                    activate(orb);
                    orb.setFilter(CATEGORY_COLLECTIBLE, MASK_COLLECTIBLE);
                    orb.getFilterData().categoryBits = CATEGORY_COLLECTIBLE;
                    PointLight orbPoint = new PointLight(rays, 20,
                         new Color(.8f, 1, .9f, 0.65f), 4, x,y);
                    orbPoint.attachToBody(orb.getBody());
                    objLights.put(orbPoint, orb);
                    break;
                case "floatingGum":
                    Collectible coll = new Collectible();
                    coll.initialize(directory, x, y, scale, constants.get(objType));
                    activate(coll);
                    coll.setFilter(CATEGORY_COLLECTIBLE, MASK_COLLECTIBLE);
                    coll.getFilterData().categoryBits = CATEGORY_COLLECTIBLE; // Do this for ID purposes
                    break;
                case "star":
                    Captive cap =  new Captive();
                    cap.initialize(directory, x, y, scale, constants.get(objType));
                    activate(cap);
                    cap.setFilter(CATEGORY_COLLECTIBLE, MASK_COLLECTIBLE);
                    cap.getFilterData().categoryBits = CATEGORY_COLLECTIBLE; // Do this for ID purposes
                    break;
                case "doorVLocked":
                case "doorV":
                case "doorH":
                case "doorHLocked":
                    DoorModel door = new DoorModel();
                    boolean isLocked = objType.contains("Locked");
                    JsonValue doorJv = objType.contains("doorH") ? constants.get("doorH") : constants.get("door");
                    door.initialize(directory, x, y, scale, levelHeight, object, doorJv, objType.contains("doorH"), isLocked, enemyIds, camera);
                    activate(door);
                    doors.add(door);
                    break;
                case "alarm":
                    alarmPos.add(new Vector2(decorX, decorY));
                    break;
                case "crushingBlock":
                    CrusherModel crush = new CrusherModel();
                    crush.initialize(directory, scale, x, y, object, constants.get("crushingBlock"));
                    activate(crush);
                    flippableObjects.add(crush);
                    crush.setFixtureMasks(CATEGORY_CRUSHER, CATEGORY_CRUSHER_BOX, MASK_CRUSHER, MASK_CRUSHER_BOX, MASK_TERRAIN);
                    break;
                case "glass":
                    SpecialTileModel glass = new SpecialTileModel();
                    glass.initialize(directory, x, y, scale, object, constants.get("glass"), "glass");
                    activate(glass);
                    glass.setFilter(CATEGORY_TERRAIN, MASK_TERRAIN);
                    glass.setDrawScale(scale);
                    break;
                case "hazard":
                    SpecialTileModel hazard = new SpecialTileModel();
                    hazard.initialize(directory, x, y, scale, object, constants.get("hazard"), "hazard");
                    activate(hazard);
                    hazard.setFilter(CATEGORY_TERRAIN, MASK_TERRAIN);
                    hazard.setDrawScale(scale);
                    break;
                case "reactor":
                    reactorPos.add(new Vector2(decorX, decorY));
                    break;
                default:
                    throw new UnsupportedOperationException(objType + " is not a valid object");
            }
            object = object.next();
        }

        postOrb = postOrb.child();
        while (postOrb != null){
            JsonValue objTypeJson = postOrb.get("type");
            String objType;
            if (objTypeJson != null) {
                objType = postOrb.get("type").asString();
            } else { // Template
                objType = postOrb.get("template").asString();
                int substringStart = objType.lastIndexOf("/") + 1;
                int substringEnd = objType.lastIndexOf(".");
                objType = objType.substring(substringStart, substringEnd);
            }
            int objId = (postOrb.getInt("id"));
            int objGid = (postOrb.getInt("gid"));
            boolean isFacingRight = !((objGid & 0x40000000) == 0x40000000); // Check if bit 30 of gid is 1

            float x = (postOrb.getFloat("x") + (postOrb.getFloat("width") / 2)) / scale.x;
            float y = levelHeight - ((postOrb.getFloat("y") - (postOrb.getFloat("height") / 2)) / scale.y);


            switch (objType) {
                case "smallEnemy":
                case "shieldedSmallEnemy":
                    enemyConstants = constants.get("smallEnemy");
                    x = (float) ((int) x + .5);
                    enemy = new ShockEnemyModel(world, enemyCount);
                    enemy.initialize(directory, x, y, enemyConstants, isFacingRight);
                    enemy.setDrawScale(scale);
                    //if shielded add shield
                    if (objType.contains("shielded")) enemy.hasShield(true);
                    postOrbEnemies.add(enemy);
                    enemyIds.put(objId, enemy);
                    break;
                case "mediumEnemy":
                case "shieldedMediumEnemy":
                    enemyConstants = constants.get("mediumEnemy");
                    x = (float) ((int) x + .5);
                    enemy = new RollingEnemyModel(world, enemyCount);
                    enemy.initialize(directory, x, y, enemyConstants, isFacingRight);
                    enemy.setDrawScale(scale);
                    //if shielded add shield
                    if (objType.contains("shielded")) enemy.hasShield(true);
                    enemyIds.put(objId, enemy);
                    postOrbEnemies.add(enemy);
                    break;
                case "shieldedLargeEnemy":
                case "largeEnemy":
                    enemyConstants = constants.get("largeEnemy");
                    enemy = new LaserEnemyModel(world, enemyCount);
                    enemy.initialize(directory, x, y, enemyConstants, isFacingRight);
                    enemy.setDrawScale(scale);
                    //if shielded add shield
                    if (objType.contains("shielded")) enemy.hasShield(true);
                    enemyIds.put(objId, enemy);
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

        bandit.setOrbPostion(orbPosition);
        activate(goalDoor);
        goalDoor.setFilter(CATEGORY_EXIT, MASK_COLLECTIBLE);

        for (EnemyModel e : newEnemies) {
            activate(e);
            e.setFilter(CATEGORY_ENEMY, MASK_ENEMY);
            enemyControllers.add(new AIController(e, bandit, tiledGraphGravityUp, tiledGraphGravityDown));
            enemyCount++;
        }

        // Add bandit at the end because this affects draw order
        activate(bandit);
        bandit.setFilter(CATEGORY_PLAYER, MASK_PLAYER);

        alarms = new AlarmController(alarmPos, directory, world, rays);

        if (reactorPos.size >= 2) {
            reactorModel = new ReactorModel(reactorPos, orbPosition, directory);
        }

        //gumEffectController = new EffectController("gum",
        //    "splat", directory, true, true, 1);
        glassEffectController = new EffectController("glass", "shatter",
            directory, true, true, 0);
        sparkEffectController = new EffectController("sparks", "sparks",
            directory, true, true, 0.3f);

    }

    //public void makeGumSplat(float x, float y){
    //    gumEffectController.makeEffect(x, y, scale, false);
    //}

    public void makeShatter(float x, float y){
        glassEffectController.makeEffect(x, y, scale, false);
        tiledGraphGravityUp.getNode((int) x, (int) y - 1).disableNode();
        tiledGraphGravityUp.getNode((int) x, (int) y + 1).disableNode();
        tiledGraphGravityDown.getNode((int) x, (int) y - 1).disableNode();
        tiledGraphGravityDown.getNode((int) x, (int) y + 1).disableNode();
    }
    public void makeSpark(float x, float y){
        sparkEffectController.makeEffect(x, y, scale, false);
    }

    /**
     * Spawns all EnemyModels that should drop in after the Bandit picks
     * up the orb.
     * */
    public void spawnPostOrbEnemies(){
        for(EnemyModel e : postOrbEnemies){
            activate(e);
            e.setFilter(CATEGORY_ENEMY, MASK_ENEMY);
            enemyControllers.add(new AIController(e, bandit, tiledGraphGravityUp, tiledGraphGravityDown));
            enemyCount++;
            if (world.getGravity().y > 0){
                e.flipGravity();
            }
        }
    }

    /**
     * Notifies each door in the level that the orb has been collected, in case they are supposed
     * to lock / unlock.
     * */
    public void postOrbDoors() {
        for (DoorModel doorModel : doors) {
            doorModel.postOrb();
        }
    }

    public void dispose() {
        for (Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        icons.clear();
        objects.clear();
        if (world != null) {
            world.dispose();
            world = null;
            alarms.dispose();
            reactorModel = null;
        }
        objLights.clear();
        captiveCount = 0;
    }

    public int getTotalCaptives() {
        return captiveCount;
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
            // TODO: Add custom state for dead enemies
//            adjustForDrift(controller.getEnemy());
            EnemyModel enemy = controller.getEnemy();
            if (!enemy.isRemoved()) {
                float xDiff = (enemy.getX() - bandit.getX());
                float yDiff = (enemy.getY() - bandit.getY());
                double distFromPlayer = Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
                if (distFromPlayer < UPDATE_DIST) {
                    controller.getEnemyStateMachine().update();
                }
                // if not updating set next action to no action
                else{
                    enemy.setNextAction(InputController.CONTROL_NO_ACTION);
                }
            }
        }
        Array<Light> garbage = new Array<>();
        for(Light light : objLights.keySet()) {

            if (objLights.get(light).isRemoved()) {
                light.remove();
                garbage.add(light);
                System.out.println("was removed");
            }
        }
        for(Light light: garbage) {
            objLights.remove(light);
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
        iterator = flippableObjects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<Obstacle>.Entry entry = iterator.next();
            Obstacle obj = entry.getValue();
            if (obj.isRemoved()) {
                entry.remove();
            }
        }
        if (bandit.shouldSpark()) {
            makeSpark(bandit.getX(), bandit.getY());
        }

        alarms.update();
        if (reactorModel != null) reactorModel.update();

        glassEffectController.update();
        sparkEffectController.update();

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
            gumProjectile, TextureRegion laserBeam, TextureRegion laserBeamEnd,
                     float dt) {
        canvas.begin();

        bandit.setFacingDirection(getAim().getProjTarget(canvas).x);

        for(BackgroundTileModel tile: backgroundTiles) {
            tile.draw(canvas);
        }

        for (TileModel tile : worldTiles) {
            tile.draw(canvas);
        }

        alarms.drawAlarms(canvas, scale);
        for(TutorialIcon icon: icons) icon.draw(canvas);

        for(BackgroundTileModel tile: supportTiles) {
            tile.draw(canvas);
        }

        if (reactorModel != null){
            reactorModel.draw(canvas);
        }

        bandit.setFacingDirection(getAim().getProjTarget(canvas).x);

        for (Obstacle obj : objects) {
            obj.draw(canvas);
            if (obj.equals(aim.highlighted)) { // Probably inefficient, but the draw order needs to be maintained.
                aim.highlighted.drawWithOutline(canvas);
            }

        }
        drawChargeLasers(laserBeam, laserBeamEnd, canvas);

        if(bandit.getHealth()>0 && !disableShooting) aim.drawProjectileRay(canvas);
       // gumEffectController.draw(canvas);
        glassEffectController.draw(canvas);
        sparkEffectController.draw(canvas);

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
        FitViewport viewport = canvas.getUIViewport();
        GameCamera camera = canvas.getCamera();
        Matrix4 box2dcombined = camera.combined.cpy();
        box2dcombined.scl(scale.x);
        rays.setCombinedMatrix(box2dcombined, camera.position.x / scale.x, camera.position.y / scale.y,
            camera.viewportWidth * camera.zoom / scale.x, camera.viewportHeight * camera.zoom / scale.y);
        int bufferScale = Math.round(Gdx.graphics.getBackBufferScale());
        rays.useCustomViewport((viewport.getScreenX() * bufferScale), viewport.getScreenY() * bufferScale, viewport.getScreenWidth() * bufferScale, viewport.getScreenHeight() * bufferScale);
        rays.render();
    }

    public void drawChargeLasers(TextureRegion beam, TextureRegion beamEnd, GameCanvas canvas) {

        //Local variables to scale our laser depending on its phase.
        final float chargeLaserScale = .75f;
        final float lockedLaserScale = .9f;
        final float firingLaserScale = 1.5f;


        for (AIController ai : enemyControllers) {
            if (ai.getEnemy() instanceof LaserEnemyModel) {
                //Don't draw inactive lasers.
                LaserEnemyModel enemy = (LaserEnemyModel) ai.getEnemy();
                if (enemy.isCrushing()) continue;
                if(enemy.inactiveLaser()) continue;

                //Don't draw if enemy can't see.
                if(enemy.chargingLaser() && !enemy.canSeeBandit(getBandit())) continue;


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
                Vector2 beamStartPos = enemy.getBeamOrigin();
                Vector2 dir = new Vector2(
                        intersect.x - beamStartPos.x,
                        intersect.y - beamStartPos.y
                );

                beam.setRegionWidth(1);
                int numSegments = (int)((dir.len() * scale.x) / beam.getRegionWidth());
                dir.nor();

                //Offset calculations to match the animation.

                float laserEyeNormalOffsetXLeft = 41 + canvas.getShadowOffset();
                float laserEyeNormalOffsetYLeft = 20;
                float laserEyeJettedOffsetXRight = 41 + canvas.getShadowOffset();
                float laserEyeJettedOffsetYRight = 20;
                float jetBoostY = 3;

                boolean jetted = enemy.getCurrentFrameNum() > 0;

                Array<Float> xScales = enemy.getRandomXScale();
                Array<Float> yScales = enemy.getRandomYScale();

                //Draw her up!
                for(int i = 0; i < numSegments; i++){

                    //Calculate the positions and angle of the charging laser.
                    float enemyOffsetX = enemy.getFaceRight()? laserEyeJettedOffsetXRight : laserEyeNormalOffsetXLeft;
                    float enemyOffsetY = enemy.getFaceRight()? laserEyeJettedOffsetYRight : laserEyeNormalOffsetYLeft;
                    if(!jetted) enemyOffsetY += jetBoostY;
                    if(!enemy.getFaceRight()) enemyOffsetX = -enemyOffsetX;


                    float x = enemy.getPosition().x * scale.x + (i * dir.x * beam.getRegionWidth());
                    float y = enemy.getPosition().y * scale.y + (i * dir.y * beam.getRegionWidth());
                    float scaleX = 1f;
                    float scaleY = laserThickness;
                    float ang = (float) Math.atan2(dir.y, dir.x);

                    //Vibrations
                    scaleX *= xScales.get(i % xScales.size);
                    scaleY *= yScales.get(i % yScales.size);

                    canvas.draw(
                            beam,
                            laserColor,
                            beam.getRegionWidth()/2f,
                            beam.getRegionHeight()/2f,
                            x + enemyOffsetX,
                            y + (enemyOffsetY * enemy.getYScale()),
                            ang,
                            scaleX,
                            scaleY);


                    if(i == 0){
                        if(!beamEnd.isFlipX()) beamEnd.flip(true, false);
                        canvas.draw(
                                beamEnd,
                                laserColor,
                                beamEnd.getRegionWidth()/2f,
                                beamEnd.getRegionHeight()/2f,
                                x + enemyOffsetX,
                                y+ (enemyOffsetY * enemy.getYScale()),
                                ang,
                                scaleX,
                                scaleY);

                    }

                    if(i == numSegments - 1){
                        if(beamEnd.isFlipX()) beamEnd.flip(true, false);
                        canvas.draw(
                                beamEnd,
                                laserColor,
                                beamEnd.getRegionWidth()/2f,
                                beamEnd.getRegionHeight()/2f,
                                x + enemyOffsetX,
                                y+ (enemyOffsetY * enemy.getYScale()),
                                ang,
                                scaleX,
                                scaleY);
                    }
                }
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

    public void remakeOrb(AssetDirectory directory, JsonValue constants){
        Collectible coll = new Collectible();
        coll.initialize(directory, orbPosition.x, orbPosition.y, scale, constants.get("orb"));
        activate(coll);
        coll.setFilter(CATEGORY_COLLECTIBLE, MASK_COLLECTIBLE);
        coll.getFilterData().categoryBits = CATEGORY_COLLECTIBLE; // Do this for ID purposes
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
                 if (!canUnstickThrough.contains(ob.getName()) && !ob.getName().equals("door") && !ob.equals(bandit)) {
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

        private HashSet<java.lang.String> canUnstickThrough = new HashSet<>();

        /**
         * The raycast callback used for the unsticking raycast.
         * This has a different raycast so the origin can be within the player.
         */
        private final RayCastCallback unstickRay = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point,
                                          Vector2 normal, float fraction) {
                Obstacle ob = (Obstacle) fixture.getBody().getUserData();
                if (!ob.equals(bandit) && ob.getFilterData().categoryBits != CATEGORY_COLLECTIBLE && !ob.getName().equals("gumProjectile")) {
                    if (canUnstickThrough.contains(ob.getName())) {
                        return -1;
                    }
                    if (ob instanceof CrusherModel && ob.getStuck() && !ob.getGummed()) {
                        return -1;
                    }
                    if (fixture.getUserData() instanceof DoorModel) {
                        return -1;
                    }
                    if (ob instanceof DoorModel && ((DoorModel) ob).isOpen()) {
                        return -1;
                    }
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
            canUnstickThrough.add("projectile");
            canUnstickThrough.add("hazard");
            canUnstickThrough.add("exit");
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
         * Draws the path of the projectile using the result of a raycast.
         * Only works for shooting in a straight line (gravity scale of 0).
         *
         * @param canvas The GameCanvas to draw the trajectory on.
         */
        public void drawProjectileRay(GameCanvas canvas) {
            for (int i = 0; i < range; i++) {
                canvas.draw(trajectoryTexture, COLORS[i],
                    trajectoryTexture.getRegionWidth() / 2f,
                    trajectoryTexture.getRegionHeight() / 2f, dotPos[2 * i] * scale.x,
                        dotPos[2 * i + 1] * scale.y,
                    trajectoryTexture.getRegionWidth() * trajectoryScale,
                    trajectoryTexture.getRegionHeight() * trajectoryScale);
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