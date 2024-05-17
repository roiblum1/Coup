package com.example.demo6.AI;

import com.example.demo6.Model.Actions.Action;
import com.example.demo6.Model.Actions.ActionCode;
import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Game;
import com.example.demo6.Model.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.example.demo6.Model.Deck.CardType.*;

public class Heuristic {

    /**
     * Selects an action for the AI to perform heuristically. The method uses a simplified
     * decision-making process based on the current game state, without exhaustive search or
     * randomness typically involved in Monte Carlo Tree Search (MCTS).
     * It analyzes the available actions and current game situation, such as the number of coins
     * and cards the AI and the human player have, to determine the most advantageous move.
     * The AI prioritizes actions that could lead to immediate victory, maximizes coin gain,
     * or sets up a stronger position for subsequent turns.
     *
     * @param availableActions List of actions that the AI can currently take.
     * @param game The current state of the game, which includes both AI and human players' statuses.
     * @return The selected Action that the AI will perform.
     */
    static Action selectActionHeuristically(List<Action> availableActions, Game game) {
        Player aiPlayer = game.getCurrentPlayer();
        Player humanPlayer = game.getOpponent(aiPlayer);
        int aiPlayerCoins = aiPlayer.getCoins();
        int humanPlayerCoins = humanPlayer.getCoins();
        int humanPlayerCardCount = humanPlayer.getCards().size();

        // Immediate winning moves: If human player has 1 card, prioritize COUP or ASSASSINATE if possible.
        if (aiPlayerCoins >= 7) {
            return availableActions.stream()
                    .filter(action -> action.getActionCode() == ActionCode.COUP)
                    .findFirst()
                    .orElse(null);
        }

        if (aiPlayerCoins >= 3 && aiPlayer.hasCard(ASSASSIN)) { // Assassinate if possible.
            return availableActions.stream()
                    .filter(action -> action.getActionCode() == ActionCode.ASSASSINATE)
                    .findFirst()
                    .orElse(null);
        }

        // Use Duke to collect taxes if available to maximize coin gain safely.
        if (aiPlayer.hasCard(DUKE)) {
            return availableActions.stream()
                    .filter(action -> action.getActionCode() == ActionCode.TAX)
                    .findFirst()
                    .orElse(null);
        }

        // Use Captain to steal if the human player has coins and the AI has the Captain.
        if (aiPlayer.hasCard(CAPTAIN) && humanPlayerCoins > 0) {
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
        }

        if(humanPlayerCardCount ==  1 && aiPlayer.getCoins() > 3)
        {
            return availableActions.stream()
                    .filter(action -> action.getActionCode() == ActionCode.ASSASSINATE)
                    .findFirst()
                    .orElse(null);
        }
        Random random = new Random();
        double probability = random.nextDouble();
        if(probability < 0.8) {
            return availableActions.stream()
                    .filter(action -> action.getActionCode() == ActionCode.INCOME)
                    .findFirst()
                    .orElse(null);
        }
        else {
            return availableActions.get(random.nextInt(availableActions.size()));
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
    public static boolean simulateChallenge(Game game, Action action) {
        // Check if the current player is the AI player
        if (game.getCurrentPlayer().equals(game.getAIPlayer())) {
            Player aiPlayer = game.getCurrentPlayer();
            Player humanPlayer = game.getOpponent(aiPlayer);
            // Always challenge if the AI has only one card left and the human player is performing an Assassinate action,
            // and the AI does not have a Contessa to block it. This is to avoid losing the game.
            if (aiPlayer.getCards().size() == 1 && action.getActionCode() == ActionCode.ASSASSINATE && !aiPlayer.hasCard(CONTESSA)) {
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
                return !aiPlayer.hasCard(DUKE);
            }
            // If none of the specific conditions are met, do not challenge.
            Random random = new Random();
            double probability = random.nextDouble();
            return probability < 0.3;
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
    public static boolean simulateBlock(Game game, Action action) {
        // Check if the current player is the AI player
        if (game.getCurrentPlayer().getName().equals(game.getAIPlayer().getName())) {
            Player aiPlayer = game.getCurrentPlayer();

            // If AI has only one card left and the human player is performing an Assassinate action,
            // always block to avoid losing the game, provided the AI has a Contessa.
            if (aiPlayer.getCards().size() == 1 && action.getActionCode() == ActionCode.ASSASSINATE) {
                return aiPlayer.hasCard(CONTESSA);
            }

            // Block Foreign Aid if the AI has a Duke, since a Duke allows blocking Foreign Aid.
            if (action.getActionCode() == ActionCode.FOREIGN_AID && aiPlayer.hasCard(DUKE)) {
                return true;
            }

            // Block Steal if the AI has a Captain or Ambassador, as these characters can block Stealing.
            if (action.getActionCode() == ActionCode.STEAL && (aiPlayer.hasCard(CAPTAIN) || aiPlayer.hasCard(AMBASSADOR))) {
                return true;
            }

            // Block Assassinate if the AI has a Contessa, which can block an Assassinate action.
            return action.getActionCode() == ActionCode.ASSASSINATE && aiPlayer.hasCard(CONTESSA);

            // If none of the conditions apply, do not block.
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
    public static boolean simulateBlockChallenge(Game game, Action action) {
        // Determine if the current player is the AI player
        if (game.getCurrentPlayer().getName().equals(game.getAIPlayer().getName())){
            Player aiPlayer = game.getCurrentPlayer();
            // Avoid challenging if the AI player is at a high risk (only one card left)
            if (aiPlayer.getCards().size() == 1) {
                return false;
            }
            // Decide whether to challenge the block based on the action being blocked
            switch (action.getActionCode()) {
                case FOREIGN_AID:
                    // Challenge if blocking a FOREIGN AID with a Duke seems unlikely
                    return Heuristic.isSuspiciousBlock(DUKE, aiPlayer);
                case ASSASSINATE:
                    // Challenge if blocking an ASSASSINATE with a Contessa seems unlikely
                    return isSuspiciousBlock(Deck.CardType.CONTESSA, aiPlayer);
                case STEAL:
                    // Challenge if blocking a STEAL with an Ambassador or Captain seems unlikely
                    boolean ambassadorSuspicious = isSuspiciousBlock(Deck.CardType.AMBASSADOR, aiPlayer);
                    boolean captainSuspicious = isSuspiciousBlock(CAPTAIN, aiPlayer);
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
    public static boolean isSuspiciousBlock(Deck.CardType requiredCard, Player aiPlayer) {
        List<Card> aiPlayerCards = aiPlayer.getCards();
        long countInAIHand = aiPlayerCards.stream().filter(card -> card.getType() == requiredCard).count();
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
    public static List<Card> selectCardsToKeep(Game game, Player player, List<Card> newCards) {
        List<Card> allCards = new ArrayList<>(player.getCards());
        allCards.addAll(newCards);
        allCards.sort(Comparator.comparingInt(Heuristic::getCardValue).reversed());
        return allCards.subList(0, 2); // Keeping the two most valuable cards
    }

    /**
     * Selects a card to give up during a swap action.
     * If the player has only one card, it returns that card directly.
     * Otherwise, it uses a stream to find the card with the minimum value according to the {@link #getCardValue} method.
     * @param game the current game state
     * @param player the player whose card is to be selected
     * @return the selected card to give up
     */
    public static Card selectCardToGiveUp(Game game, Player player) {
        return player.getCards().stream()
                .min(Comparator.comparingInt(Heuristic::getCardValue))
                .orElse(null);
    }


    /**
     * Evaluates the current position of a player in the game.
     * The evaluation is based on the number of cards, coins, and the value of the cards held by the player.
     * @param player the player whose position is to be evaluated
     * @return the score of the player's current position
     */
    static int evaluatePosition(Player player) {
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
    private static int getCardValue(Card card) {
        return switch (card.getType()) {
            case DUKE -> 5;
            case ASSASSIN -> 4;
            case CAPTAIN -> 3;
            case AMBASSADOR -> 2;
            case CONTESSA -> 1;
        };
    }
}
