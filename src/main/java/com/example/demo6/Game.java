package com.example.demo6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game
{
    private List<Player> playerList;
    private Deck deck;
    private Map<String, Action> rules;

    public Game(Deck deck)
    {
        this.playerList = new ArrayList<>();
        this.deck = deck;
        this.rules = new HashMap<>();
    }

    public void initAllPossibleAction()
    {
        rules.put("income", new Action());
        rules.put("steal", new Action());
        rules.put("assassination", new Action());
        rules.put("tax", new Action());
        rules.put("block", new Action());
    }
    // Adds a new player to the game.
    public void addPlayer(String name) {
        Player player = new Player(name);
        player.setDeck(deck);
        playerList.add(player);
        player.pickCards();
    }

    //function that get all the possible actions and check if the player can perform each one if he can add it to the list
    public Map<String,Action> getPossibleActions(Player player)
    {
        Map<String, Action> possibleActions = new HashMap<String, Action>();
        for (String action : rules.keySet()) {
            if (rules.get(action).canPlayerPerform(player))
                possibleActions.put(action, rules.get(action));
        }
        return possibleActions;
    }
}
