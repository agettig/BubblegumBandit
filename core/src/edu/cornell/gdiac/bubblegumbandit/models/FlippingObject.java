package edu.cornell.gdiac.bubblegumbandit.models;

/** A class to manage the yscale of all objects that is affected by gravity flipping */
public class FlippingObject {

     /**
     * The scale of this object (for flipping when gravity swaps)
     */
    private float scale = 1f;

    /** The rate at which the object rotates to its reflected position */
    private float rotateRate = 0.15f;

    public FlippingObject() {
        scale = 1f;
    }

    /** Constructs a FlippingObject with the given rotation rate.
     * Tip: Make slower objects have a slower rotation rate. */
    public FlippingObject(float rotateRate) { super(); this.rotateRate = rotateRate; }

    /** rotate the object by ROTATE_RATE when it is initially being affected by a gravity switch *
     *
     * @param isFlipped is whether the object is flipped to the ceiling
     */
    public void updateYScale(boolean isFlipped) {
        if (scale < 1f && !isFlipped) {
            scale += rotateRate;
        } else if (scale > -1f && isFlipped) {
            scale -= rotateRate;
        }

        if (scale > 1f) scale = 1f;
        if (scale < -1f) scale = -1f;
    }

    /** Returns the y scale of the rotation to be used in drawing */
    public float getScale() {
        return scale;
    }

}

