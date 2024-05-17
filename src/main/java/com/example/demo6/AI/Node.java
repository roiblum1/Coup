package com.example.demo6.AI;

import com.example.demo6.Model.Actions.Action;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a node in the Monte Carlo Tree Search algorithm.
 * Each node corresponds to a game state, represented by an action taken from the parent node.
 */
public class Node {
    private static final double EXPLORATION = 2;
    private final Action action;
    private final Node parent;
    private int visitCount;
    private int reward;
    private final Map<Action, Node> children;

    /**
     * Constructs a root node with a specified action.
     * @param action The action corresponding to this node.
     */
    public Node(Action action) {
        this(action, null);
    }

    /**
     * Constructs a node with a specified action and parent.
     * @param action The action corresponding to this node.
     * @param parent The parent node of this node.
     */
    public Node(Action action, Node parent) {
        this.action = action;
        this.parent = parent;
        this.visitCount = 0;
        this.reward = 0;
        this.children = new HashMap<>();
    }

    /**
     * Adds a list of child nodes to this node.
     * @param children The list of child nodes to be added.
     */
    public void addChildren(List<Node> children) {
        for (Node child : children) {
            this.children.put(child.getAction(), child);
        }
    }

    /**
     * Calculates and returns the Upper Confidence Bound 1 (UCB1) value for this node.
     * @return The UCB1 value for this node.
     */
    public double getUCB1Value() {
        if (visitCount == 0) {
            return Double.POSITIVE_INFINITY;
        }
        double averageReward = (double) reward / visitCount;
        double explorationFactor;
        if (parent != null && parent.getVisitCount() > 0) {
            explorationFactor = Math.sqrt(EXPLORATION * Math.log(parent.getVisitCount()) / visitCount);
        } else {
            explorationFactor = Math.sqrt(EXPLORATION * Math.log(visitCount) / visitCount);
        }
        return averageReward + explorationFactor;
    }

    /**
     * Returns the action associated with this node.
     * @return The action.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns the parent of this node.
     * @return The parent node.
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Returns the visit count of this node.
     * @return The number of times this node has been visited.
     */
    public int getVisitCount() {
        return visitCount;
    }

    /**
     * Increments the visit count by one.
     */
    public void incrementVisitCount() {
        visitCount++;
    }

    /**
     * Increments the reward of this node by a specified value.
     * @param value The value to add to the node's reward.
     */
    public void incrementReward(int value) {
        reward += value;
    }

    /**
     * Returns the map of children nodes.
     * @return The children of this node.
     */
    public Map<Action, Node> getChildren() {
        return children;
    }

    /**
     * Adds a single child node to this node.
     * @param child The child node to add.
     */
    public void addChild(Node child) {
        children.put(child.getAction(), child);
    }

    /**
     * Selects and returns the child node with the highest UCT value.
     * @return The child node with the highest UCT value.
     */
    public Node selectChild() {
        return children.values().stream()
                .max(Comparator.comparingDouble(Node::getUCB1Value))
                .orElse(null);
    }


    /**
     * Checks if this node is a leaf node (i.e., has no children).
     * @return true if this node is a leaf, false otherwise.
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Returns the reward of this node.
     * @return The reward.
     */
    public double getReward() {
        return this.reward;
    }
}
