package com.example.demo6.Actions;

import com.example.demo6.Card;
import com.example.demo6.Player;

import java.util.Scanner;

public class BlockAction extends Action {
    private Action actionToBlock;
    private boolean isBlocked;

    public BlockAction(Player player, Action actionToBlock) {
        super(player, "block");
        this.actionToBlock = actionToBlock;
        this.isBlocked = false;
    }

    @Override
    public boolean canPlayerPerform() {
        // Check if the action can be blocked
        String actionName = actionToBlock.getNameOfAction();
        return actionName.equals("foreign_aid") || actionName.equals("assassinate") || actionName.equals("steal");
    }

    @Override
    public void execute() {
        if (canPlayerPerform()) {
            if (isChallenged()) {
                if (!challenge()) {
                    // Player failed the challenge, so the block is not successful
                    // Apply consequences for failing the challenge, e.g., losing a card
                    player.selectCardToGiveUp();
                    isBlocked = false;
                } else {
                    // Player successfully blocked the action
                    System.out.println(player.getName() + " successfully blocked the " + actionToBlock.getNameOfAction() + " action.");
                    isBlocked = true;
                }
            } else {
                // No challenge, so the block is successful
                System.out.println(player.getName() + " blocked the " + actionToBlock.getNameOfAction() + " action.");
                isBlocked = true;
            }
        } else {
            isBlocked = false;
        }
    }

    private boolean isChallenged() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(actionToBlock.getPlayer().getName() + ", do you want to challenge the block? (Y/N)");
        String input = scanner.nextLine().trim().toUpperCase();
        if (input.equals("Y")) {
            System.out.println(actionToBlock.getPlayer().getName() + " challenges the block!");
            return true;
        }
        System.out.println(actionToBlock.getPlayer().getName() + " does not challenge the block.");
        return false;
    }

    private boolean challenge() {
        // Check if the player has the appropriate card to block the action
        String actionName = actionToBlock.getNameOfAction();
        return switch (actionName) {
            case "foreign_aid" -> player.hasCard(new Card("Duke"));
            case "assassinate" -> player.hasCard(new Card("Contessa"));
            case "steal" -> player.hasCard(new Card("Ambassador")) || player.hasCard(new Card("Captain"));
            default -> false;
        };
    }

    public boolean isBlocked() {
        return isBlocked;
    }
}