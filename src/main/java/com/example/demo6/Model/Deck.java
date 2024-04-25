package com.example.demo6.Model;

import java.io.Serializable;
import java.util.*;

public class Deck implements Serializable {

    /**
     * Enum representing different card types with unique names.
     */
    public enum CardType {
        DUKE("Duke"),
        ASSASSIN("Assassin"),
        CAPTAIN("Captain"),
        AMBASSADOR("Ambassador"),
        CONTESSA("Contessa");

        private final String name;

        CardType(String name) {
            this.name = name;
        }

        /**
         * Gets the name of the card type.
         * @return The name of the card type.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the CardType corresponding to the provided card's name.
         * @param card The card whose type is to be determined.
         * @return CardType if found, null otherwise.
         */
        public static CardType getTypeCard(Card card) {
            return Arrays.stream(CardType.values())
                    .filter(cardType -> cardType.getName().equals(card.getName()))
                    .findFirst()
                    .orElse(null);
        }
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
                contents.add(CardFactory.createCard(cardType.getName()));
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
    public Stack<Card> getContents() {
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
     * Static inner class to handle card creation.
     */
    static class CardFactory {
        /**
         * Factory method to create a card with a specified name.
         * @param name The name of the card to create.
         * @return A new Card instance.
         */
        static Card createCard(String name) {
            return new Card(name);
        }
    }

    /**
     * Creates a deep copy of this deck.
     * @return a deep copy of the deck.
     * @throws IllegalStateException if the deck contains a null card or if card types are empty.
     */
    public Deck deepCopy() {
        Set<Deck.CardType> cardTypes = EnumSet.allOf(Deck.CardType.class);
        if (cardTypes.isEmpty()) {
            throw new IllegalStateException("Card types cannot be empty.");
        }

        Deck copiedDeck = new Deck(cardTypes, 2);  // Assuming default number of copies is 2
        for (Card card : this.getContents()) {
            if (card == null) {
                throw new IllegalStateException("Deck contains a null card.");
            }
            copiedDeck.returnCard(new Card(card.getName()));
        }
        return copiedDeck;
    }
}
