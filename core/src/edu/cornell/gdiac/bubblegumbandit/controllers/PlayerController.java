/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.bubblegumbandit.helpers.SaveData;
import edu.cornell.gdiac.util.Controllers;
import edu.cornell.gdiac.util.XBoxController;

/**
 * Class for reading player input.
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */

public class PlayerController{
    // Sensitivity for moving crosshair with gameplay
    private static final float GP_ACCELERATE = 1.0f;
    private static final float GP_MAX_SPEED  = 10.0f;
    private static final float GP_THRESHOLD  = 0.01f;

    /** The singleton instance of the input controller */
    private static PlayerController theController = null;

    /**
     * Return the singleton instance of the input controller
     *
     * @return the singleton instance of the input controller
     */
    public static PlayerController getInstance() {
        if (theController == null) {
            theController = new PlayerController();
        }
        return theController;
    }

    // Fields to manage buttons
    /**Whether the gum reload button was pressed */
    private boolean reloadPressed;
    /** Whether the reset button was pressed. */
    private boolean resetPressed;
    private boolean resetPrevious;
    /** Whether the button to advanced worlds was pressed. */
    private boolean nextPressed;
    private boolean nextPrevious;
    /** Whether the button to step back worlds was pressed. */
    private boolean prevPressed;
    private boolean prevPrevious;
    /** Whether the primary action button was pressed. */
    private boolean primePressed;
    private boolean primePrevious;
    /** Whether the secondary action button was pressed. */
    private boolean secondPressed;
    private boolean secondPrevious;
    /** Whether the shoot button was pressed. */
    private boolean shootPressed;
    private boolean shootPrevious;
    /** Whether the debug toggle was pressed. */
    private boolean debugPressed;
    private boolean debugPrevious;
    /** Whether the camera mode toggle was pressed. */
    private boolean cameraPressed;
    private boolean cameraPrevious;
    /** Whether the controls toggle was pressed. */
    private boolean controlTogglePressed;
    private boolean controlTogglePrevious;

    /** Whether the exit button was pressed. */
    private boolean exitPressed;
    private boolean exitPrevious;

    /** Whether the minimap button was pressed.*/
    private boolean minimapPressed;
    private  boolean minimapPrevious;

    /** How much did we move horizontally? */
    private float horizontal;
    /** How much did we move vertically? */
    private float vertical;
    /** The crosshair position (for raddoll) */
    private Vector2 crosshair;
    /** The crosshair cache (for using as a return value) */
    private Vector2 crosscache;
    /** For the gamepad crosshair control */
    private float momentum;

    private boolean pausePressed;

    private boolean pausePrevious;

    /** If gravity was pulled down */
    private boolean gravityDown;
    private boolean gravityDownPrevious;

    /** If gravity was pulled up / toggled */
    private boolean gravityUp;
    private boolean gravityUpPrevious;

    private boolean unstickPressed;
    private boolean unstickPrevious;

    /** If gum was collected */
    private boolean collect;

    private static int[] keyBindings;

    /** An X-Box controller (if it is connected) */
    XBoxController xbox;

    /**
     * Returns the amount of sideways movement.
     *
     * -1 = left, 1 = right, 0 = still
     *
     * @return the amount of sideways movement.
     */
    public float getHorizontal() {
        return horizontal;
    }

    /**
     * Returns the amount of vertical movement.
     *
     * -1 = down, 1 = up, 0 = still
     *
     * @return the amount of vertical movement.
     */
    public float getVertical() {
        return vertical;
    }

    /**
     * Returns the current position of the crosshairs on the screen.
     *
     * This value does not return the actual reference to the crosshairs position.
     * That way this method can be called multiple times without any fair that
     * the position has been corrupted.  However, it does return the same object
     * each time.  So if you modify the object, the object will be reset in a
     * subsequent call to this getter.
     *
     * @return the current position of the crosshairs on the screen.
     */
    public Vector2 getCrossHair() {
        return crosscache.set(crosshair);
    }

    /** Returns true if the player swapped gravity upwards / toggled gravity */
    public boolean getGravityUp(){return gravityUp && !gravityUpPrevious;};

    /** Returns true if the player swapped gravity downwards */
    public boolean getGravityDown(){return gravityDown && !gravityDownPrevious;};

    /** Returns true if the player inputted some command to collect Bubblegum.
     *
     * @returns true if the player input maps to collecting Bubblegum.
     * */
    public boolean didCollect(){return collect;}

    /**
     * Returns true if the primary action button was pressed.
     *
     * This is a one-press button. It only returns true at the moment it was
     * pressed, and returns false at any frame afterwards.
     *
     * @return true if the primary action button was pressed.
     */
    public boolean didPrimary() {
        return primePressed && !primePrevious;
    }

    /** Returns true if pause was pressed*/
    public boolean getPause() {
        return pausePressed && !pausePrevious;
    }

    /**
     *
     * */
    public static void changeControls(int[] v){
        keyBindings = v;
    }

    /**
     * Returns true if the secondary action button was pressed.
     *
     * This is a one-press button. It only returns true at the moment it was
     * pressed, and returns false at any frame afterwards.
     *
     * @return true if the secondary action button was pressed.
     */
    public boolean didSecondary() {
        return secondPressed && !secondPrevious;
    }

    /**
     * Returns true if the shoot action was pressed.
     *1
     * This is a one-press button. It only returns true at the moment it was
     * pressed, and returns false at any frame afterwards.
     *
     * @return true if the shoot button was pressed.
     */
    public boolean didShoot() {
        return shootPressed && !shootPrevious;
    }

    public boolean didUnstick() { return unstickPressed && !unstickPrevious; }

    /** Returns x coordinate of mouse click*/
    public int getX() {
        return Gdx.input.getX();
    }

    /**Returns y coordinate of mouse click */
    public int getY() {
        return Gdx.input.getY();
    }

    /**
     * Returns true if the reset button was pressed.
     *
     * @return true if the reset button was pressed.
     */
    public boolean didReset() {
        return resetPressed && !resetPrevious;
    }

    public boolean didExpandMinimap() {return minimapPressed;}

    public boolean didReload() {return reloadPressed;}

    public boolean didPause() {return pausePressed & !pausePrevious;}

    /**
     * Returns true if the player wants to go to the next level.
     *
     * @return true if the player wants to go to the next level.
     */
    public boolean didAdvance() {
        return nextPressed && !nextPrevious;
    }

    /**
     * Returns true if the player wants to go to the previous level.
     *
     * @return true if the player wants to go to the previous level.
     */
    public boolean didRetreat() {
        return prevPressed && !prevPrevious;
    }

    /**
     * Returns true if the player wants to go toggle the debug mode.
     *
     * @return true if the player wants to go toggle the debug mode.
     */
    public boolean didDebug() {
        return debugPressed && !debugPrevious;
    }

    /**
     * Returns true if the player wants to go toggle the camera mode.
     *
     * @return true if the player wants to go toggle the camera mode.
     */
    public boolean didCameraSwap() {
        return cameraPressed && !cameraPrevious;
    }

    /**
     * Returns true if the player wants to go toggle the full camera view.
     *
     * @return true if the player wants to go toggle the full camera view.
     */
    public boolean didControlsSwap() {
        return controlTogglePressed && !controlTogglePrevious;
    }

    /**
     * Returns true if the exit button was pressed.
     *
     * @return true if the exit button was pressed.
     */
    public boolean didExit() {
        return exitPressed && !exitPrevious;
    }

    /**
     * Creates a new input controller
     *
     * The input controller attempts to connect to the X-Box controller at device 0,
     * if it exists.  Otherwise, it falls back to the keyboard control.
     */
    public PlayerController() {
        // If we have a game-pad for id, then use it.
        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get( 0 );
        } else {
            xbox = null;
        }
        crosshair = new Vector2();
        crosscache = new Vector2();
        keyBindings = SaveData.getKeyBindings();
        //change + fetch keyBindings from save data
    }

    /**
     * Reads the input for the player and converts the result into game logic.
     *
     * Saves crosshairs as screen coordinates, as they cannot be properly converted
     * into physics coordinates without reference to the current camera.
     *

     */
    public void readInput() {
        // Copy state from last animation frame
        // Helps us ignore buttons that are held down
        primePrevious  = primePressed;
        secondPrevious = secondPressed;
        shootPrevious = shootPressed;
        unstickPrevious = unstickPressed;
        resetPrevious  = resetPressed;
        minimapPrevious = minimapPressed;
        debugPrevious  = debugPressed;
        pausePrevious = pausePressed;
        exitPrevious = exitPressed;
        nextPrevious = nextPressed;
        prevPrevious = prevPressed;
        cameraPrevious = cameraPressed;
        controlTogglePrevious = controlTogglePressed;
        gravityDownPrevious = gravityDown;
        gravityUpPrevious = gravityUp;

        // Check to see if a GamePad is connected
        if (xbox != null && xbox.isConnected()) {
            readGamepad();
            readKeyboard( true); // Read as a back-up
        } else {
            readKeyboard(false);
        }
    }

    /**
     * Reads input from an X-Box controller connected to this computer.
     * * Saves crosshairs as screen coordinates, as they cannot be properly converted
     * 	 * into physics coordinates without reference to the current camera.
     *

     */
    private void readGamepad() {
        resetPressed = xbox.getStart();
        exitPressed  = xbox.getBack();
        nextPressed  = xbox.getRBumper();
        prevPressed  = xbox.getLBumper();
        primePressed = xbox.getA();
        debugPressed  = xbox.getY();

        // Increase animation frame, but only if trying to move
        horizontal = xbox.getLeftX();
        vertical   = xbox.getLeftY();
        secondPressed = xbox.getRightTrigger() > 0.6f;

        // Move the crosshairs with the right stick.
        shootPressed = xbox.getRightTrigger() > 0.6f;
        crosscache.set(xbox.getLeftX(), xbox.getLeftY());
        if (crosscache.len2() > GP_THRESHOLD) {
            momentum += GP_ACCELERATE;
            momentum = Math.min(momentum, GP_MAX_SPEED);
            crosscache.scl(momentum);
            //crosscache.scl(1/scale.x,1/scale.y);
            crosshair.add(crosscache);
        } else {
            momentum = 0;
        }

    }

    /**
     * Reads input from the keyboard.
     *
     * This controller reads from the keyboard regardless of whether or not an X-Box
     * controller is connected.  However, if a controller is connected, this method
     * gives priority to the X-Box controller.
     *
     * @param secondary true if the keyboard should give priority to a gamepad
     */
    private void readKeyboard( boolean secondary) {
        // Give priority to gamepad results
        /*the key bindings are as follows:
        0: left
        1: right
        2: grav up
        3: grav down
        4: minimap
        5: reload (and you can't be moving at the time? why doesn't it just stop you from moving?)
        6: shoot
        7: unstick
        8: pause

        */
        // TODO remove
        resetPressed = (secondary && resetPressed) || (Gdx.input.isKeyPressed(Input.Keys.NUM_4));
        debugPressed = (secondary && debugPressed) || (Gdx.input.isKeyPressed(Input.Keys.NUM_1));
        primePressed = (secondary && primePressed) || (Gdx.input.isKeyPressed(Input.Keys.UP)) ||
                (Gdx.input.isKeyPressed(Input.Keys.W) || (Gdx.input.isKeyPressed(Input.Keys.SPACE)));
        secondPressed = (secondary && secondPressed) || (Gdx.input.isKeyPressed(Input.Keys.SPACE));
        nextPressed = (secondary && nextPressed) || (Gdx.input.isKeyPressed(Input.Keys.NUM_0));
        prevPressed = (secondary && prevPressed) || (Gdx.input.isKeyPressed(Input.Keys.NUM_9));
        collect = (secondary && exitPressed) || (Gdx.input.isKeyPressed(Input.Keys.C));
        cameraPressed = (secondary && cameraPressed) || (Gdx.input.isKeyPressed(Input.Keys.NUM_2));
        controlTogglePressed = (secondary && controlTogglePressed) || (Gdx.input.isKeyPressed(Input.Keys.NUM_3));

        // Directional controls
        horizontal = (secondary ? horizontal : 0.0f);
        if (Gdx.input.isKeyPressed(keyBindings[1])) {
            horizontal += 1.0f;
        }
        if (Gdx.input.isKeyPressed(keyBindings[0])) {
            horizontal -= 1.0f;
        }
        reloadPressed = Gdx.input.isKeyPressed(keyBindings[5]) && !primePressed && !Gdx.input.isKeyPressed(
            keyBindings[1]) && !Gdx.input.isKeyPressed(keyBindings[0]);


        minimapPressed = (secondary && minimapPressed) || (Gdx.input.isKeyPressed(keyBindings[4]));
        gravityUp = (secondary && gravityUp) || (Gdx.input.isKeyPressed(keyBindings[2]));
        gravityDown = (secondary && gravityDown) || (Gdx.input.isKeyPressed(keyBindings[3]));
        pausePressed = (secondary && pausePressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));


        // Mouse results
        shootPressed = (secondary && shootPressed) || (Gdx.input.isButtonPressed(keyBindings[6]));
        unstickPressed = (secondary && unstickPressed) || (Gdx.input.isButtonPressed(keyBindings[7]));
        crosshair.set(Gdx.input.getX(), Gdx.input.getY());

    }
}