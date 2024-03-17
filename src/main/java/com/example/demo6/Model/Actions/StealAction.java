package com.example.demo6.Model.Actions;

import com.example.demo6.HelloApplication;
import com.example.demo6.Model.Player;
import com.example.demo6.Model.Card;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
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
        Alert challengeAlert = new Alert(Alert.AlertType.CONFIRMATION);
        challengeAlert.setTitle("Challenge Action");
        challengeAlert.setHeaderText(null);
        challengeAlert.setContentText(targetPlayer.getName() + ", do you want to challenge this action?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        challengeAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = challengeAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            System.out.println(targetPlayer.getName() + " challenges the action!");
            return true;
        } else {
            System.out.println("No one challenges the action.");
            return false;
        }
    }

    private boolean challenge() {
        // Check if the player has the required card (Captain) to perform the steal action
        return player.hasCard(new Card("Captain"));
    }

    private boolean isBlocked() {
        Alert blockAlert = new Alert(Alert.AlertType.CONFIRMATION);
        blockAlert.setTitle("Block Action");
        blockAlert.setHeaderText(null);
        blockAlert.setContentText(targetPlayer.getName() + ", do you want to block the "+this.getNameOfAction()+" action?");

        blockAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = blockAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            System.out.println(targetPlayer.getName() + " attempts to block the "+this.getNameOfAction()+" action.");
            BlockAction blockAction = new BlockAction(targetPlayer, this);
            if (blockAction.canPlayerPerform()) {
                blockAction.execute();
                return blockAction.isBlocked();
            }
            else return false;
        } else {
            System.out.println(targetPlayer.getName() + " does not block the "+this.getNameOfAction()+" action.");
            return false;
        }
    }
}