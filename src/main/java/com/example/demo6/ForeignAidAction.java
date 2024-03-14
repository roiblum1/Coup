package com.example.demo6;

public class ForeignAidAction extends Action {
    public ForeignAidAction(Player player) {
        super(player);
    }

    @Override
    public boolean canPlayerPerform() {
        // Assume true for simplicity; actual logic might involve checking if any opponent will block this.
        return true;
    }

    @Override
    public void execute() {
        if (canPlayerPerform()) {
            player.updateCoins(2); // Player takes 2 coins from the bank.
        }
    }

    // Method to check if the action is blocked by the Duke.
    public boolean isBlockedByDuke() {
        // This method would interact with the game state to check if another player is blocking this action as the Duke.
        // For simplicity, return false; actual implementation would involve game logic to handle this interaction.
        return false;
    }
}

