package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;

import java.util.Scanner;

public class ForeignAidAction extends Action {
    Player targetPlayer;
    public ForeignAidAction(Player player, Player targetPlayer) {
        super(player, "foreign_aid");
        this.targetPlayer = targetPlayer;
    }

    @Override
    public boolean canPlayerPerform() {
        // Any player can perform the Foreign Aid action
        return true;
    }

    @Override
    public void execute() {
        if (canPlayerPerform()) {
            System.out.println(player.getName() + " attempts to take foreign aid.");

            if (isBlocked()) {
                System.out.println(player.getName() + "'s foreign aid action is blocked.");
            } else {
                player.updateCoins(2);
                System.out.println(player.getName() + " takes 2 coins from the bank.");
            }
        }
    }

    private boolean isBlocked() {
        Scanner scanner = new Scanner(System.in);

        System.out.println(targetPlayer.getName() + ", do you want to block the foreign aid action? (Y/N)");
        String input = scanner.nextLine().trim().toUpperCase();

        if (input.equals("Y")) {
            System.out.println(targetPlayer.getName() + " attempts to block the foreign aid action.");
            BlockAction blockAction = new BlockAction(targetPlayer, this);
            if (blockAction.canPlayerPerform()) {
                blockAction.execute();
                return blockAction.isBlocked();
            }
        }

        System.out.println(targetPlayer.getName() + " does not block the foreign aid action.");
        return false;
    }
}
