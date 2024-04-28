package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

/**
 * Represents an assassination action in the game, which a player can perform against an opponent.
 * This action requires the player to have at least 3 coins and can be challenged or blocked by other players.
 * If the assassination is successful, the targeted opponent will lose an Influence card.
 */
public class AssassinateAction extends Action {

    /**
     * Constructs an AssassinateAction object.
     *
     * @param player   The player who is performing the assassination.
     * @param opponent The opponent who is being targeted for assassination.
     */
    public AssassinateAction(Player player, Player opponent) {
        super(player, opponent, ActionCode.ASSASSINATE);
        this.canBeBlocked = true;
        this.canBeChallenged = true;
    }

    /**
     * Checks if the player has enough coins to perform the assassination.
     *
     * @return true if the player has at least 3 coins, false otherwise.
     */
    @Override
    public boolean canPlayerPerform() {
        return player.getCoins() >= 3;
    }

    /**
     * Executes the assassination action, considering whether it is challenged or blocked.
     * The action costs the player 3 coins. If the action is not challenged or blocked successfully,
     * the opponent will lose an Influence card.
     *
     * @param isChallenged Indicates if the action is being challenged.
     * @param isBlocked    Indicates if the action is being blocked.
     * @return true if the action is successfully executed, false if it is challenged unsuccessfully or blocked.
     */
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (!canPlayerPerform()) {
            return false;
        }
        if (isChallenged) {
            if (!challenge()) {
                // Assassination attempt fails due to unsuccessful challenge
                return false;
            }
        }
        if (isBlocked) {
            // Assassination attempt is blocked
            return false;
        }
        // If the method reaches this point, the assassination attempt is neither blocked nor failed
        player.updateCoins(-3);
        return true;
    }

    /**
     * Challenges the player's claim to perform the assassination.
     * The challenge is successful if the player actually has an Assassin card.
     *
     * @return true if the player has an Assassin card, false otherwise.
     */
    @Override
    public boolean challenge() {
        return player.hasCard(Deck.CardType.ASSASSIN);
    }

}
