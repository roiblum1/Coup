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

    @Override
    public boolean canPlayerPerform() {
        return player.getCoins() >= 3;
    }

    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (!canPlayerPerform()) {
            return false;
        }

        if (isChallenged) {
            if (!challenge()) {
                return false; // Assassination attempt fails due to unsuccessful challenge
            }
        }

        if (isBlocked) {
            return false; // Assassination attempt is blocked
        }

        // If the method reaches this point, the assassination attempt is neither blocked nor failed
        return true;
    }


    public boolean challenge() {
        // Here, you should verify if the player has an "Assassin" card to successfully assassinate.
        // The logic depends on your game's specific rules for card visibility and verification.
        return player.hasCard(new Card(Deck.CardType.ASSASSIN.getName()));
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }
}