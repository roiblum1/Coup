package com.example.demo6.Model;

import java.io.Serializable;

/**
 * Represents a card in the game. Each card has a unique name.
 * This class implements Serializable to allow card instances to be serialized for storage or transmission.
 */
public class Card implements Serializable {
    private String name;

    /**
     * Constructs a new Card with the specified name.
     * @param name the name of the card, which identifies the type of the card.
     */
    public Card(String name) {
        this.name = name;
    }

    /**
     * Retrieves the name of the card.
     * @return the name of the card.
     */
    public String getName() {
        return name;
    }

    /**
     * Provides a string representation of the card, which is the name of the card.
     * @return the name of the card as a string.
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Compares this card with the specified object for equality.
     * The result is true if and only if the argument is not null and is a Card object that has the same name as this object.
     * @param o the object to compare this Card against.
     * @return true if the given object represents a Card equivalent to this card, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return name.equals(card.name);
    }

    /**
     * Returns a hash code for this card. The hash code for a Card object is computed as
     * the hash code of the name of the card.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
