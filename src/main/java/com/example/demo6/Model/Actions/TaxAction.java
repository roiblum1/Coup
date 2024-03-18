package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Card;
import com.example.demo6.Model.Player;

public class TaxAction extends Action {
    public TaxAction(Player player) {
        super(player, "tax");
    }

    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isChallenged) {
            // If challenged, the player must verify they have the capability (e.g., they have a "Duke")
            if (!challenge()) {
                return false; // Player failed the challenge and cannot collect tax
            }
        }

        player.updateCoins(3);
        return true;
    }

    private boolean challenge() {
        // Here, you should verify if the player has a "Duke" card to successfully collect tax.
        // The logic depends on your game's specific rules for card visibility and verification.
        return player.hasCard(new Card("Duke"));
    }
}