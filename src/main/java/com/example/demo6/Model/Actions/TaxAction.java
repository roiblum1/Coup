package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

public class TaxAction extends Action {
    public TaxAction(Player player, Player opponent) {
        super(player, opponent, ActionCode.TAX);
        this.canBeBlocked = false;
        this.canBeChallenged = true;
    }

    // Checks if the player can perform the tax action
    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    // Executes the tax action
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isChallenged && !challenge()) {
            return false; // Challenge failed, do not collect taxes
        }
        System.out.println("Before TAX action, coins: " + player.getCoins());
        player.updateCoins(3); // Update coins if no challenge or challenge passed
        System.out.println("After TAX action, coins: " + player.getCoins());

        return true;
    }


    // Handles the challenge to the tax action
    public boolean challenge() {
        return player.hasCard(Deck.CardType.DUKE);
    }
}
