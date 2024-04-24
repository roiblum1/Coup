package com.example.demo6.AI;

import com.example.demo6.Model.Actions.*;
import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Game;
import com.example.demo6.Model.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MCTS {
    private final Game rootGame;
    private Node root;
    private final int numOfSimulations;
    private final int maxDepth;


    public MCTS(Game game, int numOfSimulations, int maxDepth) {
        this.rootGame = deepCopy(game);
        this.root = new Node(null);
        this.numOfSimulations = numOfSimulations;
        this.maxDepth = maxDepth;
    }



    /**
     * This method returns the best action for the AI to take in the game.
     * It uses a Monte Carlo Tree Search (MCTS) algorithm to simulate and evaluate the game tree.
     * The best action is determined by selecting the action with the highest visit count and reward value.
     * If there are multiple actions with the same highest value, the algorithm selects one of them randomly.
     * If no valid moves are available, the method returns null.
     * @return the best action for the AI to take in the game
     */
    public Action bestMove() {
        if (rootGame.isGameOver()) {
            return null;
        }

        search(numOfSimulations, maxDepth);

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

    /**
     * This method performs a Monte Carlo Tree Search (MCTS) algorithm to simulate and evaluate the game tree.
     * The algorithm starts at the root node and traverses the game tree to a leaf node.
     * It then selects the action with the highest visit count and reward value.
     * If there are multiple actions with the same highest value, the algorithm selects one of them randomly.
     * @param numSimulations the number of simulations to run
     * @param maxDepth the maximum depth of the game tree to search
     **/
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

            Player winner = rollOut(game, maxDepth);
            if (winner != null) {
                System.out.println("Rollout winner: " + winner.getName());
            } else {
                System.out.println("Rollout ended with no winner.");
            }

            System.out.println(game.getCurrentPlayer().getName() + "'s turn.");
            backPropagate(node, game.getCurrentPlayer(), winner, game);

            if (node.getParent() == root) {
                return;
            }
        }
    }

    /**
     * Selects a node and game state from the MCTS tree.
     * @param maxDepth the maximum depth to search in the MCTS tree
     * @return a pair containing the selected node and the corresponding game state
     */
    private NodeGamePair selectNode(int maxDepth) {
        Node node = root;
        Game game = deepCopy(rootGame);
        int depth = 0;

        // Loop through the MCTS tree up to the specified maximum depth
        while (depth < maxDepth) {
            // If the current node is a leaf node, expand it by simulating rollouts
            if (node.isLeaf()) {
                expand(node, game);
            }

            // If the current node has no children, break the loop
            if (node.getChildren().isEmpty()) {
                break;
            }

            // Select a child node for further exploration
            node = node.selectChild();

            // Simulate the AI's decision-making process based on the current game state
            boolean isChallenged = simulateChallenge(game, node.getAction());
            boolean isBlocked = simulateBlock(game, node.getAction());

            // Execute the selected action in the game state
            executeAction(game, node.getAction(), isChallenged, isBlocked);
            game.switchTurns();
            // Increment the depth counter
            depth++;
        }
        // Return a pair containing the selected node and the corresponding game state
        return new NodeGamePair(node, game);
    }

    /**
     * Expands the MCTS tree by creating new nodes based on the current game state.
     * @param parent The parent node of the new nodes.
     * @param game The current game state.
     */
    private void expand(Node parent, Game game) {
        //Checks if the game is over. If it is, do not expand the tree.
        if (game.isGameOver()) {
            return;
        }
        Player currentPlayer = game.getCurrentPlayer();
        if (currentPlayer == null) {
            return;
        }
        //Creating a node for every action in the game
        List<Action> availableActions = game.getAvailableActions(currentPlayer);
        List<Node> childNodes = new ArrayList<>();
        for (Action action : availableActions) {
            Node child = new Node(action, parent);
            childNodes.add(child);
            System.out.println("Created child node for action: " + action.getCodeOfAction());
        }
        parent.addChildren(childNodes);
    }

    private Player rollOut(Game game, int maxDepth) {
        int depth = 0;

        while (!game.isGameOver() && depth < maxDepth) {
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer == null) {
                break;
            }

            List<Action> availableActions = game.getAvailableActions(currentPlayer);
            if (availableActions.isEmpty()) {
                game.switchTurns();
                continue;
            }

            Action action;
            if (currentPlayer == game.getPlayers().get(1)) { // Assuming the AI player is at index 1
                action = selectActionHeuristically(availableActions, game);
            } else {
                // Select a random action for the human player (simulating random play)
                action = availableActions.get(ThreadLocalRandom.current().nextInt(availableActions.size()));
            }

            System.out.println("Rollout action: " + action.getCodeOfAction());

            boolean isChallenged = simulateChallenge(game, action);
            boolean isBlocked = simulateBlock(game, action);

            executeAction(game, action, isChallenged, isBlocked);

            depth++;

            // Evaluate the current position of the AI player
            int aiPlayerScore = evaluatePosition(game.getPlayers().get(1));

            // If the AI player's score is significantly lower than the human player's score,
            // stop searching this branch and return null (indicating a losing node)
            if (aiPlayerScore < evaluatePosition(game.getPlayers().get(0)) - 20) {
                return null;
            }
        }
        // If the game reaches a non-terminal state, evaluate the position to determine the winner
        if (!game.isGameOver()) {
            int aiPlayerScore = evaluatePosition(game.getPlayers().get(1));
            int humanPlayerScore = evaluatePosition(game.getPlayers().get(0));

            if (aiPlayerScore > humanPlayerScore) {
                return game.getPlayers().get(1);
            } else if (humanPlayerScore > aiPlayerScore) {
                return game.getPlayers().get(0);
            }
        }
        return game.getWinner();
    }

    private Action selectActionHeuristically(List<Action> availableActions, Game game) {
        Player aiPlayer = game.getCurrentPlayer();
        Player humanPlayer = game.getOpponent(aiPlayer);
        List<Card> aiPlayerCards = aiPlayer.getCards();
        int aiPlayerCoins = aiPlayer.getCoins();
        int humanPlayerCoins = humanPlayer.getCoins();
        int humanPlayerCardCount = humanPlayer.getCards().size();

        // Immediate winning moves: If human player has 1 card, prioritize COUP or ASSASSINATE if possible.
        if (humanPlayerCardCount == 1) {
            if (aiPlayerCoins >= 7) { // Coup is a guaranteed kill if it can be afforded and not blocked.
                return availableActions.stream()
                        .filter(action -> action.getActionCode() == ActionCode.COUP)
                        .findFirst()
                        .orElse(null);
            }
            if (aiPlayerCoins >= 3 && aiPlayerCards.contains(Deck.CardType.ASSASSIN)) { // Assassinate if possible.
                return availableActions.stream()
                        .filter(action -> action.getActionCode() == ActionCode.ASSASSINATE)
                        .findFirst()
                        .orElse(null);
            }
        }

        // Use Duke to collect taxes if available to maximize coin gain safely.
        if (aiPlayerCards.contains(Deck.CardType.DUKE)) {
            return availableActions.stream()
                    .filter(action -> action.getActionCode() == ActionCode.TAX)
                    .findFirst()
                    .orElse(null);
        }

        // Use Captain to steal if the human player has coins and the AI has the Captain.
        if (aiPlayerCards.contains(Deck.CardType.CAPTAIN) && humanPlayerCoins > 0) {
            return availableActions.stream()
                    .filter(action -> action.getActionCode() == ActionCode.STEAL)
                    .findFirst()
                    .orElse(null);
        }

        // If the AI has enough coins to coup on the next turn, consider gaining more coins or keeping a low profile.
        if (aiPlayerCoins >= 5) {
            if (aiPlayerCoins == 6) {
                return availableActions.stream()
                        .filter(action -> action.getActionCode() == ActionCode.FOREIGN_AID || action.getActionCode() == ActionCode.INCOME)
                        .findFirst()
                        .orElse(null); // Foreign Aid or Income to get to 7 coins for a Coup next turn.
            }
            // Consider swapping if having excess coins and possibly bad cards.
            if (aiPlayerCoins > 8) {
                return availableActions.stream()
                        .filter(action -> action.getActionCode() == ActionCode.SWAP)
                        .findFirst()
                        .orElse(null);
            }
        }

        // Default to income if no strategic moves are immediately necessary.
        return availableActions.stream()
                .filter(action -> action.getActionCode() == ActionCode.INCOME)
                .findFirst()
                .orElse(null);
    }


    private void backPropagate(Node node, Player turn, Player winner, Game game) {
        while (node != null) {
            node.incrementVisitCount();

            if (winner != null) {
                if (winner.getName().equals(turn.getName())) {
                    node.incrementReward(1);
                } else {
                    node.incrementReward(-1);
                }
                System.out.println("Backpropagating for player: " + turn.getName() + ", Winner: " + winner.getName());
            } else {
                // Evaluate the position when there is no clear winner
                int aiPlayerScore = evaluatePosition(game.getPlayers().get(1));
                int humanPlayerScore = evaluatePosition(game.getPlayers().get(0));

                if (aiPlayerScore > humanPlayerScore) {
                    node.incrementReward(1);
                } else if (humanPlayerScore > aiPlayerScore) {
                    node.incrementReward(-1);
                }
                System.out.println("AI Score: " + aiPlayerScore + ", Human Score: " + humanPlayerScore);
            }

            if (node.getParent() == null) {
                break;
            }

            if (node.getAction() != null) {
                System.out.println("Backpropagation: Action = " + node.getAction().getCodeOfAction() + ", Visit Count = " + node.getVisitCount() + ", Reward = " + node.getReward());
            } else {
                System.out.println("Backpropagation: Action = null, Visit Count = " + node.getVisitCount() + ", Reward = " + node.getReward());
            }

            node = node.getParent();
        }
    }

    public void handleAction(Action action) {
        if (root.getChildren().containsKey(action)) {
            root = root.getChildren().get(action);
        } else {
            root = new Node(null);
        }
    }

    public void handleGameOver(Player winner) {
        if (winner == rootGame.getPlayers().get(0)) {
            root.incrementReward(-1000);
        } else if (winner == rootGame.getPlayers().get(1)) {
            root.incrementReward(1000);
        } else {
            root.incrementReward(1);
        }

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
                currentPlayer.loseRandomInfluence();
                return;
            }
        }

        if (isBlocked) {
            if (simulateBlockChallenge(simulationGame, action)) {
                if (targetPlayer != null) {
                    targetPlayer.loseRandomInfluence();
                }
            } else {
                return;
            }
        }

        List<Card> cards = null;
        if (action instanceof SwapAction) {
            List<Card> newCards = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                if (!simulationGame.getDeck().isEmpty()) {
                    newCards.add(simulationGame.getDeck().getCard());
                }
            }
            List<Card> swapOptions = new ArrayList<>(currentPlayer.getCards());
            swapOptions.addAll(newCards);
            List<Card> selectedCards = currentPlayer.selectRandomCardsToKeep(swapOptions);
            cards = new ArrayList<>();
            cards.addAll(selectedCards);
            cards.addAll(newCards);
        } else if (action instanceof CoupAction || action instanceof AssassinateAction) {
            if (!targetPlayer.getCards().isEmpty()) {
                Card cardToLose = targetPlayer.getCards().get(0);
                cards = new ArrayList<>();
                cards.add(cardToLose);
            }
        }

        simulationGame.executeAction(action, cards);
        System.out.println("The executed action is : " + action.getActionCode() + " the player " +action.getPlayer().getName());
        simulationGame.switchTurns();

    }

    public boolean simulateChallenge(Game game, Action action) {
        Player aiPlayer = game.getCurrentPlayer();
        Player humanPlayer = game.getOpponent(aiPlayer);

        // If AI has only one card left and the human player is performing an Assassinate action,
        // always challenge to avoid losing the game
        if (aiPlayer.getCards().size() == 1 && action.getActionCode() == ActionCode.ASSASSINATE && !aiPlayer.hasCard(Deck.CardType.CONTESSA)) {
            return true;
        }

        // If AI has only one card left, avoid challenging unless it's a critical situation
        if (aiPlayer.getCards().size() == 1 && humanPlayer.getCards().size() > 1) {
            return false;
        }

        // Challenge if the human player performs an action that could directly lead to the AI's loss
        if (action.getActionCode() == ActionCode.ASSASSINATE || action.getActionCode() == ActionCode.COUP) {
            return aiPlayer.getCards().size() <= humanPlayer.getCards().size();
        }

        // Simulate challenge based on AI's belief about human player's card holdings
        // Example: AI may think it's highly unlikely the human player has a Duke
        if (action.getActionCode() == ActionCode.TAX) {
            return !aiPlayer.getCards().contains(Deck.CardType.DUKE); // Challenge if AI does not have a Duke, suspecting the same for human
        }
        return false;
    }

    public boolean simulateBlock(Game game, Action action) {
        Player aiPlayer = game.getCurrentPlayer();
        Player humanPlayer = game.getOpponent(aiPlayer);
        // If AI has only one card left and the human player is performing an Assassinate action,
        // always block to avoid losing the game
        if (aiPlayer.getCards().size() == 1 && action.getActionCode() == ActionCode.ASSASSINATE) {
            return true;
        }
        // Block foreign aid if AI has a Duke
        if (action.getActionCode() == ActionCode.FOREIGN_AID && aiPlayer.getCards().contains(Deck.CardType.DUKE)) {
            return true;
        }
        // Block steal if AI has a Captain or Ambassador
        if (action.getActionCode() == ActionCode.STEAL && (aiPlayer.getCards().contains(Deck.CardType.CAPTAIN) || aiPlayer.getCards().contains(Deck.CardType.AMBASSADOR))) {
            return true;
        }
        // Block assassinate if AI has a Contessa
        if (action.getActionCode() == ActionCode.ASSASSINATE && aiPlayer.getCards().contains(Deck.CardType.CONTESSA)) {
            return true;
        }
        return false;
    }



    public boolean simulateBlockChallenge(Game game, Action action) {
        Player aiPlayer = game.getCurrentPlayer();
        if (aiPlayer.getCards().size() == 1) {
            return false;
        }
        // Challenge block if AI is about to lose and the block prevents a game-saving move
        switch (action.getActionCode()) {
            case FOREIGN_AID:
                // Challenge if block (Duke) seems unlikely
                return isSuspiciousBlock(Deck.CardType.DUKE, aiPlayer);

            case ASSASSINATE:
                // Challenge if block (Contessa) seems unlikely
                return isSuspiciousBlock(Deck.CardType.CONTESSA, aiPlayer);

            case STEAL:
                // Challenge if block (Ambassador or Captain) seems unlikely
                boolean ambassadorSuspicious = isSuspiciousBlock(Deck.CardType.AMBASSADOR, aiPlayer);
                boolean captainSuspicious = isSuspiciousBlock(Deck.CardType.CAPTAIN, aiPlayer);
                return ambassadorSuspicious || captainSuspicious;

            default:
                return false;  // No challenge by default if not one of the specified blockable actions
        }
    }

    // Function to check if blocking is suspicious based on the rarity of cards
    boolean isSuspiciousBlock(Deck.CardType requiredCard, Player aiPlayer) {
        List<Card> aiPlayerCards = aiPlayer.getCards();
        long countInAIHand = aiPlayerCards.stream().filter(card -> card.getName().equals(requiredCard.getName())).count();
        long totalInGame = 2;  // Total copies of each card type in the game
        long totalPossible = totalInGame - countInAIHand;

        // More suspicious if fewer possible cards are available to the opponent
        return totalPossible < 1;  // Less than 1 means the opponent unlikely to have a card
    }

    public List<Card> selectCardsToKeep(Game game, Player player, List<Card> newCards) {
        List<Card> allCards = new ArrayList<>(player.getCards());
        allCards.addAll(newCards);
        allCards.sort(Comparator.comparingInt(this::getCardValue).reversed());
        return allCards.subList(0, 2);
    }

    public Card selectCardToGiveUp(Game game, Player player) {
        List<Card> cards = player.getCards();

        // If the player has only one card, return that card
        if (cards.size() == 1) {
            return cards.get(0);
        }

        // Heuristic: Give up the least valuable card
        // You can define the value of each card based on your game strategy
        Card leastValuableCard = null;
        int minValue = Integer.MAX_VALUE;

        for (Card card : cards) {
            int cardValue = getCardValue(card);
            if (cardValue < minValue) {
                minValue = cardValue;
                leastValuableCard = card;
            }
        }

        return leastValuableCard;
    }

    private int evaluatePosition(Player player) {
        int score = 0;
        // Add points for each card held by the player
        score += player.getCards().size() * 10;
        // Add points for each coin possessed by the player
        score += player.getCoins();
        // Add bonus points for valuable cards based on their value
        for (Card card : player.getCards()) {
            score += getCardValue(card);
        }
        return score;
    }

    private int getCardValue(Card card) {
        // Assign values to each card based on your game strategy
        // Higher values indicate more valuable cards
        switch (card.getName()) {
            case "Duke":
                return 5;
            case "Assassin":
                return 4;
            case "Captain":
                return 3;
            case "Ambassador":
                return 2;
            case "Contessa":
                return 1;
            default:
                return 0;
        }
    }

    private static Game deepCopy(Game game) {
        Game copiedGame = new Game(deepCopyDeck(game.getDeck()));
        copiedGame.setCurrentPlayerIndex(game.getCurrentPlayerIndex());
        copiedGame.setLastExecutedAction(game.getLastExecutedAction());
        List<Player> copiedPlayerList = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            Player copiedPlayer = new Player(player.getName());
            copiedPlayer.setCoins(player.getCoins());
            copiedPlayer.setCards(deepCopyCards(player.getCards()));
            copiedPlayer.setDeck(copiedGame.getDeck());
            copiedPlayerList.add(copiedPlayer);
        }
        copiedGame.setPlayerList(copiedPlayerList);

        return copiedGame;
    }

    private static Deck deepCopyDeck(Deck deck) {
        Set<Deck.CardType> cardTypes = EnumSet.allOf(Deck.CardType.class);
        if (cardTypes.isEmpty()) {
            throw new IllegalStateException("Card types cannot be empty.");
        }

        Deck copiedDeck = new Deck(cardTypes, 2);

        for (Card card : deck.getContents()) {
            if (card == null) {
                throw new IllegalStateException("Deck contains a null card.");
            }
            copiedDeck.returnCard(new Card(card.getName()));
        }

        return copiedDeck;
    }

    private static List<Card> deepCopyCards(List<Card> cards) {
        List<Card> copiedCards = new ArrayList<>();
        for (Card card : cards) {
            copiedCards.add(new Card(card.getName()));
        }
        return copiedCards;
    }


}