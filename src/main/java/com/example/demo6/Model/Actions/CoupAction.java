package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

public class CoupAction extends Action {
    private Player targetPlayer;

    public CoupAction(Player player, Player targetPlayer) {
        super(player, ActionCode.COUP);
        this.targetPlayer = targetPlayer;
        this.canBeBlocked = false;
    }

    // Checks if the player can perform the coup action
    @Override
    public boolean canPlayerPerform() {
        return player.getCoins() >= 7;
    }

    // Executes the coup action
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (canPlayerPerform()) {
            player.updateCoins(-7);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean challenge() {
        return true;
    }
}
