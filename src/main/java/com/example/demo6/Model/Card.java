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

    public int hashCode() {
        return this.name.hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof Card && o.hashCode() == this.hashCode() && !((Card) o).isRevealed();
    }
}