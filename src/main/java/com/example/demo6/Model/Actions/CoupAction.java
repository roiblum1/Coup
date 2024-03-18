package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

public class CoupAction extends Action {
    private Player targetPlayer;

    public CoupAction(Player player, Player targetPlayer) {
        super(player, ActionName.COUP);
        this.targetPlayer = targetPlayer;
    }

    @Override
    public boolean canPlayerPerform() {
        return player.getCoins() >= 7;
    }

    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (canPlayerPerform()) {
            player.updateCoins(-7);
            return true;
        } else {
            return false;
        }
    }
}