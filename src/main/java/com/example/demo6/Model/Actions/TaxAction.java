package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

/**
 * Represents the Tax action in the game, which allows a player to collect 3 coins from the bank
 * by claiming to be the Duke. This action can be challenged by other players who may doubt
 * the claimant's role as the Duke.
 */
public class TaxAction extends Action {

    /**
     * Constructs a TaxAction object.
     *
     * @param player   The player who is attempting to collect tax as the Duke.
     * @param opponent Not used in this context as the tax action is not directly against another player,
     *                 but maintained for consistency with other actions.
     */
    public TaxAction(Player player, Player opponent) {
        super(player, opponent, ActionCode.TAX);
        // Indicates that the Tax action cannot be blocked.
        this.canBeBlocked = false;
        // Indicates that the Tax action can be challenged.
        this.canBeChallenged = true;
    }

    /**
     * Checks if the player can perform the tax action. Since this action is based solely on the claim
     * of being the Duke, it always returns true, but success is contingent on not being successfully challenged.
     *
     * @return true always, indicating that a player can attempt to perform the tax action at any time.
     */
    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    /**
     * Executes the tax action. The action grants 3 coins to the player if their claim to be the Duke
     * is not successfully challenged. If a challenge occurs and the player cannot prove they are the Duke,
     * the action fails, and no coins are collected.
     *
     * @param isChallenged Indicates if the tax attempt is being challenged.
     * @param isBlocked Not used as this action cannot be blocked.
     * @return true if the tax collection is successful (either unchallenged or successfully defended against a challenge),
     *         false if the player fails to defend against a challenge.
     */
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isChallenged && !challenge()) {
            // The player successfully collects 3 coins as tax.
            return false;
        }
        // The player successfully collects 3 coins as tax.
        player.updateCoins(3);
        return true;
    }

    /**
     * Handles the challenge to the tax action by verifying if the player has the Duke card.
     * A successful challenge validates the player's claim and allows the tax collection to proceed.
     *
     * @return true if the player has the Duke card, false if they do not.
     */
    @Override
    public boolean challenge() {
        return player.hasCard(Deck.CardType.DUKE);
    }
}
