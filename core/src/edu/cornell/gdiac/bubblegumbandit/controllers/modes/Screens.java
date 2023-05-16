package edu.cornell.gdiac.bubblegumbandit.controllers.modes;

/** Class to store integer identifiers for different modes. */
public class Screens {

    /**Identifier to quit the game */
    public static int EXIT_CODE = 0;

    /**Identifier to enter the loading mode */
    public static int LOADING_SCREEN = 1;

    /**Identifier to enter the level select mode */
    public static int LEVEL_SELECT = 2;

    /**Identifier to enter the credits mode */
    public static int CREDITS = 3;

    /**Identifier to enter the settings mode */
    public static int SETTINGS = 4;

    /**Identifier for the controller */
    public static int CONTROLLER = 5;

    /**Identifier for the game win state */
    public static int GAME_WON = 6;

    /**Identifier for the game lose state */
    public static int GAME_LOST = 7;

    /**Identifier for the game resume state */
    public static int RESUME_CONTROLLER = 8;
}
