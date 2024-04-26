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

import static com.example.demo6.AI.Heuristic.*;

/**
 * The GameController class is responsible for managing the game logic and communication between the game model and the game view.
 * It handles user interactions, executes actions, manages game state transitions, and integrates with the AI component.
 */
public class GameController {
    private Game game;
    private GameView view;
    private Player currentPlayer;
    private Player aiPlayer;
    private MCTS mcts;

    /**
     * Constructor for GameController.
     * @param view The game view to interact with the user.
     * @param game The game model containing game data.
     */
    public GameController(GameView view, Game game) {
        this.game = game;
        this.view = view;
        this.view.setController(this);
    }

    /**
     * Initializes the game and updates the view.
     */
    public void initializeGame() {
        Set<Deck.CardType> allCardTypes = EnumSet.allOf(Deck.CardType.class);
        this.game = new Game(new Deck(allCardTypes, 2));

        Player humanPlayer = new Player("Human Player");
        this.aiPlayer = new Player("AI Player");
        this.game.addPlayer(humanPlayer);
        this.game.addPlayer(aiPlayer);

        this.currentPlayer = this.game.getCurrentPlayer();
        this.mcts = new MCTS(game, 2500, 100);

        Platform.runLater(() -> {
            view.updatePlayerInfo(this.game.getPlayers());
            view.updateCurrentPlayer(this.currentPlayer);
            view.updateAvailableActions(this.game.getAvailableActions(this.currentPlayer));
            view.createCardStackArea(this.game.getDeck());
        });
    }

    /**
     * Executes a given action within the game context. This method first validates if the action can be legally performed
     * by the current player. It then handles potential challenges and blocks by opponents, and if unchallenged or
     * successfully challenged, executes the action. This method also updates the game state and view after the action
     * is processed, and manages transitions between game states like ending a turn or concluding the game.
     *
     * @param action The action to be executed, derived from the current player's available actions.
     */
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
                challengeResponse = simulateChallenge(game, action);
            } else {
                challengeResponse = view.promptForChallenge("Do you want to challenge " + currentPlayer.getName() + "'s action?");
            }
            if (challengeResponse) {
                view.displayMessage(opponent.getName() + " challenges " + currentPlayer.getName() + "'s action!");
                challengeResult = handleChallenge(action);
            }
        }

        // Handling blocks
        if (action.canBeBlocked && !challengeResult) {
            if (opponent == aiPlayer) {
                blockResponse = simulateBlock(game, action);
            } else {
                blockResponse = view.promptForBlock("Do you want to block " + currentPlayer.getName() + "'s action?");
            }
            if (blockResponse) {
                view.displayMessage(opponent.getName() + " blocks " + currentPlayer.getName() + "'s action!");
                boolean challengeBlock = currentPlayer == aiPlayer ? simulateBlockChallenge(game, action) : view.promptForChallenge("Do you want to challenge this block?");
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
                    selectedCards = selectCardsToKeep(game, currentPlayer, newCards);
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
                        cardToLose = selectCardToGiveUp(game, targetPlayer);
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

    /**
     * Handles the execution of a block action in the game. This method is responsible for determining the outcome of a block
     * action, which may involve the loss of a card by the player who attempted the block.
     *
     * @param blocker The player who is attempting to block the action.
     * @param actionToBlock The action that is being blocked.
     * @param isChallenged A boolean value indicating whether the block action is being challenged.
     * @return A boolean value indicating whether the block action was successful.
     */
    private boolean handleBlockAction(Player blocker, Action actionToBlock, boolean isChallenged) {
        BlockAction blockAction = new BlockAction(blocker, game.getOpponent(blocker), actionToBlock);
        boolean blockSuccessful = blockAction.execute(isChallenged, false);

        if (blockSuccessful) {
            if (actionToBlock.getPlayer() == aiPlayer) {
                Card cardToLose = selectCardToGiveUp(game, actionToBlock.getPlayer());
                actionToBlock.getPlayer().returnCard(cardToLose);
            } else {
                handleLoseCard(actionToBlock.getPlayer());
            }
        } else {
            if (blocker == aiPlayer) {
                Card cardToLose = selectCardToGiveUp(game, blocker);
                blocker.returnCard(cardToLose);
            } else {
                handleLoseCard(blocker);
            }
        }

        return blockSuccessful;
    }

    /**
     * Handles the challenge of an action performed by a player. This method determines if the challenge is successful
     * and manages the outcome of losing a card based on the result of the challenge. If the challenge is successful,
     * the player who performed the action loses a card. If the challenge fails, the challenger loses a card instead.
     *
     * @param action The action being challenged.
     * @return true if the challenge was successful, false if it failed.
     */
    private boolean handleChallenge(Action action) {
        // Determine the outcome of the challenge
        boolean challengeSuccess = action.challenge();

        // If the challenge succeeds, the player who performed the action loses a card
        if (challengeSuccess) {
            Player challenger = game.getOpponent(action.getPlayer());
            if (challenger == aiPlayer) {
                Card cardToLose = selectCardToGiveUp(game, challenger);
                challenger.returnCard(cardToLose);
                view.displayMessage("Challenge failed. " + challenger.getName() + " loses a card.");
            } else {
                handleLoseCard(challenger);
                view.displayMessage("Challenge failed. " + challenger.getName() + " loses a card.");
            }
        } else {
            // If the challenge fails, the challenger loses a card
            if (action.getPlayer() == aiPlayer) {
                Card cardToLose = selectCardToGiveUp(game, action.getPlayer());
                action.getPlayer().returnCard(cardToLose);
                view.displayMessage("Challenge successful. " + action.getPlayer().getName() + " loses a card.");
            } else {
                handleLoseCard(action.getPlayer());
                view.displayMessage("Challenge successful. " + action.getPlayer().getName() + " loses a card.");
            }
        }
        return challengeSuccess;
    }

    /**
     * Ends the current player's turn and transitions to the next player's turn. If the game is not over, it updates
     * the game view and potentially executes the AI player's turn. If the game is over, it calls the method to end
     * the game.
     */
    private void endTurn() {
        if (!isGameOver()) {
            currentPlayer = game.switchTurns();
            updateView();
            if (currentPlayer.equals(aiPlayer)) {
                executeAIPlayerTurn();
            }
        } else {
            endGame();
        }
    }

    /**
     * Executes the turn for the AI player. Determines the best move using the MCTS algorithm and performs it.
     * Displays which action the AI decided to execute in the view.
     */
    private void executeAIPlayerTurn() {
        Action bestAction = mcts.bestMove();
        if (bestAction != null) {
            bestAction.setPlayer(aiPlayer);
            bestAction.setOpponent(game.getOpponent(aiPlayer));
            System.out.println("The best action is : " + bestAction.getCodeOfAction() + " " + bestAction.getPlayer().getName());
            view.displayMessage("AI decides to execute the action: " + bestAction.getCodeOfAction());
            executeAction(bestAction);
        }
    }

    /**
     * Checks if the game is over. The game is considered over when only one active player remains.
     *
     * @return true if the game is over, otherwise false.
     */
    private boolean isGameOver() {
        return game.getActivePlayers().size() == 1;
    }

    /**
     * Handles the event when a player must lose a card. This could occur as a result of a challenge, coup, or
     * assassination. The method prompts the player to choose a card to lose, removes the chosen card from the player's
     * hand, and updates the view accordingly.
     *
     * @param player The player who is required to lose a card.
     */
    public void handleLoseCard(Player player) {
        if (!player.getCards().isEmpty()) {
            Card cardToLose = view.promptPlayerForCardToGiveUp(player);
            player.returnCard(cardToLose);
            updateView();
        }
    }

    /**
     * Ends the game and displays the winner. Also, notifies the MCTS algorithm about the game's end.
     */
    private void endGame() {
        Player winner = game.getActivePlayers().get(0);
        view.displayWinner(winner);
        mcts.handleGameOver(winner);
    }

    /**
     * Retrieves the current player of the game.
     *
     * @return The player who is currently taking their turn.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;  // Returns the reference to the current player
    }

    /**
     * Updates the game view with the latest game state information. This method is responsible for synchronizing the
     * graphical user interface with the current state of the game model. It updates player information, the current player's
     * state, available actions, and the state of the deck. This method runs its updates on the JavaFX Application Thread to
     * ensure thread safety with UI components.
     */
    private void updateView() {
        Platform.runLater(() -> {  // Ensures updates are performed on the JavaFX application thread
            view.updatePlayerInfo(game.getPlayers());          // Updates UI with current player list
            view.updateCurrentPlayer(currentPlayer);           // Updates UI to show the current player
            view.updateAvailableActions(game.getAvailableActions(currentPlayer));  // Updates UI with actions available to the current player
            view.updateDeckInfo(game.getDeck());               // Updates UI with the current state of the deck
        });
    }
}