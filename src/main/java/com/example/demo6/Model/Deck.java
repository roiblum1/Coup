package com.example.demo6.Model;

import java.util.*;

public class Deck {
    public static final int NUMBER_OF_COPIES = 2;
    /**
     * Enum representing different card types.
     */
    public enum CardType {
        DUKE, ASSASSIN, CAPTAIN, AMBASSADOR, CONTESSA
    }

    private Stack<Card> contents;

    /**
     * Initializes the deck with a specified number of copies of each card type.
     * @param cardTypes A set of CardType enums to include in the deck.
     * @param copies Number of copies of each card type.
     */
    public Deck(Set<CardType> cardTypes, int copies) {
        this.contents = new Stack<>();
        for (CardType cardType : cardTypes) {
            for (int i = 0; i < copies; i++) {
                contents.add(new Card(cardType));
            }
        }
        Collections.shuffle(contents);
    }

    /**
     * Returns the number of cards left in the deck.
     * @return Size of the deck.
     */
    public int getSize() {
        return this.contents.size();
    }

    /**
     * Checks if the deck is empty.
     * @return true if no cards are left in the deck; otherwise, false.
     */
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    /**
     * Provides a copy of the contents of the deck.
     * @return A stack containing all cards currently in the deck.
     */
    public Stack<Card> copyContents() {
        Stack<Card> copiedStack = new Stack<>();
        copiedStack.addAll(contents);
        return copiedStack;
    }

    /**
     * Retrieves and removes the top card of the deck.
     * @return The top card of the deck.
     * @throws NoSuchElementException if the deck is empty.
     */
    public Card getCard() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deck is empty.");
        }
        return contents.pop();
    }

    /**
     * Adds a card to the deck and then shuffles the deck.
     * @param card The card to be returned to the deck.
     */
    public void returnCard(Card card) {
        contents.add(card);
        Collections.shuffle(contents);
    }

    /**
     * Draws a specified number of cards from the deck.
     * @param count The number of cards to draw.
     * @return A list of cards drawn from the deck.
     */
    public List<Card> drawCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (!this.isEmpty()) {
                cards.add(this.getCard());
            }
        }
        return cards;
    }
    /**
     * Creates a deep copy of this deck.
     * @return a deep copy of the deck.
     * @throws IllegalStateException if the deck contains a null card or if card types are empty.
     */
    public Deck deepCopy() {
        Set<CardType> cardTypes = EnumSet.allOf(CardType.class);
        if (cardTypes.isEmpty()) {
            throw new IllegalStateException("Card types cannot be empty.");
        }

        Deck copiedDeck = new Deck(cardTypes, Deck.NUMBER_OF_COPIES);
        for (Card card : this.copyContents()) {
            if (card == null) {
                throw new IllegalStateException("Deck contains a null card.");
            }
            copiedDeck.returnCard(new Card(card.getType()));
        }
        return copiedDeck;
    }
}
