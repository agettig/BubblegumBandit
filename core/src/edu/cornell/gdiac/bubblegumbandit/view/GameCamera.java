package edu.cornell.gdiac.bubblegumbandit.view;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.bubblegumbandit.helpers.CameraShake;
import edu.cornell.gdiac.physics.obstacle.Obstacle;

public class GameCamera  extends OrthographicCamera {
    // CAMERA CONSTANTS
    /** The horizontal speed at which the camera converges on the target. */
    private final float xSpeed = 4f;

    /** The vertical speed at which the camera converges on the target. */
    private final float ySpeed = 6f;

    /** The speed at which the camera converges on a new zoom. */
    private final float zoomSpeed = 3f;

    /** The weight of the secondary target */
    private final float secondaryWeight = 0.25f;

    /** Defines how far the target y can be from the camera position y, as a proportion of the screen height.
     * Temporarily disabled when in fixed camera position. */
    private final float targetYClamp = 0.15f;

    /** The speed to converge on the target y clamp when transitioning from a fixed camera. */
    private final float clampSpeed = 6f;

    /** The speed to carry out the smoothstep. */
    private final float smoothstepSpeed = 400f;

    /** Helper class used to manage screen shake. */
    private final CameraShake shake = new CameraShake();

    // CAMERA FIELDS
    /** The target the camera is focused on. */
    private Vector2 target;

    /** The secondary target the camera tries to include. */
    private Vector2 secondaryTarget;

    /** Whether the camera is fixed on the X axis */
    private boolean isFixedX;

    /** Whether the camera is fixed on the Y axis */
    private boolean isFixedY;

    /** The target zoom of the camera (does not instantly change) */
    private float targetZoom;

    /** Whether the camera is in debug mode. */
    private boolean isCameraDebug;

    /** The width of the level in pixels */
    private float levelWidth;

    /** The height of the level in pixels */
    private float levelHeight;

    /** The current yClamp of the camera */
    private float curYClamp;

    /** The base position of the camera before screen shake. */
    private Vector2 basePos;

    /** Constructs a new GameCamera, using the given viewport width and height. For pixel perfect 2D rendering just supply
     * the screen size, for other unit scales (e.g. meters for box2d) proceed accordingly. The camera will show the region
     * [-viewportWidth/2, -(viewportHeight/2-1)] - [(viewportWidth/2-1), viewportHeight/2]
     * @param viewportWidth the viewport width
     * @param viewportHeight the viewport height */
    public GameCamera (float viewportWidth, float viewportHeight) {
        super(viewportWidth, viewportHeight);
        secondaryTarget = new Vector2();
        target = new Vector2();
        isFixedX = false;
        isFixedY = false;
        targetZoom = 1f;
        isCameraDebug = false;
        curYClamp = targetYClamp;
        basePos = new Vector2();
    }

    /** Toggles the debug mode of the camera. */
    public void toggleDebug() {
        isCameraDebug = !isCameraDebug;
    }

    /** Toggles the camera mode between using a secondary target or not.
     *
     * @param zoom the amount to zoom on the camera
     */
    public void setZoom(float zoom) { targetZoom = zoom; }

    /** Zooms the camera to a given width or height. Uses whichever one causes a bigger camera size
     * since the aspect ratio doesn't change.
     *
     * @param width the new width of the viewport in pixel coords (0 if unchanged)
     * @param height the new height of the viewport in pixel coords (0 if unchanged)
     */
    public void setZoom(float width, float height) {
        float aspectRatio = viewportWidth / viewportHeight;
        if ((width / height) > aspectRatio) {
            // width is the limiting factor
            targetZoom = width / viewportWidth;
        } else {
            // height is the limiting factor
            targetZoom = height / viewportHeight;
        }
    }


    /** Gets whether the camera is fixed on the x axis.
     *
     */
    public boolean isFixedX() { return isFixedX; }

    /** Gets whether the camera is fixed on the y axis.
     *
     */
    public boolean isFixedY() { return isFixedY; }


    /** Sets whether the camera is fixed on the x axis.
     *
     * @param isFixedX whether the camera should be fixed on the x axis
     */
    public void setFixedX(boolean isFixedX) {
        this.isFixedX = isFixedX;
        // if (isFixedX) { curYClamp = 1; }
    }


    /** Sets whether the camera is fixed on the y axis.
     *
     * @param isFixedY whether the camera should be fixed on the y axis
     */
    public void setFixedY(boolean isFixedY) {
        this.isFixedY = isFixedY;
        if (isFixedY) { curYClamp = 1; }
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
     * Sets the width and height of the level for the debug view.
     * @param width the width of the level
     * @param height the height of the level
     */
    public void setLevelSize(float width, float height) {
        levelWidth = width;
        levelHeight = height;
    }

    /** Apply screen shake trauma if the obstacle causing it is on screen.
     * Use positive trauma for the camera to move up at first (good for gravity facing up)
     * and negative trauma for the camera to move down at first (something falling down).
     *
     * @param xPos the pixel x position of the obstacle causing the trauma.
     * @param yPos the pixel y position of the obstacle causing the trauma.
     * @param trauma the amount of trauma to apply.*/
    public void addTrauma(float xPos, float yPos, float trauma) {
        float halfHeight = (viewportHeight * zoom) / 2;
        float lowerYBound = basePos.y - halfHeight;
        float upperYBound = basePos.y + halfHeight;
        float halfWidth = (viewportWidth * zoom) / 2;
        float lowerXBound = basePos.x - halfWidth;
        float upperXBound = basePos.x + halfWidth;


        if (yPos >= lowerYBound && yPos <= upperYBound && xPos >= lowerXBound && xPos <= upperXBound) {
            shake.addTrauma(trauma);
        }
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
        if (isCameraDebug) {
            basePos.x = levelWidth / 2;
            basePos.y = levelHeight / 2;
            float aspectRatio = viewportWidth / viewportHeight;
            if ((levelWidth / levelHeight) > aspectRatio) {
                // width is the limiting factor
                zoom = levelWidth / viewportWidth;
            } else {
                // height is the limiting factor
                zoom = levelHeight / viewportHeight;
            }
            super.update(updateFrustum);
            return;
        }

        float newTargetX = target.x;
        if (!isFixedX) {
            newTargetX = target.x * (1 - secondaryWeight) + secondaryTarget.x * secondaryWeight;
        }

        float newTargetY = target.y;
        if (!isFixedY) {
            newTargetY = target.y * (1 - secondaryWeight) + secondaryTarget.y * secondaryWeight;
        }

        basePos.x += (newTargetX - basePos.x) * xSpeed * dt;
        basePos.y += (newTargetY - basePos.y) * ySpeed * dt;

        // Adjust y clamp
        if (!isFixedY) {
            curYClamp += (targetYClamp - curYClamp) * clampSpeed * dt;
        }

        // Cap how far offscreen
        float maxDistY = viewportHeight * curYClamp;
        if (basePos.y - target.y > maxDistY) {
            basePos.y = target.y + maxDistY;
        } else if (target.y - basePos.y > maxDistY) {
            basePos.y = target.y - maxDistY;
        }

        // Adjust zoom
        zoom += (targetZoom - zoom) * zoomSpeed * dt;

        // Apply screen shake
        rotate(-shake.getAngle()); // Undo last rotation
        shake.update(dt, viewportWidth * zoom, viewportHeight * zoom);
        position.x = basePos.x + shake.getOffsetX();
        position.y = basePos.y + shake.getOffsetY();
        rotate(shake.getAngle());

        super.update(updateFrustum);
    }

}
