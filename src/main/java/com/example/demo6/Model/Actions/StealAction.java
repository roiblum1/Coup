package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

public class StealAction extends Action {
    private Player targetPlayer;

    public StealAction(Player player, Player targetPlayer) {
        super(player, ActionCode.STEAL);
        this.targetPlayer = targetPlayer;
        this.canBeBlocked = true;
        this.canBeChallenged = true;
    }

    // Checks if the player can perform the steal action
    @Override
    public boolean canPlayerPerform() {
        // Assuming the player can always attempt to steal, but you might add more conditions here.
        return true;
    }

    // Executes the steal action
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isChallenged) {
            // If challenged, the stealing player must verify they have the capability (e.g., they have a "Captain")
            if (!challenge()) {
                // Player failed the challenge and cannot steal
                return false;
            }
        }
        if (isBlocked) {
            // The steal action was blocked
            return false;
        } else {
            // Perform the steal action
            int stolenCoins = Math.min(2, targetPlayer.getCoins());
            player.updateCoins(stolenCoins);
            targetPlayer.updateCoins(-stolenCoins);
            // The steal action was successful
            return true;
        }
    }

    // Handles the challenge to the steal action
    public boolean challenge() {
      return player.hasCard(Deck.CardType.CAPTAIN);
    }


    // Retrieves the target player of the steal action
    public Player getTargetPlayer() {
        return targetPlayer;
    }
}
