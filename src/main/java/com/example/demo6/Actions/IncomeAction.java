package com.example.demo6.Actions;

import com.example.demo6.Actions.Action;
import com.example.demo6.Player;

public class IncomeAction extends Action {

    public IncomeAction(Player player) {
        super(player, "income");
    }

    @Override
    public boolean canPlayerPerform() {
        // Check conditions. For Income, you might always return true.
        return true;
    }

    @Override
    public void execute() {
        if (canPlayerPerform()) {
            player.updateCoins(1);
        }
    }
}
