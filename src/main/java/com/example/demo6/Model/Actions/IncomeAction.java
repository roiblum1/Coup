package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

/**
 * Represents the Income action in the game, allowing a player to take one coin from the treasury.
 * This action is the most basic economic move available to all players,
 * and it cannot be blocked or challenged by other players.
 */
public class IncomeAction extends Action {
    /**
     * Constructs an IncomeAction object.
     *
     * @param player   The player who is taking the income.
     * @param opponent Not used in this context, as the income action is not against another player,
     *                 but maintained for consistency with other actions.
     */
    public IncomeAction(Player player, Player opponent) {
        super(player, opponent, ActionCode.INCOME);
        // Indicates that the Income action cannot be blocked.
        this.canBeBlocked = false;
        // Indicates that the Income action cannot be challenged.
        this.canBeChallenged = false;
    }

    /**
     * Checks if the player can perform the income action.
     * This action is always available to any player.
     *
     * @return true always, indicating that any player can perform this action at any time.
     */
    @Override
    public boolean canPlayerPerform() {
        return true;
    }

    /**
     * Executes the income action. The action grants 1 coin to the player unconditionally.
     *
     * @param isChallenged Not used as this action cannot be challenged.
     * @param isBlocked    Not used as this action cannot be blocked.
     * @return true always, as the income action always succeeds.
     */
    @Override
    public boolean execute(boolean isChallenged, boolean isBlocked) {
        // The player receives 1 coin from the treasury.
        player.updateCoins(1);
        return true;
    }

    /**
     * The challenge method for an Income action always returns true as this action cannot be challenged.
     * This implementation reflects the game rule that the income action is a fundamental right of the player.
     *
     * @return true always.
     */
    @Override
    public boolean challenge() {
        return true;
    }
}
