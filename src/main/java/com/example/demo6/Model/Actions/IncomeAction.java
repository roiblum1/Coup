package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

public class IncomeAction extends Action {
    public IncomeAction(Player player) {
        super(player, "income");
    }

    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        player.updateCoins(1);
        return true;
    }
}