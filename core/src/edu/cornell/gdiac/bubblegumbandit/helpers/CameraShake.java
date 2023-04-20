package edu.cornell.gdiac.bubblegumbandit.helpers;

import java.util.Random;

public class CameraShake {

    // Shake constants
    /** The max screen shake angle */
    private final float MAX_ANGLE = 5;

    /** The max camera offset from screen shake */
    private final float MAX_OFFSET = 10;

    private final float TRAUMA_DECREASE = 5f;

    private final Random random = new Random();

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
        this.trauma += trauma;
        if (this.trauma > 1) { // Clamp
            this.trauma = 1;
        }
    }

    public void update(float dt) {
        // Reduce trauma
        if (trauma > 0) {
            trauma -= (TRAUMA_DECREASE * dt);
        }
        float shake = trauma * trauma;
        angle = MAX_ANGLE * shake * (random.nextFloat() * 2 - 1);
        offsetX = MAX_OFFSET * shake * (random.nextFloat() * 2 - 1);
        offsetY = MAX_OFFSET * shake * (random.nextFloat() * 2 - 1);
    }


}
