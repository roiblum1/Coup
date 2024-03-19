package com.example.demo6.Model;

public class Card {
    private String name;

    //* Constructor to initialize the card with a name */
    public Card(String name) {
        this.name = name;
    }

    //* Retrieves the name of the card */
    public String getName() {
        return name;
    }

    //* Represents the card as a string */
    public String toString() {
        return this.name;
    }

    //* Checks if two cards are equal */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return name.equals(card.name);
    }

    //* Generates a hash code for the card */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
