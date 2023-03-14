/*
 * PlayerModel.java
 *
 * This is a refactored version of DudeModel that allows us to read its properties
 * from a JSON file.  As a result, it has a lot more getter and setter "hooks" than
 * in lab.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * JSON version, 3/2/2016
 */
package edu.cornell.gdiac.json;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import java.lang.reflect.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Player avatar for the plaform game.
 *
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.
 */
public class PlayerModel extends CapsuleObstacle {
	// Physics constants
	/** The factor to multiply by the input */
	private float force;
	/** The amount to slow the character down */
	private float damping;
	/** The maximum character speed */
	private float maxspeed;

	/** The current horizontal movement of the character */
	private float   movement;
	/** Which direction is the character facing */
	private boolean faceRight;
	/** Whether our feet are on the ground */
	private boolean isGrounded;

	// SENSOR FIELDS
	/** Ground sensor to represent our feet */
	private Fixture bottomSensorFixture;
	private PolygonShape bottomSensorShape;
	/** The name of the sensor for detection purposes */
	private String bottomSensorName;
	/** The color to paint the sensor in debug mode */
	private Color bottomSensorColor;

	/** Top sensor to represent our feet when flipped */
	private Fixture topSensorFixture;
	private PolygonShape topSensorShape;
	/** The name of the sensor for detection purposes */
	private String topSensorName;
	/** The color to paint the sensor in debug mode */
	private Color topSensorColor;

	/** Cache for internal force calculations */
	private Vector2 forceCache = new Vector2();

	/** Whether we are actively shooting */
	private boolean isShooting;

	/** How long until we can shoot again */
	private int shootCooldown;

	/** Cooldown (in animation frames) for shooting */
	private final int shotLimit;

	/** Cache for flipping player orientation */
	private float angle;

	/** Whether this player is flipped */
	private boolean isFlipped;

	/** The y scale for this player (used for flip effect) */
	private float yScale;

	/** The physics world */
	private World world;

	/**
	 * Returns left/right movement of this character.
	 *
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
	 *
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
		isGrounded = value;
	}

	/**
	 * Returns how much force to apply to get the dude moving
	 *
	 * Multiply this by the input to get the movement value.
	 *
	 * @return how much force to apply to get the dude moving
	 */
	public float getForce() {
		return force;
	}

	/**
	 * Sets how much force to apply to get the dude moving
	 *
	 * Multiply this by the input to get the movement value.
	 *
	 * @param value	how much force to apply to get the dude moving
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
	 * @param value	how hard the brakes are applied to get a dude to stop moving
	 */
	public void setDamping(float value) {
		damping = value;
	}

	/**
	 * Returns the upper limit on dude left-right movement.
	 *
	 * This does NOT apply to vertical movement.
	 *
	 * @return the upper limit on dude left-right movement.
	 */
	public float getMaxSpeed() {
		return maxspeed;
	}

	/**
	 * Sets the upper limit on dude left-right movement.
	 *
	 * This does NOT apply to vertical movement.
	 *
	 * @param value	the upper limit on dude left-right movement.
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
	 *
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

	/**
	 * Returns true if this character is facing right
	 *
	 * @return true if this character is facing right
	 */
	public boolean isFacingRight() {
		return faceRight;
	}

	/**
	 * Creates a new dude with degenerate settings
	 *
	 * The main purpose of this constructor is to set the initial capsule orientation.
	 */
	public PlayerModel(World world) {
		super(0,0,0.5f,1.0f);
		setFixedRotation(true);

		shotLimit = 6;
		// Gameplay attributes
		isGrounded = false;
		isShooting = false;
		faceRight = false;

		shootCooldown = 0;

		isFlipped = false;
		yScale = 1.0f;
		this.world = world;

	}

	/**
	 * Initializes the dude via the given JSON value
	 *
	 * The JSON value has been parsed and is part of a bigger level file.  However,
	 * this JSON value is limited to the dude subtree
	 *
	 * @param directory the asset manager
	 * @param json		the JSON subtree defining the dude
	 */
	public void initialize(AssetDirectory directory, JsonValue json) {
		setName(json.name());
		float[] pos  = json.get("pos").asFloatArray();
		float[] size = json.get("size").asFloatArray();
		setPosition(pos[0],pos[1]);
		setDimension(size[0],size[1]);

		// Technically, we should do error checking here.
		// A JSON field might accidentally be missing
		setBodyType(json.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
		setDensity(json.get("density").asFloat());
		setFriction(json.get("friction").asFloat());
		setRestitution(json.get("restitution").asFloat());
		setForce(json.get("force").asFloat());
		setDamping(json.get("damping").asFloat());
		setMaxSpeed(json.get("maxspeed").asFloat());

		// Reflection is best way to convert name to color
		Color debugColor;
		try {
			String cname = json.get("debugcolor").asString().toUpperCase();
			Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
			debugColor = new Color((Color)field.get(null));
		} catch (Exception e) {
			debugColor = null; // Not defined
		}
		int opacity = json.get("debugopacity").asInt();
		debugColor.mul(opacity/255.0f);
		setDebugColor(debugColor);

		// Now get the texture from the AssetManager singleton
		String key = json.get("texture").asString();
		TextureRegion texture = new TextureRegion(directory.getEntry(key, Texture.class));
		setTexture(texture);

		// Get the sensor information
		Vector2 sensorCenter = new Vector2(0, -getHeight()/2);
		float[] sSize = json.get("sensorsize").asFloatArray();
		bottomSensorShape = new PolygonShape();
		bottomSensorShape.setAsBox(sSize[0], sSize[1], sensorCenter, 0.0f);

		// Reflection is best way to convert name to color
		try {
			String cname = json.get("sensorcolor").asString().toUpperCase();
			Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
			bottomSensorColor = new Color((Color)field.get(null));
		} catch (Exception e) {
			bottomSensorColor = null; // Not defined
		}
		opacity = json.get("sensoropacity").asInt();
		bottomSensorColor.mul(opacity/255.0f);
		bottomSensorName = json.get("bottomsensorname").asString();

		sensorCenter = new Vector2(0, getHeight()/2);
		topSensorShape = new PolygonShape();
		topSensorShape.setAsBox(sSize[0], sSize[1], sensorCenter, 0.0f);

		// Reflection is best way to convert name to color
		try {
			String cname = json.get("sensorcolor").asString().toUpperCase();
			Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
			topSensorColor = new Color((Color)field.get(null));
		} catch (Exception e) {
			topSensorColor = null; // Not defined
		}
		opacity = json.get("sensoropacity").asInt();
		topSensorColor.mul(opacity/255.0f);
		topSensorName = json.get("topsensorname").asString();
	}

	/**
	 * Creates the physics Body(s) for this object, adding them to the world.
	 *
	 * This method overrides the base method to keep your ship from spinning.
	 *
	 * @param world Box2D world to store body
	 *
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

		return true;
	}


	/**
	 * Applies the force to the body of this dude
	 *
	 * This method should be called after the force attribute is set.
	 */
	public void applyForce() {
		if (!isActive()) {
			return;
		}

		// Don't want to be moving. Damp out player motion
		if (getMovement() == 0f) {
			forceCache.set(-getDamping()*getVX(),0);
			body.applyForce(forceCache,getPosition(),true);
		}

		// Velocity too high, clamp it
		if (Math.abs(getVX()) >= getMaxSpeed()) {
			setVX(Math.signum(getVX())*getMaxSpeed());
			if (getVX() * getMovement() < 0) { // Velocity and movement in opposite directions
				forceCache.set(getMovement(),0);
				body.applyForce(forceCache,getPosition(),true);
			}
		}
		else {
			// TODO: This seems to create an issue where you can't slow down a jump
			// once you hit top speed. Should check both directions independently
			// and let you move the opposite direction even if at top speed.
			// Might not be a problem without jumping, but it might be.
			forceCache.set(getMovement(),0);
			body.applyForce(forceCache,getPosition(),true);
		}
	}

	/**
	 * Updates the object's physics state (NOT GAME LOGIC).
	 *
	 * We use this method to reset cooldowns.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {

		if (isShooting()) {
			shootCooldown = shotLimit;
		} else {
			shootCooldown = Math.max(0, shootCooldown - 1);
		}

		if (yScale < 1f && !isFlipped) {
			yScale += 0.1f;
		} else if (yScale > -1f && isFlipped) {
			yScale -= 0.1f;
		}

		super.update(dt);
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		if (texture != null) {
			float effect = faceRight ? 1.0f : -1.0f;

			canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,
					getY()*drawScale.y,getAngle(),effect,yScale);

		}

	}


	/**
	 * Draws the outline of the physics body, including the field of vision
	 *
	 * This method can be helpful for understanding issues with collisions.
	 *
	 *
	 * @param canvas Drawing context
	 */
	public void drawDebug(GameCanvas canvas) {
		super.drawDebug(canvas);
	}

	/**
	 * Flips the player's angle and direction when the world gravity is flipped
	 *
	 * */
	public void flippedGravity(){
		isFlipped = !isFlipped;
	}
}