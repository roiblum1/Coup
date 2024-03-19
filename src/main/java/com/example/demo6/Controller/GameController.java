package com.example.demo6.Controller;

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
        // Challenge Phase
        boolean isChallenged = view.promptForChallenge(currentPlayer.getName() + " is attempting to steal from " + stealAction.getTargetPlayer().getName());
        if (isChallenged) {
            boolean challengeResult = handleChallenge(stealAction);
            if (!challengeResult) {
                updateView(); // Update view due to challenge outcome.
                return; // Stop action if challenge was successful.
            }
            // Continue to block phase if challenge fails (action is valid).
        }

        // Block Phase
        boolean isBlocked = view.promptForBlock(stealAction.getTargetPlayer().getName() + ", do you want to block the steal?");
        if (isBlocked) {
            // Challenge the Block
            boolean isBlockChallenged = view.promptForChallenge(currentPlayer.getName() + ", do you want to challenge the block of " + stealAction.getTargetPlayer().getName() + "?");
            handleBlockAction(stealAction.getTargetPlayer(), stealAction, isBlockChallenged);
            // Note: handleBlockAction will decide if action proceeds based on block challenge outcome.
        } else {
            // Execute the steal action directly if not blocked.
            stealAction.execute(false, false);
            updateView(); // Update view as action concludes successfully.
        }
    }


    private void executeStealAction2(StealAction stealAction) {
        stealAction.execute(false, false);
    }

    // Handles executing an AssassinateAction
    private void executeAssassinateAction(AssassinateAction assassinateAction) {
        // Initial challenge phase
        boolean isChallenged = view.promptForChallenge(currentPlayer.getName() + " is attempting to assassinate " + assassinateAction.getTargetPlayer().getName());
        if (isChallenged) {
            boolean challengeResult = handleChallenge(assassinateAction);
            if (!challengeResult) {
                // Assassination stopped due to successful challenge against the assassin
                return; // Stop the action here.
            }
        }
        // Block phase only if the action was not successfully challenged or if the assassin proved their role
        boolean isBlocked = view.promptForBlock(assassinateAction.getTargetPlayer().getName() + ", do you want to block the assassination?");
        if (isBlocked) {
            boolean blockChallengeResult = view.promptForChallenge(currentPlayer.getName() + ", do you want to challenge the block?");
            handleBlockAction(assassinateAction.getTargetPlayer(), assassinateAction, blockChallengeResult);
            return; // Block handling will take care of action conclusion.
        }
        // Proceed with the assassination if not blocked or if the block challenge fails
        if (assassinateAction.execute(false, false)) {
            // Assassination successful
            currentPlayer.updateCoins(-3); // Deduct cost of assassination
            handleLoseCard(assassinateAction.getTargetPlayer()); // Target loses a card
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
            boolean isChallenged = view.promptForChallenge(currentPlayer.getName() + " is do you want to challenged the block of " + game.getOpponent(currentPlayer).getName());
            handleBlockAction(game.getOpponent(currentPlayer), foreignAidAction, isChallenged);
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
        if (isChallenged) {
            if (success) {
                Player challenger = game.getOpponent(currentPlayer);
                handleLoseCard(challenger);
                view.displayMessage(currentPlayer.getName() + " successfully proved their action. " + challenger.getName() + " loses a card.");
            } else {
                handleLoseCard(currentPlayer);
                view.displayMessage(currentPlayer.getName() + " could not prove their action. They lose a card.");
            }
        }
        updateView();
    }

    //* Handles executing an IncomeAction */
    private void executeIncomeAction(IncomeAction incomeAction) {
        incomeAction.execute(false, false);
        updateView();
    }

    //* Handles executing a CoupAction */
    private void executeCoupAction(CoupAction coupAction) {
        boolean success = coupAction.execute(false, false);
        if (success)
        {
            handleLoseCard(game.getOpponent(currentPlayer));
        }
        updateView();
    }

    //* Handles executing a SwapAction */
    private void executeSwapAction(SwapAction swapAction) {
        boolean isChallenged = view.promptForChallenge(currentPlayer.getName() + " is attempting to swap influence.");
        if (isChallenged) {
            boolean challengeSuccess = handleChallenge(swapAction);
            if (!challengeSuccess) {
                return;
            }
        }
        List<Card> newCards = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            newCards.add(game.getDeck().getCard());
        }
        List<Card> swapOptions = new ArrayList<>(currentPlayer.getCards());
        swapOptions.addAll(newCards);
        List<Card> selectedCards = view.promptForCardSelection(swapOptions, 2);
        currentPlayer.swapCards(selectedCards, newCards);
        updateView();
    }

    //* Handles the outcome of a BlockAction, potentially including a challenge */
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
                if (actionToBlock instanceof StealAction) {
                    executeStealAction2((StealAction) actionToBlock);
                } else if (actionToBlock instanceof AssassinateAction) {
                    executeAssassinateAction2((AssassinateAction) actionToBlock);
                } else if (actionToBlock instanceof ForeignAidAction) {
                    executeForeignAidAction2((ForeignAidAction) actionToBlock);
                }
            }
        }
        updateView();
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