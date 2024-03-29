/*
 * BanditModel.java
 *
 * This is a refactored version of DudeModel that allows us to read its properties
 * from a JSON file.  As a result, it has a lot more getter and setter "hooks" than
 * in lab.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * JSON version, 3/2/2016
 */
package edu.cornell.gdiac.bubblegumbandit.models.player;

import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.CATEGORY_CRUSHER;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.CATEGORY_CRUSHER_BOX;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.MASK_CRUSHER;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.MASK_CRUSHER_BOX;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.MASK_CRUSHER_BOX_NO_PLAYER;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.MASK_TERRAIN;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.BubblegumController;
import edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController;
import edu.cornell.gdiac.bubblegumbandit.controllers.EffectController;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.helpers.Damage;
import edu.cornell.gdiac.bubblegumbandit.models.level.CrusherModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.DoorModel;
import edu.cornell.gdiac.bubblegumbandit.models.level.ShockModel;
import edu.cornell.gdiac.bubblegumbandit.controllers.InputController;
import edu.cornell.gdiac.bubblegumbandit.models.level.SpecialTileModel;
import edu.cornell.gdiac.bubblegumbandit.view.AnimationController;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.BoxObstacle;
import edu.cornell.gdiac.physics.obstacle.CapsuleObstacle;

import edu.cornell.gdiac.physics.obstacle.Obstacle;
import java.lang.reflect.Field;
import org.w3c.dom.Text;

/**
 * Player avatar for the plaform game.
 * <p>
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.
 */
public class BanditModel extends CapsuleObstacle {

    /** The time the player can't move after knockback */
    private final float STUN_TIME = 0.3f;
    // Physics constants
    /**
     * The factor to multiply by the input
     */
    private float force;
    /**
     * The amount to slow the character down
     */
    private float damping;
    /**
     * The maximum character speed
     */
    private float maxspeed;

    /**
     * The current horizontal movement of the character
     */
    private float movement;
    /**
     * Which direction is the character facing
     */
    private boolean faceRight;
    /**
     * Whether our feet are on the ground
     */
    private boolean isGrounded;

    /** Knockback timer */
    private float knockbackTimer;

    // SENSOR FIELDS
    /**
     * Ground sensor to represent our feet
     */
    private Fixture bottomSensorFixture;
    private PolygonShape bottomSensorShape;
    /**
     * The name of the sensor for detection purposes
     */
    private String bottomSensorName;
    /**
     * The color to paint the sensor in debug mode
     */
    private Color bottomSensorColor;

    /**
     * Top sensor to represent our feet when flipped
     */
    private Fixture topSensorFixture;
    private PolygonShape topSensorShape;
    /**
     * The name of the sensor for detection purposes
     */
    private String topSensorName;
    /**
     * The color to paint the sensor in debug mode
     */
    private Color topSensorColor;

    /**
     * Cache for internal force calculations
     */
    private Vector2 forceCache = new Vector2();

    private Texture reloadSymbol;

    private int healthCountdown;

    private final int HEALTH_REGEN_COOLDOWN = 180;

    /**
     * Whether we are actively shooting
     */
    private boolean isShooting;

    /**
     * Whether the player has flipped in the air
     */
    private boolean hasFlipped;

    /**
     * How long until we can shoot again
     */
    private int shootCooldown;

    /**
     * Cooldown (in animation frames) for shooting
     */
    private final int shotLimit = 30;

    /**
     * Cache for flipping player orientation
     */
    private float angle;

    /**
     * Animation controller for controlling animations??
     */
    private AnimationController animationController;

    /**
     * Number of ticks since game started
     */
    private long ticks;

    /** The y scale for this player (used for flip effect) */
    private float yScale;

    /** used to decide between backpedal/run */
    private boolean backpedal;


    /**
     * Camera target for player
     */
    private final Vector2 cameraTarget;

    /** The timer for deciding to start the AFK animation */
    private float AFKtimer;

    /** The time until the AFK animation plays */
    private float AFKwait = 8f;


    /**
     * Whether the player has collected the orb.
     */
    private boolean orbCollected;
    /**
     * Returns the number of stars that have been collected
     */
    private int numStars;

    private boolean isKnockback;

    /** Reference to PoofController, which renders player particle effects */
    private EffectController poofController;


    private Vector2 orbPostion;

    /**
     * Amount of time to stun player
     *
     * Non-positive stunTime means player is not stunned
     * */
    private int stunTime = 0;


    /* Gum variables */
    /** The current amount of gum ammo of the player. */
    private int gumAmmo;

    private int startingGum;


    /**Maximum allowed gum for the level*/
    private int maxGum;

    /** The shock obstacles currently colliding with the player */
    private ObjectSet<Fixture> shockFixtures;

    /** The current frame of the animation */
    private TextureRegion curFrame;

    private boolean atDoor;

    public void setOrbPostion(Vector2 orbPostion){
        assert orbPostion != null;
        this.orbPostion = orbPostion;
    }
    public void respawnPlayer(){
        setX(orbPostion.x - 1);
        setY(orbPostion.y);
        orbCollected = false;
        healPlayer(100);
    }

    /** Whether or  not the player was just hit and is in cooldown*/
    private boolean inCooldown;

    /** Returns whether or not the player is in cooldown*/
    public boolean getCooldown() {return inCooldown; }

    /** Sets the players cooldwon status*/
    public void startCooldown() {
        this.inCooldown = true;
        ticks = 0;
    }

    /**
     * Returns the camera target for the player.
     * <p>
     * This is based on the player's position, velocity, and gravity.
     *
     * @return the camera target for the player
     */
    public Vector2 getCameraTarget() {
        return cameraTarget;
    }


    /**
     * The max amount of health the player can have
     */
    private final float MAX_HEALTH = 100;

    /**
     * The current amount of health the player has
     */
    private float health;

    /**
     * Whether the bandit should be sparking this frame.
     */
    private boolean shouldSpark;

    /** The current crusher on the bandit */
    private CrusherModel crusher = null;

    /** Whether the bandit is being crushed */
    private boolean isCrushing;

    /** Draw scale for crush anim */
    private float crushScale;

    /** ref to the box2d world */
    private World world;

    /** whether to show the victory pose */
    private boolean victory;

    /** whether the bandit should not be damaged anymore */
    private boolean invulnerable;

    /** The texture for the win pose */
    private TextureRegion victoryText;

    /**
     * Whether the player has flipped in the air.
     */
    public boolean hasFlipped() {
        return hasFlipped;
    }

    /**
     * Returns the player's current health for the HUD
     */
    public float getHealth() {
        return health;
    }

    /**
     * Returns the player's max health for the HUD
     */
    public float getMaxHealth() {
        return MAX_HEALTH;
    }

    public boolean isKnockback() {
        return isKnockback;
    }

    public void setKnockback(boolean knockback, boolean shock) {
        isKnockback = knockback;
        if (invulnerable) return;
        System.out.println("knock");
        if( health>0) {
           if(!shock) animationController.setAnimation("knock", false, false);
           else {
               animationController.setAnimation("shock", false, false);
               SoundController.playSound("banditShock", 0.75f);
           }
       }
        knockbackTimer = STUN_TIME;
    }

    public boolean isStunned(){ return stunTime>0;}

    public void setKnockback(boolean knockback) {
        isKnockback = knockback;
        if(knockback && health>0) {
            animationController.setAnimation("knock", false, false);
        }
       knockbackTimer = STUN_TIME;
    }

    public void setAnimation(String anim, boolean isLooping, boolean ending) {
        animationController.setAnimation(anim, isLooping, ending);
    }


    /**
     * Decreases the player's health if not in cooldown. Returns whether the player was hit
     *
     * @param damage The amount of damage done to the player
     * @param laser Whether the player was hit by a laser
     */
    public boolean hitPlayer(float damage, boolean laser) {
        if(invulnerable) return false;
        if (!inCooldown || laser) {
            health = Math.max(0, health - damage);
            if (!laser) {
                SoundController.playSound("banditHurt", .9f);
                SoundController.lastPlayed(-28);
            }
            healthCountdown = HEALTH_REGEN_COOLDOWN;
            startCooldown();
            return true;
        }
        return false;
    }

    /**
     * Increases the player's health
     *
     * @param heal The amount of healing done to the player
     */
    public void healPlayer(float heal) {
        health = Math.min(MAX_HEALTH, health + heal);
    }

    /**
     * Collects a star
     */
    public void collectStar() {
        numStars += 1;
    }

    /**
     * Returns whether or not a star has been collected
     */
    public int getNumStars() {
        return numStars;
    }

    /**
     * Collects the orb.
     */
    public void collectOrb() {
        orbCollected = true;
    }

    /**
     * Gets whether the orb has been collected.
     */
    public boolean isOrbCollected() {
        return orbCollected;
    }

    /**
     * Gets whether the bandit should be sparking.
     */
    public boolean shouldSpark() { return shouldSpark; }


    public void resetAmmo() {
        gumAmmo = startingGum;
    }
    /** gets the amount of bubblegum player has */
    public int getAmmo() {
        return gumAmmo;
    }

    /** reduces max gum by 1 */
    public void fireGum() {
        gumAmmo -= 1;
    }

    /** Check if player already holds max gum*/
    public boolean atMaxGum() {return gumAmmo == maxGum;}

    /** increases amount of ammo by ammo */
    public void addAmmo(int ammo) {
        gumAmmo += ammo;
    }


    /**
     * Returns left/right movement of this character.
     * <p>
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovement() {
        return movement;
    }

    /**
     * Returns true if the dude is actively firing.
     *
     * @return true if the dude is actively firing.
     */
    public boolean isShooting() {
        return isShooting && shootCooldown <= 0;
    }

    /**
     * Sets whether the dude is actively firing.
     *
     * @param value whether the dude is actively firing.
     */
    public void setShooting(boolean value) {
        isShooting = value;
    }

    public void setStuck(boolean value) {stuck = value; isKnockback = false; }


    /**
     * Returns the current yScale of the player.
     *
     * @return the value of the player's yScale.
     */
    public float getYScale() {
        return yScale;
    }

    /**
     * Sets left/right movement of this character.
     * <p>
     * This is the result of input times dude force.
     *
     * @param value left/right movement of this character.
     */
    public void setMovement(float value) {
        movement = value;
        // Change facing if appropriate
        if (movement < 0) {
            faceRight = false;
        } else if (movement > 0) {
            faceRight = true;
        }
    }


    /**
     * Returns true if the dude is on the ground.
     *
     * @return true if the dude is on the ground.
     */
    public boolean isGrounded() {
        return isGrounded;
    }

    /**
     * Sets whether the dude is on the ground.
     *
     * @param value whether the dude is on the ground.
     */
    public void setGrounded(boolean value) {
        if(!isGrounded&&value) {
            poofController.makeEffect(getX(),getY()-getHeight()/2*yScale,
                    drawScale, yScale==-1);
            SoundController.playSound("banditLanding", .5f);
        }
        isGrounded = value;
        if (isGrounded) {
            hasFlipped = false;
        }
    }

    /**
     * Returns how much force to apply to get the dude moving
     * <p>
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return force;
    }

    /**
     * Sets how much force to apply to get the dude moving
     * <p>
     * Multiply this by the input to get the movement value.
     *
     * @param value how much force to apply to get the dude moving
     */
    public void setForce(float value) {
        force = value;
    }

    /**
     * Returns how hard the brakes are applied to get a dude to stop moving
     *
     * @return how hard the brakes are applied to get a dude to stop moving
     */
    public float getDamping() {
        return damping;
    }

    /**
     * Sets how hard the brakes are applied to get a dude to stop moving
     *
     * @param value how hard the brakes are applied to get a dude to stop moving
     */
    public void setDamping(float value) {
        damping = value;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     * <p>
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return maxspeed;
    }

    /**
     * Sets the upper limit on dude left-right movement.
     * <p>
     * This does NOT apply to vertical movement.
     *
     * @param value the upper limit on dude left-right movement.
     */
    public void setMaxSpeed(float value) {
        maxspeed = value;
    }

    /**
     * Returns topSensorName when flipped else bottomSensorName
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return (isFlipped) ? topSensorName : bottomSensorName;
    }

    /**
     * Returns bottomSensorName when flipped else topSensorName
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName2() {
        return (isFlipped) ? bottomSensorName : topSensorName;
    }

    /**
     * Sets the name of the ground sensor
     * <p>
     * This is used by ContactListener
     *
     * @param name the name of the ground sensor
     */
    public void setSensorName(String name) {
        if (isFlipped) {
            topSensorName = name;
        } else {
            bottomSensorName = name;
        }
    }

    /** Kills the bandit! Officially. Drains health and triggers death animation. */
    public void kill() {
        health = 0;
        animationController.setAnimation("death", false, true);
    }

    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }

    /** Start crushing the bandit */
    public void crush(CrusherModel crusher) {
        setFilter(CollisionController.CATEGORY_PLAYER, CollisionController.MASK_CRUSHED_PLAYER);
        this.crusher = crusher;
        isCrushing = true;
    }

    /** Mark that the bandit should start getting crushed soon */
    public void shouldCrush(CrusherModel crusher) {
        this.crusher = crusher;
    }

    /** End the crush without destroying the bandit (bandit not fully squished) */
    public void endCrush() {
        setFilter(CollisionController.CATEGORY_PLAYER, CollisionController.MASK_PLAYER);
        isCrushing = false;
        crusher = null;
    }

    /**
     * Creates a new dude with degenerate settings
     * <p>
     * The main purpose of this constructor is to set the initial capsule orientation.
     */
    public BanditModel(World world) {
        super(0, 0, 0.5f, 1.0f);
        setFixedRotation(true);

        // Gameplay attributes
        isGrounded = false;
        isShooting = false;
        faceRight = false;

        victory = false;
        invulnerable = false;

        shootCooldown = 0;

        isFlipped = false;
        yScale = 1.0f;

        health = MAX_HEALTH;
        ticks = 0;
        cameraTarget = new Vector2();
        numStars = 0;
        orbCollected = false;
        hasFlipped = false;
        shockFixtures = new ObjectSet<>();
        healthCountdown = 0;
        isCrushing = false;
        crushScale = 1;
        atDoor = false;
    }

    public void setAtDoor(boolean b){
        atDoor = b;
    }

    public boolean getAtDoor(){
        return atDoor;
    }

    public boolean winConditionMet(){
        return isOrbCollected() && isGrounded() && !isFlipped() && getAtDoor();
    }

    /**
     * Initializes the dude via the given JSON value
     * <p>
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the dude subtree
     *
     * @param directory the asset manager
     * @param x         the x position of the bandit
     * @param y         the y position of the bandit
     * @oaram constantsJson the JSON subtree defining the constant player information
     */
    public void initialize(AssetDirectory directory, float x, float y, JsonValue constantsJson) {
        gumAmmo = constantsJson.get("startingGum").asInt();
        maxGum = constantsJson.get("maxGum").asInt();
        startingGum = gumAmmo;
        setName(constantsJson.name());
        float[] size = constantsJson.get("size").asFloatArray();
        setPosition(x, y);
        cameraTarget.set(x * drawScale.x, y * drawScale.y);
        setDimension(size[0], size[1]);
        poofController = new EffectController("poof", "poof",
            directory, true, false, 1);


        animationController = new AnimationController(directory, "bandit");
        reloadSymbol = directory.getEntry("reloadGumSymbol", Texture.class);

        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        setBodyType(constantsJson.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(constantsJson.get("density").asFloat());
        setFriction(constantsJson.get("friction").asFloat());
        setRestitution(constantsJson.get("restitution").asFloat());
        setForce(constantsJson.get("force").asFloat());
        setDamping(constantsJson.get("damping").asFloat());
        setMaxSpeed(constantsJson.get("maxspeed").asFloat());

        // Reflection is best way to convert name to color
        Color debugColor;
        try {
            String cname = constantsJson.get("debugcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            debugColor = null; // Not defined
        }
        int opacity = constantsJson.get("debugopacity").asInt();
        debugColor.mul(opacity / 255.0f);
        setDebugColor(debugColor);

        // Now get the texture from the AssetManager singleton
        String key = constantsJson.get("texture").asString();
        TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));
        setTexture(texture);

        key = constantsJson.get("victory").asString();
        victoryText = new TextureRegion(directory.getEntry(key, Texture.class));
        setTexture(texture);


        // Get the sensor information
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        float[] sSize = constantsJson.get("sensorSize").asFloatArray();
        bottomSensorShape = new PolygonShape();
        bottomSensorShape.setAsBox(sSize[0], sSize[1], sensorCenter, 0.0f);

        // Reflection is best way to convert name to color
        try {
            String cname = constantsJson.get("sensorColor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            bottomSensorColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            bottomSensorColor = null; // Not defined
        }
        opacity = constantsJson.get("sensorOpacity").asInt();
        bottomSensorColor.mul(opacity / 255.0f);
        bottomSensorName = constantsJson.get("bottomSensorName").asString();

        sensorCenter = new Vector2(0, getHeight() / 2);
        topSensorShape = new PolygonShape();
        topSensorShape.setAsBox(sSize[0], sSize[1], sensorCenter, 0.0f);

        // Reflection is best way to convert name to color
        try {
            String cname = constantsJson.get("sensorColor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            topSensorColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            topSensorColor = null; // Not defined
        }
        opacity = constantsJson.get("sensorOpacity").asInt();
        topSensorColor.mul(opacity / 255.0f);
        topSensorName = constantsJson.get("topSensorName").asString();
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     * <p>
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        // Ground Sensor
        // -------------
        // We only allow the dude to jump when he's on the ground.
        // Double jumping is not allowed.
        //
        // To determine whether or not the dude is on the ground,
        // we create a thin sensor under his feet, which reports
        // collisions with the world but has no collision response.
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = getDensity();
        sensorDef.isSensor = true;
        sensorDef.shape = bottomSensorShape;
        bottomSensorFixture = body.createFixture(sensorDef);
        bottomSensorFixture.setUserData(bottomSensorName);

        sensorDef.density = getDensity();
        sensorDef.isSensor = true;
        sensorDef.shape = topSensorShape;
        topSensorFixture = body.createFixture(sensorDef);
        topSensorFixture.setUserData(topSensorName);

        //actviate physics for raycasts
        //vision.test(world);

        this.world = world;

        return true;
    }

    public void setFacingDirection(float cursorX) {
        if (shootCooldown > 0) {
            if(!faceRight) {
                backpedal = (cursorX>getX());
            } else {
                backpedal = (cursorX<getX());
            }
        } else {
            backpedal = false;
        }
    }

    /**Draw the reload gum symbol above bandits head*/
    public void drawReload(GameCanvas canvas) {
        if (isFlipped) {
            canvas.draw(reloadSymbol, Color.WHITE, (getX() - getWidth()/3) * drawScale.x, (getY() - getHeight() * 1.1f) * drawScale.y, reloadSymbol.getWidth(), reloadSymbol.getHeight());
        }
        else {
            canvas.draw(reloadSymbol, Color.WHITE, (getX() - getWidth()/3) * drawScale.x, (getY() + getHeight()/2 * 1.3f) * drawScale.y, reloadSymbol.getWidth(), reloadSymbol.getHeight());
        }
    }

    /**
     * Applies the force to the body of this dude
     * <p>
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        // Don't want to be moving. Damp out player motion
        if (isKnockback) {
            return;
        }

        if (getMovement() == 0f) {
            forceCache.set(-getDamping() * getVX(), 0);
            body.applyForce(forceCache, getPosition(), true);
        }

        // Velocity too high, clamp it
        if (Math.abs(getVX()) >= getMaxSpeed() && !isCrushing) {
            setVX(Math.signum(getVX()) * getMaxSpeed());
            if (getVX() * getMovement() < 0) { // Velocity and movement in opposite directions
                forceCache.set(getMovement(), 0);
                body.applyForce(forceCache, getPosition(), true);
            }
        } else {
            forceCache.set(getMovement(), 0);
           body.applyForce(forceCache, getPosition(), true);
        }
    }

    /** Add a shock fixture that the bandit is currently colliding with. */
    public void addShockFixture(Fixture fix) {
        shockFixtures.add(fix);
    }

    /** Remove a shock fixture that the bandit is no longer colliding with. */
    public void removeShockFixture(Fixture fix) {
        shockFixtures.remove(fix);
    }


    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     * <p>
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        healthCountdown--;
        ticks++;
        stunTime--;
        poofController.update();

        if (shockFixtures.size != 0) {
            hitPlayer(Damage.DPS_ON_SHOCK * dt, true);
            shouldSpark = true;
//            animationController.setAnimation("knock", false);
        } else {
            shouldSpark = false;
        }

        if (isKnockback) {
            knockbackTimer -= dt;
            if (knockbackTimer <= 0) {
                isKnockback = false;
            }
        }

        if (inCooldown) {
            if (ticks >= 30) {
                inCooldown = false;
            }
        } else {
            SoundController.lastPlayed(0);
            if (ticks % 3 == 0 && health>0 && healthCountdown <= 0) {
                healPlayer(0.25f);
            }
        }

        if (isShooting()) {
            shootCooldown = shotLimit;
        } else {
            shootCooldown = Math.max(0, shootCooldown - 1);
        }

        if (yScale > 1) {
            yScale = 1;
        } else if (yScale < -1) {
            yScale = -1;
        }

        if (!isFlipped && yScale < 1) {
            if (yScale != -1 || !stuck) {
                yScale += 0.1f;
            }
        } else if (isFlipped && yScale > -1) {
            if (yScale != 1 || !stuck) {
                yScale -= 0.1f;
            }
        }

        // Change camera target
        cameraTarget.x = getX() * drawScale.x;
        cameraTarget.y = getY() * drawScale.y;

        super.update(dt);

        // Anim controller update
        if(!animationController.hasTemp()&&!animationController.isEnding()
                &&!animationController.getCurrentAnimation().equals("victory")&&!invulnerable) {
             if (!isGrounded) {
                if(hasFlipped) animationController.setAnimation("fallNeg", true, false);
                else animationController.setAnimation("fall", true, false);
            }
            else if (stunTime > 0) {
                animationController.setAnimation("stunned", true, false);
             }
            else if(playingReload) animationController.setAnimation("reload", true, false);
             else if (getMovement() == 0)  {
                 if(AFKtimer>AFKwait) animationController.setAnimation("afk", true, false);
                 else animationController.setAnimation("idle", true, false);
             }
            else {
                if (backpedal) {
                    animationController.setAnimation("back", true, false);
                } else {
                    animationController.setAnimation("run", true, false);
                }

            }
        }
        if(animationController.getCurrentAnimation().equals("idle")
            ||animationController.getCurrentAnimation().equals("afk")) {
            AFKtimer+=dt;
        } else {
            AFKtimer = 0;
        }

        if(isShooting||playingReload) AFKtimer = 0;

        curFrame = animationController.getFrame();

        if (crusher != null) {
            if (isCrushing) {
                if (getHealth() <= 0) {
                    crushScale = 0;
                    return;
                }
                float banditLeft = getX() - getWidth() / 2f;
                float banditRight = getX() + getWidth() / 2f;
                float crusherLeft = crusher.getX() - crusher.getWidth() / 2f;
                float crusherRight = crusher.getX() + crusher.getWidth() / 2f;
                if (banditLeft > crusherRight) {
                    endCrush();
                }
                else if (banditRight < crusherLeft) {
                    endCrush();
                }

                else {
                    if (world.getGravity().y < 0) {
                        float bottomOfCrusher = crusher.getY() - (crusher.getHeight() / 2f);
                        float bottomOfPlayer = getY() - (getHeight() / 2);
                        crushScale = (bottomOfCrusher - bottomOfPlayer) / getHeight();
                        if (crushScale <= 0.05f) {
                            hitPlayer(getHealth(), true);
                            setVX(0);
                        } else if (crushScale > 1.02f) {
                            endCrush();
                        }
                    } else {
                        float topOfCrusher = crusher.getY() + (crusher.getHeight() / 2f);
                        float topOfPlayer = getY() + (getHeight() / 2);
                        crushScale = (topOfPlayer - topOfCrusher) / getHeight();
                        if (crushScale <= 0.05f) {
                            hitPlayer(getHealth(), true);
                            setVX(0);
                        } else if (crushScale > 1.02f) {
                            endCrush();
                        }
                    }
                    if (crusher != null) {
                        if (crushScale >= 0.05f && Math.abs(crusher.getX() - getX()) > crusher.getWidth() / 3) {
                            body.applyForce(crusher.getX() < getX() ? 500 : -500, 0, getX(), getY(), true);
                        }
                    }
                }
            } else {
                boolean shouldStartCrush = false;
                float hw = (crusher.getWidth() / 2f);
                float crusherRight = crusher.getX() + hw;
                float crusherLeft = crusher.getX() - hw;
                float banditLeft = getX() - (getWidth() / 2f);
                float banditRight = getX() + (getWidth() / 2f);
                boolean isCrusher = false;
                for (Obstacle ob : getCollisions()) {
                    if (!(ob instanceof CrusherModel)) {
                        float obHW = 0;

                        if (ob instanceof CapsuleObstacle) {
                            obHW = ((CapsuleObstacle) ob).getWidth() / 2f;
                        } else if (ob instanceof BoxObstacle) {
                            obHW = ((BoxObstacle) ob).getWidth() / 2f;
                        }
                        if (obHW != 0) {
                            float obLeft = ob.getX() - obHW;
                            float obRight = ob.getX() + obHW;
                            if (obRight > crusherLeft && banditRight > crusherLeft && banditLeft < crusherRight && obLeft < crusherRight) {
                                shouldStartCrush = true;
                            }
                        }
                    } else {
                        isCrusher = true;
                    }
                }
                if (!isCrusher) {
                    endCrush();
                }
                else if (shouldStartCrush) {
                    crush(crusher);
                }
            }
        } else {
            if (crushScale < 1) {
                crushScale += 0.1f;
            } else {
                crushScale = 1;
            }
        }
    }

    private boolean playingReload;

    public void startReload() {
        playingReload = true;
    }

    public void stopReload() {
       playingReload = false;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;
        if(backpedal&&health>0) effect *= -1f;

        if(victory) {  canvas.drawWithShadow(victoryText, Color.WHITE, origin.x, origin.y,
            getX() * drawScale.x - getWidth() / 2 * drawScale.x * effect, //adjust for animation origin
            getY() * drawScale.y, getAngle(), effect, 1);
            return;
        }
        if (curFrame != null) {

            float yOffset = ((1 - crushScale) * texture.getRegionHeight() * (world.getGravity().y < 0 ? -.5f : .5f));

            canvas.drawWithShadow(curFrame, Color.WHITE, origin.x, origin.y,
                    getX() * drawScale.x - getWidth() / 2 * drawScale.x * effect, //adjust for animation origin
                    getY() * drawScale.y + yOffset, getAngle(), effect, yScale*crushScale);

        }
        poofController.draw(canvas);
    }

    /**
     * Flips the player's angle and direction when the world gravity is flipped
     */
    public void flippedGravity() {
        isFlipped = !isFlipped;
        if (!isGrounded) {
            hasFlipped = true;
        }
    }

    /**
     * Stuns player for time t
     *
     * @param t Time to stun player for
     */
    public void stun(int t){
        stunTime = t;
    }

    /**
     * Stuns player for time t
     *
     * @return amount of time remaining for player to be stunned
     */
    public int getStunTime() {
        return stunTime;
    }

    /**
     * Draws the outline of the physics body, including the field of vision
     * <p>
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
    }

  public void setVictory() {
        victory = true;
  }

  public void setInvulnerable() {invulnerable = true;}
}