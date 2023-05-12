/*
 * WallModel.java
 *
 * This is a refactored version of the wall (screen boundary) from Lab 4.  We have made 
 * it a specialized class so that we can import its properties from a JSON file.  
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * JSON version, 3/2/2016
 */
package edu.cornell.gdiac.bubblegumbandit.models.level;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import java.lang.reflect.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.obstacle.*;


/**
 * A box used to aggregate tiles into colliders
 */
public class WallModel extends BoxObstacle {
	
	/**
	 * Create a new WallModel with degenerate settings
	 */	
	public WallModel() {
		super(0, 0, 1, 1);
	}

	/**
	 * Initializes the platform via the given JSON value
	 *
	 * The JSON value has been parsed and is part of a bigger level file.  However,
	 * this JSON value is limited to the platform subtree
	 *
	 * @param constants     the JSON subtree defining the platform constants
	 */
	public void initialize(float startX, float startY, float endX, float endY, JsonValue constants) {
		setName("wall");

		float x = (startX + endX + 1) / 2;
		float y = (startY + endY + 1) / 2;
		setPosition(x, y);
		float width = endX - startX + 1;
		float height = endY - startY + 1;
		setDimension(width, height);

		// Technically, we should do error checking here.
		// A JSON field might accidentally be missing
		setBodyType(constants.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
		setDensity(constants.get("density").asFloat());
		setFriction(constants.get("friction").asFloat());
		setRestitution(constants.get("restitution").asFloat());

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
	}
}
