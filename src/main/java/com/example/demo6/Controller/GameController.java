package com.example.demo6.Controller;

import com.example.demo6.AI.MCTS;
import com.example.demo6.Model.Actions.*;
import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Game;
import com.example.demo6.Model.Player;
import com.example.demo6.View.GameView;
import javafx.application.Platform;

import java.util.*;

public class GameController {
    private Game game;
    private GameView view;
    private Player currentPlayer;
    private Player aiPlayer;
    private MCTS mcts;

    public GameController(GameView view, Game game) {
        this.game = game;
        this.view = view;
        this.view.setController(this);
    }

    // Initialize game and update view
    public void initializeGame() {
        Set<Deck.CardType> allCardTypes = EnumSet.allOf(Deck.CardType.class);
        this.game = new Game(new Deck(allCardTypes, 2));

        Player humanPlayer = new Player("Human Player");
        this.aiPlayer = new Player("AI Player");
        this.game.addPlayer(humanPlayer);
        this.game.addPlayer(aiPlayer);

        this.currentPlayer = this.game.getCurrentPlayer();
        this.mcts = new MCTS(game);

        Platform.runLater(() -> {
            view.updatePlayerInfo(this.game.getPlayers());
            view.updateCurrentPlayer(this.currentPlayer);
            view.updateAvailableActions(this.game.getAvailableActions(this.currentPlayer));
            view.createCardStackArea(this.game.getDeck());
        });
    }

    public void executeAction(Action action) {
        boolean challengeResponse = false;
        boolean blockResponse = false;
        boolean actionExecuted = true;
        boolean challengeResult = false;

        Player opponent = game.getOpponent(currentPlayer);

        if (action.canBeChallenged) {
            if (opponent == aiPlayer) {
                challengeResponse = mcts.simulateChallenge(game, action);  // AI decides based on MCTS simulation
            } else {
                challengeResponse = view.promptForChallenge("Do you want to challenge this action?");
            }
            if (challengeResponse) {
                view.displayMessage(currentPlayer.getName() + " challenges " + opponent.getName() + "'s action!");
                challengeResult = handleChallenge(action);
            }
        }

        if (action.canBeBlocked && !challengeResult) {
            if (opponent == aiPlayer) {
                blockResponse = mcts.simulateBlock(game, action);  // AI decides whether to block
            } else {
                blockResponse = view.promptForBlock("Do you want to block this action?");
            }
            if (blockResponse) {
                view.displayMessage(currentPlayer.getName() + " blocks " + opponent.getName() + "'s action!");
                boolean challengeBlock = opponent == aiPlayer ?
                        mcts.simulateBlockChallenge(game, action) : // Simulate AI's decision to challenge the block
                        view.promptForChallenge("Do you want to challenge this block?");
                if (!challengeBlock) {
                    actionExecuted = false;
                } else {
                    view.displayMessage(opponent.getName() + " challenges the block of" + currentPlayer.getName());
                    boolean blockSucceed = handleBlockAction(opponent, action, challengeBlock);
                    if (blockSucceed) {
                        actionExecuted = false;
                    }
                }
            }
        }


        if (actionExecuted) {
            List<Card> cards = null;
            if (action.getActionCode() == ActionCode.SWAP) {
                List<Card> newCards = drawCards(2);
                List<Card> swapOptions = new ArrayList<>(currentPlayer.getCards());
                swapOptions.addAll(newCards);
                List<Card> selectedCards = view.promptForCardSelection(swapOptions, 2);
                cards = new ArrayList<>();
                cards.addAll(selectedCards);
                cards.addAll(newCards);
            } else if (action.getActionCode() == ActionCode.COUP || action.getActionCode() == ActionCode.ASSASSINATE) {
                Player targetPlayer = game.getOpponent(action.getPlayer());
                if (!targetPlayer.getCards().isEmpty()) {
                    Card cardToLose = view.promptPlayerForCardToGiveUp(targetPlayer);
                    cards = new ArrayList<>();
                    cards.add(cardToLose);
                }
            }
            game.executeAction(action, cards);

            // Update the MCTS tree only if the action is executed successfully
            mcts.handleAction(action);
        }

        updateView();

        if (isGameOver()) {
            endGame();
        } else {
            endTurn();
        }
    }

    private boolean handleBlockAction(Player blocker, Action actionToBlock, boolean isChallenged) {
        BlockAction blockAction = new BlockAction(blocker, actionToBlock);
        boolean blockSuccessful = blockAction.execute(isChallenged, false);

        if (blockSuccessful) {
            handleLoseCard(actionToBlock.getPlayer());
        } else {
            handleLoseCard(blocker);
        }

        return blockSuccessful;
    }

    private boolean handleChallenge(Action action) {
        boolean challengeSuccess = action.challenge();
        if (challengeSuccess) {
            Player challenger = game.getOpponent(action.getPlayer());
            handleLoseCard(challenger);
            view.displayMessage("Challenge failed. " + challenger.getName() + " loses a card.");
        } else {
            handleLoseCard(action.getPlayer());
            view.displayMessage("Challenge successful. " + action.getPlayer().getName() + " loses a card.");
        }
        return challengeSuccess;
    }

    private void endTurn() {
        if (!isGameOver()) {
            mcts.handleAction(game.getLastExecutedAction());
            currentPlayer = game.switchTurns();
            updateView();

            if (currentPlayer == aiPlayer) {
                executeAIPlayerTurn();
            }
        } else {
            endGame();
        }
    }

    private void executeAIPlayerTurn() {
        Action bestAction = mcts.bestMove();
        if (bestAction != null) {
            bestAction.setPlayer(aiPlayer);
            System.out.println("The best action is : " + bestAction.getCodeOfAction() + " " + bestAction.getPlayer().getName());
            view.displayMessage("AI decides to execute the action: " + bestAction.getCodeOfAction() );
            executeAction(bestAction);
        }
    }

    private boolean isGameOver() {
        return game.getActivePlayers().size() == 1;
    }

    public void handleLoseCard(Player player) {
        if (!player.getCards().isEmpty()) {
            Card cardToLose = view.promptPlayerForCardToGiveUp(player);
            player.returnCard(cardToLose);
            updateView();
        }
    }

    private void endGame() {
        Player winner = game.getActivePlayers().get(0);
        view.displayWinner(winner);
        mcts.handleGameOver(winner);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    private void updateView() {
        Platform.runLater(() -> {
            view.updatePlayerInfo(game.getPlayers());
            view.updateCurrentPlayer(currentPlayer);
            view.updateAvailableActions(game.getAvailableActions(currentPlayer));
            view.updateDeckInfo(game.getDeck());
        });
    }

    private List<Card> drawCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (!game.getDeck().isEmpty()) {
                cards.add(game.getDeck().getCard());
            }
        }
        return cards;
    }
}