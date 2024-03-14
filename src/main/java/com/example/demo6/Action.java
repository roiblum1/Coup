package com.example.demo6;

public abstract class Action {
    protected Player player;

    public Action(Player player) {
        this.player = player;
    }

    // This method checks if the action can be performed by the player.
    public abstract boolean canPlayerPerform();

    // Executes the action.
    public abstract void execute();
}
