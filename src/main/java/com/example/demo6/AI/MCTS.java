package com.example.demo6.AI;

import com.example.demo6.Model.Actions.*;
import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Game;
import com.example.demo6.Model.Player;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MCTS {
    private static final double EXPLORATION = Math.sqrt(2);
    private final Game rootGame;
    private Node root;

    public MCTS(Game game) {
        this.rootGame = deepCopy(game);
        this.root = new Node(null);
    }

    public Action bestMove() {
        if (rootGame.isGameOver()) {
            return null;
        }

        search(100, 10);

        double maxValue = Double.NEGATIVE_INFINITY;
        List<Node> maxNodes = new ArrayList<>();

        System.out.println("Available actions:");
        for (Node child : root.getChildren().values()) {
            double childValue = child.getVisitCount();
            System.out.println(child.getAction().getCodeOfAction() + ": Visit Count = " + child.getVisitCount() + ", Reward = " + child.getReward());
            if (childValue > maxValue) {
                maxValue = childValue;
                maxNodes.clear();
                maxNodes.add(child);
            } else if (childValue == maxValue) {
                maxNodes.add(child);
            }
        }

        if (maxNodes.isEmpty()) {
            System.out.println("No valid moves available.");
            return null;
        }

        Node bestChild = maxNodes.get(ThreadLocalRandom.current().nextInt(maxNodes.size()));
        System.out.println("Selected best action: " + bestChild.getAction().getCodeOfAction());
        return bestChild.getAction();
    }

    public void search(int numSimulations, int maxDepth) {
        for (int i = 0; i < numSimulations; i++) {
            NodeGamePair nodeGamePair = selectNode(maxDepth);
            Node node = nodeGamePair.node;
            Game game = nodeGamePair.game;

            if (node.getAction() != null) {
                System.out.println("Selected node: " + node.getAction().getCodeOfAction());
            } else {
                System.out.println("Selected node: null");
            }

            Player winner = rollOut(game, maxDepth);
            if (winner != null) {
                System.out.println("Rollout winner: " + winner.getName());
            } else {
                System.out.println("Rollout ended with no winner.");
            }

            backPropagate(node, game.getCurrentPlayer(), winner);

            if (node.getParent() == root) {
                break;
            }
        }
    }

    private NodeGamePair selectNode(int maxDepth) {
        Node node = root;
        Game game = deepCopy(rootGame);
        int depth = 0;

        while (depth < maxDepth) {
            if (node.isLeaf()) {
                expand(node, game);
            }

            if (node.getChildren().isEmpty()) {
                break;
            }

            node = node.selectChild();
            executeAction(game, node.getAction(), false, false);
            depth++;
        }

        return new NodeGamePair(node, game);
    }

    private void expand(Node parent, Game game) {
        if (game.isGameOver()) {
            return;
        }

        Player currentPlayer = game.getCurrentPlayer();
        if (currentPlayer == null) {
            return;
        }

        List<Action> availableActions = game.getAvailableActions(currentPlayer);
        List<Node> childNodes = new ArrayList<>();
        for (Action action : availableActions) {
            Node child = new Node(action, parent);
            childNodes.add(child);
            System.out.println("Created child node for action: " + action.getCodeOfAction());
        }
        parent.addChildren(childNodes);
    }

    private Player rollOut(Game game, int maxDepth) {
        int depth = 0;

        while (!game.isGameOver() && depth < maxDepth) {
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer == null) {
                break;
            }

            List<Action> availableActions = game.getAvailableActions(currentPlayer);
            if (availableActions.isEmpty()) {
                game.switchTurns();
                continue;
            }

            Action action;
            if (currentPlayer == game.getPlayers().get(1)) { // Assuming the AI player is at index 1
                action = selectActionHeuristically(availableActions, game);
            } else {
                // Select a random action for the human player (simulating random play)
                action = availableActions.get(ThreadLocalRandom.current().nextInt(availableActions.size()));
            }

            System.out.println("Rollout action: " + action.getCodeOfAction());

            boolean isChallenged = simulateChallenge(game, action);
            boolean isBlocked = simulateBlock(game, action);

            executeAction(game, action, isChallenged, isBlocked);

            depth++;
        }

        return game.getWinner();
    }

    private Action selectActionHeuristically(List<Action> availableActions, Game game) {
        Player aiPlayer = game.getCurrentPlayer();
        Player humanPlayer = game.getOpponent(aiPlayer);
        List<Card> aiPlayerCards = aiPlayer.getCards();
        int aiPlayerCoins = aiPlayer.getCoins();
        int humanPlayerCoins = humanPlayer.getCoins();
        int humanPlayerCardCount = humanPlayer.getCards().size();

        // Immediate winning moves: If human player has 1 card, prioritize COUP or ASSASSINATE if possible.
        if (humanPlayerCardCount == 1) {
            if (aiPlayerCoins >= 7) { // Coup is a guaranteed kill if it can be afforded and not blocked.
                return availableActions.stream()
                        .filter(action -> action.getActionCode() == ActionCode.COUP)
                        .findFirst()
                        .orElse(null);
            }
            if (aiPlayerCoins >= 3 && aiPlayerCards.contains(Deck.CardType.ASSASSIN)) { // Assassinate if possible.
                return availableActions.stream()
                        .filter(action -> action.getActionCode() == ActionCode.ASSASSINATE)
                        .findFirst()
                        .orElse(null);
            }
        }

        // Use Duke to collect taxes if available to maximize coin gain safely.
        if (aiPlayerCards.contains(Deck.CardType.DUKE)) {
            return availableActions.stream()
                    .filter(action -> action.getActionCode() == ActionCode.TAX)
                    .findFirst()
                    .orElse(null);
        }

        // Use Captain to steal if the human player has coins and the AI has the Captain.
        if (aiPlayerCards.contains(Deck.CardType.CAPTAIN) && humanPlayerCoins > 0) {
            return availableActions.stream()
                    .filter(action -> action.getActionCode() == ActionCode.STEAL)
                    .findFirst()
                    .orElse(null);
        }

        // If the AI has enough coins to coup on the next turn, consider gaining more coins or keeping a low profile.
        if (aiPlayerCoins >= 5) {
            if (aiPlayerCoins == 6) {
                return availableActions.stream()
                        .filter(action -> action.getActionCode() == ActionCode.FOREIGN_AID || action.getActionCode() == ActionCode.INCOME)
                        .findFirst()
                        .orElse(null); // Foreign Aid or Income to get to 7 coins for a Coup next turn.
            }
            // Consider swapping if having excess coins and possibly bad cards.
            if (aiPlayerCoins > 8) {
                return availableActions.stream()
                        .filter(action -> action.getActionCode() == ActionCode.SWAP)
                        .findFirst()
                        .orElse(null);
            }
        }

        // Default to income if no strategic moves are immediately necessary.
        return availableActions.stream()
                .filter(action -> action.getActionCode() == ActionCode.INCOME)
                .findFirst()
                .orElse(null);
    }


    private void backPropagate(Node node, Player turn, Player winner) {
        while (node != null) {
            node.incrementVisitCount();

            if (winner != null) {
                if (winner.equals(turn)) {
                    node.incrementReward(1);
                } else {
                    node.incrementReward(-1);
                }
            }

            if (node.getParent() == null) {
                break;
            }

            if (node.getAction() != null) {
                System.out.println("Backpropagation: Action = " + node.getAction().getCodeOfAction() + ", Visit Count = " + node.getVisitCount() + ", Reward = " + node.getReward());
            } else {
                System.out.println("Backpropagation: Action = null, Visit Count = " + node.getVisitCount() + ", Reward = " + node.getReward());
            }

            node = node.getParent();
        }
    }

    public void handleAction(Action action) {
        if (root.getChildren().containsKey(action)) {
            root = root.getChildren().get(action);
        } else {
            root = new Node(null);
        }
    }

    public void handleGameOver(Player winner) {
        if (winner == rootGame.getPlayers().get(0)) {
            root.incrementReward(-1000);
        } else if (winner == rootGame.getPlayers().get(1)) {
            root.incrementReward(1000);
        } else {
            root.incrementReward(1);
        }

        Node node = root;
        while (node != null) {
            node.incrementVisitCount();
            node = node.getParent();
        }
    }

    private void executeAction(Game game, Action action, boolean isChallenged, boolean isBlocked) {
        Game simulationGame = deepCopy(game);
        Player currentPlayer = simulationGame.getCurrentPlayer();
        Player targetPlayer = simulationGame.getOpponent(currentPlayer);

        if (isChallenged) {
            if (!action.challenge()) {
                currentPlayer.loseRandomInfluence();
                return;
            }
        }

        if (isBlocked) {
            if (simulateBlockChallenge(simulationGame, action)) {
                if (targetPlayer != null) {
                    targetPlayer.loseRandomInfluence();
                }
            } else {
                return;
            }
        }

        List<Card> cards = null;
        if (action instanceof SwapAction) {
            List<Card> newCards = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                if (!simulationGame.getDeck().isEmpty()) {
                    newCards.add(simulationGame.getDeck().getCard());
                }
            }
            List<Card> swapOptions = new ArrayList<>(currentPlayer.getCards());
            swapOptions.addAll(newCards);
            List<Card> selectedCards = currentPlayer.selectRandomCardsToKeep(swapOptions);
            cards = new ArrayList<>();
            cards.addAll(selectedCards);
            cards.addAll(newCards);
        } else if (action instanceof CoupAction || action instanceof AssassinateAction) {
            if (!targetPlayer.getCards().isEmpty()) {
                Card cardToLose = targetPlayer.getCards().get(0);
                cards = new ArrayList<>();
                cards.add(cardToLose);
            }
        }

        simulationGame.executeAction(action, cards);
        if (currentPlayer == simulationGame.getCurrentPlayer()) {
            simulationGame.switchTurns();
        }
    }

    public boolean simulateChallenge(Game game, Action action) {
        Player aiPlayer = game.getCurrentPlayer();
        Player humanPlayer = game.getOpponent(aiPlayer);

        // If AI has only one card left, avoid challenging unless it's a critical situation
        if (aiPlayer.getCards().size() == 1 && humanPlayer.getCards().size() > 1) {
            return false;
        }

        // Challenge if the human player performs an action that could directly lead to the AI's loss
        if (action.getActionCode() == ActionCode.ASSASSINATE || action.getActionCode() == ActionCode.COUP) {
            return aiPlayer.getCards().size() <= humanPlayer.getCards().size();
        }

        // Simulate challenge based on AI's belief about human player's card holdings
        // Example: AI may think it's highly unlikely the human player has a Duke
        if (action.getActionCode() == ActionCode.TAX) {
            return !aiPlayer.getCards().contains(Deck.CardType.DUKE); // Challenge if AI does not have a Duke, suspecting the same for human
        }
        return false;
    }

    public boolean simulateBlock(Game game, Action action) {
        Player aiPlayer = game.getCurrentPlayer();
        Player humanPlayer = game.getOpponent(aiPlayer);
        // Block foreign aid if AI has a Duke
        if (action.getActionCode() == ActionCode.FOREIGN_AID && aiPlayer.getCards().contains(Deck.CardType.DUKE)) {
            return true;
        }
        // Block steal if AI has a Captain or Ambassador
        if (action.getActionCode() == ActionCode.STEAL && (aiPlayer.getCards().contains(Deck.CardType.CAPTAIN) || aiPlayer.getCards().contains(Deck.CardType.AMBASSADOR))) {
            return true;
        }
        // Block assassinate if AI has a Contessa
        if (action.getActionCode() == ActionCode.ASSASSINATE && aiPlayer.getCards().contains(Deck.CardType.CONTESSA)) {
            return true;
        }
        return false;
    }



    public boolean simulateBlockChallenge(Game game, Action action) {
        Player aiPlayer = game.getCurrentPlayer();
        Player humanPlayer = game.getOpponent(aiPlayer);

        // Challenge block if AI is about to lose and the block prevents a game-saving move
        if (action.getActionCode() == ActionCode.COUP && humanPlayer.getCards().size() == 1 && aiPlayer.getCoins() >= 7) {
            return true;
        }
        return false;
    }

    public List<Card> selectCardsToKeep(Game game, Player player, List<Card> newCards) {
        List<Card> allCards = new ArrayList<>(player.getCards());
        allCards.addAll(newCards);

        // Sort the cards based on their value in descending order
        allCards.sort(Comparator.comparingInt(this::getCardValue).reversed());

        // Return the two most valuable cards
        return allCards.subList(0, 2);
    }

    public Card selectCardToGiveUp(Game game, Player player) {
        List<Card> cards = player.getCards();

        // If the player has only one card, return that card
        if (cards.size() == 1) {
            return cards.get(0);
        }

        // Heuristic: Give up the least valuable card
        // You can define the value of each card based on your game strategy
        Card leastValuableCard = null;
        int minValue = Integer.MAX_VALUE;

        for (Card card : cards) {
            int cardValue = getCardValue(card);
            if (cardValue < minValue) {
                minValue = cardValue;
                leastValuableCard = card;
            }
        }

        return leastValuableCard;
    }

    private int getCardValue(Card card) {
        // Assign values to each card based on your game strategy
        // Higher values indicate more valuable cards
        switch (card.getName()) {
            case "Duke":
                return 5;
            case "Assassin":
                return 4;
            case "Captain":
                return 3;
            case "Ambassador":
                return 2;
            case "Contessa":
                return 1;
            default:
                return 0;
        }
    }

    private static Game deepCopy(Game game) {
        Game copiedGame = new Game(deepCopyDeck(game.getDeck()));
        copiedGame.setCurrentPlayerIndex(game.getCurrentPlayerIndex());
        copiedGame.setLastExecutedAction(game.getLastExecutedAction());
        List<Player> copiedPlayerList = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            Player copiedPlayer = new Player(player.getName());
            copiedPlayer.setCoins(player.getCoins());
            copiedPlayer.setCards(deepCopyCards(player.getCards()));
            copiedPlayer.setDeck(copiedGame.getDeck());
            copiedPlayerList.add(copiedPlayer);
        }
        copiedGame.setPlayerList(copiedPlayerList);

        return copiedGame;
    }

    private static Deck deepCopyDeck(Deck deck) {
        Set<Deck.CardType> cardTypes = EnumSet.allOf(Deck.CardType.class);
        if (cardTypes.isEmpty()) {
            throw new IllegalStateException("Card types cannot be empty.");
        }

        Deck copiedDeck = new Deck(cardTypes, 2);

        for (Card card : deck.getContents()) {
            if (card == null) {
                throw new IllegalStateException("Deck contains a null card.");
            }
            copiedDeck.returnCard(new Card(card.getName()));
        }

        return copiedDeck;
    }

    private static List<Card> deepCopyCards(List<Card> cards) {
        List<Card> copiedCards = new ArrayList<>();
        for (Card card : cards) {
            copiedCards.add(new Card(card.getName()));
        }
        return copiedCards;
    }

    private static class Node {
        private final Action action;
        private final Node parent;
        private int visitCount;
        private int reward;
        private final Map<Action, Node> children;

        public Node(Action action) {
            this(action, null);
        }

        public Node(Action action, Node parent) {
            this.action = action;
            this.parent = parent;
            this.visitCount = 0;
            this.reward = 0;
            this.children = new HashMap<>();
        }

        public void addChildren(List<Node> children) {
            for (Node child : children) {
                this.children.put(child.getAction(), child);
            }
        }

        public double getUCTValue() {
            if (visitCount == 0) {
                return Double.POSITIVE_INFINITY;
            }

            double exploitation = (double) reward / visitCount;
            double exploration = EXPLORATION * Math.sqrt(Math.log(parent.getVisitCount()) / visitCount);
            return exploitation + exploration;
        }

        public Action getAction() {
            return action;
        }

        public Node getParent() {
            return parent;
        }

        public int getVisitCount() {
            return visitCount;
        }

        public void incrementVisitCount() {
            visitCount++;
        }

        public void incrementReward(int value) {
            reward += value;
        }

        public Map<Action, Node> getChildren() {
            return children;
        }

        public void addChild(Node child) {
            children.put(child.getAction(), child);
        }

        public Node selectChild() {
            double maxUCT = Double.NEGATIVE_INFINITY;
            Node selectedChild = null;

            for (Node child : children.values()) {
                double uct = child.getUCTValue();
                if (uct > maxUCT) {
                    maxUCT = uct;
                    selectedChild = child;
                }
            }

            return selectedChild;
        }

        public boolean isLeaf() {
            return children.isEmpty();
        }

        public String getReward() {
            return "" + this.reward;
        }
    }

    private static class NodeGamePair {
        private final Node node;
        private final Game game;

        public NodeGamePair(Node node, Game game) {
            this.node = node;
            this.game = game;
        }
    }
}