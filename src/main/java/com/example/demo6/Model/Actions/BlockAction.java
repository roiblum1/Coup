package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

public class BlockAction extends Action {
    private Action actionToBlock;
    private boolean isBlocked;

    public BlockAction(Player player, Action actionToBlock) {
        super(player, ActionName.BLOCK);
        this.actionToBlock = actionToBlock;
        this.isBlocked = false;
    }

    @Override
    public boolean canPlayerPerform() {
        // Logic to determine if the action can be blocked
        return actionToBlock instanceof ForeignAidAction ||
                actionToBlock instanceof StealAction ||
                actionToBlock instanceof AssassinateAction;
    }

    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (!canPlayerPerform()) {
            this.isBlocked = false;
            return false; // Player cannot block the action
        }

        if (isChallenged) {
            if (!challenge()) {
                // Player failed to block because the challenge was lost
                this.isBlocked = false;
                return false;
            } else {
                // Player successfully blocked the action
                this.isBlocked = true;
                return true;
            }
        } else {
            // Player has blocked the action without challenge
            this.isBlocked = true;
            return true;
        }
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    private boolean challenge() {
        // Check if the player has the appropriate card to block the action
        if (actionToBlock.nameOfAction.equals(ActionName.FOREIGN_AID)) {
            return player.hasCard(new Card(Deck.CardType.DUKE.getName()));
        } else if (actionToBlock.nameOfAction.equals(ActionName.ASSASSINATE)) {
            return player.hasCard(new Card(Deck.CardType.CONTESSA.getName()));
        } else if (actionToBlock.getActionName().equals(ActionName.STEAL)) {
            return player.hasCard(new Card(Deck.CardType.AMBASSADOR.getName())) || player.hasCard(new Card(Deck.CardType.CAPTAIN.getName()));
        } else {
            return false;
        }
    }
}