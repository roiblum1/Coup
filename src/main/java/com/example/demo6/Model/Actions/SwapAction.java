package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

/**
 * Represents the Swap action in the game, allowing a player to swap cards with the deck.
 * This action is intended to refresh the player's hand with potentially more beneficial influence cards.
 * The swap can be challenged if there is doubt that the player has an Ambassador.
 */
public class SwapAction extends Action {

    /**
     * Constructs a SwapAction object.
     *
     * @param player   The player who is initiating the swap.
     * @param opponent Not directly relevant in this context as the action is not directly against another player,
     *                 but maintained for consistency with other actions.
     */
    public SwapAction(Player player, Player opponent) {
        super(player, opponent, ActionCode.SWAP);
        // Indicates that the Swap action cannot be blocked.
        this.canBeBlocked = false;
        // Indicates that the Swap action can be challenged.
        this.canBeChallenged = true;
    }

    /**
     * Checks if the player can perform the swap action.
     * Since there are no specific conditions limiting this action other than possessing an Ambassador if challenged,
     * this method always returns true.
     *
     * @return true always, indicating that a player can attempt to perform a swap at any time.
     */
    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    /**
     * Executes the swap action. This method only returns false if the action is challenged and the player fails to prove
     * they possess an Ambassador card. If the challenge is not made or if the player successfully proves their card,
     * the action proceeds.
     *
     * @param isChallenged Indicates if the swap attempt is being challenged.
     * @param isBlocked Not used as this action cannot be blocked.
     * @return true if the swap is successful (either unchallenged or successfully defended against a challenge),
     *         false if the player fails to defend against a challenge.
     */
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isChallenged && !challenge()) {
            // The swap fails because the player could not validate possession of an Ambassador.
            return false;
        }
        // Because the action required interaction with the player, it being handled in the GameClass
        return true;
    }

    /**
     * Handles the challenge to the swap action by verifying if the player has an Ambassador card.
     * A successful challenge validates the player's right to perform the swap.
     *
     * @return true if the player has an Ambassador card, false if they do not.
     */
    @Override
    public boolean challenge() {
        return player.hasCard(Deck.CardType.AMBASSADOR);
    }
}
