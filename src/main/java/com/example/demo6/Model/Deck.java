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

    // Initialises deck with each card -copies' times.
    public Deck(Set<CardType> cardTypes, int copies) {
        this.contents = new Stack<>();
        for (CardType cardType : cardTypes) {
            for (int i = 0; i < copies; i++) {
                contents.add(CardFactory.createCard(cardType.getName()));
            }
        }
        Collections.shuffle(contents);
    }

    // Return the number of cards left in the deck
    public int getSize() {
        return this.contents.size();
    }

    // Check if there are no cards left
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    // Get the top card off the deck if not empty, otherwise throw an exception
    public Card getCard() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deck is empty.");
        }
        return contents.pop();
    }

    // Add card into the deck and shuffle
    public void returnCard(Card card) {
        contents.add(card);
        Collections.shuffle(contents);
    }

    // A simple factory for creating cards
    static class CardFactory {
        static Card createCard(String name) {
            // This method can be expanded to handle different types of cards more complexly
            return new Card(name);
        }
    }
}