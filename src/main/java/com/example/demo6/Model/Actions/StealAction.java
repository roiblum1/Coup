package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Card;
import com.example.demo6.Model.Player;

public class StealAction extends Action {
    private Player targetPlayer;

    public StealAction(Player player, Player targetPlayer) {
        super(player, "steal");
        this.targetPlayer = targetPlayer;
    }

    @Override
    public boolean canPlayerPerform() {
        // Assuming the player can always attempt to steal, but you might add more conditions here.
        return true;
    }

    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isChallenged) {
            // If challenged, the stealing player must verify they have the capability (e.g., they have a "Captain")
            if (!challenge()) {
                return false; // Player failed the challenge and cannot steal
            }
        }

        if (isBlocked) {
            return false; // The steal action was blocked
        } else {
            // Perform the steal action
            int stolenCoins = Math.min(2, targetPlayer.getCoins());
            player.updateCoins(stolenCoins);
            targetPlayer.updateCoins(-stolenCoins);
            return true; // The steal action was successful
        }
    }

    private boolean challenge() {
        // Here, you should verify if the player has a "Captain" card to successfully steal.
        // The logic depends on your game's specific rules for card visibility and verification.
        return player.hasCard(new Card("Captain"));
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }
}