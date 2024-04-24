package com.example.demo6.AI;

import com.example.demo6.Model.Actions.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
    private static final double EXPLORATION = Math.sqrt(2);
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

    public double getReward() {
        return this.reward;
    }

    public String getRewardToString()
    {
        return "" + this.reward;
    }
}
