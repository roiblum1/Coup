package com.example.demo6;

import java.util.ArrayList;
import java.util.List;

public class Player
{
    private final int NUMBER_OF_CARDS = 2;
    private String name;
    private int coins;
    private Action action;
    private List<Card> cards;
    private Deck currentDeck;

    public Player(String name)
    {
        this.name = name;
        this.coins = 3; //the base number of coins that each player start with
        this.cards = new ArrayList<>();
    }
    public String getName()
    {
        return this.name;
    }

    public void setDeck(Deck currentDeck) {
        this.currentDeck = currentDeck;
    }

    public void updateCoins(int coins)
    {
        this.coins += coins;
        if (coins < 0) this.coins = 0 ;
    }
    //cards function :
    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public List<Card> getCards() {
        return this.cards;
    }
    public void pickCards()
    {
        for (int i = 0; i < NUMBER_OF_CARDS; i++) {
            Card card = this.currentDeck.getCard();
            if (card != null)
            {
                this.cards.add(card);
            }
            else System.out.println("There are no cards left in the deck");
        }
    }
    //return card from the hand of the player back to the deck
    public void returnCard(Card card)
    {
        this.cards.remove(card);
        this.currentDeck.returnCard(card);
    }

    //swap the current cards with new cards from the deck
    public void swap() {
        List<Card> oldCards = new ArrayList<>(this.cards); // Store old cards
        this.cards.clear(); // Clear current cards

        // Draw new cards
        for (int i = 0; i < oldCards.size(); i++) {
            Card card = this.currentDeck.getCard();
            if (card != null) {
                this.cards.add(card);
            } else {
                System.out.println("There are no cards left in the deck");
            }
        }

        // Return old cards to the deck
        for (Card card : oldCards) {
            this.currentDeck.returnCard(card);
        }
    }

    //Action Functions :
    public void setAction(Action action) {
        this.action = action;
    }

    public void doAction() { if (action != null) action.execute(this);}

    @Override
    public String toString() {
        return getName();
    }
}
