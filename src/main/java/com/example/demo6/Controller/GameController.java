package com.example.demo6.Controller;

import com.example.demo6.AI.MCTS;
import com.example.demo6.Model.Actions.*;
import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Game;
import com.example.demo6.Model.Player;
import com.example.demo6.View.GameView;
import javafx.application.Platform;
import javafx.collections.ObservableList;

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
        this.mcts = new MCTS(game, 3000, 100);

        Platform.runLater(() -> {
            view.updatePlayerInfo(this.game.getPlayers());
            view.updateCurrentPlayer(this.currentPlayer);
            view.updateAvailableActions(this.game.getAvailableActions(this.currentPlayer));
            view.createCardStackArea(this.game.getDeck());
        });
    }

    public void executeAction(Action action) {
        //another validation that the action is legal to be performed
        if (!action.canPlayerPerform()) {
            view.displayMessage("Action cannot be performed due to game rules.");
            return;
        }
        boolean challengeResponse;
        boolean blockResponse;
        boolean actionExecuted = true;
        boolean challengeResult = false;
        Player opponent = game.getOpponent(currentPlayer);


        // Handling challenges
        if (action.canBeChallenged) {
            if (opponent == aiPlayer) {
                challengeResponse = mcts.simulateChallenge(game, action);
            } else {
                challengeResponse = view.promptForChallenge("Do you want to challenge " + currentPlayer.getName() + "'s action?");
            }
            if (challengeResponse) {
                view.displayMessage(opponent.getName() + " challenges " + currentPlayer.getName() + "'s action!");
                challengeResult = handleChallenge(action);
            }
        }

        // Handling blocks
// Handling blocks
        if (action.canBeBlocked && !challengeResult) {
            if (opponent == aiPlayer) {
                blockResponse = mcts.simulateBlock(game, action);
            } else {
                blockResponse = view.promptForBlock("Do you want to block " + currentPlayer.getName() + "'s action?");
            }
            if (blockResponse) {
                view.displayMessage(opponent.getName() + " blocks " + currentPlayer.getName() + "'s action!");
                boolean challengeBlock = currentPlayer == aiPlayer ? mcts.simulateBlockChallenge(game, action) : view.promptForChallenge("Do you want to challenge this block?");
                if (!challengeBlock) {
                    actionExecuted = false;
                } else {
                    view.displayMessage(currentPlayer.getName() + " challenges the block by " + opponent.getName());
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
                List<Card> newCards = this.game.drawCards(2);
                List<Card> selectedCards;
                if (currentPlayer == aiPlayer) {
                    selectedCards = mcts.selectCardsToKeep(game, currentPlayer, newCards);
                } else {
                    List<Card> swapOptions = new ArrayList<>(currentPlayer.getCards());
                    swapOptions.addAll(newCards);
                    selectedCards = view.promptForCardSelection(swapOptions, 2);
                }
                cards = new ArrayList<>();
                cards.addAll(selectedCards);
                cards.addAll(newCards);
            } else if (action.getActionCode() == ActionCode.COUP || action.getActionCode() == ActionCode.ASSASSINATE) {
                Player targetPlayer = game.getOpponent(action.getPlayer());
                if (!targetPlayer.getCards().isEmpty()) {
                    Card cardToLose;
                    if (targetPlayer == aiPlayer) {
                        cardToLose = mcts.selectCardToGiveUp(game, targetPlayer);
                    } else {
                        cardToLose = view.promptPlayerForCardToGiveUp(targetPlayer);
                    }
                    cards = new ArrayList<>();
                    cards.add(cardToLose);
                }
            }
            game.executeAction(action, cards);
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
            if (actionToBlock.getPlayer() == aiPlayer) {
                Card cardToLose = mcts.selectCardToGiveUp(game, actionToBlock.getPlayer());
                actionToBlock.getPlayer().returnCard(cardToLose);
            } else {
                handleLoseCard(actionToBlock.getPlayer());
            }
        } else {
            if (blocker == aiPlayer) {
                Card cardToLose = mcts.selectCardToGiveUp(game, blocker);
                blocker.returnCard(cardToLose);
            } else {
                handleLoseCard(blocker);
            }
        }

        return blockSuccessful;
    }

    private boolean handleChallenge(Action action) {
        boolean challengeSuccess = action.challenge();
        if (challengeSuccess) {
            Player challenger = game.getOpponent(action.getPlayer());
            if (challenger == aiPlayer) {
                Card cardToLose = mcts.selectCardToGiveUp(game, challenger);
                challenger.returnCard(cardToLose);
                view.displayMessage("Challenge failed. " + challenger.getName() + " loses a card.");
            } else {
                handleLoseCard(challenger);
                view.displayMessage("Challenge failed. " + challenger.getName() + " loses a card.");
            }
        } else {
            if (action.getPlayer() == aiPlayer) {
                Card cardToLose = mcts.selectCardToGiveUp(game, action.getPlayer());
                action.getPlayer().returnCard(cardToLose);
                view.displayMessage("Challenge successful. " + action.getPlayer().getName() + " loses a card.");
            } else {
                handleLoseCard(action.getPlayer());
                view.displayMessage("Challenge successful. " + action.getPlayer().getName() + " loses a card.");
            }
        }
        return challengeSuccess;
    }
    private void endTurn() {
        if (!isGameOver()) {
//            mcts.handleAction(game.getLastExecutedAction());
            currentPlayer = game.switchTurns();
            updateView();

            if (currentPlayer.equals(aiPlayer)) {
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
}