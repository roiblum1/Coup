package com.example.demo6.Model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final int NUMBER_OF_CARDS = 2;
    private String name;
    private int coins;
    private List<Card> cards;
    private Deck currentDeck;

    public Player(String name) {
        this.name = name;
        this.coins = 3; //the base number of coins that each player start with
        this.cards = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public void setDeck(Deck currentDeck) {
        this.currentDeck = currentDeck;
    }

    public int getCoins() {
        return this.coins;
    }

    public void updateCoins(int coins) {
        this.coins += coins;
        if (this.coins < 0) this.coins = 0;
    }

    public List<Card> getCards() {
        return this.cards;
    }

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

    public void returnCard(Card card) {
        this.cards.remove(card);
        this.currentDeck.returnCard(card);
    }

    public void swapCards(List<Card> selectedCards) {
        List<Card> oldCards = new ArrayList<>(this.cards);
        this.cards.clear();
        this.cards.addAll(selectedCards);
        oldCards.removeAll(selectedCards);
        for (Card card : oldCards) {
            this.currentDeck.returnCard(card);
        }
    }

    public boolean hasCard(Card givenCard) {
        return this.cards.stream().anyMatch(card -> card.equals(givenCard));
    }

    public void loseCard(Card cardToLose) {
        returnCard(cardToLose);
    }
}