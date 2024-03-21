package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

public class BlockAction extends Action {
    private Action actionToBlock;
    private boolean isBlocked;

    public BlockAction(Player player, Action actionToBlock) {
        super(player, ActionCode.BLOCK);
        this.actionToBlock = actionToBlock;
        this.isBlocked = false;
        this.isBlockAble = false;
    }



    // Checks if the player can perform the blocking action
    //TODO : change instance of to polymorphism
    @Override
    public boolean canPlayerPerform() {
        // Logic to determine if the action can be blocked
        return actionToBlock instanceof ForeignAidAction ||
                actionToBlock instanceof StealAction ||
                actionToBlock instanceof AssassinateAction;
    }

    // Executes the blocking action
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (!canPlayerPerform()) {
            // Player cannot block the action
            this.isBlocked = false;
            return false;
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

    // Checks if the action is blocked
    public boolean isBlocked() {
        return isBlocked;
    }

    @Override
    // Handles the challenge to the blocking action
    //TODO : check with switch
    public boolean challenge() {
        // Check if the player has the appropriate card to block the action
        if (actionToBlock.codeOfAction == (ActionCode.FOREIGN_AID)) {
            return player.hasCard(new Card(Deck.CardType.DUKE.getName()));
        } else if (actionToBlock.codeOfAction == (ActionCode.ASSASSINATE)) {
            return player.hasCard(new Card(Deck.CardType.CONTESSA.getName()));
        } else if (actionToBlock.getActionCode() == (ActionCode.STEAL)) {
            return player.hasCard(new Card(Deck.CardType.AMBASSADOR.getName())) || player.hasCard(new Card(Deck.CardType.CAPTAIN.getName()));
        } else {
            return false;
        }
    }
}
