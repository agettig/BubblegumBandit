package edu.cornell.gdiac.bubblegumbandit.controllers;

import edu.cornell.gdiac.bubblegumbandit.models.enemy.EnemyModel;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.MovingEnemyModel;

/**
 * Controller for an Enemy.
 * */
public class AIController {

    /** Enemy controlled by this AIController*/
    private EnemyModel enemy;

    /**
     * Creates an AIController.
     *
     * @param enemy the enemy this AIController controls.
     * */
    public AIController(EnemyModel enemy){
        this.enemy = enemy;
    }

    /**
     * Main update loop for the AI Controller.
     * */
    public void update(){
        enemy.update();
    }

    /**
     * Flips the EnemyModel's gravity.
     * */
    public void flipEnemy(){
        enemy.flippedGravity();
    }

    /**
     * Sets the vision range of the enemy.
     * Adjusts the range of the enemy's vision and updates the
     * vision object accordingly.
     *
     * @param range  The new vision range (in units) for the enemy.
     */
    public void setVisionRange(float range) {
        enemy.vision.setRange(range);
        enemy.updateVision();
    }

    /**
     * Sets the vision radius of the enemy.
     * Adjusts the radius of the enemy's vision and updates the
     * vision object accordingly.
     *
     * @param radius  The new vision radius (in units) for the enemy.
     */
    public void setVisionRadius(float radius) {
        enemy.vision.setRadius(radius);
        enemy.updateVision();
    }

    /**
     * Sets the movement speed of the enemy, if it's an
     * instance of MovingEnemyModel.
     * If the enemy is not an instance of MovingEnemyModel,
     * the method will
     * return without making any changes.
     *
     * @param moveSpeed  The new movement speed for the enemy.
     */
    public void setMovementSpeed(float moveSpeed) {
        if (!(enemy instanceof MovingEnemyModel)) return;
        MovingEnemyModel movingEnemyModel = (MovingEnemyModel) enemy;
        movingEnemyModel.setMoveSpeed(moveSpeed);
    }
}
