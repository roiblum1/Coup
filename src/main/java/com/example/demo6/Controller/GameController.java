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
    private final GameView view;
    private Player currentPlayer;
    private Player aiPlayer;
    private MCTS mcts;
    private final String HUMANN_PLAYER_NAME = "Human Player";
    private final String AI_PLAYER_NAME = "AI Player";

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
        this.game = new Game(new Deck(allCardTypes, Deck.NUMBER_OF_COPIES));

        Player humanPlayer = new Player(HUMANN_PLAYER_NAME);
        this.aiPlayer = new Player(AI_PLAYER_NAME);
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
     * Returns the game model associated with this GameController instance.
     *
     * @return the game model
     */
    public Game getGame() {
        return game;
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
        Player opponent = game.getOpponent(currentPlayer);
        boolean actionExecuted = getChallengeDecision(action, opponent);

        if (action.canBeBlocked && actionExecuted) {
            actionExecuted = getBlockDecision(action, opponent);
        }

        if (actionExecuted) {
            List<Card> cards = getCardsForAction(action);
            game.executeAction(action, cards);
            mcts.handleAction(action);
        }

        updateView();

        if (game.isGameOver()) {
            endGame();
        } else {
            endTurn();
        }
    }

    /**
     * Retrieves the cards required for a specific action.
     *
     * @param action The action for which cards are needed.
     * @return The list of cards required for the action, or null if no cards are needed.
     */
    private List<Card> getCardsForAction(Action action) {
        List<Card> cards = null;
        if (action.getActionCode() == ActionCode.SWAP) {
            List<Card> newCards = this.game.getDeck().drawCards(2);
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
        return cards;
    }

    /**
     * Determines if a challenge is issued for the given action. This method checks if the action can be challenged, and if so,
     * prompts the opponent (if it's the AI player) or the user to decide whether to challenge the action. If the challenge is
     * accepted, it proceeds to handle the challenge.
     *
     * @param action The action to be challenged.
     * @param opponent The opponent player who is being prompted to challenge the action.
     * @return true if the challenge is accepted, false otherwise.
     */
    private boolean getChallengeDecision(Action action, Player opponent) {
        if (!action.canBeChallenged) {
            return true;
        }

        boolean challengeResponse = opponent.equals(aiPlayer)
                ? simulateChallenge(game, action)
                : view.promptForChallenge("Do you want to challenge " + currentPlayer.getName() + "'s action?");

        if (!challengeResponse) {
            return true;
        }

        view.displayMessage(opponent.getName() + " challenges " + currentPlayer.getName() + "'s action!");
        return handleChallenge(action);
    }

    /**
     * Determines whether a block is issued against a specified action.
     * This method prompts the defender (either an AI or human player) to decide if they want to block the action.
     * If a block is initiated, it then handles the sequence of events that may include the attacker challenging the block.
     * The interactions are managed based on player responses or AI simulation results.
     *
     * @param action The action that might be blocked.
     * @param opponent The player attempting to block the action.
     * @return true if the block is not successful (either it was not attempted, or challenged successfully by the initiator),
     *         false if the block is successful and the action is stopped.
     */
    private boolean getBlockDecision(Action action, Player opponent) {
        boolean blockResponse = opponent.equals(aiPlayer)
                ? simulateBlock(game, action)
                : view.promptForBlock("Do you want to block " + currentPlayer.getName() + "'s action?");

        if (!blockResponse) {
            return true;
        }

        view.displayMessage(opponent.getName() + " blocks " + currentPlayer.getName() + "'s action!");
        boolean challengeBlock = currentPlayer == aiPlayer
                ? simulateBlockChallenge(game, action)
                : view.promptForChallenge("Do you want to challenge this block?");

        if (!challengeBlock) {
            return false;
        }

        view.displayMessage(currentPlayer.getName() + " challenges the block by " + opponent.getName());
        boolean blockSucceed = handleBlockAction(opponent, action, challengeBlock);
        return !blockSucceed;
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

        if (!isChallenged)
        {
            return blockSuccessful;
        }
        if (blockSuccessful) {
            handleLoseCard(actionToBlock.getPlayer());
        } else {
            handleLoseCard(blocker);
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
        boolean challengeSuccess = action.challenge();

        if (challengeSuccess) {
            handleLoseCard(game.getOpponent(action.getPlayer()));
            view.displayMessage("Challenge failed. " + game.getOpponent(action.getPlayer()).getName() + " loses a card.");
        } else {
            handleLoseCard(action.getPlayer());
            view.displayMessage("Challenge successful. " + action.getPlayer().getName() + " loses a card.");
        }

        return challengeSuccess;
    }


    /**
     * Ends the current player's turn and transitions to the next player's turn. If the game is not over, it updates
     * the game view and potentially executes the AI player's turn. If the game is over, it calls the method to end
     * the game.
     */
    private void endTurn() {
        if (!(game.isGameOver())) {
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
     * Uses a separate thread to prevent the GUI from freezing during the computation.
     */
    private void executeAIPlayerTurn() {
        view.setControlsDisable(true);
        Thread aiThread = new Thread(() -> {
            Action bestAction = mcts.bestMove(game.deepCopy());
            Platform.runLater(() -> {
                if (bestAction != null) {
                    bestAction.setPlayer(aiPlayer);
                    bestAction.setOpponent(game.getHumanPlayer());
                    System.out.println("The best action is: " + bestAction.actionCodeToString() +"\n");
                    view.displayMessage("AI decides to execute the action: " + bestAction.actionCodeToString());
                    executeAction(bestAction);
                }
                view.setControlsDisable(false);
            });
        });
        aiThread.setDaemon(true);
        aiThread.start();
    }
    /**
     * Handles the loss of a card for a given player. This method determines which card the player should lose,
     * either through AI selection or player input, and then performs the necessary actions to update the game state.
     *
     * @param player The player who is losing a card.
     */
    public void handleLoseCard(Player player) {
        Card cardToLose;
        if (player.equals(aiPlayer)) {
            cardToLose = selectCardToGiveUp(game, player);
        } else {
            cardToLose = view.promptPlayerForCardToGiveUp(player);
        }
        player.returnCard(cardToLose);
        updateView();
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
     * Updates the game view with the latest game state information. This method is responsible for synchronizing the
     * graphical user interface with the current state of the game model. It updates player information, the current player's
     * state, available actions, and the state of the deck. This method runs its updates on the JavaFX Application Thread to
     * ensure thread safety with UI components.
     */
    private void updateView() {
        Platform.runLater(() -> {
            view.updatePlayerInfo(game.getPlayers());
            view.updateCurrentPlayer(currentPlayer);
            view.updateAvailableActions(game.getAvailableActions(currentPlayer));
            view.updateDeckInfo(game.getDeck());
        });
    }
}