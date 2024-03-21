package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

public class IncomeAction extends Action {
    public IncomeAction(Player player) {
        super(player, ActionCode.INCOME);
        this.isBlockAble = false;
    }

    // Checks if the player can perform the income action
    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    // Executes the income action
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        player.updateCoins(1);
        return true;
    }

    @Override
    public boolean challenge() {
        return true;
    }
}
