package com.example.demo6.Controller;

import com.example.demo6.Model.Actions.*;
import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Game;
import com.example.demo6.Model.Player;
import com.example.demo6.View.GameView;
import javafx.application.Platform;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        initializeActionExecutors();
    }

    // Initialize game and update view
    public void initializeGame() {
        // Initialize the game with all card types and two copies of each card
        Set<Deck.CardType> allCardTypes = EnumSet.allOf(Deck.CardType.class);
        this.game = new Game(new Deck(allCardTypes, 2)); // Make sure the number of copies matches your game design

        // Create players and add them to the game
        Player playerOne = new Player("Player 1");
        Player playerTwo = new Player("Player 2");
        this.game.addPlayer(playerOne);
        this.game.addPlayer(playerTwo);

        // The current player is the first player by default
        this.currentPlayer = this.game.getCurrentPlayer();

        // Now that the game is initialized, update the view components
        Platform.runLater(() -> {
            view.updatePlayerInfo(this.game.getPlayers());
            view.updateCurrentPlayer(this.currentPlayer);
            view.updateAvailableActions(this.game.getAvailableActions(this.currentPlayer));
            view.createCardStackArea(this.game.getDeck()); // This line should add the card stack area to the gameContent layout
        });
    }


    private void initializeActionExecutors() {
        actionExecutors = Map.of(
                StealAction.class, action -> executeStealAction((StealAction) action),
                AssassinateAction.class, action -> executeAssassinateAction((AssassinateAction) action),
                ForeignAidAction.class, action -> executeForeignAidAction((ForeignAidAction) action),
                TaxAction.class, action -> executeTaxAction((TaxAction) action),
                IncomeAction.class, action -> executeIncomeAction((IncomeAction) action),
                CoupAction.class, action -> executeCoupAction((CoupAction) action),
                SwapAction.class, action -> executeSwapAction((SwapAction) action)
        );
    }

    // Handles executing an action
    public void executeAction(Action action) {
        Consumer<Action> executor = actionExecutors.get(action.getClass());
        if (executor != null) {
            executor.accept(action);
        } else {
            System.err.println("Unsupported action: " + action.getClass().getName());
        }
        if (isGameOver()) {
            endGame();
        } else {
            endTurn();
        }
    }

    // Handles executing a StealAction
    private void executeStealAction(StealAction stealAction) {
        boolean isChallenged = view.promptForChallenge(currentPlayer.getName() + " is attempting to steal from " + stealAction.getTargetPlayer().getName());
        boolean isBlocked = view.promptForBlock(stealAction.getTargetPlayer().getName() + ", do you want to block the steal?");
        stealAction.execute(isChallenged, isBlocked);
        if (isBlocked) {
            handleBlockAction(stealAction.getTargetPlayer(), stealAction, isChallenged);
        } else {
            updateView(); // Action concluded without block, or block was not challenged
        }
    }
    private void executeStealAction2(StealAction stealAction) {
        stealAction.execute(false, false);
    }

    // Handles executing an AssassinateAction
    private void executeAssassinateAction(AssassinateAction assassinateAction) {
        boolean isChallenged = view.promptForChallenge(currentPlayer.getName() + " is attempting to assassinate " + assassinateAction.getTargetPlayer().getName());
        boolean isBlocked = false;
        if (!isChallenged || assassinateAction.challenge()) {
            isBlocked = view.promptForBlock(assassinateAction.getTargetPlayer().getName() + ", do you want to block the assassination?");
        }
        boolean assassinationSuccessful = assassinateAction.execute(isChallenged, isBlocked);
        if (isChallenged && !assassinationSuccessful) {
            handleLoseCard(currentPlayer); // Current player loses a card due to unsuccessful challenge
        } else if (isBlocked) {
            handleBlockAction(assassinateAction.getTargetPlayer(), assassinateAction, isChallenged);
        } else if (assassinationSuccessful) {
            currentPlayer.updateCoins(-3); // Current player pays 3 coins for successful assassination
            handleLoseCard(assassinateAction.getTargetPlayer()); // Target player loses a card
        }
        updateView();
    }
    private void executeAssassinateAction2(AssassinateAction assassinateAction) {
        boolean assassinationSuccessful = assassinateAction.execute(false, false);
        if (assassinationSuccessful)
        {
            currentPlayer.updateCoins(-3); // Current player pays 3 coins for successful assassination
            handleLoseCard(assassinateAction.getTargetPlayer()); // Target player loses a card
        }
        updateView();
    }

    // Handles executing a ForeignAidAction
    private void executeForeignAidAction(ForeignAidAction foreignAidAction) {
        boolean isBlocked = view.promptForBlock(currentPlayer.getName() + " is attempting to take foreign aid. Does anyone want to block it?");
        foreignAidAction.execute(false, isBlocked);
        if (isBlocked) {
            handleBlockAction(game.getOpponent(currentPlayer), foreignAidAction, false);
        } else {
            updateView(); // Action concluded without block
        }
    }
    private void executeForeignAidAction2(ForeignAidAction foreignAidAction) {
        foreignAidAction.execute(false, false);
        updateView();
    }

    // Handles executing a TaxAction
    private void executeTaxAction(TaxAction taxAction) {
        boolean isChallenged = view.promptForChallenge(currentPlayer.getName() + " is attempting to collect tax.");
        boolean success = taxAction.execute(isChallenged, false);
        if (!success) {
            handleLoseCard(currentPlayer);
        }
        updateView(); // Action concluded
    }

    // Handles executing an IncomeAction
    private void executeIncomeAction(IncomeAction incomeAction) {
        incomeAction.execute(false, false);
        updateView(); // Action concluded
    }

    // Handles executing a CoupAction
    private void executeCoupAction(CoupAction coupAction) {
        boolean success = coupAction.execute(false, false);
        if (success)
        {
            // the opponent loses a card due to successful coup
            handleLoseCard(game.getOpponent(currentPlayer));
        }
        updateView(); // Action concluded
    }

    // Handles executing a SwapAction
    private void executeSwapAction(SwapAction swapAction) {
        boolean isChallenged = view.promptForChallenge(currentPlayer.getName() + " is attempting to swap influence.");
        boolean success = swapAction.execute(isChallenged, false);

        if (success) {
            // Prompt the player to select the cards to swap
            List<Card> selectedCards = view.promptForCardSelection(currentPlayer, 2);
            currentPlayer.swapCards(selectedCards);
        } else if (isChallenged) {
            // If the swap action failed and was challenged, the player loses a card
            handleLoseCard(currentPlayer);
        }

        updateView(); // Reflect the new game state in the view
    }

    // Handles the outcome of a BlockAction, potentially including a challenge
    private void handleBlockAction(Player blocker, Action actionToBlock, boolean isChallenged) {
        BlockAction blockAction = new BlockAction(blocker, actionToBlock);
        boolean blockSuccessful = blockAction.execute(isChallenged, false);

        if (isChallenged) {
            if (blockSuccessful) {
                // Block succeeded and was challenged
                System.out.println(blocker.getName() + " successfully blocked " + actionToBlock.getNameOfAction() + " action.");
            } else {
                // Block failed and was challenged
                System.out.println(blocker.getName() + " failed to block " + actionToBlock.getNameOfAction() + " action.");
                handleLoseCard(blocker); // Blocker loses a card

                // Execute the original action directly
                if (actionToBlock instanceof StealAction) {
                    executeStealAction2((StealAction) actionToBlock);
                } else if (actionToBlock instanceof AssassinateAction) {
                    executeAssassinateAction2((AssassinateAction) actionToBlock);
                } else if (actionToBlock instanceof ForeignAidAction) {
                    executeForeignAidAction2((ForeignAidAction) actionToBlock);
                }
            }
        } else {
            if (blockSuccessful) {
                // Block succeeded without challenge
                System.out.println(blocker.getName() + " blocked " + actionToBlock.getNameOfAction() + " action.");
            } else {
                // Block failed without challenge
                System.out.println(blocker.getName() + " failed to block " + actionToBlock.getNameOfAction() + " action.");

                // Execute the original action directly
                if (actionToBlock instanceof StealAction) {
                    executeStealAction2((StealAction) actionToBlock);
                } else if (actionToBlock instanceof AssassinateAction) {
                    executeAssassinateAction2((AssassinateAction) actionToBlock);
                } else if (actionToBlock instanceof ForeignAidAction) {
                    executeForeignAidAction2((ForeignAidAction) actionToBlock);
                }
            }
        }

        updateView(); // Reflect the new game state in the view
    }

    // Ends the current player's turn and switches to the next player
    private void endTurn() {
        currentPlayer = game.switchTurns();
        updateView();
    }

    // Checks if the game has ended (e.g., only one player remains)
    private boolean isGameOver() {
        return game.getActivePlayers().size() == 1;
    }

    // Retrieves the winner of the game
    public Player getWinner() {
        if (isGameOver()) {
            return game.getActivePlayers().get(0);
        }
        return null;
    }

    public void handleLoseCard(Player player) {
        Card cardToLose = view.promptPlayerForCardToGiveUp(player);
        player.loseCard(cardToLose);
        updateView();
    }

    // Ends the game and displays the winner
    private void endGame() {
        Player winner = game.getActivePlayers().get(0);
        view.displayWinner(winner);
        // Optionally, you can ask the players if they want to play again here
    }

    private void updateView() {
        view.updatePlayerInfo(game.getPlayers());
        view.updateCurrentPlayer(currentPlayer);
        view.updateAvailableActions(game.getAvailableActions(currentPlayer));
        view.updateDeckInfo(game.getDeck());
    }

}