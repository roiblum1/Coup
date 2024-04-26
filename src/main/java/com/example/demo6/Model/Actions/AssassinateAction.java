package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

public class AssassinateAction extends Action {


    public AssassinateAction(Player player, Player opponent) {
        super(player ,opponent, ActionCode.ASSASSINATE);
        this.canBeBlocked = true;
        this.canBeChallenged = true;
    }

    // Checks if the player can perform the assassination action
    @Override
    public boolean canPlayerPerform() {
        return player.getCoins() >= 3;
    }

    // Executes the assassination action
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

    @Override
    // check if the player is capable of do
    public boolean challenge() {
        return player.hasCard(Deck.CardType.ASSASSIN);
    }

}
