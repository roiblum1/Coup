package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

/**
 * Represents the Foreign Aid action in the game. This action allows a player to
 * gain 2 coins from the bank, simulating receiving aid from an external source.
 * It can be blocked by a player claiming to have the Duke card, but it cannot be directly challenged.
 */
public class ForeignAidAction extends Action {
    /**
     * Constructs a Foreign Aid action.
     *
     * @param player   The player who is attempting to take foreign aid.
     * @param opponent Not directly relevant in this context as the action isn't targeted,
     *                 but maintained for consistency with other actions.
     */
    public ForeignAidAction(Player player, Player opponent) {
        super(player, opponent, ActionCode.FOREIGN_AID);
        // Indicates that the Foreign Aid action can be blocked by the Duke.
        this.canBeBlocked = true;
        // Indicates that the Foreign Aid action itself cannot be challenged.
        this.canBeChallenged = false;
    }

    /**
     * Checks if the player can perform the foreign aid action.
     * This action is always available unless blocked.
     *
     * @return true always, indicating that any player can attempt to perform foreign aid.
     */
    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    /**
     * Executes the foreign aid action. The action grants 2 coins to the player unless it is blocked.
     * If the action is blocked, no coins are granted and the action fails.
     *
     * @param isChallenged Not used as this action cannot be challenged.
     * @param isBlocked Indicates whether the action has been blocked by another player claiming the Duke.
     * @return true if the foreign aid is successful (i.e., not blocked), false if it is blocked.
     */
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        if (isBlocked) {
            return false; // The foreign aid action was blocked and thus fails.
        } else {
            player.updateCoins(2); // The player successfully receives 2 coins.
            return true;
        }
    }

    /**
     * The challenge method for a Foreign Aid action always returns true as this action cannot be challenged.
     * This implementation reflects the game rule that challenges do not apply to foreign aid.
     *
     * @return true always.
     */
    @Override
    public boolean challenge() {
        return true;
    }
}
