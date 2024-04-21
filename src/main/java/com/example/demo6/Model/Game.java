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

    //* Initializes a game with a specified deck */
    public Game(Deck deck) {
        currentPlayerIndex = 0;
        this.playerList = new ArrayList<>();
        this.deck = deck;
    }

    //* Adds a new player to the game */
    public void addPlayer(Player player) {
        player.setDeck(deck);
        playerList.add(player);
        player.pickCards();
    }

    //* Retrieves all the possible actions for the current player */
    public List<Action> getAvailableActions(Player currentPlayer) {
        List<Action> actions = new ArrayList<>();
        actions.add(new IncomeAction(currentPlayer));
        actions.add(new ForeignAidAction(currentPlayer));
        actions.add(new CoupAction(currentPlayer, getOpponent(currentPlayer)));
        actions.add(new TaxAction(currentPlayer));
        actions.add(new AssassinateAction(currentPlayer, getOpponent(currentPlayer)));
        actions.add(new StealAction(currentPlayer, getOpponent(currentPlayer)));
        actions.add(new SwapAction(currentPlayer));
        List<Action> availableActions = actions.stream().filter(Action::canPlayerPerform).collect(Collectors.toList());
        return availableActions;
    }

    //* Retrieves the opponent of the specified player */
    public Player getOpponent(Player player) {
        return playerList.stream().filter(p -> !p.equals(player)).findFirst().orElse(null);
    }

    //* Retrieves the active players */
    public List<Player> getActivePlayers() {
        return playerList.stream().filter(player -> !player.getCards().isEmpty()).collect(Collectors.toList());
    }

    //* Retrieves the current player */
    public Player getCurrentPlayer() {
        if (playerList.isEmpty()) {
            if (isGameOver())
            {
                System.out.println("Game Over");
            }
            return null; // or throw an appropriate exception
        }
        return getActivePlayers().get(currentPlayerIndex);
    }

    //* Retrieves all players */
    public List<Player> getPlayers() {
        return this.playerList;
    }

    //* Retrieves the deck */
    public Deck getDeck() {
        return this.deck;
    }

    //* Sets the deck */
    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    //* Checks if the game is over */
    public boolean isGameOver() {
        return getActivePlayers().size() == 1;
    }

    //* Switches turns to the next player */
    public Player switchTurns() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.isEmpty()) {
            System.out.println("No active players left");
            return null;
        }

        System.out.println("Active players are: ");
        for (Player player : activePlayers) {
            System.out.println(player.getName());
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % activePlayers.size();

        System.out.println("Switching turns from " + getCurrentPlayer().getName() + " to " + activePlayers.get(currentPlayerIndex).getName());
        return activePlayers.get(currentPlayerIndex);
    }

    //* Retrieve the winner of the game //
    public Player getWinner() {
        if (isGameOver()) {
            return getActivePlayers().get(0);
        }
        else return null;
    }


    public void setPlayerList(List<Player> clonedPlayerList) {
        this.playerList = clonedPlayerList;
    }


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
    }
}
