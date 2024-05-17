package com.example.demo6.AI;

class TranspositionEntry {
    private Node node;
    private int depth;
    private double value;

    public TranspositionEntry(Node node, int depth, double value) {
        this.node = node;
        this.depth = depth;
        this.value = value;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
