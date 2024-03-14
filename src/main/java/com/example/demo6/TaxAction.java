package com.example.demo6;

public class TaxAction extends Action {
    public TaxAction(Player player) {
        super(player);
    }

    @Override
    public boolean canPlayerPerform() {
        // For Tax, you might always allow the action, or check if the player is a Duke (depending on your game rules).
        return true; // Assuming any player can claim to take tax for simplicity.
    }

    @Override
    public void execute() {
        if (canPlayerPerform()) {
            player.updateCoins(3); // Player claims to be the Duke and takes 3 coins.
        }
    }

    // Method to handle a challenge - you may need additional logic here depending on how challenges work in your game.
    public boolean challenge() {
        // If the player cannot prove they are the Duke, they might lose the action's benefits or face other consequences.
        return player.hasCard(new Card("Duke")); // Simplified; actual implementation will depend on your game's mechanics.
    }
}
