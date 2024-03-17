package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Card;
import com.example.demo6.Model.Player;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.Scanner;

public class TaxAction extends Action {
    Player opponent;
    public TaxAction(Player player, Player opponent ){
        super(player, "tax");
        this.opponent = opponent;
    }

    @Override
    public boolean canPlayerPerform() {
        // In Coup, any player can claim to be the Duke and take the tax action.
        return true;
    }

    @Override
    public void execute() {
        if (canPlayerPerform()) {
            player.updateCoins(3); // Player claims to be the Duke and takes 3 coins.
            if(isChallenged())
            {
                if(!challenge())
                {
                    player.selectCardToGiveUp();
                    player.updateCoins(-3);
                }
                else
                {
                    opponent.selectCardToGiveUp();
                }
            }
        }
    }

    // Method to handle a challenge - you may need additional logic here depending on how challenges work in your game.
    public boolean challenge() {
        // If the player cannot prove they are the Duke, they might lose the action's benefits or face other consequences.
        return player.hasCard(new Card("Duke")); // Simplified; actual implementation will depend on your game's mechanics.
    }

    private boolean isChallenged() {
        Alert challengeAlert = new Alert(Alert.AlertType.CONFIRMATION);
        challengeAlert.setTitle("Challenge Action");
        challengeAlert.setHeaderText(null);
        challengeAlert.setContentText(opponent.getName() + ", do you want to challenge this action?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        challengeAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = challengeAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            System.out.println(opponent.getName() + " challenges the action!");
            return true;
        } else {
            System.out.println("No one challenges the action.");
            return false;
        }
    }
}
