package com.example.demo6.Model;

import com.example.demo6.Model.Actions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Game
{
    private List<Player> playerList;
    private Deck deck;
    private Map<String, Action> rules;
    private int currentPlayerIndex;

    public Game(Deck deck)
    {
        currentPlayerIndex = 0;
        this.playerList = new ArrayList<>();
        this.deck = deck;
        this.rules = new HashMap<>();

    }

    public void initAllPossibleAction()
    {
        rules.put("income", new IncomeAction(playerList.get(currentPlayerIndex)));
        rules.put("tax", new TaxAction(playerList.get(currentPlayerIndex), playerList.get((currentPlayerIndex+1)%2)));
        rules.put("swap", new SwapAction(playerList.get(currentPlayerIndex)));
        rules.put("steal", new StealAction(playerList.get(currentPlayerIndex), playerList.get((currentPlayerIndex+1)%2)));
        rules.put("assassinate", new AssassinateAction(playerList.get(currentPlayerIndex), playerList.get((currentPlayerIndex+1)%2)));
        rules.put("foreign_aid", new ForeignAidAction(playerList.get(currentPlayerIndex), playerList.get((currentPlayerIndex+1)%2)));
        rules.put("coup", new CoupAction(playerList.get(currentPlayerIndex), playerList.get((currentPlayerIndex+1)%2)));
    }
    // Adds a new player to the game.
    public void addPlayer(Player player) {
        player.setDeck(deck);
        playerList.add(player);
        player.pickCards();
    }

    //function that get all the possible actions and check if the player can perform each one if he can add it to the list
    public List<Action> getPossibleActions(Player player) {
        List<Action> actions = new ArrayList<>();
        actions.add(new IncomeAction(player));
        actions.add(new ForeignAidAction(player, getOpponent(player)));
        actions.add(new CoupAction(player, getOpponent(player)));
        actions.add(new TaxAction(player, getOpponent(player)));
        actions.add(new AssassinateAction(player, getOpponent(player)));
        actions.add(new StealAction(player, getOpponent(player)));
        actions.add(new SwapAction(player));
        return actions.stream().filter(Action::canPlayerPerform).collect(Collectors.toList());
    }

    private Player getOpponent(Player player) {
        return playerList.stream().filter(p -> !p.equals(player)).findFirst().orElse(null);
    }

    //get the active players
    private List<Player> getActivePlayers()
    {
        List<Player> activePlayers = new ArrayList<>();
        for (Player player : playerList) {
            if (!player.getCards().isEmpty())
                activePlayers.add(player);
        }
        return activePlayers;
    }

    public Player getCurrentPlayer() {
        return playerList.get(currentPlayerIndex);
    }

    public List<Player> getPlayers()
    {
        return this.playerList;
    }

    public Deck getDeck() {
        return this.deck;
    }


    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public void switchTurns() {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerList.size();
    }
}
