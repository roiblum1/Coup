package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

public abstract class Action {
    protected Player player;
    protected ActionName nameOfAction;

    public Action(Player player, ActionName name) {
        this.player = player;
        this.nameOfAction = name;
    }

    // Retrieves the action name
    public ActionName getActionName() {
        return nameOfAction;
    }

    // Retrieves the name of the action
    public String getNameOfAction() {
        return nameOfAction.toString();
    }

    // Checks if the action can be performed by the player
    public abstract boolean canPlayerPerform();

    // Executes the action
    public abstract boolean execute(boolean isChallenged, boolean isBlocked);

    public abstract boolean challenge();

    // Retrieves the player associated with the action
    public Player getPlayer() {
        return this.player;
    }
}
