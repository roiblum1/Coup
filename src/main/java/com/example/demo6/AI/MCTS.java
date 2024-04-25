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
    private Player aiPlayer, humanPlayer;


    /**
     * Constructs a new instance of the Monte Carlo Tree Search (MCTS) class.
     *
     * @param game The initial game state used to initialize the MCTS tree.
     * @param numOfSimulations The number of simulations to be performed during the MCTS process.
     * @param maxDepth The maximum depth of the MCTS tree.
     */
    public MCTS(Game game, int numOfSimulations, int maxDepth) {
        this.rootGame = game.deepCopy(); // Creates a deep copy of the initial game state.
        this.root = new Node(null); // Initializes the root node of the MCTS tree.
        this.numOfSimulations = numOfSimulations; // Sets the number of simulations to be performed during the MCTS process.
        this.maxDepth = maxDepth; // Sets the maximum depth of the MCTS tree.
        this.aiPlayer = game.getPlayers().get(1); // Identifies the AI player in the game.
        this.humanPlayer = game.getPlayers().get(0); // Identifies the human player in the game.
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
    public Action bestMove() {
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
                System.out.println(child.getAction().getCodeOfAction() + ": Visit Count = " + child.getVisitCount()
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
            System.out.println("Created child node for action: " + action.getCodeOfAction());
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

                    System.out.println("Rollout action: " + action.getCodeOfAction());

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
            return selectActionHeuristically(availableActions, game);
        } else { // If the player is simulated as a human
            // Select a random action to simulate unpredictability
            return availableActions.get(ThreadLocalRandom.current().nextInt(availableActions.size()));
        }
    }

    private boolean handleChallenge(Game game, Action action, boolean isChallenged, Player currentPlayer) {
        if (action.canBeChallenged && isChallenged) {
            if (!action.challenge()) {
                if (currentPlayer.getName().equals(game.getPlayers().get(1).getName())) {
                    Card card = selectCardToGiveUp(game, currentPlayer);
                    currentPlayer.returnCard(card);
                }
                else {
                    currentPlayer.loseRandomInfluence();
                }
                // The challenging player loses influence if the challenge fails
                return false; // Action fails if the challenge is successful
            } else {
                if (currentPlayer.getName().equals(game.getPlayers().get(1).getName())) {
                    currentPlayer.loseRandomInfluence();
                }
                else {
                    Card card = selectCardToGiveUp(game, currentPlayer);
                    currentPlayer.returnCard(card);
                }
            }
        }
        return true; // Continue with the action if no challenge or if challenge failed
    }
    private boolean handleBlock(Game game, Action action, boolean isBlocked, Player currentPlayer) {
        if (action.canBeBlocked && isBlocked) {
            if (!simulateBlockChallenge(game, action)) {
                currentPlayer.loseRandomInfluence(); // The blocking player loses influence if the block challenge fails
                return false; // Action does not proceed if block is successful
            } else {
                game.getOpponent(currentPlayer).loseRandomInfluence();
            }
        }
        return true; // Continue with the action if no block or if block failed
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
        }

        if (aiPlayerCoins >= 3 && aiPlayerCards.contains(Deck.CardType.ASSASSIN)) { // Assassinate if possible.
            return availableActions.stream()
                    .filter(action -> action.getActionCode() == ActionCode.ASSASSINATE)
                    .findFirst()
                    .orElse(null);
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


            // Log the backpropagation process for debugging and insight into tree development
            if (node.getAction() != null) {
                System.out.println("Backpropagation: Action = " + node.getAction().getCodeOfAction() + ", Visit Count = " + node.getVisitCount() + ", Reward = " + node.getReward());
            } else {
                System.out.println("Backpropagation: Action = null, Visit Count = " + node.getVisitCount() + ", Reward = " + node.getReward());
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

    private void executeAction(Game game, Action action, boolean isChallenged, boolean isBlocked) {
        Player currentPlayer = game.getCurrentPlayer();
        Player targetPlayer = game.getOpponent(currentPlayer);

        if (isChallenged) {
            if (!action.challenge()) {
                currentPlayer.loseRandomInfluence();
                return;
            }
        }

        if (isBlocked) {
            if (simulateBlockChallenge(game, action)) {
                if (targetPlayer != null) {
                    targetPlayer.loseRandomInfluence();
                }
            } else {
                return;
            }
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
        } else if (action.getActionCode() == ActionCode.COUP || action.getActionCode() == ActionCode.ASSASSINATE ) {
            assert targetPlayer != null;
            if (!targetPlayer.getCards().isEmpty()) {
                Card cardToLose = targetPlayer.getCards().get(0);
                cards = new ArrayList<>();
                cards.add(cardToLose);
            }
        }

        game.executeAction(action, cards);
        System.out.println("The executed action is : " + action.getActionCode() + " the player " +action.getPlayer().getName());
        if(!game.isGameOver()) {
            game.switchTurns();
        }
    }

    /**
     * Simulates the decision to challenge an action during the Monte Carlo Tree Search (MCTS).
     * This method evaluates whether the AI should challenge an opponent's action based on strategic considerations,
     * such as the likelihood of the opponent possessing a certain card and the criticality of the situation
     * regarding the AI's card holdings and the game state.
     *
     * @param game The current state of the game, used to access players and their cards.
     * @param action The action being considered for a challenge by the opponent.
     * @return true if the action should be challenged, false otherwise.
     */
    public boolean simulateChallenge(Game game, Action action) {
        // Check if the current player is the AI player
        if (game.getCurrentPlayer().equals(game.getPlayers().get(1))) {
            Player aiPlayer = game.getCurrentPlayer();
            Player humanPlayer = game.getOpponent(aiPlayer);
            // Always challenge if the AI has only one card left and the human player is performing an Assassinate action,
            // and the AI does not have a Contessa to block it. This is to avoid losing the game.
            if (aiPlayer.getCards().size() == 1 && action.getActionCode() == ActionCode.ASSASSINATE && !aiPlayer.hasCard(Deck.CardType.CONTESSA)) {
                return true;
            }
            // Avoid challenging if the AI has only one card left unless it's a critical situation,
            // as losing a challenge could mean losing the game.
            if (aiPlayer.getCards().size() == 1 && humanPlayer.getCards().size() > 1) {
                return false;
            }
            // Challenge actions like Assassinate or Coup if they could directly lead to the AI's loss,
            // especially if the AI's number of cards is less than or equal to the human player's.
            if (action.getActionCode() == ActionCode.ASSASSINATE || action.getActionCode() == ActionCode.COUP) {
                return aiPlayer.getCards().size() <= humanPlayer.getCards().size();
            }
            // Challenge a Tax action if the AI suspects the human player might not have a Duke,
            // especially if the AI itself does not have a Duke.
            if (action.getActionCode() == ActionCode.TAX) {
                return !aiPlayer.getCards().contains(Deck.CardType.DUKE);
            }
            // If none of the specific conditions are met, do not challenge.
            return false;
        } else {
            // For the human player, simulate a random decision to challenge with a 50% probability.
            // This adds an element of unpredictability to the human player's strategy in the simulation.
            return Math.random() < 0.5;
        }
    }


    /**
     * Simulates the decision to block an action during the Monte Carlo Tree Search (MCTS).
     * This method determines whether the AI should block an action based on the current state of the game
     * and the specific action being taken by the opponent.
     * The decision to block is based on strategic considerations, such as the cards the AI currently holds
     * and the type of action being attempted by the opponent.
     *
     * @param game The current state of the game, used to access the current player and game context.
     * @param action The action attempted by the opponent, which the AI considers blocking.
     * @return true if the action should be blocked, false otherwise.
     */
    public boolean simulateBlock(Game game, Action action) {
        // Check if the current player is the AI player
        if (game.getCurrentPlayer().getName().equals(aiPlayer.getName())) {
            Player aiPlayer = game.getCurrentPlayer();

            // If AI has only one card left and the human player is performing an Assassinate action,
            // always block to avoid losing the game, provided the AI has a Contessa.
            if (aiPlayer.getCards().size() == 1 && action.getActionCode() == ActionCode.ASSASSINATE) {
                return aiPlayer.getCards().contains(Deck.CardType.CONTESSA);
            }

            // Block Foreign Aid if the AI has a Duke, since a Duke allows blocking Foreign Aid.
            if (action.getActionCode() == ActionCode.FOREIGN_AID && aiPlayer.getCards().contains(Deck.CardType.DUKE)) {
                return true;
            }

            // Block Steal if the AI has a Captain or Ambassador, as these characters can block Stealing.
            if (action.getActionCode() == ActionCode.STEAL && (aiPlayer.getCards().contains(Deck.CardType.CAPTAIN) || aiPlayer.getCards().contains(Deck.CardType.AMBASSADOR))) {
                return true;
            }

            // Block Assassinate if the AI has a Contessa, which can block an Assassinate action.
            if (action.getActionCode() == ActionCode.ASSASSINATE && aiPlayer.getCards().contains(Deck.CardType.CONTESSA)) {
                return true;
            }

            // If none of the conditions apply, do not block.
            return false;
        } else {
            // For a human player, simulate a random decision to block with a 50% probability.
            // This randomness reflects uncertainty in human decision-making in the simulation.
            return Math.random() < 0.5;
        }
    }



    /**
     * Simulates the decision to challenge a block during the Monte Carlo Tree Search (MCTS).
     * This method determines whether the AI should challenge a block based on strategic considerations.
     * The AI challenges a block if it deems the opponent's claim to have the blocking card as unlikely.
     * This function is particularly cautious if the AI is at risk of losing, making more conservative decisions
     * when the AI has only one card left.
     *
     * @param game The current state of the game, used to access current players and game context.
     * @param action The action being blocked, which influences the decision process.
     * @return true if the block should be challenged, false otherwise.
     */
    public boolean simulateBlockChallenge(Game game, Action action) {
        // Determine if the current player is the AI player
        if (game.getCurrentPlayer().getName().equals(aiPlayer.getName())){
            Player aiPlayer = game.getCurrentPlayer();
            // Avoid challenging if the AI player is at a high risk (only one card left)
            if (aiPlayer.getCards().size() == 1) {
                return false;
            }
            // Decide whether to challenge the block based on the action being blocked
            switch (action.getActionCode()) {
                case FOREIGN_AID:
                    // Challenge if blocking a FOREIGN AID with a Duke seems unlikely
                    return isSuspiciousBlock(Deck.CardType.DUKE, aiPlayer);
                case ASSASSINATE:
                    // Challenge if blocking an ASSASSINATE with a Contessa seems unlikely
                    return isSuspiciousBlock(Deck.CardType.CONTESSA, aiPlayer);
                case STEAL:
                    // Challenge if blocking a STEAL with an Ambassador or Captain seems unlikely
                    boolean ambassadorSuspicious = isSuspiciousBlock(Deck.CardType.AMBASSADOR, aiPlayer);
                    boolean captainSuspicious = isSuspiciousBlock(Deck.CardType.CAPTAIN, aiPlayer);
                    return ambassadorSuspicious || captainSuspicious;

                default:
                    // Do not challenge by default if the action is not one of the specified blockable actions
                    return false;
            }
        } else {
            // For a human player, simulate a random decision to block with a 50% probability.
            // This randomness reflects uncertainty in human decision-making in the simulation.
            return Math.random() < 0.5;
        }
    }


    /**
     * Function to check if blocking is suspicious based on the rarity of cards.
     * This function is used to determine if a block action by the opponent is likely or not.
     * It returns true if the blocking action seems suspicious, and false otherwise.
     * @param requiredCard the card that the opponent is suspected of blocking
     * @param aiPlayer the player whose hand is being analyzed
     * @return true if the blocking action seems suspicious, false otherwise
     */
    public boolean isSuspiciousBlock(Deck.CardType requiredCard, Player aiPlayer) {
        List<Card> aiPlayerCards = aiPlayer.getCards();
        long countInAIHand = aiPlayerCards.stream().filter(card -> card.getName().equals(requiredCard.getName())).count();
        long totalInGame = 2;  // Total copies of each card type in the game
        long totalPossible = totalInGame - countInAIHand;
        return totalPossible < 1;  // Less than 1 means the opponent unlikely to have a card
    }

    /**
     * Selects a card to give up during a swap action.
     * If the player has only one card, it returns that card directly.
     * Otherwise, it uses a stream to find the card with the minimum value according to the {@link #getCardValue} method.
     * @param game the current game state
     * @param player the player whose card is to be selected
     * @return the selected card to give up
     */
    public List<Card> selectCardsToKeep(Game game, Player player, List<Card> newCards) {
        List<Card> allCards = new ArrayList<>(player.getCards());
        allCards.addAll(newCards);
        allCards.sort(Comparator.comparingInt(this::getCardValue).reversed());
        return allCards.subList(0, 2);
    }

    /**
     * Selects a card to give up during a swap action.
     * If the player has only one card, it returns that card directly.
     * Otherwise, it uses a stream to find the card with the minimum value according to the {@link #getCardValue} method.
     * @param game the current game state
     * @param player the player whose card is to be selected
     * @return the selected card to give up
     */
    public Card selectCardToGiveUp(Game game, Player player) {
        List<Card> cards = player.getCards();
        // If the player has only one card, return that card directly
        if (cards.size() == 1) {
            return cards.get(0);
        }
        // Use stream to find the card with the minimum value according to the getCardValue method
        return cards.stream().min(Comparator.comparingInt(this::getCardValue)).orElse(null);
    }


    /**
     * Evaluates the current position of a player in the game.
     * The evaluation is based on the number of cards, coins, and the value of the cards held by the player.
     * @param player the player whose position is to be evaluated
     * @return the score of the player's current position
     */
    private int evaluatePosition(Player player) {
        int score = 0;
        // Add points for each card held by the player
        score += player.getCards().size() * 5;
        // Add points for each coin possessed by the player
        score += player.getCoins();
        // Add bonus points for valuable cards based on their value
        for (Card card : player.getCards()) {
            score += getCardValue(card);
        }
        return score;
    }

    /**
     * Assigns values to each card based on the game strategy.
     * Higher values indicate more valuable cards.
     *
     * @param card the card to evaluate
     * @return the value of the card
     */
    private int getCardValue(Card card) {
        switch (Deck.CardType.getTypeCard(card)) {
            case DUKE:
                return 5;
            case ASSASSIN:
                return 4;
            case CAPTAIN:
                return 3;
            case AMBASSADOR:
                return 2;
            case CONTESSA:
                return 1;
            default:
                return 0;
        }
    }
}