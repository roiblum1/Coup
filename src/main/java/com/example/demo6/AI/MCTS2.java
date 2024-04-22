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
    private Node rootNode;

    public MCTS2(Game game) {
        this.game = game;
        this.random = new Random();
    }

    public Action bestMove() {
        Player currentPlayer = game.getCurrentPlayer();
        rootNode = new Node(null, null, game);
        for (int i = 0; i < ITERATIONS; i++) {
            Node node = select(rootNode);
            double score = simulate(node);
            backpropagate(node, score);
        }

        return getBestChild(rootNode, 0).getAction();
    }

    private Node select(Node node) {
        while (!node.isTerminal()) {
            System.out.println("Selecting in node with action: " + (node.getAction() != null ? node.getAction().getClass().getSimpleName() : "Root"));
            if (!node.isFullyExpanded()) {
                Node expandedNode = expand(node);
                if (expandedNode != null) {
                    System.out.println("Expanding node: " + expandedNode.getAction().getClass().getSimpleName());
                    return expandedNode;
                }
            } else {
                node = getBestChild(node, EXPLORATION_PARAMETER);
                System.out.println("Moving to best child node with action: " + node.getAction().getClass().getSimpleName());
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

                // Create child nodes for different challenge and block scenarios
                Node challengeSuccessNode = new Node(node, action, deepCopy(newGame));
                Node challengeFailureNode = new Node(node, action, deepCopy(newGame));
                Node blockSuccessNode = new Node(node, action, deepCopy(newGame));
                Node blockFailureNode = new Node(node, action, deepCopy(newGame));

                // Simulate the action and update the game state for each scenario
                challengeSuccessNode.getGame().executeAction(action, null);
                challengeFailureNode.getGame().executeAction(action, null);
                blockSuccessNode.getGame().executeAction(action, null);
                blockFailureNode.getGame().executeAction(action, null);

                // Assign probabilities to each scenario
                double challengeSuccessProbability = 0.5; // Adjust the probability as needed
                double challengeFailureProbability = 0.5;
                double blockSuccessProbability = 0.5; // Adjust the probability as needed
                double blockFailureProbability = 0.5;

                // Add child nodes to the parent node
                node.getChildren().add(challengeSuccessNode);
                node.getChildren().add(challengeFailureNode);
                node.getChildren().add(blockSuccessNode);
                node.getChildren().add(blockFailureNode);

                return node;
            }
        }
        return null;
    }

    private double simulate(Node node) {
        Game game = deepCopy(node.getGame());
        while (!game.isGameOver()) {
            List<Action> availableActions = game.getAvailableActions(game.getCurrentPlayer());
            Action randomAction = availableActions.get(random.nextInt(availableActions.size()));

            // Randomly determine if a challenge occurs
            boolean isChallenged = random.nextDouble() < 0.5; // Adjust the probability as needed

            // Randomly determine if a block occurs
            boolean isBlocked = random.nextDouble() < 0.5; // Adjust the probability as needed

            // Update the game state based on the challenge and block outcomes
            if (isChallenged) {
                if (random.nextDouble() < 0.5) { // Adjust the probability as needed
                    // Challenge succeeded
                    game.executeAction(randomAction, null);
                } else {
                    // Challenge failed
                    // Handle the consequence of a failed challenge
                }
            } else if (isBlocked) {
                if (random.nextDouble() < 0.5) { // Adjust the probability as needed
                    // Block succeeded
                    // Handle the consequence of a successful block
                } else {
                    // Block failed
                    game.executeAction(randomAction, null);
                }
            } else {
                // No challenge or block occurred
                game.executeAction(randomAction, null);
            }

            double evaluation = evaluatePosition(game);

            if (evaluation < 0) {
                return -1.0;
            }
        }
        // Calculate the result based on the winner
        Player winner = game.getWinner();
        if (winner != null) {
            if (winner.equals(node.getParent().getPlayer())) {
                return 1.0; // The player of the parent node is the winner
            } else {
                return -1.0; // The opponent is the winner
            }
        } else {
            return 0.0; // The game ended in a draw
        }
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

        // Consider the impact of challenges and blocks on the evaluation
        double challengeScore = evaluateChallengeImpact(game);
        double blockScore = evaluateBlockImpact(game);

        return currentPlayerScore - opponentPlayerScore + challengeScore + blockScore;
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

        // Consider the impact of challenges and blocks on the player's position
        double playerChallengeScore = evaluatePlayerChallengeImpact(player);
        double playerBlockScore = evaluatePlayerBlockImpact(player);

        return score + playerChallengeScore + playerBlockScore;
    }

    private double evaluateChallengeImpact(Game game) {
        // Evaluate the impact of challenges on the game state
        // You can assign scores based on the likelihood and consequences of successful and failed challenges
        // For example, you can consider the number of challenges made, the success rate, and the resulting changes in the game state
        // Adjust the evaluation based on your game's specific rules and strategies
        return 0.0; // Placeholder value, replace with your own evaluation logic
    }

    private double evaluateBlockImpact(Game game) {
        // Evaluate the impact of blocks on the game state
        // You can assign scores based on the likelihood and consequences of successful and failed blocks
        // For example, you can consider the number of blocks made, the success rate, and the resulting changes in the game state
        // Adjust the evaluation based on your game's specific rules and strategies
        return 0.0; // Placeholder value, replace with your own evaluation logic
    }

    private double evaluatePlayerChallengeImpact(Player player) {
        // Evaluate the impact of challenges on the player's position
        // You can assign scores based on the player's ability to make successful challenges and defend against challenges
        // Consider factors such as the player's influence cards, challenge history, and potential outcomes
        // Adjust the evaluation based on your game's specific rules and strategies
        return 0.0; // Placeholder value, replace with your own evaluation logic
    }

    private double evaluatePlayerBlockImpact(Player player) {
        // Evaluate the impact of blocks on the player's position
        // You can assign scores based on the player's ability to make successful blocks and defend against blocks
        // Consider factors such as the player's influence cards, block history, and potential outcomes
        // Adjust the evaluation based on your game's specific rules and strategies
        return 0.0; // Placeholder value, replace with your own evaluation logic
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

        public void setGame(Game updatedGame) {
            this.game = updatedGame;
        }
    }

    public void handleAction(Action action) {
        // Traverse the tree to find the corresponding node
        Node currentNode = findNodeForAction(action);

        if (currentNode != null) {
            // Update the node's state based on the executed action
            Game updatedGame = deepCopy(currentNode.getGame());
            updatedGame.executeAction(action, null);

            // Update the current node with the updated game state
            currentNode.setGame(updatedGame);
        }
    }

    private Node findNodeForAction(Action action) {
        Node currentNode = rootNode;
        while (currentNode != null) {
            if (currentNode.getAction().equals(action)) {
                return currentNode;
            }
            currentNode = currentNode.getChildren().stream()
                    .filter(child -> child.getAction().equals(action))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public void handleGameOver(Player winner) {
        // Backpropagate the result to the root node and update the scores
        backpropagateResult(rootNode, winner);
    }

    private void backpropagateResult(Node node, Player winner) {
        while (node != null) {
            node.incrementVisitCount();
            if (node.getPlayer().equals(winner)) {
                node.updateScore(1.0); // Update score for the winning player
            } else {
                node.updateScore(0.0); // Update score for the losing player
            }
            node = node.getParent();
        }
    }
}