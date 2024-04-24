package com.example.demo6.AI;

import com.example.demo6.Model.Game;
import com.example.demo6.AI.Node;
public class NodeGamePair {
    public final Node node;
    public final Game game;

    public NodeGamePair(Node node, Game game) {
        this.node = node;
        this.game = game;
    }
}
