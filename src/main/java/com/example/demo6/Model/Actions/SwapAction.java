package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Card;
import com.example.demo6.Model.Player;

import java.util.Scanner;

public class SwapAction extends Action {
    public SwapAction(Player player) {
        super(player, "swap");
    }

    @Override
    public boolean canPlayerPerform() {
        // Assuming any player can perform the Swap Influence action
        return true;
    }

    @Override
    public void execute() {
        if (canPlayerPerform()) {
            if (isChallenged()) {
                if (!challenge()) {
                    // Player failed the challenge, so the action is not performed
                    // Apply consequences for failing the challenge, e.g., losing a card
                    player.selectCardToGiveUp();
                    return;
                }
            }
            player.swap();
        }
    }

    private boolean isChallenged() {
        System.out.println("Does any player want to challenge the swap action? (Y/N)");
        String input = new Scanner(System.in).nextLine().trim().toUpperCase();
        return input.equals("Y");
    }

    private boolean challenge() {
        // Check if the player has the Ambassador card
        return player.hasCard(new Card("Ambassador"));
    }
}
