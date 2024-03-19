package com.example.demo6.Model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final int NUMBER_OF_CARDS = 2;
    private final String name;
    private int coins;
    private List<Card> cards;
    private Deck currentDeck;

    //* Initializes a player with a name */
    public Player(String name) {
        this.name = name;
        this.coins = 3; //the base number of coins that each player start with
        this.cards = new ArrayList<>();
    }

    //* Retrieves the name of the player */
    public String getName() {
        return this.name;
    }

    //* Sets the deck for the player */
    public void setDeck(Deck currentDeck) {
        this.currentDeck = currentDeck;
    }

    //* Retrieves the number of coins the player has */
    public int getCoins() {
        return this.coins;
    }

    //* Updates the number of coins the player has */
    public void updateCoins(int coins) {
        this.coins += coins;
        if (this.coins < 0) this.coins = 0;
    }

    //* Retrieves the cards held by the player */
    public List<Card> getCards() {
        return this.cards;
    }

    //* Picks cards from the deck */
    public void pickCards() {
        for (int i = 0; i < NUMBER_OF_CARDS; i++) {
            Card card = this.currentDeck.getCard();
            if (card != null) {
                this.cards.add(card);
            } else {
                System.out.println("There are no cards left in the deck");
            }
        }
    }

    //* Returns a card to the deck */
    public void returnCard(Card card) {
        this.cards.remove(card);
        this.currentDeck.returnCard(card);
    }

    //* Swaps cards with the deck */
    public void swapCards(List<Card> selectedCards) {
        List<Card> oldCards = new ArrayList<>(this.cards);
        this.cards.clear();
        this.cards.addAll(selectedCards);
        oldCards.removeAll(selectedCards);
        for (Card card : oldCards) {
            this.currentDeck.returnCard(card);
        }
    }

    //* Checks if the player has a specific card */
    public boolean hasCard(Card givenCard) {
        return this.cards.stream().anyMatch(card -> card.equals(givenCard));
    }
}
