package com.example.demo6.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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

    /**
     * Swaps the player's selected cards with the deck, returning any unselected cards to the deck.
     *
     * @param selectedCards The cards to keep in the player's hand.
     * @param newCards The new cards drawn from the deck.
     */
    public void swapCards(List<Card> selectedCards, List<Card> newCards) {
        // Combine the current cards and new cards to form the total pool of cards to choose from.
        List<Card> totalCards = new ArrayList<>(this.cards);
        totalCards.addAll(newCards);

        // Determine which cards need to be returned to the deck.
        // These are the cards in the total pool that are not in the selected cards.
        List<Card> toReturn = totalCards.stream()
                .filter(card -> !selectedCards.contains(card))
                .toList();

        // Return the unselected cards to the deck.
        toReturn.forEach(this.currentDeck::returnCard);

        // Clear the current cards and add the selected cards to the player's hand.
        this.cards.clear();
        this.cards.addAll(selectedCards);
    }


    //* Checks if the player has a specific card */

    public boolean hasCard(Deck.CardType cardType)
    {
        return this.cards.stream().anyMatch(card -> card.getName().equals(cardType.getName()));
    }

    // This function selects a random card from the player's influence and returns it to the deck
    public void loseRandomInfluence() {
        Random random = new Random();
        if (!cards.isEmpty()) {
            int randomIndex = random.nextInt(cards.size());
            Card selectedCard = cards.remove(randomIndex);
            currentDeck.returnCard(selectedCard);
        }
    }

    // This function selects 2 random cards from the player's influence and the drawnCards,
    // and returns the selected cards. The other cards are returned to the deck.
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

        // Return the remaining cards to the deck
        for (Card card : allCards) {
            currentDeck.returnCard(card);
        }
        return selectedCards;
    }

}
