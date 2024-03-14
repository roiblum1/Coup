package com.example.demo6.Actions;

import com.example.demo6.Card;
import com.example.demo6.Player;

import java.util.Scanner;

public class AssassinateAction extends Action {
    private Player targetPlayer;

    public AssassinateAction(Player player, Player targetPlayer) {
        super(player, "assassinate");
        this.targetPlayer = targetPlayer;
    }

    @Override
    public boolean canPlayerPerform() {
        // Check if the player has enough coins to perform an assassination.
        return player.getCoins() >= 3;
    }

    @Override
    public void execute() {
        if (canPlayerPerform()) {
            player.updateCoins(-3); // Deduct the cost of performing an assassination.
            if (isChallenged()) {
                if (!challenge()) {
                    // Player failed the challenge, so the action is not performed
                    // Apply consequences for failing the challenge, e.g., losing a card
                    player.selectCardToGiveUp();
                    return;
                }
            }
            if (isBlocked()) {
                System.out.println(targetPlayer.getName() + " blocked the assassination attempt.");
            } else {
                targetPlayer.selectCardToGiveUp();
                System.out.println(player.getName() + " has successfully assassinated " + targetPlayer.getName());
            }
        } else {
            System.out.println(player.getName() + " does not have enough coins to perform an assassination.");
        }
    }

    private boolean isChallenged() {
        System.out.println(targetPlayer.getName() + ", do you want to challenge the assassination? (Y/N)");
        String input = new Scanner(System.in).nextLine().trim().toUpperCase();
        return input.equals("Y");
    }

    private boolean challenge() {
        // Check if the player has the Assassin card
        return player.hasCard(new Card("Assassin"));
    }

    private boolean isBlocked() {
        System.out.println(targetPlayer.getName() + ", do you want to block the assassination? (Y/N)");
        String input = new Scanner(System.in).nextLine().trim().toUpperCase();
        if (input.equals("Y")) {
            BlockAction blockAction = new BlockAction(targetPlayer, this);
            if (blockAction.canPlayerPerform()) {
                blockAction.execute();
                return blockAction.isBlocked();
            }
        }
        return false;
    }
}