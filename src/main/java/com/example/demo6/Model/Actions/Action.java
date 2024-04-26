package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

public abstract class Action {
    protected Player player;
    protected Player opponent;
    protected ActionCode codeOfAction;
    public boolean canBeBlocked;
    public boolean canBeChallenged;

    public Action(Player player, Player opponent, ActionCode code) {
        this.player = player;
        this.codeOfAction = code;
        this.opponent = opponent;
    }

    // Retrieves the action name
    public ActionCode getActionCode() {
        return codeOfAction;
    }

    public String actionCodeToString() {
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

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }
}
