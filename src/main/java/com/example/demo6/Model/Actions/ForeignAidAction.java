package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

public class ForeignAidAction extends Action {
    public ForeignAidAction(Player player) {
        super(player, ActionName.FOREIGN_AID);
    }

    // Checks if the player can perform the foreign aid action
    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    // Executes the foreign aid action
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isBlocked) {
            // The foreign aid action was blocked
            return false;
        } else {
            player.updateCoins(2);
            return true;
        }
    }
}
