package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

public class CoupAction extends Action {


    public CoupAction(Player player, Player opponent) {
        super(player ,opponent ,ActionCode.COUP);
        this.canBeBlocked = false;
        this.canBeChallenged = false;
    }

    // Checks if the player can perform the coup action
    @Override
    public boolean canPlayerPerform() {
        return this.player.getCoins() >= 7;
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
