package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Player;

/**
 * Represents a blocking action in the game. This action is used by a player to block another player's action.
 * It specifies whether a particular action taken by an opponent can be blocked based on the current state of the game.
 * The block action cannot be blocked itself but can be challenged by other players.
 */
public class BlockAction extends Action {
    private Action actionToBlock;
    private boolean isBlocked;

    /**
     * Constructs a BlockAction object to block a specific action.
     *
     * @param player        The player who is performing the block action.
     * @param opponent      The opponent whose action is being blocked.
     * @param actionToBlock The action that is being attempted to be blocked.
     */
    public BlockAction(Player player, Player opponent, Action actionToBlock) {
        super(player, opponent, ActionCode.BLOCK);
        this.actionToBlock = actionToBlock;
        this.isBlocked = false;
        this.canBeBlocked = false;
        this.canBeChallenged = true;
    }

    /**
     * Checks if the block action can be performed by the player.
     * This typically involves checking if the action being blocked is allowed to be blocked according to the game rules.
     *
     * @return true if the action can be blocked, false otherwise.
     */
    @Override
    public boolean canPlayerPerform() {
        return actionToBlock.canBeBlocked;
    }

    /**
     * Executes the block action. If challenged, the player must prove they have the appropriate card to perform the block.
     * If the player cannot prove this or the action is not allowed to be blocked, the block fails.
     *
     * @param isChallenged Indicates if the block action is being challenged.
     * @param isBlocked    Indicates if the block action is being blocked, which should always be false as block actions cannot be blocked.
     * @return true if the block action is successfully executed, false if the block fails or is challenged unsuccessfully.
     */
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (!canPlayerPerform()) {
            this.isBlocked = false;
            return false;
        }

        if (isChallenged) {
            if (!challenge()) {
                this.isBlocked = false;
                return false;
            } else {
                this.isBlocked = true;
                return true;
            }
        } else {
            this.isBlocked = true;
            return true;
        }
    }

    /**
     * Indicates whether the action has been successfully blocked.
     *
     * @return true if the action is blocked, false otherwise.
     */
    public boolean isBlocked() {
        return isBlocked;
    }

    /**
     * Handles the challenge to the blocking action. To win the challenge, the player must have a card that justifies the block.
     * The specific card required depends on the action being blocked.
     *
     * @return true if the player has the appropriate card to block the action, false if the player loses the challenge.
     */
    @Override
    public boolean challenge() {
        return switch (actionToBlock.getActionCode()) {
            case FOREIGN_AID -> player.hasCard(Deck.CardType.DUKE);
            case ASSASSINATE -> player.hasCard(Deck.CardType.CONTESSA);
            case STEAL -> player.hasCard(Deck.CardType.AMBASSADOR) || player.hasCard(Deck.CardType.CAPTAIN);
            default -> false;
        };
    }
}
