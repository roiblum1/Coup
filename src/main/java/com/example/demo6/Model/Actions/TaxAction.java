package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

public class TaxAction extends Action {
    public TaxAction(Player player) {
        super(player, ActionCode.TAX);
        this.isBlockAble = false;
    }

    // Checks if the player can perform the tax action
    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    // Executes the tax action
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isChallenged) {
            if (!challenge()) {
                // Player failed the challenge and cannot collect tax
                return false;
            }
        }

        player.updateCoins(3);
        return true;
    }

    // Handles the challenge to the tax action
    public boolean challenge() {
        return player.hasCard(new Card(Deck.CardType.DUKE.getName()));
    }
}
