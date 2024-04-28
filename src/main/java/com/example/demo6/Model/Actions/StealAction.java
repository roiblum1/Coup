package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

/**
 * Represents the Steal action in the game, which allows a player to take coins from an opponent.
 * This action can be challenged and blocked, making it a strategic and risky move.
 */
public class StealAction extends Action {

    /**
     * Constructs a StealAction object.
     *
     * @param player   The player who is initiating the steal.
     * @param opponent The opponent from whom the coins are being stolen.
     */
    public StealAction(Player player, Player opponent) {
        super(player, opponent, ActionCode.STEAL);
        // Indicates that the Steal action can be blocked by a player claiming to have the Ambassador or another Captain.
        this.canBeBlocked = true;
        // Indicates that the Steal action can be challenged if there is doubt the player possesses a Captain.
        this.canBeChallenged = true;
    }

    /**
     * Checks if the player can perform the steal action.
     * In the current implementation, a player can always attempt to steal, but additional conditions may be applied.
     *
     * @return true, indicating the player can attempt to perform the steal, subject to further conditions.
     */
    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    /**
     * Executes the steal action. If the action is challenged, the player must prove they have a Captain.
     * If the action is blocked or if the challenge is lost, the steal fails. Otherwise, the player steals up to 2 coins.
     *
     * @param isChallenged Indicates if the steal attempt is being challenged.
     * @param isBlocked Indicates if the steal attempt is being blocked.
     * @return true if the steal is successful and not blocked or unsuccessfully challenged, false otherwise.
     */
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isChallenged) {
            if (!challenge()) {
                // The challenge was unsuccessful, and the player cannot steal.
                return false;
            }
        }
        if (isBlocked) {
            // The steal action was successfully blocked.
            return false;
        } else {
            // Steal up to 2 coins, or the opponent's total coins if fewer than 2.
            int stolenCoins = Math.min(2, opponent.getCoins());
            player.updateCoins(stolenCoins);
            opponent.updateCoins(-stolenCoins);
            return true;
        }
    }

    /**
     * Handles the challenge to the steal action by verifying if the player has the Captain card.
     *
     * @return true if the player has the Captain card, false otherwise.
     */
    @Override
    public boolean challenge() {
        return player.hasCard(Deck.CardType.CAPTAIN);
    }
}
