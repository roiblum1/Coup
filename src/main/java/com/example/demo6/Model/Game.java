package com.example.demo6.Model;

import com.example.demo6.Model.Actions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Game {
    private List<Player> playerList;
    private Deck deck;
    private int currentPlayerIndex;

    public Game(Deck deck) {
        currentPlayerIndex = 0;
        this.playerList = new ArrayList<>();
        this.deck = deck;
    }

    // Adds a new player to the game.
    public void addPlayer(Player player) {
        player.setDeck(deck);
        playerList.add(player);
        player.pickCards();
    }

    // Function that gets all the possible actions for the current player
    public List<Action> getAvailableActions(Player currentPlayer) {
        List<Action> actions = new ArrayList<>();
        actions.add(new IncomeAction(currentPlayer));
        actions.add(new ForeignAidAction(currentPlayer));
        actions.add(new CoupAction(currentPlayer, getOpponent(currentPlayer)));
        actions.add(new TaxAction(currentPlayer));
        actions.add(new AssassinateAction(currentPlayer, getOpponent(currentPlayer)));
        actions.add(new StealAction(currentPlayer, getOpponent(currentPlayer)));
        actions.add(new SwapAction(currentPlayer));
        return actions.stream().filter(Action::canPlayerPerform).collect(Collectors.toList());
    }

    public Player getOpponent(Player player) {
        return playerList.stream().filter(p -> !p.equals(player)).findFirst().orElse(null);
    }

    // Get the active players
    public List<Player> getActivePlayers() {
        return playerList.stream().filter(player -> !player.getCards().isEmpty()).collect(Collectors.toList());
    }

    public Player getCurrentPlayer() {
        return playerList.get(currentPlayerIndex);
    }

    public List<Player> getPlayers() {
        return this.playerList;
    }

    public Deck getDeck() {
        return this.deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public Player switchTurns() {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerList.size();
        return getCurrentPlayer();
    }
}