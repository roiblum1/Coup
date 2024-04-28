package com.example.demo6.AI;

import com.example.demo6.Model.Game;
import com.example.demo6.AI.Node;
public class NodeGamePair {
    public final Node node;
    public final Game game;

    /**
     * Constructs a new instance of NodeGamePair with the provided Node and Game.
     *
     * @param node  The Node instance to be associated with this pair.
     * @param game  The Game instance to be associated with this pair.
     */
    public NodeGamePair(Node node, Game game) {
        this.node = node;
        this.game = game;
    }
}
