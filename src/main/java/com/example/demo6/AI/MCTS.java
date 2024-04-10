package com.example.demo6.AI;

import com.example.demo6.Model.Actions.Action;
import com.example.demo6.Model.Actions.IncomeAction;
import com.example.demo6.Model.Card;
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
        // Debugging statements
        System.out.println("Original Game Players:");
        for (Player player : game.getPlayers()) {
            System.out.println(player.getName());
        }

        System.out.println("Cloned Game Players:");
        for (Player player : rootGame.getPlayers()) {
            System.out.println(player.getName());
        }
    }

    public Action bestMove() {
        if (rootGame.isGameOver()) {
            return null;
        }

        // Perform MCTS search
        search(1000); // Adjust the time limit as needed

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

    public void search(int timeLimit) {
        long startTime = System.currentTimeMillis();

        int numRollouts = 0;
        while (System.currentTimeMillis() - startTime < timeLimit) {
            NodeGamePair nodeGamePair = selectNode();
            Node node = nodeGamePair.node;
            Game game = nodeGamePair.game;

            if (node.getAction() != null) {
                System.out.println("Selected node: " + node.getAction().getCodeOfAction());
            } else {
                System.out.println("Selected node: null");
            }

            Player winner = rollOut(game);
            if (winner != null) {
                System.out.println("Rollout winner: " + winner.getName());
            } else {
                System.out.println("Rollout ended with no winner.");
            }

            backPropagate(node, game.getCurrentPlayer(), winner);
            numRollouts++;
        }

        long runtime = System.currentTimeMillis() - startTime;
        System.out.println("Runtime: " + runtime + ", numRollouts: " + numRollouts);
    }


    private NodeGamePair selectNode() {
        Node node = root;
        Game game = deepCopy(rootGame);

        while (!node.getChildren().isEmpty()) {
            double maxValue = Double.NEGATIVE_INFINITY;
            List<Node> maxNodes = new ArrayList<>();

            for (Node child : node.getChildren().values()) {
                double childValue = child.getUCTValue();
                System.out.println("Child action: " + child.getAction().getCodeOfAction() + ", UCT value: " + childValue);
                if (childValue > maxValue) {
                    maxValue = childValue;
                    maxNodes.clear();
                    maxNodes.add(child);
                } else if (childValue == maxValue) {
                    maxNodes.add(child);
                }
            }

            node = maxNodes.get(ThreadLocalRandom.current().nextInt(maxNodes.size()));
            executeAction(game, node.getAction(), false, false);

            if (node.getVisitCount() == 0) {
                return new NodeGamePair(node, game);
            }
        }

        expand(node, game);
        List<Node> children = new ArrayList<>(node.getChildren().values());
        if (children.isEmpty()) {
            // Handle the case when there are no valid child nodes
            return new NodeGamePair(node, game); // or return a default value or handle accordingly
        }
        node = children.get(ThreadLocalRandom.current().nextInt(children.size()));
        executeAction(game, node.getAction(), false, false);

        return new NodeGamePair(node, game);
    }

    private void expand(Node parent, Game game) {
        if (game.isGameOver()) {
            return;
        }

        Player currentPlayer = game.getCurrentPlayer();
        if (currentPlayer == null) {
            // Handle the case when there are no active players
            return;
        }

        List<Node> children = new ArrayList<>();
        for (Action action : game.getAvailableActions(currentPlayer)) {
            Node child = new Node(action, parent);
            children.add(child);
            System.out.println("Created child node for action: " + action.getCodeOfAction());
        }
        parent.addChildren(children);
    }

    private Player rollOut(Game game) {
        List<Player> players = game.getPlayers();
        if (players != null) {
            for (Player player : players) {
                System.out.println(player.getName());
            }
        } else {
            System.out.println("No players found in the game.");
        }
        while (!game.isGameOver()) {
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer == null) {
                // Handle the case when there are no active players
                // You can either terminate the rollout or take appropriate action
                break; // Terminate the rollout early
            }

            List<Action> availableActions = game.getAvailableActions(currentPlayer);
            if (availableActions.isEmpty()) {
                // Handle the case when there are no available actions for the current player
                // You can either skip the player's turn or take appropriate action
                game.switchTurns(); // Skip the player's turn
                continue; // Move to the next iteration
            }

            Action action = availableActions.get(ThreadLocalRandom.current().nextInt(availableActions.size()));

            System.out.println("Rollout action: " + action.getCodeOfAction());

            // Simulate challenges and blocks
            boolean isChallenged = simulateChallenge(game, action);
            boolean isBlocked = simulateBlock(game, action);

            executeAction(game, action, isChallenged, isBlocked);
        }

        Player winner = game.getWinner();
        if (winner == null) {
            // Handle the case when there is no winner
            // You can return a default value or handle it based on your game's rules
            return game.getPlayers().get(0); // Return the first player as the default winner
        }
        return winner;
    }
    private void backPropagate(Node node, Player turn, Player outcome) {
        int reward = (outcome == turn) ? 1 : 0;

        while (node != null) {
            node.incrementVisitCount();
            node.incrementReward(reward);
            if (node.getAction() != null) {
                System.out.println("Backpropagation: Action = " + node.getAction().getCodeOfAction() + ", Visit Count = " + node.getVisitCount() + ", Reward = " + node.getReward());
            } else {
                System.out.println("Backpropagation: Action = null, Visit Count = " + node.getVisitCount() + ", Reward = " + node.getReward());
            }
            node = node.getParent();
        }
    }
    public void handleAction(Action action) {
        if (rootGame != null) {
            executeAction(rootGame, action, false, false);
        }

        if (root.getChildren().containsKey(action)) {
            root = root.getChildren().get(action);
        } else {
            root = new Node(null);
        }
    }

    public void handleGameOver(Player winner) {
        // Update the reward of the root node based on the winner
        if (winner == rootGame.getPlayers().get(0)) {
            // Human player won
            root.incrementReward(-1);
        } else if (winner == rootGame.getPlayers().get(1)) {
            // AI player won
            root.incrementReward(1);
        } else {
            // Game ended in a draw
            root.incrementReward(0);
        }

        // Backpropagate the result to the root node
        Node node = root;
        while (node != null) {
            node.incrementVisitCount();
            node = node.getParent();
        }
    }

    private void executeAction(Game game, Action action, boolean isChallenged, boolean isBlocked) {
        Player currentPlayer = action.getPlayer();
        Player targetPlayer = game.getOpponent(currentPlayer);

        if (isChallenged) {
            if (!action.challenge()) {
                // Action challenged successfully, player loses a card
                currentPlayer.loseRandomInfluence();
                return;
            }
        }

        if (isBlocked) {
            if (simulateBlockChallenge(game, action)) {
                // Block challenged successfully, blocker loses a card
                if (targetPlayer != null) {
                    targetPlayer.loseRandomInfluence();
                }
            } else {
                // Block not challenged or challenge failed, action is blocked
                return;
            }
        }

        // Execute the action
        switch (action.getActionCode()) {
            case INCOME:
                currentPlayer.updateCoins(1);
                break;
            case FOREIGN_AID:
                currentPlayer.updateCoins(2);
                break;
            case TAX:
                currentPlayer.updateCoins(3);
                break;
            case STEAL:
                if (targetPlayer != null) {
                    int stolenCoins = Math.min(2, targetPlayer.getCoins());
                    currentPlayer.updateCoins(stolenCoins);
                    targetPlayer.updateCoins(-stolenCoins);
                }
                break;
            case ASSASSINATE:
                if (currentPlayer.getCoins() >= 3) {
                    currentPlayer.updateCoins(-3);
                    if (targetPlayer != null) {
                        targetPlayer.loseRandomInfluence();
                    }
                }
                break;
            case COUP:
                if (currentPlayer.getCoins() >= 7) {
                    currentPlayer.updateCoins(-7);
                    if (targetPlayer != null) {
                        targetPlayer.loseRandomInfluence();
                    }
                }
                break;
            case SWAP:
                List<Card> drawnCards = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    if (!game.getDeck().isEmpty()) {
                        drawnCards.add(game.getDeck().getCard());
                    }
                }
                if (!drawnCards.isEmpty()) {
                    List<Card> selectedCards = currentPlayer.selectRandomCardsToKeep(drawnCards);
                    currentPlayer.swapCards(selectedCards, drawnCards);
                }
                break;
            default:
                break;
        }

        // Switch turns to the next player
        game.switchTurns();
    }

    private boolean simulateChallenge(Game game, Action action) {
        // Implement your own logic to decide whether to challenge an action
        // For example, you can use a random probability or consider game state
        return Math.random() < 0.3; // 30% chance of challenging the action
    }

    private boolean simulateBlock(Game game, Action action) {
        // Implement your own logic to decide whether to block an action
        // For example, you can use a random probability or consider game state
        return action.canBeBlocked && Math.random() < 0.2; // 20% chance of blocking the action
    }

    private boolean simulateBlockChallenge(Game game, Action action) {
        // Implement your own logic to decide whether to challenge a block
        // For example, you can use a random probability or consider game state
        return Math.random() < 0.4; // 40% chance of challenging the block
    }

    private static <T extends Serializable> T deepCopy(Game object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            T clonedObject = (T) ois.readObject();
            ois.close();

            // Manually copy the playerList and its associated objects
            Game clonedGame = (Game) clonedObject;
            List<Player> clonedPlayerList = new ArrayList<>();
            for (Player player : object.getPlayers()) {
                Player clonedPlayer = new Player(player.getName());
                clonedPlayer.setCoins(player.getCoins());
                List<Card> clonedCards = new ArrayList<>(player.getCards());
                clonedPlayer.setCards(clonedCards);
                clonedPlayer.setDeck(clonedGame.getDeck()); // Set the cloned game's deck to the player
                clonedPlayerList.add(clonedPlayer);
            }
            clonedGame.setPlayerList(clonedPlayerList);

            return clonedObject;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
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