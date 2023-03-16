package edu.cornell.gdiac.json;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.physics.obstacle.Obstacle;

public class GameCamera  extends OrthographicCamera {
    // CAMERA CONSTANTS
    /** The horizontal speed at which the camera converges on the target. */
    private final float xSpeed = 3f;

    /** The vertical speed at which the camera converges on the target. */
    private final float ySpeed = 3f;

    /** The weight of the secondary target */
    private final float secondaryWeight = 0.2f;

    /** Whether the secondary target is enabled */
    private final boolean useSecondaryTarget = true;

    /** Defines how far the target y can be from the camera position y, as a proportion of the screen height. */
    private final float yClampFactor = 0.25f;

    // CAMERA FIELDS
    /** The target the camera is focused on. */
    private Vector2 target;

    /** The secondary target the camera tries to include. */
    private Vector2 secondaryTarget;

    public GameCamera() {
        super();
    }

    /** Constructs a new GameCamera, using the given viewport width and height. For pixel perfect 2D rendering just supply
     * the screen size, for other unit scales (e.g. meters for box2d) proceed accordingly. The camera will show the region
     * [-viewportWidth/2, -(viewportHeight/2-1)] - [(viewportWidth/2-1), viewportHeight/2]
     * @param viewportWidth the viewport width
     * @param viewportHeight the viewport height */
    public GameCamera (float viewportWidth, float viewportHeight) {
        super(viewportWidth, viewportHeight);
        secondaryTarget = new Vector2();
        target = new Vector2();
    }

    /**
     * Returns the current target of the game camera.
     * @return the target of the camera
     */
    public Vector2 getTarget() {
        return target;
    }

    /**
     * Sets the camera target to the given position.
     * @param x the x target of the camera
     * @param y the y target of the camera
     */
    public void setTarget(float x, float y) {
        target.set(x, y);
    }


    /**
     * Sets the camera target to the given position.
     * @param target the target of the camera
     */
    public void setTarget(Vector2 target) {
        this.target.set(target);
    }

    /**
     * Sets the camera target x to the given position.
     * @param targetX the target x of the camera
     */
    public void setTargetX(float targetX) {
        this.target.x = targetX;
    }

    /**
     * Sets the camera target y to the given position.
     * @param targetY the target y of the camera
     */
    public void setTargetY(float targetY) {
        this.target.y = targetY;
    }

    /**
     * Returns the current secondary target of the game camera.
     * @return the secondary target of the camera
     */
    public Vector2 getSecondaryTarget() {
        return secondaryTarget;
    }

    /**
     * Sets the secondary camera target to the given position.
     * @param x the x secondary target of the camera
     * @param y the y secondary target of the camera
     */
    public void setSecondaryTarget(float x, float y) {
        secondaryTarget.set(x, y);
    }


    /**
     * Sets the secondary camera target to the given position.
     * @param target the secondary target of the camera
     */
    public void setSecondaryTarget(Vector2 target) {
        this.secondaryTarget.set(target);
    }

    /**
     * Sets the secondary camera target x to the given position.
     * @param targetX the secondary target x of the camera
     */
    public void setSecondaryTargetX(float targetX) {
        this.secondaryTarget.x = targetX;
    }

    /**
     * Sets the secondary camera target y to the given position.
     * @param targetY the secondary target y of the camera
     */
    public void setSecondaryTargetY(float targetY) {
        this.secondaryTarget.y = targetY;
    }

    /**
     * Updates this camera.
     */
    public void update(float dt) {
        update(true, dt);
    }

    /**
     * Updates this camera based on its target.
     */
    public void update(boolean updateFrustum, float dt) {
        float newTargetX = target.x;
        if (useSecondaryTarget) {
            newTargetX = target.x * (1 - secondaryWeight) + secondaryTarget.x * secondaryWeight;
        }
        position.x += (newTargetX - position.x) * xSpeed * dt;

        float newTargetY = target.y;
        if (useSecondaryTarget) {
            newTargetY = target.y * (1 - secondaryWeight) + secondaryTarget.y * secondaryWeight;
        }
        position.y += (newTargetY - position.y) * ySpeed * dt;

        // Cap how far offscreen
        float maxDistY = viewportHeight * yClampFactor;
        if (position.y - target.y > maxDistY) {
            position.y = target.y + maxDistY;
        } else if (target.y - position.y > maxDistY) {
            position.y = target.y - maxDistY;
        }

        super.update(updateFrustum);
    }

}
