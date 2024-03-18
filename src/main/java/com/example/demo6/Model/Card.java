package com.example.demo6.Model;

public class Card {
    private String name;
    private boolean revealed;
    public Card(String name) {
        this.name = name;
        this.revealed = false;
    }
    public String getName() {
        return name;
    }
    public boolean isRevealed() {
        return revealed;
    }
    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return name.equals(card.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}