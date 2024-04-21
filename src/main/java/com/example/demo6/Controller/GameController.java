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
import java.util.function.Consumer;

public class GameController {
    private Game game;
    private GameView view;
    private Player currentPlayer;
    private Map<Class<? extends Action>, Consumer<Action>> actionExecutors;


    public GameController(GameView view, Game game) {
        this.game = game;
        this.view = view;
        this.view.setController(this);

    }

    // Initialize game and update view
    public void initializeGame() {
        // Initialize the game with all card types and two copies of each card
        Set<Deck.CardType> allCardTypes = EnumSet.allOf(Deck.CardType.class);
        this.game = new Game(new Deck(allCardTypes, 2)); // Make sure the number of copies matches your game design

        // Create players and add them to the game
        Player humanPlayer = new Player("Human Player");
        Player aiPlayer = new Player("AI Player");
        this.game.addPlayer(humanPlayer);
        this.game.addPlayer(aiPlayer);

        // Set the current player and AI player
        this.currentPlayer = this.game.getCurrentPlayer();

//        this.mcts = new MCTS(game);
        // Now that the game is initialized, update the view components
        Platform.runLater(() -> {
            view.updatePlayerInfo(this.game.getPlayers());
            view.updateCurrentPlayer(this.currentPlayer);
            view.updateAvailableActions(this.game.getAvailableActions(this.currentPlayer));
            view.createCardStackArea(this.game.getDeck()); // This line should add the card stack area to the gameContent layout
        });
    }


    public void executeAction(Action action) {
        boolean challengeResponse = false;
        boolean blockResponse = false;
        boolean actionExecuted = true;

        // Check if the action can be challenged
        if (action.canBeChallenged) {
            // Ask if the player wants to challenge the action
            challengeResponse = view.promptForChallenge("Do you want to challenge this action?");
            if (challengeResponse) {
                // Challenge the action
                boolean challengeResult = handleChallenge(action);
                if (!challengeResult) {
                    // Check if the action can be blocked
                    if (action.canBeBlocked) {
                        // Ask if the player wants to block the action
                        blockResponse = view.promptForBlock("Do you want to block this action?");
                        if (blockResponse) {
                            // Challenge the block
                            boolean challengeBlock = view.promptForChallenge("Do you want to challenge this block?");
                            if (!challengeBlock) {
                                // The function cannot be executed
                                actionExecuted = false;
                            } else {
                                // Check if the block is valid and handle the block action
                                boolean blockSucceed = handleBlockAction(game.getOpponent(action.getPlayer()), action, challengeBlock);
                                if (blockSucceed) {
                                    // The function cannot be executed
                                    actionExecuted = false;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Check if the action can be blocked
            if (action.canBeBlocked) {
                // Ask if the player wants to block the action
                blockResponse = view.promptForBlock("Do you want to block this action?");
                if (blockResponse) {
                    // Challenge the block
                    boolean challengeBlock = view.promptForChallenge("Do you want to challenge this block?");
                    if (!challengeBlock) {
                        // The function cannot be executed
                        actionExecuted = false;
                    } else {
                        // Check if the block is valid and handle the block action
                        boolean blockSucceed = handleBlockAction(game.getOpponent(action.getPlayer()), action, challengeBlock);
                        if (blockSucceed) {
                            // The function cannot be executed
                            actionExecuted = false;
                        }
                    }
                }
            }
        }

        if (actionExecuted) {
            List<Card> cards = null;
            if (action.getActionCode() == ActionCode.SWAP) {
                List<Card> newCards = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    newCards.add(game.getDeck().getCard());
                }
                List<Card> swapOptions = new ArrayList<>(currentPlayer.getCards());
                swapOptions.addAll(newCards);
                List<Card> selectedCards = view.promptForCardSelection(swapOptions, 2);
                cards = new ArrayList<>();
                cards.addAll(selectedCards);
                cards.addAll(newCards);
            } else if (action.getActionCode() == ActionCode.COUP || (action.getActionCode() == ActionCode.ASSASSINATE)) {
                Card cardToLose = view.promptPlayerForCardToGiveUp(game.getOpponent(action.getPlayer()));
                cards = new ArrayList<>();
                cards.add(cardToLose);
            }
            game.executeAction(action, cards);
        }

        updateView();

        if (isGameOver()) {
            System.out.println("Game over");
            endGame();
        } else {
            System.out.println("Ending turn");
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

    //* Handles resolving a challenge when an action is performed */
    private boolean handleChallenge(Action action) {
        boolean challengeSuccess = action.challenge();
        if (challengeSuccess) {
            Player challenger = game.getOpponent(action.getPlayer());
            Card lostCard = view.promptPlayerForCardToGiveUp(challenger);
            challenger.returnCard(lostCard);
            Platform.runLater(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                view.displayMessage("Challenge failed. " + challenger.getName() + " loses a card.");
            });
        } else {
            Card lostCard = view.promptPlayerForCardToGiveUp(action.getPlayer());
            action.getPlayer().returnCard(lostCard);
            Platform.runLater(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                view.displayMessage("Challenge successful. " + action.getPlayer().getName() + " loses a card.");
            });
        }
        return challengeSuccess;
    }

    //* Ends the current player's turn and switches to the next player */
    private void endTurn() {
        currentPlayer = game.switchTurns();
        updateView();
    }
    //* Checks if the game has ended */
    private boolean isGameOver() {
        return game.getActivePlayers().size() == 1;
    }

    //* handle the view and the model in case of returning card */
    public void handleLoseCard(Player player) {
        Card cardToLose = view.promptPlayerForCardToGiveUp(player);
        player.returnCard(cardToLose);
        updateView();
    }

    //* Ends the game and displays the winner */
    private void endGame() {
        Player winner = game.getActivePlayers().get(0);
        view.displayWinner(winner);

        // Update the MCTS tree with the game result
//        mcts.handleGameOver(winner);
    }
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    //* Update the view */
    private void updateView() {
        Platform.runLater(() -> {
            view.updatePlayerInfo(game.getPlayers());
            view.updateCurrentPlayer(currentPlayer);
            view.updateAvailableActions(game.getAvailableActions(currentPlayer));
            view.updateDeckInfo(game.getDeck());
        });
    }

}