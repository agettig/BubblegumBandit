package edu.cornell.gdiac.bubblegumbandit.models;

/** A class to manage the yscale of all objects that is affected by gravity flipping */
public class FlippingObject {

     /**
     * The scale of this object (for flipping when gravity swaps)
     */
    private float scale;

    /** The rate at which the object rotates to its reflected position */
    private static final float ROTATE_RATE = 0.15f;

    public FlippingObject() {
        scale = 1f;
    }

    /** rotate the object by ROTATE_RATE when it is initially being affected by a gravity switch *
     *
     * @param isFlipped is whether the object is flipped to the ceiling
     */
    public void updateYScale(boolean isFlipped) {
        if (scale < 1f && !isFlipped) {
            scale += ROTATE_RATE;
        } else if (scale > -1f && isFlipped) {
            scale -= ROTATE_RATE;
        }

        if (scale > 1f) scale = 1f;
        if (scale < -1f) scale = -1f;
    }

    /** Returns the y scale of the rotation to be used in drawing */
    public float getScale() {
        return scale;
    }

}

