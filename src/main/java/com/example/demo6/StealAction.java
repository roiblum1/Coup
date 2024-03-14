package com.example.demo6;

public class StealAction extends Action {
    private Player targetPlayer;

    public StealAction(Player player, Player targetPlayer) {
        super(player);
        this.targetPlayer = targetPlayer;
    }

    @Override
    public boolean canPlayerPerform() {
        // Assuming a player can always attempt to steal, but you might have conditions like having a specific card.
        return true;
    }

    @Override
    public void execute() {
        if (canPlayerPerform()) {
            int stolenCoins = Math.min(2, targetPlayer.getCoins());
            player.updateCoins(stolenCoins);
            targetPlayer.updateCoins(-stolenCoins);
        }
    }
}
