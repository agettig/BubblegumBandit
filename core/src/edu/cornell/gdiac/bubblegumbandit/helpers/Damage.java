package edu.cornell.gdiac.bubblegumbandit.helpers;


/**
 * Class to store damage-related constants.
 * */
public class Damage {


    // --- BEGIN REGION: ENVIRONMENT ---

    /** Damage dealt when a block falls on the Bandit. */
    public static final float CRUSH_DAMAGE = 20f;

    /** Damage dealt when the Bandit touches an environmental hazard. */
    public static final float HAZARD_DAMAGE = 20f;

    // --- END REGION: ENVIRONMENT ---


    // --- BEGIN REGION: ENEMIES ---

    /** Damage dealt when the Bandit touches an environmental hazard. */
    public static final float LASER_JUMP_DAMAGE = 30f;

    /** Damage dealt when the Bandit touches a LaserEnemy's laser. */
    public static final float LASER_TICK_DAMAGE = .75f;

    /** Damage dealt when a RollingEnemy rolls into the Bandit. */
    public static final float ROLLING_HIT_DAMAGE = 25f;

    /** Initial shock damage*/
    public static final float SHOCK_DAMAGE = 20f;

    /** Continued shock damage*/
    public static final float DPS_ON_SHOCK = 10f;

    // --- END REGION: ENEMIES ---

}
