package edu.cornell.gdiac.bubblegumbandit.helpers;

import com.badlogic.gdx.math.MathUtils;
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

    /** How long a shock effect plays for */
    private float SHOCK_TIME = 10f;

    /** The direction of the block effect */
    private Vector2 BLOCK_VEC = Vector2.Y;

    /** How long the shock has left to play for */
    private float shockTimer = 0f;

    /** Basically the max offset of the shock effect */
    private float shockScale = 0f;

    /** The current lerping objective during shocking */
    private Vector2 shakeAdjust = Vector2.Zero;

    /** Time since the lerping changed locations */
    private float timeSinceLastShake = 0f;


    public CameraShake() {
        trauma = 0;
        offsetX = 0;
        offsetY = 0;
        angle = 0;
        time = 0;
        shockTimer = 0;
        timeSinceLastShake = 0;
        traumaDir = BLOCK_VEC;
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

    public void addShockTrauma(float scale){
        shockTimer = SHOCK_TIME;
        shockScale = scale;
        shakeAdjust = new Vector2(
            MathUtils.random(-shockScale, shockScale),
            MathUtils.random(-shockScale, shockScale)
        );
    }

    public void update(float dt, float viewportWidth, float viewportHeight) {
        float maxOffset = MAX_OFFSET_PERCENT * viewportHeight;
        Vector2 traumaVec = new Vector2(traumaDir).scl(trauma);

        if(shockTimer<=0) {
            offsetY = (float) (Math.sin(time) * maxOffset * traumaVec.y);
            offsetX = (float) (Math.sin(time) * maxOffset * traumaVec.x);
        } else {
            offsetX = MathUtils.lerp(0, shakeAdjust.x, 5 * timeSinceLastShake);
            offsetY = MathUtils.lerp(0, shakeAdjust.y, 5 * timeSinceLastShake);

            shockTimer -= dt * DT_FACTOR;
            timeSinceLastShake  += dt * DT_FACTOR;

            if(shockTimer<=0) {
                timeSinceLastShake = 0f;
            }

             if(timeSinceLastShake>1f) {
                shakeAdjust = new Vector2(
                    MathUtils.random(-shockScale, shockScale),
                    MathUtils.random(-shockScale, shockScale)
                );
                timeSinceLastShake = 0f;
            }

        }

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
