package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

public class SwapAction extends Action {
    public SwapAction(Player player) {
        super(player, ActionName.SWAP);
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
    private boolean challenge() {
        return player.hasCard(new Card(Deck.CardType.AMBASSADOR.getName()));
    }
}
