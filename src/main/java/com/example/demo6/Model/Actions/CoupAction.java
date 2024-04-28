package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

/**
 * Represents a Coup action in the game, which is a decisive move a player can make against an opponent.
 * This action forces an opponent to lose an Influence card and cannot be blocked or challenged,
 * reflecting its nature as a powerful, direct attack.
 */
public class CoupAction extends Action {

    /**
     * Constructs a CoupAction object.
     *
     * @param player   The player who is initiating the coup.
     * @param opponent The opponent who is targeted by the coup.
     */
    public CoupAction(Player player, Player opponent) {
        super(player, opponent, ActionCode.COUP);
        // Indicates that the Coup action cannot be blocked.
        this.canBeBlocked = false;
        // Indicates that the Coup action cannot be challenged.
        this.canBeChallenged = false;
    }

    /**
     * Checks if the player has enough coins to perform the coup.
     * A coup requires 7 coins to execute.
     *
     * @return true if the player has at least 7 coins, false otherwise.
     */
    @Override
    public boolean canPlayerPerform() {
        return this.player.getCoins() >= 7;
    }

    /**
     * Executes the coup action. If the player has enough coins, the coup proceeds,
     * deducting 7 coins from the player's total and potentially causing the opponent to lose an Influence card.
     * The specific mechanics of the Influence loss are handled outside this method.
     *
     * @param isChallenged Not applicable as coups cannot be challenged.
     * @param isBlocked    Not applicable as coups cannot be blocked.
     * @return true if the coup was successfully executed (i.e., if the player had enough coins), false otherwise.
     */
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (canPlayerPerform()) {
            player.updateCoins(-7); // Deducts the cost of the coup from the player's coin total.
            return true; // The coup is successful.
        } else {
            return false; // The player does not have enough coins to execute the coup.
        }
    }

    /**
     * The challenge method for a Coup action always returns true as coups cannot be challenged.
     * This implementation reflects the game rule that coups are final and decisive actions.
     *
     * @return true always.
     */
    @Override
    public boolean challenge() {
        return true;
    }
}
