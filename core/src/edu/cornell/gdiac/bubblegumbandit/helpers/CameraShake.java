package edu.cornell.gdiac.bubblegumbandit.helpers;

import java.util.Random;

public class CameraShake {

    // Shake constants

    /** The max camera offset from screen shake */
    // TODO: Make dependent on resolution (percentage of screen)
    private final float MAX_OFFSET = 40;

    /** Changes the frequency of the damped harmonic motion */
    private final float DT_FACTOR = 20;

    private final float TRAUMA_DECREASE = .925f;

    private float time;

    // Shake fields
    /** The current screen shake angle */
    private float angle;

    /** The current camera offset x from screen shake */
    private float offsetX;

    /** The current camera offset y from screen shake */
    private float offsetY;

    /** The current camera trauma */
    private float trauma;

    public CameraShake() {
        trauma = 0;
        offsetX = 0;
        offsetY = 0;
        angle = 0;
        time = 0;
    }

    public float getAngle() {
        return angle;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void addTrauma(float trauma) {
        time = trauma > 0 ? 0 : (float) Math.PI;
        this.trauma += Math.abs(trauma);
        if (this.trauma > 1) { // Clamp
            this.trauma = 1;
        }
    }

    public void update(float dt) {
        offsetY = (float) (Math.sin(time) * trauma * MAX_OFFSET);

        // Reduce trauma
        if (trauma > 0) {
            trauma *= TRAUMA_DECREASE;
        }

        time += dt * DT_FACTOR;
        if (time > Float.MAX_VALUE) {
            time -= Float.MAX_VALUE;
        }

    }


}
