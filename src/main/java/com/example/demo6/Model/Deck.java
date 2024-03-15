package com.example.demo6.Model;

import java.util.Collections;
import java.util.Set;
import java.util.Stack;

public class Deck
{
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
    protected Stack<Card> contents;

    //Initialise deck with each card -copies' times.
    public Deck(Set<CardType> cardTypes, int copies) {
        this.contents = new Stack<>();
        for (CardType cardType : cardTypes) {
            for (int i = 0; i < copies; i++) {
                contents.add(new Card(cardType.getName()));
            }
        }
        Collections.shuffle(contents);
    }

    //return the number of cards left in the deck
    public int getSize() {
        return this.contents.size();
    }

    //check if there are no cards lefts
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    // Get the top card off the deck if not empty.
    public Card getCard() {
        if (!isEmpty()) {
            Card card = contents.pop();
            card.setRevealed(false);
            return card;
        }
        return null;
    }


    //add card into the deck and shuffle the deck
    public void returnCard(Card card) {
        contents.add(card);
        Collections.shuffle(contents);
    }
}
