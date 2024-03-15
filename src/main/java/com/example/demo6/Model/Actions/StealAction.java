package com.example.demo6.Model.Actions;

import com.example.demo6.HelloApplication;
import com.example.demo6.Model.Player;
import com.example.demo6.Model.Card;
import java.util.Scanner;

public class StealAction extends Action {
    private Player targetPlayer;

    public StealAction(Player player, Player targetPlayer) {
        super(player, "steal");
        this.targetPlayer = targetPlayer;
    }

    @Override
    public boolean canPlayerPerform() {
        // Assuming a player can always attempt to steal
        return true;
    }

    @Override
    public void execute() {
        if (canPlayerPerform())
        {
            if (isChallenged())
            {
                if (!challenge()) {
                    // Player failed the challenge, so the action is not performed
                    // Apply consequences for failing the challenge, e.g., losing a card
                    player.selectCardToGiveUp();
                    return;
                }
            }

            if (isBlocked()) {
                System.out.println(targetPlayer.getName() + " blocked the steal action.");
                return;
            }

            int stolenCoins = Math.min(2, targetPlayer.getCoins());
            player.updateCoins(stolenCoins);
            targetPlayer.updateCoins(-stolenCoins);
            System.out.println(player.getName() + " stole " + stolenCoins + " coins from " + targetPlayer.getName());
        }
    }

    private boolean isChallenged() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(targetPlayer.getName() + ", do you want to challenge the steal action? (Y/N)");
        String input = scanner.nextLine().trim().toUpperCase();
        if (input.equals("Y")) {
            System.out.println(targetPlayer.getName() + " challenges the steal action!");
            return true;
        }
        System.out.println(targetPlayer.getName() + " does not challenge the steal action.");
        return false;
    }

    private boolean challenge() {
        // Check if the player has the required card (Captain) to perform the steal action
        return player.hasCard(new Card("Captain"));
    }

    private boolean isBlocked() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(targetPlayer.getName() + ", do you want to block the steal action? (Y/N)");
        String input = scanner.nextLine().trim().toUpperCase();
        if (input.equals("Y")) {
            System.out.println(targetPlayer.getName() + " attempts to block the steal action.");
            BlockAction blockAction = new BlockAction(targetPlayer, this);
            if (blockAction.canPlayerPerform()) {
                blockAction.execute();
                return blockAction.isBlocked();
            }
        }
        System.out.println(targetPlayer.getName() + " does not block the steal action.");
        return false;
    }
}