package com.example.demo6.Model;

import com.example.demo6.Model.Actions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Game  {
    private List<Player> playerList;
    private Deck deck;
    private int currentPlayerIndex;

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
            // or throw an appropriate exception
            return null;
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
        Player currentPlayer = getCurrentPlayer();
        Player nextPlayer = activePlayers.stream()
                .filter(p -> !p.equals(currentPlayer))
                .findFirst()
                .orElse(null);

        if (nextPlayer == null) {
            System.out.println("No opponent found");
            return null;
        }
        currentPlayerIndex = activePlayers.indexOf(nextPlayer);
        return nextPlayer;
    }


    /**
     * Sets the list of players in the game.
     * @param clonedPlayerList The new list of players.
     */
    public void setPlayerList(List<Player> clonedPlayerList) {
        this.playerList = clonedPlayerList;
    }

    /**
     * Retrieves the first player in the list, typically the human player.
     *
     * @return the first player in the list.
     */
    public Player getHumanPlayer()
    {
        return this.playerList.get(0);
    }
    /**
     * Retrieves the AI player from the list of players.
     *
     * @return the AI player.
     */
    public Player getAIPlayer() {
        return this.playerList.get(1);
    }
    /**
     * Creates a deep copy of this game instance.
     * @return a deep copy of the game.
     */
    public Game deepCopy() {
        Game copiedGame = new Game(this.deck.deepCopy());
        copiedGame.setCurrentPlayerIndex(this.currentPlayerIndex);
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
            if (action.getActionCode() == ActionCode.COUP || action.getActionCode() == ActionCode.ASSASSINATE) {
                boolean success = action.execute(false, false);
                if (success) {
                    //card 0 is the card that send to this function in the cards list
                    //It represented the card that the opponent choose to return
                    getOpponent(action.getPlayer()).returnCard(cards.get(0));
                }
            }
            else if (action.getActionCode() == ActionCode.SWAP) {
                Player currentPlayer = action.getPlayer();
                List<Card> selectedCards = cards.subList(0, 2);
                List<Card> newCards = cards.subList(2, 4);
                currentPlayer.swapCards(selectedCards, newCards);
            }
        } else {
            action.execute(false, false);
        }
    }

    /**
     * Retrieves a hash value representing the current state of the game.
     * This hash value is used to compare the current state of the game with previous states,
     * and to determine if the game has reached a winning or losing state.
     *
     * @return a long hash value representing the current state of the game.
     */
    public long getStateHash() {
        long hash = 0;
        // Hash the cards of the AI player
        Player aiPlayer = getAIPlayer();
        for (Card card : aiPlayer.getCards()) {
            hash = 31 * hash + card.hashCode();
        }
        Player humanPlayer = getHumanPlayer();
        hash = 31 * hash + HashUtils.hashCard(humanPlayer.getCards().size());
        // Hash the coins of each player
        for (Player player : getPlayers()) {
            hash = 31 * hash + HashUtils.hashCoin(player.getCoins());
        }
        return hash;
    }

    public static class HashUtils{
        private static final int CARD_PRIME1 = 31;
        private static final int CARD_PRIME2 = 37;
        private static final int CARD_PRIME3 = 41;
        private static final int COIN_PRIME1 = 43;
        private static final int COIN_PRIME2 = 47;
        private static final int COIN_PRIME3 = 53;
        /**
         * Hashes a card's value using a combination of prime numbers.
         *
         * @param value The value of the card to be hashed.
         * @return A hash value for the card.
         */
        public static int hashCard(int value) {
            return (value * CARD_PRIME1) ^ (value * CARD_PRIME2) ^ (value * CARD_PRIME3);
        }

        /**
         * Hashes a coin's value using a combination of prime numbers.
         *
         * @param value The value of the coin to be hashed.
         * @return A hash value for the coin.
         */
        public static int hashCoin(int value) {
            return (value * COIN_PRIME1) ^ (value * COIN_PRIME2) ^ (value * COIN_PRIME3);
        }
    }
}
