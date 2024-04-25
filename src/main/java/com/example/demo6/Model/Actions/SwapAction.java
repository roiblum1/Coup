package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

public class SwapAction extends Action {
    public SwapAction(Player player, Player opponent) {
        super(player, opponent, ActionCode.SWAP);
        this.canBeBlocked = false;
        this.canBeChallenged = true;
    }

    // Checks if the player can perform the swap action
    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    // Executes the swap action
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isChallenged && !challenge()) {
            return false; // Correctly handles challenge failure
        }
        // Perform the swap logic here if necessary
        return true;
    }


    // Handles the challenge to the swap action
    @Override
    public boolean challenge() {
        return player.hasCard(Deck.CardType.AMBASSADOR);
    }

}
