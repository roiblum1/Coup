package com.example.demo6.AI;
import com.example.demo6.Model.Actions.Action;
import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Game;
import com.example.demo6.Model.Player;

import java.util.*;

public class MCTS2 {
    private static final int ITERATIONS = 1000;
    private static final double EXPLORATION_PARAMETER = Math.sqrt(2);

    private Game game;
    private Random random;

    public MCTS2(Game game) {
        this.game = game;
        this.random = new Random();
    }

    public Action bestMove() {
        Player currentPlayer = game.getCurrentPlayer();
        Node rootNode = new Node(null, null, game);

        for (int i = 0; i < ITERATIONS; i++) {
            Node node = select(rootNode);
            double score = simulate(node);
            backpropagate(node, score);
        }

        return getBestChild(rootNode, 0).getAction();
    }

    private Node select(Node node) {
        while (!node.isTerminal()) {
            if (!node.isFullyExpanded()) {
                Node expandedNode = expand(node);
                if (expandedNode != null) {
                    return expandedNode;
                }
            } else {
                node = getBestChild(node, EXPLORATION_PARAMETER);
            }
        }
        return node;
    }

    private Node expand(Node node) {
        Game game = node.getGame();
        List<Action> availableActions = game.getAvailableActions(game.getCurrentPlayer());
        for (Action action : availableActions) {
            if (!node.hasChildWithAction(action)) {
                Game newGame = deepCopy(game);
                newGame.executeAction(action);

                double evaluation = evaluatePosition(newGame);

                if (evaluation < 0) {
                    continue;
                }

                Node newNode = new Node(node, action, newGame);
                node.getChildren().add(newNode);
                return newNode;
            }
        }
        return null;
    }

    private double simulate(Node node) {
        Game game = deepCopy(node.getGame());
        while (!game.isGameOver()) {
            List<Action> availableActions = game.getAvailableActions(game.getCurrentPlayer());
            Action randomAction = availableActions.get(random.nextInt(availableActions.size()));
            game.executeAction(randomAction);

            double evaluation = evaluatePosition(game);

            if (evaluation < 0) {
                return -1.0;
            }
        }
        return game.getResult(node.getParent().getPlayer());
    }

    private void backpropagate(Node node, double score) {
        while (node != null) {
            node.incrementVisitCount();
            node.updateScore(score);
            node = node.getParent();
        }
    }

    private Node getBestChild(Node node, double explorationParameter) {
        double bestValue = Double.NEGATIVE_INFINITY;
        Node bestNode = null;
        for (Node child : node.getChildren()) {
            double uctValue = child.getScore() / child.getVisitCount()
                    + explorationParameter * Math.sqrt(Math.log(node.getVisitCount()) / child.getVisitCount());
            if (uctValue > bestValue) {
                bestValue = uctValue;
                bestNode = child;
            }
        }
        return bestNode;
    }

    private double evaluatePosition(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
        Player opponentPlayer = game.getOpponent(currentPlayer);

        double currentPlayerScore = evaluatePlayerPosition(currentPlayer);
        double opponentPlayerScore = evaluatePlayerPosition(opponentPlayer);

        return currentPlayerScore - opponentPlayerScore;
    }

    private double evaluatePlayerPosition(Player player) {
        double score = 0;

        score += player.getCoins() * 0.5;
        score += player.getCards().size() * 2;

        for (Card card : player.getCards()) {
            switch (card.getName()) {
                case "Duke":
                    score += 3;
                    break;
                case "Assassin":
                    score += 2;
                    break;
                case "Captain":
                    score += 2;
                    break;
                case "Ambassador":
                    score += 1;
                    break;
                case "Contessa":
                    score += 1;
                    break;
            }
        }

        return score;
    }

    private Game deepCopy(Game game) {
        Set<Deck.CardType> allCardTypes = EnumSet.allOf(Deck.CardType.class);
        Game newGame = new Game(new Deck(allCardTypes, 2));

        List<Player> clonedPlayers = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            Player clonedPlayer = new Player(player.getName());
            clonedPlayer.setCoins(player.getCoins());

            List<Card> clonedCards = new ArrayList<>(player.getCards());
            clonedPlayer.setCards(clonedCards);

            clonedPlayers.add(clonedPlayer);
        }
        newGame.setPlayerList(clonedPlayers);

        newGame.setCurrentPlayerIndex(game.getCurrentPlayerIndex());

        return newGame;
    }

    private static class Node {
        private Node parent;
        private Action action;
        private Game game;
        private List<Node> children;
        private int visitCount;
        private double score;

        public Node(Node parent, Action action, Game game) {
            this.parent = parent;
            this.action = action;
            this.game = game;
            this.children = new ArrayList<>();
            this.visitCount = 0;
            this.score = 0;
        }

        public Node getParent() {
            return parent;
        }

        public Action getAction() {
            return action;
        }

        public Game getGame() {
            return game;
        }

        public List<Node> getChildren() {
            return children;
        }

        public int getVisitCount() {
            return visitCount;
        }

        public double getScore() {
            return score;
        }

        public void incrementVisitCount() {
            visitCount++;
        }

        public void updateScore(double score) {
            this.score += score;
        }

        public boolean isTerminal() {
            return game.isGameOver();
        }

        public boolean isFullyExpanded() {
            return children.size() == game.getAvailableActions(game.getCurrentPlayer()).size();
        }

        public Player getPlayer() {
            return game.getCurrentPlayer();
        }

        public boolean hasChildWithAction(Action action) {
            for (Node child : children) {
                if (child.getAction().equals(action)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void handleAction(Action action) {
        // Implement the logic to update the MCTS tree based on the executed action
        // You can traverse the tree to find the corresponding node and update its state
        // This method is called after an action is executed in the game

    }

    public void handleGameOver(Player winner) {
        // Implement the logic to update the MCTS tree when the game is over
        // You can backpropagate the result to the root node and update the scores
        // This method is called when the game ends and a winner is determined
    }

}