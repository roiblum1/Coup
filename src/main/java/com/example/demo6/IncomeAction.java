package com.example.demo6;

public class IncomeAction extends Action {

    public IncomeAction(Player player) {
        super(player);
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
