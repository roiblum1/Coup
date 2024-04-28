package com.example.demo6.AI;

import com.example.demo6.Model.Actions.*;
import com.example.demo6.Model.Card;
import com.example.demo6.Model.Game;
import com.example.demo6.Model.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.demo6.AI.Heuristic.*;

public class MCTS {
    private Game rootGame;
    private Node root;
    private final int numOfSimulations;
    private final int maxDepth;


    /**
     * Constructs a new instance of the Monte Carlo Tree Search (MCTS) class.
     *
     * @param game The initial game state used to initialize the MCTS tree.
     * @param numOfSimulations The number of simulations to be performed during the MCTS process.
     * @param maxDepth The maximum depth of the MCTS tree.
     */
    public MCTS(Game game, int numOfSimulations, int maxDepth) {
        // Creates a deep copy of the initial game state.
        this.rootGame = game.deepCopy();
        // Initializes the root node of the MCTS tree.
        this.root = new Node(null);
        // Sets the number of simulations to be performed during the MCTS process.
        this.numOfSimulations = numOfSimulations;
        // Sets the maximum depth of the MCTS tree.
        this.maxDepth = maxDepth;
    }



    /**
     * This method returns the best action for the AI to take in the game.
     * It uses a Monte Carlo Tree Search (MCTS) algorithm with the UCB1 selection policy
     * to simulate and evaluate the game tree.
     * The best action is determined by selecting the action with the highest UCB1 value,
     * which balances the average reward and the exploration factor.
     * If no valid moves are available, the method returns null.
     * @return the best action for the AI to take in the game, or null if no valid actions are available.
     */
    public Action bestMove(Game game) {
        this.rootGame =  game.deepCopy();
        if (rootGame.isGameOver()) {
            return null;
        }

        search(numOfSimulations, maxDepth);

        double maxUCB1 = Double.NEGATIVE_INFINITY;
        List<Node> maxNodes = new ArrayList<>();

        System.out.println("Available actions:");
        for (Node child : root.getChildren().values()) {
            if (child.getVisitCount() > 0) {
                double averageReward = child.getReward() / (double) child.getVisitCount();
                double ucb1 = averageReward + Math.sqrt(2 * Math.log(root.getVisitCount()) / child.getVisitCount());
                System.out.println(child.getAction().actionCodeToString() + ": Visit Count = " + child.getVisitCount()
                        + ", Reward = " + child.getReward() + ", Average Reward = " + averageReward
                        + ", UCB1 = " + ucb1);
                if (ucb1 > maxUCB1) {
                    maxUCB1 = ucb1;
                    maxNodes.clear();
                    maxNodes.add(child);
                } else if (ucb1 == maxUCB1) {
                    maxNodes.add(child);
                }
            }
        }

        if (maxNodes.isEmpty()) {
            System.out.println("No valid moves available.");
            return null;
        }

        Node bestChild = maxNodes.get(ThreadLocalRandom.current().nextInt(maxNodes.size()));
        System.out.println("Selected best action: " + bestChild.getAction().actionCodeToString());
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

            Player winner = rollOut(game, maxDepth);
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
        Game game = rootGame.deepCopy();
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
            Game simulationGame = game.deepCopy();
            executeAction(simulationGame, node.getAction(), isChallenged, isBlocked);
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
        //Creating a node for every action in the game and attached them to the parent node that we are expending
        List<Action> availableActions = game.getAvailableActions(currentPlayer);
        List<Node> childNodes = new ArrayList<>();
        for (Action action : availableActions) {
            Node child = new Node(action, parent);
            childNodes.add(child);
        }
        parent.addChildren(childNodes);
    }


    /**
     * This method performs a rollout of the game, simulating the actions of the players and updating the game state accordingly.
     * The rollout is performed by recursively selecting and executing actions for each player, based on the current game state.
     * The rollout continues until the game is over or the maximum depth is reached.
     * The method returns the winner of the game, if it reaches a terminal state, or null otherwise.
     *
     * @param nodeGame the current game state
     * @param maxDepth the maximum depth of the game tree to be explored during the rollout
     * @return the winner of the game, if it reaches a terminal state, or null otherwise
     */
    private Player rollOut(Game nodeGame, int maxDepth) {
        int depth = 0;
        Game game = nodeGame.deepCopy();
        while (!game.isGameOver() && depth < maxDepth) {
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer != null) {
                List<Action> availableActions = game.getAvailableActions(currentPlayer);
                if (!availableActions.isEmpty()) {
                    Action action = selectActionForPlayer(game, currentPlayer, availableActions);
                    boolean isChallenged = simulateChallenge(game, action);
                    boolean isBlocked = simulateBlock(game, action);

                    if (handleChallenge(game, action, isChallenged, currentPlayer) && handleBlock(game, action, isBlocked, currentPlayer)) {
                        executeAction(game, action, false, false); // Execute without further blocks or challenges
                    }
                }
            }
            depth++;
            if (shouldTerminateSearch(game)) {
                return null;
            }
        }
        return determineWinner(game);
    }

    /**
     * Selects an action for a given player based on the player's type (AI or human).
     * For the AI player, the action is chosen using a heuristic approach that evaluates the best possible move.
     * For a human player, the action is chosen randomly from the list of available actions to simulate a human's unpredictable gameplay.
     *
     * @param game The current state of the game, used to identify the player and context.
     * @param player The player for whom the action is being selected.
     * @param availableActions The list of actions that the player can currently execute.
     * @return The selected action, determined heuristically for the AI and randomly for the human simulation.
     */
    private Action selectActionForPlayer(Game game, Player player, List<Action> availableActions) {
        if (player == game.getPlayers().get(1)) { // If the player is the AI
            // Select the best action based on a heuristic evaluation
            return Heuristic.selectActionHeuristically(availableActions, game);
        } else { // If the player is simulated as a human
            // Select a random action to simulate unpredictability
            return availableActions.get(ThreadLocalRandom.current().nextInt(availableActions.size()));
        }
    }

    /**
     * Handles the challenge event during the game simulation.
     * This method evaluates the challenge outcome and updates the game state accordingly.
     * If the action can be challenged and the challenge is successful, the challenging player loses influence.
     * If the action can be challenged and the challenge fails, the challenging player loses influence.
     * If the action cannot be challenged, the method returns true, indicating that the action can proceed.
     * @param game The current state of the game, used to access the current players and game context.
     * @param action The action being challenged.
     * @param isChallenged A boolean value indicating whether the action is being challenged.
     * @param currentPlayer The player whose turn it is during the simulation.
     * @return true if the action can proceed, false otherwise.
     */
    private boolean handleChallenge(Game game, Action action, boolean isChallenged, Player currentPlayer) {
        if (action.canBeChallenged && isChallenged) {
            if (!action.challenge()) {
                handleLoseCard(currentPlayer, game);
                // The challenging player loses influence if the challenge fails
                return false; // Action fails if the challenge is successful
            } else {
                handleLoseCard(game.getOpponent(currentPlayer), game);
            }
        }
        return true; // Continue with the action if no challenge or if challenge failed
    }

    /**
     * Handles the block event during the game simulation.
     * This method evaluates the block outcome and updates the game state accordingly.
     * If the action can be blocked and the block is successful, the action does not proceed.
     * If the action can be blocked and the block is challenged successfully, the blocking player loses influence.
     * If the action can be blocked and the block is challenged unsuccessfully, the challenging player loses influence.
     * If the action cannot be blocked, the method returns true, indicating that the action can proceed.
     * @param game The current state of the game, used to access the current players and game context.
     * @param action The action being blocked.
     * @param isBlocked A boolean value indicating whether the action is being blocked.
     * @param currentPlayer The player whose turn it is during the simulation.
     * @return true if the action can proceed, false otherwise.
     */
    private boolean handleBlock(Game game, Action action, boolean isBlocked, Player currentPlayer) {
        if (action.canBeBlocked && isBlocked) {
            if (simulateBlockChallenge(game, action)) {
                // If the block is challenged successfully, the blocking player loses influence
                handleLoseCard(game.getOpponent(currentPlayer), game);
                return true; // Action proceeds if the block challenge is successful
            } else {
                // If the block is not challenged or the challenge fails, the action does not proceed
                return false; // Action does not proceed if the block is successful
            }
        }
        return true; // Continue with the action if no block
    }

    /**
     * Handles the loose card event during the game simulation.
     * This method evaluates the game state and determines whether the AI should lose a card.
     * If the AI is the player whose turn it is during the simulation, the method selects a card to lose and returns it.
     * If the AI is not the player whose turn it is during the simulation, the method loses a random influence point for the AI.
     * @param game The current state of the game, used to access the current players and game context.
     * @param player The player whose turn it is during the simulation.
     */
    public void handleLoseCard(Player player, Game game) {
        Card cardToLose;
        if (player.getName().equals(game.getPlayers().get(1).getName())) {
            // If the AI is the player whose turn it is during the simulation, the method selects a card to lose and returns it.
            cardToLose = selectCardToGiveUp(game, player);
            player.returnCard(cardToLose);
        } else {
            // If the AI is not the player whose turn it is during the simulation, the method loses a random influence point for the AI.
            player.loseRandomInfluence();
        }
    }
    /**
     * Function to check if the Monte Carlo Tree Search (MCTS) should terminate based on the current game state.
     * This function evaluates the current game state and determines whether the AI is significantly behind the opponent.
     * If the AI is significantly behind, the function returns true, indicating that the MCTS should be terminated.
     * Otherwise, the function returns false, indicating that the MCTS should continue.
     * @param game The current state of the game, used to access the current players and game context.
     * @return true if the MCTS should be terminated, false otherwise.
     */
    private boolean shouldTerminateSearch(Game game) {
        int aiPlayerScore = evaluatePosition(game.getPlayers().get(1));
        int humanPlayerScore = evaluatePosition(game.getPlayers().get(0));
        return aiPlayerScore < humanPlayerScore - 30; // Stop searching if the AI is significantly behind
    }

    /**
     * Determines the winner of the game based on the current game state.
     * This method evaluates the game state and determines the player with the highest score as the winner.
     * If the game is not over, it evaluates the current positions of the players and returns the player with the higher score.
     * If the game is over and there are no active players left, it returns null.
     * If the game is over and there are active players left, it returns the first active player as the winner.
     * @param game The current state of the game, used to access the current players and game context.
     * @return The winner of the game, or null if there are no active players left.
     */
    private Player determineWinner(Game game) {
        if (!game.isGameOver()) {
            int aiPlayerScore = evaluatePosition(game.getPlayers().get(1));
            int humanPlayerScore = evaluatePosition(game.getPlayers().get(0));
            return aiPlayerScore > humanPlayerScore ? game.getPlayers().get(1) : game.getPlayers().get(0);
        }
        List<Player> activePlayers = game.getActivePlayers();
        if (activePlayers.isEmpty()) {
            // Return null if there are no active players left
            return null;
        }
        // Return the first active player as the winner
        return activePlayers.get(0);
    }



    /**
     * Backpropagates the results of a game simulation up the MCTS tree, updating nodes with the simulation's outcome.
     * This method adjusts the visit count and reward of each node based on the outcome of the simulated game.
     * It increments the visit count for each node as the simulation trace passes through it.
     * Rewards are updated based on whether the AI won or lost the game, or based on a position evaluation:
     * - If there's a winner, the reward is adjusted by +10 or -10 depending on whether the turn player is the winner.
     * - If there's no clear winner, the game state is evaluated, and rewards are adjusted by +1 or -1 based on the AI's relative position.
     * This feedback loop helps refine future decisions made by the AI during the MCTS.
     *
     * @param node the node from which to start backpropagation.
     * @param turn the player whose turn it was during the simulation.
     * @param winner the player who won the game, or null if there was no winner.
     * @param game the game state used for position evaluation when there is no clear winner.
     */
    private void backPropagate(Node node, Player turn, Player winner, Game game) {
        while (node != null) {
            node.incrementVisitCount();

            if (winner != null) {
                if (winner.getName().equals(game.getPlayers().get(1).getName())) {
                    node.incrementReward(200);
                } else {
                    node.incrementReward(-200);
                }
            } else {
                // Evaluate the position when there is no clear winner
                int aiPlayerScore = evaluatePosition(game.getPlayers().get(1));
                int humanPlayerScore = evaluatePosition(game.getPlayers().get(0));
                node.incrementReward(aiPlayerScore-humanPlayerScore);
            }

            // Move to the parent node until the root is reached
            node = node.getParent();
            if (node == root.getParent()) { // effectively acts as if node == null for the root (as root's parent is null)
                node = null;
            }
        }
    }


    /**
     * Updates the root of the MCTS tree based on the executed action.
     * - If the root has a child node for the action, it becomes the new root.
     * - If not, the tree is reset with a new root node.
     * This adjustment aligns the MCTS tree with the current game state, ensuring the AI's decisions are based on the latest game dynamics.
     *
     * @param action The action executed in the game that determines the new root node.
     */
    public void handleAction(Action action) {
        if (root.getChildren().containsKey(action)) {
            root = root.getChildren().get(action);
        } else {
            root = new Node(null);
        }
    }


    /**
     * Handles the game over event.
     * This method updates the reward of the root node based on the winner of the game.
     * It also updates the visit count of all the nodes in the tree.
     *
     * @param winner the player who has won the game
     */
    public void handleGameOver(Player winner) {
        if (winner == rootGame.getPlayers().get(0)) {
            root.incrementReward(-1000);
        } else if (winner == rootGame.getPlayers().get(1)) {
            root.incrementReward(1000);
        }

        Node node = root;
        while (node != null) {
            node.incrementVisitCount();
            node = node.getParent();
        }
    }

    /**
     * Executes the given action in the game, handling challenges and blocks.
     * This method updates the game state based on the executed action,
     * including the handling of challenges and blocks.
     * It also updates the visit count of all the nodes in the tree.
     * @param game The current state of the game, used to access the current players and game context.
     * @param action The action executed in the game that determines the new root node.
     * @param isChallenged A boolean value indicating whether the action is being challenged.
     * @param isBlocked A boolean value indicating whether the action is being blocked.
     */
    private void executeAction(Game game, Action action, boolean isChallenged, boolean isBlocked) {
        Player currentPlayer = game.getCurrentPlayer();
        Player targetPlayer = game.getOpponent(currentPlayer);

        if (!handleChallenge(game, action, isChallenged, currentPlayer)) {
            return;
        }

        if (!handleBlock(game, action, isBlocked, currentPlayer)) {
            return;
        }

        List<Card> cards = null;
        if (action.getActionCode() == ActionCode.SWAP) {
            List<Card> newCards = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                if (!game.getDeck().isEmpty()) {
                    newCards.add(game.getDeck().getCard());
                }
            }
            List<Card> swapOptions = new ArrayList<>(currentPlayer.getCards());
            swapOptions.addAll(newCards);
            List<Card> selectedCards = currentPlayer.selectRandomCardsToKeep(swapOptions);
            cards = new ArrayList<>();
            cards.addAll(selectedCards);
            cards.addAll(newCards);
        } else if (action.getActionCode() == ActionCode.COUP || action.getActionCode() == ActionCode.ASSASSINATE) {
            assert targetPlayer != null;
            if (!targetPlayer.getCards().isEmpty()) {
                Card cardToLose = targetPlayer.getCards().get(0);
                cards = new ArrayList<>();
                cards.add(cardToLose);
            }
        }

        game.executeAction(action, cards);
        if (!game.isGameOver()) {
            game.switchTurns();
        } else {
            handleGameOver(game.getPlayers().get(0));
        }
    }
}