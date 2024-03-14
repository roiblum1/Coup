package com.example.demo6.Actions;

import com.example.demo6.Card;
import com.example.demo6.Player;

public abstract class Action {
    protected Player player;
    protected String NameOfAction;

    public Action(Player player, String name) {
        this.player = player;
        this.NameOfAction = name;
    }

    public String getNameOfAction() {
        return NameOfAction;
    }

    // This method checks if the action can be performed by the player.
    public abstract boolean canPlayerPerform();

    // Executes the action.
    public abstract void execute();

    protected Player getPlayer() {
        return this.player;
    }
}
