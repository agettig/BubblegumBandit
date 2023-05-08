package edu.cornell.gdiac.bubblegumbandit.helpers;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.util.RandomController;

public class CameraShake {

    // Shake constants

    /** The max camera offset from screen shake */
    private final float MAX_OFFSET_PERCENT = .05f;

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

    /** The direction of the trauma */
    private Vector2 traumaDir;

    private boolean shocking;

    private float SHOCK_TIME = 4f;

    private float shockTimer = 0f;

    private float shockTrauma = 0f;

    private float timeSinceLastShake = 0f;


    public CameraShake() {
        trauma = 0;
        offsetX = 0;
        offsetY = 0;
        angle = 0;
        time = 0;
        shockTimer = 0;
        timeSinceLastShake = 0;
        traumaDir = Vector2.Y;
        shocking = false;
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

    public void addTrauma(Vector2 dir, float trauma){
        traumaDir = dir.nor();
        time = trauma > 0 ? 0 : (float) Math.PI;
        this.trauma += Math.abs(trauma);
        if (this.trauma > 1) { // Clamp
            this.trauma = 1;
        }

    }

    private void addRandTrauma(float trauma) {
        Vector2 dir = new Vector2(RandomController.rollFloat(-10f, 10f),
            RandomController.rollFloat(-10f, 10f)).nor();
        addTrauma(dir, trauma);
    }

    public void addShockTrauma(float trauma){
        addRandTrauma(trauma);
        shocking = true;
        shockTrauma = trauma;
    }

    public void update(float dt, float viewportWidth, float viewportHeight) {
        float maxOffset = MAX_OFFSET_PERCENT * viewportHeight;
        Vector2 traumaVec = new Vector2(traumaDir).scl(trauma);
        if(trauma>=1)   System.out.println(traumaVec);
        offsetY = (float) (Math.sin(time) * maxOffset * traumaVec.y);
        offsetX = (float) (Math.sin(time) * maxOffset * traumaVec.x);


        // Reduce trauma
        if (trauma > 0) {
            trauma *= TRAUMA_DECREASE;
        }

        time += dt * DT_FACTOR;
        if (time > Float.MAX_VALUE) {
            time -= Float.MAX_VALUE;
        }

        if(shocking) {
            shockTimer += dt * DT_FACTOR;
            timeSinceLastShake  += dt * DT_FACTOR;
            if(shockTimer>SHOCK_TIME) {
                shocking = false;
                shockTimer = 0f;
                timeSinceLastShake = 0f;
                trauma = 0f;
            }
            else if(timeSinceLastShake>.5f) {
                addRandTrauma(shockTrauma);
                timeSinceLastShake = 0f;
            }
        }

    }


}
