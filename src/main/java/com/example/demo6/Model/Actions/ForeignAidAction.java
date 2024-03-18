package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

public class ForeignAidAction extends Action {
    public ForeignAidAction(Player player) {
        super(player, ActionName.FOREIGN_AID);
    }

    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isBlocked) {
            return false; // The foreign aid action was blocked
        } else {
            player.updateCoins(2);
            return true;
        }
    }
}