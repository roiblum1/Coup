package com.example.demo6;

import com.example.demo6.Model.Actions.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import com.example.demo6.Model.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.EnumSet;
import java.util.Set;

public class HelloController {
//    private Game game;
//    private Player currentPlayer;
//    private Player opponent;
//
//    @FXML
//    private Label playerNameLabel;
//
//    @FXML
//    private Label opponentNameLabel;
//
//    @FXML
//    private Label playerCoinsLabel;
//
//    @FXML
//    private Label opponentCoinsLabel;
//
//    @FXML
//    private Button incomeButton;
//
//    @FXML
//    private Button foreignAidButton;
//
//    @FXML
//    private Button coupButton;
//
//    @FXML
//    private Button taxButton;
//
//    @FXML
//    private Button assassinateButton;
//
//    @FXML
//    private Button stealButton;
//
//    @FXML
//    private Button swapButton;
//
//    public void initialize() {
//        // Initialize the game and players
//        Set<Deck.CardType> allCardTypes = EnumSet.allOf(Deck.CardType.class);
//        game = new Game(new Deck(allCardTypes, 2));
//        currentPlayer = new Player("Player 1");
//        opponent = new Player("Player 2");
//        game.addPlayer(currentPlayer);
//        game.addPlayer(opponent);
//
//        // Set up the initial GUI state
//        updatePlayerInfo();
//        updateActionButtons();
//    }
//
//    private void updatePlayerInfo() {
//        playerNameLabel.setText(currentPlayer.getName());
//        opponentNameLabel.setText(opponent.getName());
//        playerCoinsLabel.setText("Coins: " + currentPlayer.getCoins());
//        opponentCoinsLabel.setText("Coins: " + opponent.getCoins());
//    }
//
//    private void updateActionButtons() {
//        incomeButton.setDisable(!canPerformAction(new IncomeAction(currentPlayer)));
//        foreignAidButton.setDisable(!canPerformAction(new ForeignAidAction(currentPlayer, opponent)));
//        coupButton.setDisable(!canPerformAction(new CoupAction(currentPlayer, opponent)));
//        taxButton.setDisable(!canPerformAction(new TaxAction(currentPlayer, opponent)));
//        assassinateButton.setDisable(!canPerformAction(new AssassinateAction(currentPlayer, opponent)));
//        stealButton.setDisable(!canPerformAction(new StealAction(currentPlayer, opponent)));
//        swapButton.setDisable(!canPerformAction(new SwapAction(currentPlayer)));
//    }
//
//    private boolean canPerformAction(Action action) {
//        return action.canPlayerPerform();
//    }
//
//    @FXML
//    private void handleIncomeAction() {
//        performAction(new IncomeAction(currentPlayer));
//    }
//
//    @FXML
//    private void handleForeignAidAction() {
//        performAction(new ForeignAidAction(currentPlayer, opponent));
//    }
//
//    @FXML
//    private void handleCoupAction() {
//        performAction(new CoupAction(currentPlayer, opponent));
//    }
//
//    @FXML
//    private void handleTaxAction() {
//        performAction(new TaxAction(currentPlayer, opponent));
//    }
//
//    @FXML
//    private void handleAssassinateAction() {
//        performAction(new AssassinateAction(currentPlayer, opponent));
//    }
//
//    @FXML
//    private void handleStealAction() {
//        performAction(new StealAction(currentPlayer, opponent));
//    }
//
//    @FXML
//    private void handleSwapAction() {
//        performAction(new SwapAction(currentPlayer));
//    }
//
//    private void performAction(Action action) {
//        action.execute();
//        Player newCurrentPlayer = game.switchTurns();
//        currentPlayer = newCurrentPlayer;
//        opponent = game.getPlayers().stream().filter(p -> !p.equals(currentPlayer)).findFirst().orElse(null);
//        updatePlayerInfo();
//        updateActionButtons();
//    }
}