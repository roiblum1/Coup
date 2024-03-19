package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

public class AssassinateAction extends Action {
    private Player targetPlayer;

    public AssassinateAction(Player player, Player targetPlayer) {
        super(player, ActionName.ASSASSINATE);
        this.targetPlayer = targetPlayer;
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
        return true;
    }

    @Override
    // check if the player is capable of do
    public boolean challenge() {
        return player.hasCard(new Card(Deck.CardType.ASSASSIN.getName()));
    }

    // Retrieves the target player of the assassination
    public Player getTargetPlayer() {
        return targetPlayer;
    }
}
