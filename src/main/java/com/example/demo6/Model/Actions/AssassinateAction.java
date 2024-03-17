package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Card;
import com.example.demo6.Model.Player;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
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
        // Check if the player has the Assassin card
        return player.hasCard(new Card("Assassin"));
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