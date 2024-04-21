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

        // Perform MCTS search
        search(100, 10); // Adjust the time limit as needed

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

            Player winner = rollOut(game, maxDepth, 1000);
            if (winner != null) {
                System.out.println("Rollout winner: " + winner.getName());
            } else {
                System.out.println("Rollout ended with no winner.");
            }

            assert winner != null;
            backPropagate(node, game.getCurrentPlayer(), winner);

            // Stop the simulations after finding the best action for the current turn
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
                assert game != null;
                expand(node, game);
            }

            if (node.getChildren().isEmpty()) {
                // If the node has no children after expansion, we have reached a terminal state
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

    private Player rollOut(Game game, int maxDepth, long timeLimit) {
        int depth = 0;
        long startTime = System.currentTimeMillis();

        while (!game.isGameOver() && depth < maxDepth && System.currentTimeMillis() - startTime < timeLimit) {
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer == null) {
                break;
            }

            if (currentPlayer == game.getPlayers().get(1)) { // Assuming the AI player is at index 1
                List<Action> availableActions = game.getAvailableActions(currentPlayer);
                if (availableActions.isEmpty()) {
                    game.switchTurns();
                    continue;
                }

                Action action = selectActionHeuristically(availableActions, game);
                System.out.println("Rollout action: " + action.getCodeOfAction());

                boolean isChallenged = simulateChallenge(game, action);
                boolean isBlocked = simulateBlock(game, action);

                executeAction(game, action, isChallenged, isBlocked);
            } else {
                // Skip the human player's turn or use a simple heuristic to choose an action
                game.switchTurns();
            }

            depth++;
        }

        Player winner = game.getWinner();
        if (winner == null) {
            return evaluateGameState(game);
        }
        return winner;
    }

    private Action selectActionHeuristically(List<Action> availableActions, Game game) {
        Player aiPlayer = game.getCurrentPlayer();
        Player humanPlayer = game.getOpponent(aiPlayer);
        List<Card> aiPlayerCards = aiPlayer.getCards();
        int aiPlayerCoins = aiPlayer.getCoins();
        int humanPlayerCoins = humanPlayer.getCoins();
        int humanPlayerCardCount = humanPlayer.getCards().size();

        // If the human player has 1 card and Coup is available, prioritize Coup to eliminate them.
        if (humanPlayerCardCount == 1 && aiPlayerCoins >= 7) {
            return availableActions.stream()
                    .filter(action -> action instanceof CoupAction)
                    .findFirst()
                    .orElse(null);
        }

        // If the AI has a Duke, prioritize Tax to accumulate coins.
        if (aiPlayerCards.contains(Deck.CardType.DUKE)) {
            return availableActions.stream()
                    .filter(action -> action instanceof TaxAction)
                    .findFirst()
                    .orElse(null);
        }

        // Steal if holding a Captain and the human player has coins.
        if (aiPlayerCards.contains(Deck.CardType.CAPTAIN) && humanPlayerCoins > 0) {
            return availableActions.stream()
                    .filter(action -> action instanceof StealAction)
                    .findFirst()
                    .orElse(null);
        }

        // If holding an Assassin card, the human player has two cards, and the AI has enough coins, prioritize Assassinate.
        if (aiPlayerCards.contains(Deck.CardType.ASSASSIN) && humanPlayerCardCount >= 1 && aiPlayerCoins >= 3) {
            return availableActions.stream()
                    .filter(action -> action instanceof AssassinateAction)
                    .findFirst()
                    .orElse(null);
        }

        // Foreign Aid if low on coins.
        int coinThreshold = 7; // Adjust the threshold based on the game strategy.
        if (aiPlayerCoins < coinThreshold) {
            return availableActions.stream()
                    .filter(action -> action instanceof ForeignAidAction)
                    .findFirst()
                    .orElseGet(() -> availableActions.stream()
                            .filter(action -> action instanceof IncomeAction)
                            .findFirst()
                            .orElse(null));
        }

        // Prioritize Income if the AI player has less than 4 coins.
        if (aiPlayerCoins < 4) {
            return availableActions.stream()
                    .filter(action -> action instanceof IncomeAction)
                    .findFirst()
                    .orElse(null);
        }

        // Swap cards if the AI player has more than 8 coins.
        if (aiPlayerCoins > 8) {
            return availableActions.stream()
                    .filter(action -> action instanceof SwapAction)
                    .findFirst()
                    .orElse(null);
        }

        // Default to Income if none of the above conditions are met.
        return availableActions.stream()
                .filter(action -> action instanceof IncomeAction)
                .findFirst()
                .orElse(null);
    }


    private Player evaluateGameState(Game game) {
        List<Player> players = game.getPlayers();
        int maxScore = Integer.MIN_VALUE;
        Player bestPlayer = null;

        for (Player player : players) {
            int score = evaluatePlayerState(player);
            if (score > maxScore) {
                maxScore = score;
                bestPlayer = player;
            }
        }

        return bestPlayer;
    }

    private int evaluatePlayerState(Player player) {
        int coinWeight = 1; // Adjust the weight of coins in the evaluation
        int influenceWeight = 2; // Adjust the weight of influence cards in the evaluation

        int coinScore = player.getCoins() * coinWeight;
        int influenceScore = player.getCards().size() * influenceWeight;

        return coinScore + influenceScore;
    }
    private void backPropagate(Node node, Player turn, Player outcome) {
        int reward = (outcome.equals(turn))? 1 : 0;

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
        Game simulationGame = deepCopy(game);
        Player currentPlayer = simulationGame.getCurrentPlayer();
        Player targetPlayer = simulationGame.getOpponent(currentPlayer);

        if (isChallenged) {
            if (!action.challenge()) {
                // Action challenged successfully, player loses a card
                currentPlayer.loseRandomInfluence();
                return;
            }
        }

        if (isBlocked) {
            if (simulateBlockChallenge(simulationGame, action)) {
                // Block challenged successfully, blocker loses a card
                if (targetPlayer != null) {
                    targetPlayer.loseRandomInfluence();
                }
            } else {
                // Block not challenged or challenge failed, action is blocked
                return;
            }
        }

        // Execute the action on the 'simulationGame' object
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
                    if (!simulationGame.getDeck().isEmpty()) {
                        drawnCards.add(simulationGame.getDeck().getCard());
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

        // Switch turns on the 'simulationGame' object
        simulationGame.switchTurns();
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