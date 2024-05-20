package com.example.demo6.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player {
    private final int NUMBER_OF_CARDS = 2;
    public final int NUMBER_OF_COINS = 3;
    private final String name;
    private int coins;
    private List<Card> cards;
    private Deck currentDeck;

    /**
     * Initializes a player with a name.
     * @param name the name of the player
     */
    public Player(String name) {
        this.name = name;
        this.coins = NUMBER_OF_COINS;
        this.cards = new ArrayList<>();
    }

    /**
     * Retrieves the name of the player.
     * @return the name of the player
     */
    public String getName() {
        return this.name;
    }

    /**
     * Retrieves the number of coins the player currently has.
     * @return The current coin count of the player.
     */
    public int getCoins() {
        return this.coins;
    }

    /**
     * Retrieves the list of cards currently held by the player.
     * @return A list of cards the player currently holds.
     */
    public List<Card> getCards() {
        return this.cards;
    }

    /**
     * Sets the deck for the player.
     * @param currentDeck the Deck object that will be the new deck for the player
     */
    public void setDeck(Deck currentDeck) {
        this.currentDeck = currentDeck;
    }

    /**
     * Sets the number of coins the player has.
     * @param coins The new number of coins the player has.
     */
    public void setCoins(int coins) {
        this.coins = coins;
    }

    /**
     * Sets the player's cards to a new list of cloned cards.
     * @param clonedCards The new list of cloned cards to be set for the player.
     */
    public void setCards(List<Card> clonedCards) {
        if (clonedCards != null) {
            this.cards.clear();
            this.cards.addAll(clonedCards);
        } else {
            System.out.println("Error: Input list of cards is null");
        }
    }

    /**
     * Updates the number of coins the player has by a specified amount. Ensures coin count does not go below zero.
     * @param coins The amount to update the coin count by. Can be negative or positive.
     */
    public void updateCoins(int coins) {
        this.coins += coins;
        if (this.coins < 0) this.coins = 0;
    }

    /**
     * Draws a specified number of cards from the deck and adds them to the player's hand.
     */
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

    /**
     * Returns a specified card to the deck.
     * @param card The card to return to the deck.
     */
    public void returnCard(Card card) {
        this.cards.remove(card);
        this.currentDeck.returnCard(card);
    }

    /**
     * Swaps selected cards from the player's hand with new cards drawn from the deck,
     * returning any unselected cards to the deck.
     * @param selectedCards The cards to keep.
     * @param newCards New cards drawn from the deck.
     */
    public void swapCards(List<Card> selectedCards, List<Card> newCards) {
        List<Card> totalCards = new ArrayList<>(this.cards);
        totalCards.addAll(newCards);
        List<Card> toReturn = totalCards.stream()
                .filter(card -> !selectedCards.contains(card))
                .toList();

        toReturn.forEach(this.currentDeck::returnCard);
        this.cards.clear();
        this.cards.addAll(selectedCards);
    }

    /**
     * Removes a random card from the player's hand and returns it to the deck.
     * used for human player in the monte carlo simulation.
     */
    public void loseRandomInfluence() {
        Random random = new Random();
        if (!cards.isEmpty()) {
            int randomIndex = random.nextInt(cards.size());
            Card selectedCard = cards.remove(randomIndex);
            currentDeck.returnCard(selectedCard);
        }
    }

    /**
     * Selects 2 random cards from the player's hand and a set of drawn cards, returns these selected cards,
     * and returns the unselected cards to the deck.
     * @param drawnCards Additional cards drawn from the deck to be considered for selection.
     * @return A list of two randomly selected cards to be kept by the player.
     */
    public List<Card> selectRandomCardsToKeep(List<Card> drawnCards) {
        Random random = new Random();
        List<Card> allCards = new ArrayList<>(cards);
        allCards.addAll(drawnCards);
        List<Card> selectedCards = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            if (!allCards.isEmpty()) {
                int randomIndex = random.nextInt(allCards.size());
                Card selectedCard = allCards.remove(randomIndex);
                selectedCards.add(selectedCard);
            }
        }
        cards.clear();
        cards.addAll(selectedCards);
        allCards.forEach(this.currentDeck::returnCard);
        return selectedCards;
    }

    /**
     * Checks if the player has a card of a specific type.
     * @param cardType The type of card to check for.
     * @return true if the player has the card, false otherwise.
     */
    public boolean hasCard(Deck.CardType cardType) {
        return this.cards.stream().anyMatch(card -> card.getType() == cardType);
    }

    /**
     * Creates a deep copy of the player's cards.
     * @return a deep copy of the card list.
     */
    public List<Card> deepCopyCards() {
        List<Card> copiedCards = new ArrayList<>();
        for (Card card : this.getCards()) {
            copiedCards.add(new Card(card.getType()));
        }
        return copiedCards;
    }
}
