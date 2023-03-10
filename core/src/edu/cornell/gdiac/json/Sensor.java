package edu.cornell.gdiac.json;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class Sensor {

    /** Represents center of the sensor*/
    private final Vector2 sensorCenter;
    /** Width of sensor */
    private final float halfWidth;

    private final float halfHeight;

    private final String sensorName;

    private Fixture fixture;

    private Vector2 printLoc;

    public PolygonShape getSensorShape() {
        return sensorShape;
    }

    private PolygonShape sensorShape;

    // Getters
    public Vector2 getSensorCenter() {
        return sensorCenter;
    }

    public float getHalfWidth() {
        return halfWidth;
    }

    public float getHalfHeight() {
        return halfHeight;
    }

    public String getSensorName() {
        return sensorName;
    }

    public Fixture getFixture() {
        return fixture;
    }

    public float printX(){return printLoc.x;}

    public float printY(){return printLoc.y;}

    // Setters

    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }

    // Constructor
    public Sensor(Vector2 sensorCenter, float halfWidth, float halfHeight, String sensorName, float printX, float printY) {
        this.sensorCenter = sensorCenter;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.sensorName = sensorName;
        this.sensorShape = new PolygonShape();
        this.sensorShape.setAsBox(halfWidth, halfHeight, sensorCenter, 0);
        this.printLoc = new Vector2(printX, printY);
    }

}
