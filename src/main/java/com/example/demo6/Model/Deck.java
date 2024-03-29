package com.example.demo6.Model;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

public class Deck {
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

        public String getName() {
            return name;
        }
    }

    private Stack<Card> contents;

    //* Initializes the deck with each card and specified number of copies */
    public Deck(Set<CardType> cardTypes, int copies) {
        this.contents = new Stack<>();
        for (CardType cardType : cardTypes) {
            for (int i = 0; i < copies; i++) {
                contents.add(CardFactory.createCard(cardType.getName()));
            }
        }
        Collections.shuffle(contents);
    }

    //* Returns the number of cards left in the deck */
    public int getSize() {
        return this.contents.size();
    }

    //* Checks if there are no cards left in the deck */
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    //* Retrieves the top card from the deck, throws an exception if empty */
    public Card getCard() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deck is empty.");
        }
        return contents.pop();
    }

    //* Adds a card into the deck and shuffles */
    public void returnCard(Card card) {
        contents.add(card);
        Collections.shuffle(contents);
    }

    static class CardFactory {
        static Card createCard(String name) {
            return new Card(name);
        }
    }
}
