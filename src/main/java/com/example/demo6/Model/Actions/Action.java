package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

//TODO : add isBlockable boolean.
public abstract class Action {
    protected Player player;
    protected ActionCode codeOfAction;
    protected boolean isBlockAble;

    public Action(Player player, ActionCode name) {
        this.player = player;
        this.codeOfAction = name;
    }

    // Retrieves the action name
    public ActionCode getActionCode() {
        return codeOfAction;
    }

    // Retrieves the name of the action
    public String getCodeOfAction() {
        return codeOfAction.toString();
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
