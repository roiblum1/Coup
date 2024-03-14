package com.example.demo6;

public class Action {
    String name;
    public Action(String name)
    {
        this.name = name;
    }
    public void execute(Player player)
    {
        if (this.name.equals("income"))
        {
            player.updateCoins(1);
        }

    }

    public boolean canPlayerPerform(Player player) {
        return true;
    }
}
