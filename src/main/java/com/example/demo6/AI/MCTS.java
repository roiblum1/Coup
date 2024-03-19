package com.example.demo6.AI;

import com.example.demo6.Model.Actions.Action;
import com.example.demo6.Model.Game;
import com.example.demo6.Model.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MCTS {
//    private static final double EXPLORATION = Math.sqrt(2);
//    private final Game rootGame;
//    private Node root;
//
//    public MCTS(Game game) {
//        this.rootGame = deepCopy(game);
//        this.root = new Node(null);
//    }
//
//    public Action bestMove() {
//        if (rootGame.isGameOver()) {
//            return null;
//        }
//
//        double maxValue = Double.NEGATIVE_INFINITY;
//        List<Node> maxNodes = new ArrayList<>();
//
//        for (Node child : root.getChildren().values()) {
//            double childValue = child.getVisitCount();
//            if (childValue > maxValue) {
//                maxValue = childValue;
//                maxNodes.clear();
//                maxNodes.add(child);
//            } else if (childValue == maxValue) {
//                maxNodes.add(child);
//            }
//        }
//
//        Node bestChild = maxNodes.get(ThreadLocalRandom.current().nextInt(maxNodes.size()));
//        return bestChild.getAction();
//    }
//
//    public void search(int timeLimit) {
//        long startTime = System.currentTimeMillis();
//
//        int numRollouts = 0;
//        while (System.currentTimeMillis() - startTime < timeLimit) {
//            NodeGamePair nodeGamePair = selectNode();
//            Node node = nodeGamePair.node;
//            Game game = nodeGamePair.game;
//
//            Player winner = rollOut(game);
//            backPropagate(node, game.getCurrentPlayer(), winner);
//            numRollouts++;
//        }
//
//        long runtime = System.currentTimeMillis() - startTime;
//        System.out.println("Runtime: " + runtime + ", numRollouts: " + numRollouts);
//    }
//
//    private NodeGamePair selectNode() {
//        Node node = root;
//        Game game = deepCopy(rootGame);
//
//        while (!node.getChildren().isEmpty()) {
//            double maxValue = Double.NEGATIVE_INFINITY;
//            List<Node> maxNodes = new ArrayList<>();
//
//            for (Node child : node.getChildren().values()) {
//                double childValue = child.getUCTValue();
//                if (childValue > maxValue) {
//                    maxValue = childValue;
//                    maxNodes.clear();
//                    maxNodes.add(child);
//                } else if (childValue == maxValue) {
//                    maxNodes.add(child);
//                }
//            }
//
//            node = maxNodes.get(ThreadLocalRandom.current().nextInt(maxNodes.size()));
//            game.handleAction(node.getAction());
//
//            if (node.getVisitCount() == 0) {
//                return new NodeGamePair(node, game);
//            }
//        }
//
//        expand(node, game);
//        List<Node> children = new ArrayList<>(node.getChildren().values());
//        node = children.get(ThreadLocalRandom.current().nextInt(children.size()));
//        game.handleAction(node.getAction());
//
//        return new NodeGamePair(node, game);
//    }
//
//    private void expand(Node parent, Game game) {
//        if (game.isGameOver()) {
//            return;
//        }
//
//        List<Node> children = new ArrayList<>();
//        for (Action action : game.getAvailableActions()) {
//            children.add(new Node(action, parent));
//        }
//        parent.addChildren(children);
//    }
//
//    private Player rollOut(Game game) {
//        while (!game.isGameOver()) {
//            List<Action> availableActions = game.getAvailableActions();
//            Action action = availableActions.get(ThreadLocalRandom.current().nextInt(availableActions.size()));
//            game.handleAction(action);
//        }
//        return game.getWinner();
//    }
//
//    private void backPropagate(Node node, Player turn, Player outcome) {
//        int reward = (outcome == turn) ? 1 : 0;
//
//        while (node != null) {
//            node.incrementVisitCount();
//            node.incrementReward(reward);
//            node = node.getParent();
//        }
//    }
//
//    public void handleAction(Action action) {
//        rootGame.handleAction(action);
//
//        if (root.getChildren().containsKey(action)) {
//            root = root.getChildren().get(action);
//        } else {
//            root = new Node(null);
//        }
//    }
//
//    private static <T> T deepCopy(T object) {
//        // Implement deep copy logic for Game and other relevant objects
//        // You can use serialization or a custom deep copy method
//        // For simplicity, assuming a shallow copy is sufficient for demonstration purposes
//        return object;
//    }
//
//    private static class Node {
//        private final Action action;
//        private final Node parent;
//        private int visitCount;
//        private int reward;
//        private final Map<Action, Node> children;
//
//        public Node(Action action) {
//            this(action, null);
//        }
//
//        public Node(Action action, Node parent) {
//            this.action = action;
//            this.parent = parent;
//            this.visitCount = 0;
//            this.reward = 0;
//            this.children = new HashMap<>();
//        }
//
//        public void addChildren(List<Node> children) {
//            for (Node child : children) {
//                this.children.put(child.getAction(), child);
//            }
//        }
//
//        public double getUCTValue() {
//            if (visitCount == 0) {
//                return Double.POSITIVE_INFINITY;
//            }
//
//            double exploitation = (double) reward / visitCount;
//            double exploration = EXPLORATION * Math.sqrt(Math.log(parent.getVisitCount()) / visitCount);
//            return exploitation + exploration;
//        }
//
//        public Action getAction() {
//            return action;
//        }
//
//        public Node getParent() {
//            return parent;
//        }
//
//        public int getVisitCount() {
//            return visitCount;
//        }
//
//        public void incrementVisitCount() {
//            visitCount++;
//        }
//
//        public void incrementReward(int value) {
//            reward += value;
//        }
//
//        public Map<Action, Node> getChildren() {
//            return children;
//        }
//    }
//
//    private static class NodeGamePair {
//        private final Node node;
//        private final Game game;
//
//        public NodeGamePair(Node node, Game game) {
//            this.node = node;
//            this.game = game;
//        }
//    }
}