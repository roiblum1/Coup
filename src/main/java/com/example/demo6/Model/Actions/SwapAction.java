package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Card;
import com.example.demo6.Model.Player;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Challenge Action");
        alert.setHeaderText(null);
        alert.setContentText("Does any player want to challenge the swap action?");
        ButtonType buttonYes = new ButtonType("Yes");
        ButtonType buttonNo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonYes, buttonNo);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonYes;
    }
    private boolean challenge() {
        // Check if the player has the Ambassador card
        return player.hasCard(new Card("Ambassador"));
    }
}
