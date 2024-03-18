package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Card;
import com.example.demo6.Model.Player;

public class SwapAction extends Action {
    public SwapAction(Player player) {
        super(player, "swap");
    }

    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isChallenged) {
            // If challenged, the player must verify they have the capability (e.g., they have an "Ambassador")
            return challenge(); // Player failed the challenge and cannot swap
        }
        return true;
    }

    private boolean challenge() {
        // Here, you should verify if the player has an "Ambassador" card to successfully swap.
        // The logic depends on your game's specific rules for card visibility and verification.
        return player.hasCard(new Card("Ambassador"));
    }
}