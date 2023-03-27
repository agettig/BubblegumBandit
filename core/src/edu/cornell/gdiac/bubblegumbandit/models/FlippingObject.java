package edu.cornell.gdiac.bubblegumbandit.models;

/** A class to manage the yscale of all objects that is affected by gravity flipping */
public class FlippingObject {

     /**
     * The y scale of this object (for flipping when gravity swaps)
     */
    private float yScale;

    /** The rate at which the object rotates to its reflected position */
    private static final float ROTATE_RATE = 0.15f;

    public FlippingObject() {
        yScale = 1f;
    }

    /** rotate the object by ROTATE_RATE when it is initially being affected by a gravity switch *
     *
     * @param isFlipped is whether the object is flipped to the ceiling
     */
    public void updateYScale(boolean isFlipped) {
        if (yScale < 1f && !isFlipped) {
            yScale += ROTATE_RATE;
        } else if (yScale > -1f && isFlipped) {
            yScale -= ROTATE_RATE;
        }

        if (yScale > 1f) yScale = 1f;
        if (yScale < -1f) yScale = -1f;
    }

    /** Returns the y scale of the rotation to be used in drawing */
    public float getYScale() {
        return yScale;
    }

}

