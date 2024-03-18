package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

public abstract class Action {
    protected Player player;
    protected String nameOfAction;

    public Action(Player player, String name) {
        this.player = player;
        this.nameOfAction = name;
    }

    public String getNameOfAction() {
        return nameOfAction;
    }

    // This method checks if the action can be performed by the player.
    public abstract boolean canPlayerPerform();

    // Executes the action.
    public abstract boolean execute(boolean isChallenged, boolean isBlocked);

    protected Player getPlayer() {
        return this.player;
    }

}