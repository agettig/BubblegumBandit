package edu.cornell.gdiac.bubblegumbandit.models.level;

import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.CATEGORY_DOOR;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;

import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.helpers.Gummable;
import edu.cornell.gdiac.bubblegumbandit.helpers.Unstickable;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCamera;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import java.lang.reflect.Field;
import java.util.HashMap;

public class DoorModel extends TileModel implements Gummable {

    /** Whether this is a horizontal camera tile or a vertical camera tile. */
    private boolean isHorizontal;

    /** Upper left point of the camera after exiting bottom / left of camera tile. */
    private Vector2 firstUpperLeft;

    /** Lower right point of the camera after exiting bottom / left of camera tile. */
    private Vector2 firstLowerRight;

    /** Upper left point of the camera after exiting top / right of camera tile. */
    private Vector2 secondUpperLeft;

    /** Lower right point of the camera after exiting top / right of camera tile. */
    private Vector2 secondLowerRight;

    /** Whether the camera tile first mode fixes the x axis */
    private boolean isFirstFixedX;

    /** Whether the camera tile first mode fixes the y axis */
    private boolean isFirstFixedY;

    /** Whether the camera tile second mode fixes the x axis */
    private boolean isSecondFixedX;

    /** Whether the camera tile second mode the y axis */
    private boolean isSecondFixedY;

    /** Reference to the game camera for sound purposes */
    private GameCamera camera;

    /** Gets the value of isHorizontal */
    public boolean isHorizontal() {
        return isHorizontal;
    }

    /** Gets the value of isFirstFixedX */
    public boolean isFirstFixedX() {
        return isFirstFixedX;
    }

    /** Gets the value of isFirstFixedY */
    public boolean isFirstFixedY() {
        return isFirstFixedY;
    }

    /** Gets the value of isSecondFixedX */
    public boolean isSecondFixedX() {
        return isSecondFixedX;
    }

    /** Gets the value of isSecondFixedY */
    public boolean isSecondFixedY() {
        return isSecondFixedY;
    }

    /** Gets the value of firstUpperLeft */
    public Vector2 getFirstUpperLeft() {
        return firstUpperLeft;
    }

    /** Gets the value of firstLowerRight */
    public Vector2 getFirstLowerRight() {
        return firstLowerRight;
    }

    /** Gets the value of secondUpperLeft */
    public Vector2 getSecondUpperLeft() {
        return secondUpperLeft;
    }

    /** Gets the value of secondLowerRight */
    public Vector2 getSecondLowerRight() {
        return secondLowerRight;
    }

    /** Sensor info to detect when someone is in range of a door*/
    private Fixture sensorFixture;
    private PolygonShape sensorShape;
    private Color sensorColor;

    /** Whether the door is open. */
    private boolean isOpen;

    /** The obstacles in the range of the door. */
    private final ObjectSet<Obstacle> obsInRange;

    /** Whether the player has passed through the door. */
    public boolean playerPassed;

    /** Whether the player is in range of the door. */
    public boolean playerInRange;

    /** Whether the door is locked */
    private boolean isLocked;

    /** The texture for the locked door. */
    private TextureRegion lockedTexture;

    /** The texture for a gummed door. */
    private TextureRegion gummedTexture;

    /** The texture for the outline of the door */
    private TextureRegion outlineTexture;

    /** The ids of the enemies required to unlock the door */
    private ObjectSet<Integer> enemyIds;

    /** The map from enemy ids to enemy object */
    private HashMap<Integer, EnemyModel> enemyMap;

    /** The height of the texture */
    private int textureHeight;

    /** The width of the texture */
    private int textureWidth;

    /** How open the door is */
    private float openFraction;

    /** How fast the door opens */
    private float doorOpenRate;

    /** Whether the door locks when the orb is collected. */
    private boolean locksOnOrb;

    /** Whether the door unlocks when the orb is collected. */
    private boolean unlocksOnOrb;

    /** Whether the orb has been collected. */
    private boolean postOrb;

    private boolean locksOnPlayerPass;

    /** Constructs a new DoorModel
     * Uses generic values to start
     */
    public DoorModel() {
        super();
        firstUpperLeft = new Vector2();
        firstLowerRight = new Vector2();
        secondUpperLeft = new Vector2();
        secondLowerRight = new Vector2();
        isOpen = false;
        playerPassed = false;
        playerInRange = false;
        obsInRange = new ObjectSet<>();
        enemyIds = new ObjectSet<>();
        collidedObs = new ObjectSet<>();
        openFraction = 0;

        locksOnOrb = false;
        unlocksOnOrb = false;
        postOrb = false;
        locksOnPlayerPass = false;
    }

    /**
     * Returns whether the door is open
     * @return whether the door is open
     */
    public boolean isOpen() {
        return isOpen;
    }

    public boolean isLocked() { return isLocked;}

    /** Initializes the door model with the camera change information.
     *
     * @param directory the asset directory of the level
     * @param x the x position of the camera tile
     * @param y the y position of the camera tile
     * @param scale the scale of the level
     * @param levelHeight the height of the level
     * @param objectJson the json value representing the camera tile
     * @param constants the json value representing the constants of the camera tile
     * @param isHorizontal whether the door is horizontal
     * @param enemyMap hashmap mapping enemy ids to actual enemies
     */
    public void initialize(AssetDirectory directory, float x, float y, Vector2 scale, float levelHeight, JsonValue objectJson, JsonValue constants, boolean isHorizontal, boolean isLocked, HashMap<Integer, EnemyModel> enemyMap, GameCamera camera) {
        // make the body fixture into a sensor
        setName(isHorizontal ? "doorH" : "door");

        setPosition(x,y);
        float width = objectJson.getFloat("width") / scale.x;
        float height = objectJson.getFloat("height") / scale.y;
        setDimension(width, height);
        setSensor(false);
        this.isHorizontal = isHorizontal;

        setBodyType(BodyDef.BodyType.StaticBody);

        // Reflection is best way to convert name to color
        Color debugColor;
        try {
            String cname = constants.get("debugcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color)field.get(null));
        } catch (Exception e) {
            debugColor = null; // Not defined
        }
        int opacity = constants.get("debugopacity").asInt();
        debugColor.mul(opacity/255.0f);
        setDebugColor(debugColor);

        String key = constants.get("texture").asString();
        TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));
        setTexture(texture);
        textureHeight = texture.getRegionHeight();
        textureWidth = texture.getRegionWidth();

        key = constants.get("lockedTexture").asString();
        texture = new TextureRegion(directory.getEntry(key, Texture.class));
        lockedTexture = texture;

        key = constants.get("gummedTexture").asString();
        texture = new TextureRegion(directory.getEntry(key, Texture.class));
        gummedTexture = texture;

        key = constants.get("outlineTexture").asString();
        texture = new TextureRegion(directory.getEntry(key, Texture.class));
        outlineTexture = texture;

        // Initialize the sensors used to detect when things are being crushed.
        // Get the sensor information
        Vector2 sensorCenter = new Vector2(0, 0);
        float[] sSize = constants.get("sensorsize").asFloatArray();
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(sSize[0], sSize[1], sensorCenter, 0.0f);

        // Reflection is best way to convert name to color
        try {
            String cname = constants.get("sensorcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            sensorColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            sensorColor = null; // Not defined
        }
        opacity = constants.get("sensoropacity").asInt();
        sensorColor.mul(opacity / 255.0f);

        doorOpenRate = constants.get("doorOpenRate").asFloat();

        // Set object data
        isFirstFixedX = false;
        isFirstFixedY = false;
        isSecondFixedX = false;
        isSecondFixedY = false;

        // I'm sorry about this.
        JsonValue property = objectJson.get("properties").child();
        while (property != null) {
            String name = property.getString("name");
                float value = property.getFloat("value");
                if (value != -1) {
                    float yValue = levelHeight - value - 1;
                    switch (name) {
                        case "bottomx1":
                        case "leftx1":
                            firstUpperLeft.x = value;
                            isFirstFixedX = true;
                            break;
                        case "lefty1":
                        case "bottomy1":
                            firstUpperLeft.y = yValue + 1;
                            isFirstFixedY = true;
                            break;
                        case "leftx2":
                        case "bottomx2":
                            firstLowerRight = new Vector2();
                            firstLowerRight.x = value + 1f;
                            break;
                        case "lefty2":
                        case "bottomy2":
                            firstLowerRight.y = yValue;
                            break;
                        case "rightx1":
                        case "topx1":
                            secondUpperLeft = new Vector2();
                            secondUpperLeft.x = value;
                            isSecondFixedX = true;
                            break;
                        case "righty1":
                        case "topy1":
                            secondUpperLeft.y = yValue + 1;
                            isSecondFixedY = true;
                            break;
                        case "rightx2":
                        case "topx2":
                            secondLowerRight = new Vector2();
                            secondLowerRight.x = value + 1f;
                            break;
                        case "righty2":
                        case "topy2":
                            secondLowerRight.y = yValue;
                            break;
                        case "lockOnOrb":
                            locksOnOrb = (value == 1);
                           // System.out.println("Locks on orb: " + locksOnOrb);
                            break;
                        case "unlockOnOrb":
                            unlocksOnOrb = (value == 1);
                          //  System.out.println("Unlocks on orb: " + unlocksOnOrb);
                            break;
                        case "lockOnPlayerPass":
                            break;
                        default:
                            if (name.contains("enemy")) {
                                enemyIds.add((int) value);
                            } else {
                                throw new UnsupportedOperationException(
                                        name + " is not a valid property for a door");
                            }
                    }
                }
            property = property.next();
        }
        setDrawScale(scale);

        this.isLocked = isLocked;
        this.enemyMap = enemyMap;
        this.camera = camera;

        assert !(locksOnOrb && unlocksOnOrb);
    }

    /** Activates physics and sets up sensors / filters for the door */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = getDensity();
        sensorDef.isSensor = true;
        sensorDef.shape = sensorShape;
        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(this);

        setFilter(CATEGORY_DOOR, CollisionController.MASK_SENSOR); // Sets everything's filter, including sensor
        Filter filter = body.getFixtureList().get(0).getFilterData();
        filter.maskBits = CollisionController.MASK_DOOR;
        body.getFixtureList().get(0).setFilterData(filter);

        return true;
    }

    /**
     * Adds an obstacle in the range of the door's sensor.
     * Opens the door.
     *
     * @param ob the obstacle in the range of the door*/
    public void addObInRange(Obstacle ob) {
        obsInRange.add(ob);
    }

    /**
     * Removes an obstacle in the range of the door's sensor.
     * Closes the door, if there are no obstacles in range.
     *
     * @param ob the obstacle no longer in the range of the door*/
    public void removeObInRange(Obstacle ob) {
        obsInRange.remove(ob);
    }

    /**
     * Tries to unlock the door by checking if each of the required enemies is stuck
     */
    private void tryUnlockDoor() {
        boolean allDead = true;
        for (Integer id : enemyIds) {
            EnemyModel cur = enemyMap.get(id);
            if (cur == null || (!cur.getStuck() && !cur.getGummed() && !cur.isRemoved())) {
                allDead = false;
            }
        }
        if (allDead) {
            isLocked = false;
            SoundController.playSound("unlockDoor", 1);
        }
    }

    /**
     * Starts opening the door if it is not locked or gummed
     */
    private void openDoor() {
        if (!isLocked && !gummed) {
            if (camera.isOnScreen(getX() * drawScale.x, getY() * drawScale.y)) {
                SoundController.playSound("doorSound", 0.5f);
            }
            isOpen = true;
            body.getFixtureList().get(0).setSensor(true);
        }
    }

    /**
     * Starts closing the door if it is not gummed
     */
    private void closeDoor() {
        if (!gummed) {
            if (camera.isOnScreen(getX() * drawScale.x, getY() * drawScale.y)) {
                SoundController.playSound("doorSound", 0.5f);
            }
            isOpen = false;
            body.getFixtureList().get(0).setSensor(false);
        }
    }

    /**
     * Changes the door to its post door state.
     */
    public void postOrb() {
        postOrb = true;
        if (locksOnOrb) {
            isLocked = true;
        } else if (unlocksOnOrb) {
            isLocked = false;
        }
    }

    /**
     * Updates the door, opening it or closing it as needed.
     * @param dt Timing values from parent loop
     */
    public void update(float dt) {
        super.update(dt);

        if (locksOnPlayerPass && playerPassed) {
            if (!playerInRange) {
                closeDoor();
                if (openFraction <= 0){
                    isLocked = true;
                }
            }
            if (!isOpen && openFraction > 0){
                openFraction -= doorOpenRate;
            }
        } else {
            // Update how open the door is
            if (isOpen && openFraction < 1) {
                openFraction += doorOpenRate;
            } else if (!isOpen && openFraction > 0){
                openFraction -= doorOpenRate;
            }
            if (openFraction > 1) {
                openFraction = 1;
            } else if (openFraction < 0) {
                openFraction = 0;
            }
            if (isLocked && !(postOrb && locksOnOrb)) {
                tryUnlockDoor();
            }
            if (playerPassed || isHorizontal) {
                if (obsInRange.size == 0 && isOpen) {
                    closeDoor();
                } else if (obsInRange.size > 0 && !isOpen) {
                    openDoor();
                }
            } else {
                for (Obstacle ob : obsInRange) {
                    if (ob instanceof CrusherModel) {
                        openDoor();
                        playerPassed = true;
                    }
                }
                if (!playerInRange && isOpen) {
                    closeDoor();
                } else if (playerInRange && !isOpen) {
                    openDoor();
                }
            }
        }
    }

    /**
     * Draws the door based on its current status
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float halfHeight = textureHeight / 2f;
        float halfWidth = textureWidth / 2f;
        if (!isLocked) {
            if (isHorizontal) {
                texture.setRegionWidth((int) ((1 - openFraction) * textureWidth));
                float offsetX = textureWidth - texture.getRegionWidth();
                canvas.draw(texture, Color.WHITE, origin.x, origin.y,
                        getX()*drawScale.x-offsetX - halfWidth, getY()*drawScale.y, (float) (getAngle()+Math.PI), 1, 1);
                canvas.draw(texture, Color.WHITE, origin.x, origin.y,
                        getX()*drawScale.x + offsetX +halfWidth, getY()*drawScale.y, getAngle(), 1, 1);
            } else {
                texture.setRegionHeight((int) ((1 - openFraction) * textureHeight));
                canvas.drawWithShadow(texture, Color.WHITE, origin.x, origin.y,
                        getX()*drawScale.x, getY()*drawScale.y+halfHeight, (float) (getAngle()+Math.PI), 1, 1);
                canvas.drawWithShadow(texture, Color.WHITE, origin.x, origin.y,
                        getX()*drawScale.x, getY()*drawScale.y-halfHeight, getAngle(), 1, 1);
            }
        } else if (!isOpen) {
            if (isHorizontal) {
                canvas.draw(lockedTexture, Color.WHITE, origin.x, origin.y,
                        getX()*drawScale.x-halfWidth, getY()*drawScale.y, getAngle(), 1, 1);
            } else {
                canvas.drawWithShadow(lockedTexture, Color.WHITE, origin.x, origin.y,
                        getX()*drawScale.x, getY()*drawScale.y-halfHeight, getAngle(), 1, 1);
            }
        }
        if(!isOpen && gummed) {
            canvas.draw(gummedTexture, Color.WHITE, 0f, .5f,
                    getX()*drawScale.x-gummedTexture.getRegionWidth()/2f,
                    getY()*drawScale.y-gummedTexture.getRegionHeight()/2f, getAngle(), 1, 1);
        }
    }

    /**
     * Draws the door with an outline around the gum based on its current status
     * @param canvas Drawing context
     */
    @Override
    public void drawWithOutline(GameCanvas canvas) {
        float halfHeight = texture.getRegionHeight() / 2f;
        float halfWidth = texture.getRegionWidth() / 2f;
        if (!isLocked) {
            if (isHorizontal) {
                texture.setRegionWidth(textureWidth);
                canvas.draw(texture, Color.WHITE, origin.x, origin.y,
                        getX()*drawScale.x- halfWidth, getY()*drawScale.y, (float) (getAngle()+Math.PI), 1, 1);
                canvas.draw(texture, Color.WHITE, origin.x, origin.y,
                        getX()*drawScale.x +halfWidth, getY()*drawScale.y, getAngle(), 1, 1);

            } else {
                texture.setRegionHeight(textureHeight);
                canvas.drawWithShadow(texture, Color.WHITE, origin.x, origin.y,
                        getX()*drawScale.x, getY()*drawScale.y+halfHeight, (float) (getAngle()+Math.PI), 1, 1);
                canvas.drawWithShadow(texture, Color.WHITE, origin.x, origin.y,
                        getX()*drawScale.x, getY()*drawScale.y-halfHeight, getAngle(), 1, 1);
            }
        } else {
            if (isHorizontal) {
                canvas.draw(lockedTexture, Color.WHITE, origin.x, origin.y,
                        getX()*drawScale.x - halfWidth, getY()*drawScale.y, getAngle(), 1, 1);
            } else {
                canvas.drawWithShadow(lockedTexture, Color.WHITE, origin.x, origin.y,
                        getX()*drawScale.x, getY()*drawScale.y - halfHeight, getAngle(), 1, 1);
            }
        }
        canvas.draw(outlineTexture, Color.WHITE, 0f, 0f,
            getX()*drawScale.x-5-gummedTexture.getRegionWidth()/2,
            getY()*drawScale.y-5-gummedTexture.getRegionHeight()/2, getAngle(), 1, 1);
    }
}
