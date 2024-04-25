package com.example.demo6.Model;

import com.example.demo6.Model.Actions.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Game implements Serializable {
    private List<Player> playerList;
    private Deck deck;
    private int currentPlayerIndex;
    private Action lastExecutedAction;


    /**
     * Initializes a game with a specified deck.
     * @param deck The initial deck to be used in the game.
     */
    public Game(Deck deck) {
        currentPlayerIndex = 0;
        this.playerList = new ArrayList<>();
        this.deck = deck;
    }

    /**
     * Adds a new player to the game and assigns them an initial hand of cards.
     * @param player The player to be added to the game.
     */
    public void addPlayer(Player player) {
        player.setDeck(deck);
        playerList.add(player);
        player.pickCards();
    }

    /**
     * Gets the index of the current player in the turn order.
     * @return The index of the current player.
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Sets the index of the current player in the turn order.
     * @param currentPlayerIndex The new index for the current player.
     */
    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }
    /**
     * Retrieves all the possible actions for the current player.
     * @param currentPlayer The player whose actions are to be determined.
     * @return A list of possible actions for the player.
     */
    public List<Action> getAvailableActions(Player currentPlayer) {
        List<Action> actions = new ArrayList<>();
        actions.add(new IncomeAction(currentPlayer, getOpponent(currentPlayer)));
        actions.add(new ForeignAidAction(currentPlayer, getOpponent(currentPlayer)));
        actions.add(new CoupAction(currentPlayer, getOpponent(currentPlayer)));
        actions.add(new TaxAction(currentPlayer, getOpponent(currentPlayer)));
        actions.add(new AssassinateAction(currentPlayer, getOpponent(currentPlayer)));
        actions.add(new StealAction(currentPlayer, getOpponent(currentPlayer)));
        actions.add(new SwapAction(currentPlayer, getOpponent(currentPlayer)));
        return actions.stream().filter(Action::canPlayerPerform).collect(Collectors.toList());
    }


    /**
     * Retrieves the opponent of the specified player.
     * @param player The player whose opponent is to be identified.
     * @return The opponent player if available, null if no opponent exists.
     */
    public Player getOpponent(Player player) {
        return playerList.stream().filter(p -> !p.equals(player)).findFirst().orElse(null);
    }

    /**
     * Retrieves all active players (players with cards left).
     * @return A list of active players.
     */
    public List<Player> getActivePlayers() {
        return playerList.stream().filter(player -> !player.getCards().isEmpty()).collect(Collectors.toList());
    }

    /**
     * Retrieves the current player based on the currentPlayerIndex.
     * @return The current player.
     */
    public Player getCurrentPlayer() {
        if (playerList.isEmpty()) {
            if (isGameOver()) {
                System.out.println("Game Over");
            }
            return null; // or throw an appropriate exception
        }
        return getActivePlayers().get(currentPlayerIndex);
    }

    /**
     * Retrieves the full list of players in the game.
     * @return The list of players.
     */
    public List<Player> getPlayers() {
        return this.playerList;
    }

    /**
     * Retrieves the deck used in the game.
     * @return The game deck.
     */
    public Deck getDeck() {
        return this.deck;
    }

    /**
     * Sets the deck for the game.
     * @param deck The new deck to be used.
     */
    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    /**
     * Checks if the game is over (only one active player remains).
     * @return true if the game is over, otherwise false.
     */
    public boolean isGameOver() {
        return getActivePlayers().size() <= 1;
    }


    /**
     * Switches turns to the next player.
     *
     * @return the next player to take their turn.
     */
    public Player switchTurns() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.isEmpty()) {
            System.out.println("No active players left");
            return null;
        }
        // Toggle the current player between the two active players
        Player currentPlayer = getCurrentPlayer();
        Player nextPlayer = activePlayers.stream()
                .filter(p -> !p.equals(currentPlayer))
                .findFirst()
                .orElse(null);

        if (nextPlayer == null) {
            System.out.println("No opponent found");
            return null;
        }

        // Update the current player index to the index of the next player
        currentPlayerIndex = activePlayers.indexOf(nextPlayer);
        System.out.println("Switching turns from " + currentPlayer.getName() + " to " + nextPlayer.getName());
        return nextPlayer;
    }

    /**
     * Draws a specified number of cards from the deck.
     * @param count The number of cards to draw.
     * @return A list of cards drawn from the deck.
     */
    public List<Card> drawCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (!this.getDeck().isEmpty()) {
                cards.add(this.getDeck().getCard());
            }
        }
        return cards;
    }

    /**
     * Sets the list of players in the game.
     * @param clonedPlayerList The new list of players.
     */
    public void setPlayerList(List<Player> clonedPlayerList) {
        this.playerList = clonedPlayerList;
    }

    /**
     * Sets the last action executed in the game.
     * @param lastExecutedAction The last action to set.
     */
    public void setLastExecutedAction(Action lastExecutedAction) {
        this.lastExecutedAction = lastExecutedAction;
    }

    /**
     * Gets the last action executed in the game.
     * @return The last executed action.
     */
    public Action getLastExecutedAction() {
        return lastExecutedAction;
    }


    /**
     * Creates a deep copy of this game instance.
     * @return a deep copy of the game.
     */
    public Game deepCopy() {
        Game copiedGame = new Game(this.deck.deepCopy());
        copiedGame.setCurrentPlayerIndex(this.currentPlayerIndex);
        copiedGame.setLastExecutedAction(this.lastExecutedAction);
        List<Player> copiedPlayerList = new ArrayList<>();
        for (Player player : this.playerList) {
            Player copiedPlayer = new Player(player.getName());
            copiedPlayer.setCoins(player.getCoins());
            copiedPlayer.setCards(player.deepCopyCards());
            copiedPlayer.setDeck(copiedGame.getDeck());
            copiedPlayerList.add(copiedPlayer);
        }
        copiedGame.setPlayerList(copiedPlayerList);
        return copiedGame;
    }

    /**
     * Executes the specified action on the game.
     *
     * @param action  The action to be executed.
     * @param cards    The list of cards to be used in the execution of the action.
     */
    public void executeAction(Action action, List<Card> cards) {
        if (cards != null) {
            if (action.getActionCode() == ActionCode.COUP) {
                boolean success = action.execute(false, false);
                if (success) {
                    getOpponent(action.getPlayer()).returnCard(cards.get(0));
                }
            } else if (action.getActionCode() == ActionCode.ASSASSINATE) {
                boolean success = action.execute(false, false);
                if (success) {
                    getOpponent(action.getPlayer()).updateCoins(-3);
                    getOpponent(action.getPlayer()).returnCard(cards.get(0));
                }
            } else if (action.getActionCode() == ActionCode.SWAP) {
                Player currentPlayer = action.getPlayer();
                List<Card> selectedCards = cards.subList(0, 2);
                List<Card> newCards = cards.subList(2, 4);

                // Call the swapCards method on the current player
                currentPlayer.swapCards(selectedCards, newCards);
            }
        } else {
            action.execute(false, false);
        }
        lastExecutedAction = action;
    }
}
