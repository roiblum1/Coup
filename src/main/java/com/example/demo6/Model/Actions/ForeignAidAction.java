package com.example.demo6.Model.Actions;

import com.example.demo6.Model.Player;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
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
