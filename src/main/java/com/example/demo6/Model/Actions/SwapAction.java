package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

public class SwapAction extends Action {
    public SwapAction(Player player) {
        super(player, ActionCode.SWAP);
        this.canBeBlocked = false;
    }

    // Checks if the player can perform the swap action
    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    // Executes the swap action
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isChallenged) {
            // Player failed the challenge and cannot swap
            return challenge();
        }
        return true;
    }

    // Handles the challenge to the swap action
    @Override
    public boolean challenge() {
        return player.hasCard(Deck.CardType.AMBASSADOR);
    }

}
