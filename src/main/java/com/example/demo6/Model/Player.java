package com.example.demo6.Model;

import com.example.demo6.Model.Actions.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Player
{
    private final int NUMBER_OF_CARDS = 2;
    private String name;
    private int coins;
    private Action action;
    private List<Card> cards;
    private Deck currentDeck;

    public Player(String name)
    {
        this.name = name;
        this.coins = 3; //the base number of coins that each player start with
        this.cards = new ArrayList<>();
    }
    public String getName()
    {
        return this.name;
    }

    public void setDeck(Deck currentDeck) {
        this.currentDeck = currentDeck;
    }

    public int getCoins() {
        return this.coins;
    }

    public void updateCoins(int coins)
    {
        this.coins += coins;
        if (coins < 0) this.coins = 0 ;
    }
    //cards function :
    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public List<Card> getCards() {
        return this.cards;
    }
    public void pickCards()
    {
        for (int i = 0; i < NUMBER_OF_CARDS; i++) {
            Card card = this.currentDeck.getCard();
            if (card != null)
            {
                this.cards.add(card);
            }
            else System.out.println("There are no cards left in the deck");
        }
    }
    //return card from the hand of the player back to the deck
    public void returnCard(Card card)
    {
        this.cards.remove(card);
        this.currentDeck.returnCard(card);
    }

    //swap the current cards with new cards from the deck
    public void swap() {
        List<Card> oldCards = new ArrayList<>(this.cards); // Store old cards
        this.cards.clear(); // Clear current cards

        // Draw two new cards
        for (int i = 0; i < 2; i++) {
            Card card = this.currentDeck.getCard();
            if (card != null) {
                this.cards.add(card);
            } else {
                System.out.println("There are no cards left in the deck");
            }
        }

        // Mix the drawn cards with the old cards
        List<Card> mixedCards = new ArrayList<>(oldCards);
        mixedCards.addAll(this.cards);

        // Prompt the player to select two cards to keep
        System.out.println("Select two cards to keep:");
        for (int i = 0; i < mixedCards.size(); i++) {
            System.out.println((i + 1) + ". " + mixedCards.get(i));
        }
        Scanner scanner = new Scanner(System.in);
        int card1Index = scanner.nextInt() - 1;
        int card2Index = scanner.nextInt() - 1;

        // Place the selected cards in the player's hand and return the remaining cards to the deck
        List<Card> selectedCards = new ArrayList<>();
        selectedCards.add(mixedCards.get(card1Index));
        selectedCards.add(mixedCards.get(card2Index));
        this.cards = selectedCards;
        mixedCards.removeAll(selectedCards);
        for (Card card : mixedCards) {
            this.currentDeck.returnCard(card);
        }
        System.out.println(this.getName() + " has successfully swapped influence cards.");
    }

    public void selectCardToGiveUp() {
        System.out.println("Select card to give up");
        for (Card card : cards) {
            System.out.println(card);
        }
        Scanner scanner = new Scanner(System.in);
        returnCard(new Card(scanner.nextLine()));
    }

    //Action Functions :
    public void setAction(Action action) {
        this.action = action;
    }

    public void doAction() { if (action != null) action.execute();}

    @Override
    public String toString() {
        return getName();
    }


    public boolean hasCard(Card givenCard) {
        for (Card card : cards) {
            if (card.equals(givenCard)) {
                return true;
            }
        }
        return false;
    }
}
