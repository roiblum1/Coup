package com.example.demo6.Actions;

import com.example.demo6.Actions.Action;
import com.example.demo6.Card;
import com.example.demo6.Player;

import java.util.Scanner;

public class CoupAction extends Action {
    private Player targetPlayer;

    public CoupAction(Player player, Player targetPlayer) {
        super(player, "coup");
        this.targetPlayer = targetPlayer;
    }

    @Override
    public boolean canPlayerPerform() {
        // Check if the player has enough coins to perform a coup.
        return player.getCoins() >= 7 || player.getCoins() >= 10; // Including the rule for 10+ coins.
    }

    @Override
    public void execute() {
        if (canPlayerPerform()) {
            player.updateCoins(-7); // Deduct the cost of performing a coup.
            // Logic to make the target player lose an Influence card.
            targetPlayer.selectCardToGiveUp();
            System.out.println(player.getName() + " has successfully executed a coup on " + targetPlayer.getName());
        }
        else {
            System.out.println(player.getName() + " does not have enough coins to execute a coup.");
        }
    }
}