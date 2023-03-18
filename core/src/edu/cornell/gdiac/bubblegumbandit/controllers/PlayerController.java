package edu.cornell.gdiac.bubblegumbandit.controllers;


import edu.cornell.gdiac.bubblegumbandit.models.player.BanditModel;

/**
 * Controls the Bandit.
 * */
public class PlayerController {

    /**the bandit's BanditModel */
    private BanditModel bandit;

    /**
     * Creates the PlayerController that controls the
     * Bubblegum Bandit.
     *
     * @param bandit the bandit's BanditModel
     * */
    public PlayerController(BanditModel bandit){
        this.bandit = bandit;
    }

    /**
     * Main update loop for the PlayerController.
     *
     * <p>Attempts to move the bandit horizontally.</p>
     * */
    public void update(){
        moveHorizontal();
    }

    /**
     * Moves the Bandit character horizontally based on the InputController's horizontal input.
     * Applies force to the Bandit, causing it to move in the horizontal direction.
     * This method should be called within the game loop for continuous movement.
     */
    private void moveHorizontal() {
        InputController instance = InputController.getInstance();
        float movement = instance.getHorizontal() * bandit.getForce();
        bandit.setMovement(movement);
        bandit.applyForce();
    }

    /**
     * Checks if the Bandit character is grounded (i.e., in contact with a surface).
     * This method can be used for determining whether the Bandit can jump or perform other actions.
     *
     * @return true if the Bandit is grounded, false otherwise.
     */
    public boolean banditGrounded() {
        return bandit.isGrounded();
    }

    /**
     * Flips the gravity of the Bandit character, causing it to be affected by gravity in the opposite direction.
     * This method also sets the Bandit character's grounded state to false, as it's no longer in contact with the ground.
     * Call this method when you want the Bandit to switch between walking on the ground and the ceiling.
     */
    public void flipBandit() {
        bandit.flippedGravity();
        bandit.setGrounded(false);
    }
}
