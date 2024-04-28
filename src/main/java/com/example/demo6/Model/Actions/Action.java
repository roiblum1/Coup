package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

public abstract class Action {
    protected Player player;
    protected Player opponent;
    protected ActionCode codeOfAction;
    public boolean canBeBlocked;
    public boolean canBeChallenged;

    /**
     * Constructs a new Action with the specified player, opponent, and action code.
     *
     * @param player   The player performing the action.
     * @param opponent The opponent affected by the action.
     * @param code     The action code representing the type of action.
     */
    public Action(Player player, Player opponent, ActionCode code) {
        this.player = player;
        this.codeOfAction = code;
        this.opponent = opponent;
    }

    /**
     * Retrieves the action code.
     *
     * @return The action code representing the type of action.
     */
    public ActionCode getActionCode() {
        return codeOfAction;
    }

    /**
     * Converts the action code to a string representation.
     *
     * @return The string representation of the action code.
     */
    public String actionCodeToString() {
        return codeOfAction.toString();
    }

    /**
     * Checks if the player can perform the action.
     * This method checks the specific requirements for the action, such as having enough coins or the necessary cards.
     *
     * @return true if the player can perform the action, false otherwise.
     */
    public abstract boolean canPlayerPerform();

    /**
     * Executes the action.
     *
     * @param isChallenged Indicates whether the action has been challenged.
     * @param isBlocked    Indicates whether the action has been blocked.
     * @return true if the action is executed successfully, false otherwise.
     */
    public abstract boolean execute(boolean isChallenged, boolean isBlocked);

    /**
     * Challenges the action.
     * This method checks if the player has the necessary card to perform the action.
     *
     * @return true if the challenge is successful (player does not have the required card), false otherwise.
     */
    public abstract boolean challenge();

    /**
     * Retrieves the player associated with the action.
     *
     * @return The player performing the action.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Sets the player associated with the action.
     *
     * @param player The player performing the action.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Sets the opponent affected by the action.
     *
     * @param opponent The opponent affected by the action.
     */
    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }
}