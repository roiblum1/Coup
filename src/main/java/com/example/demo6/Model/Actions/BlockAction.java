package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

public class BlockAction extends Action {
    private Action actionToBlock;
    private boolean isBlocked;

    public BlockAction(Player player, Player opponent, Action actionToBlock) {
        super(player, opponent, ActionCode.BLOCK);
        this.actionToBlock = actionToBlock;
        this.isBlocked = false;
        this.canBeBlocked = false;
        this.canBeChallenged = true;
    }



    // Checks if the player can perform the blocking action
    @Override
    public boolean canPlayerPerform() {
        // Logic to determine if the action can be blocked
        return actionToBlock.canBeBlocked;
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
    public boolean challenge() {
        // Check if the player has the appropriate card to block the action
        return switch (actionToBlock.getActionCode()) {
            case FOREIGN_AID -> player.hasCard(Deck.CardType.DUKE);
            case ASSASSINATE -> player.hasCard(Deck.CardType.CONTESSA);
            case STEAL -> player.hasCard(Deck.CardType.AMBASSADOR) || player.hasCard(Deck.CardType.CAPTAIN);
            default -> false;
        };
    }
}
