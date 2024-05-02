package com.example.demo6.View;

import com.example.demo6.Controller.GameController;
import com.example.demo6.Model.Actions.Action;
import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Game;
import com.example.demo6.Model.Player;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.*;
import java.util.stream.Collectors;

public class GameView extends Application {
    private VBox gameContent;
    private Map<String, HBox> playerCardsMap = new HashMap<>();
    private Label cardStackCountLabel;
    private int cardStackCount;
    private VBox cardStackArea;
    private ComboBox<Action> actionsComboBox;
    private Button newGameButton;
    private Player currentPlayer;
    private GameController controller;
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes the game view and sets up the initial game state.
     *
     * @param primaryStage the primary stage for this application,
     *                         used to display the application's user interface.
     */
    @Override
    public void start(Stage primaryStage) {
        // Main horizontal box that will contain everything
        HBox mainContent = new HBox(10);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(15, 12, 15, 12));
        // VBox for player info and actions
        gameContent = new VBox(10);
        gameContent.setAlignment(Pos.CENTER);
        createTurnTable();
        createNewGameButton();
        // VBox for the card stack area
        cardStackArea = new VBox(10);
        cardStackArea.setAlignment(Pos.CENTER_RIGHT);
        // Add gameContent and cardStackArea to mainContent
        mainContent.getChildren().addAll(gameContent, cardStackArea);
        // Use a BorderPane as the root for scene for flexibility
        BorderPane root = new BorderPane();
        // Set the HBox in the center of the BorderPane
        root.setCenter(mainContent);
        // Add control buttons to the left side of the BorderPane
        root.setLeft(createControlButtonsVBox());
        Scene scene = new Scene(root, 800, 600);
        String css = Objects.requireNonNull(this.getClass().getResource("/style.css")).toExternalForm();
        scene.getStylesheets().add(css);
        primaryStage.setTitle("Coup Game");
        primaryStage.setScene(scene);
        primaryStage.show();
        // Initialize the game controller and game state
        controller = new GameController(this, new Game(new Deck(EnumSet.allOf(Deck.CardType.class), Deck.NUMBER_OF_COPIES)));
        Platform.runLater(() -> controller.initializeGame());
    }

    /**
     * Sets the controller for the game.
     *
     * @param controller the controller to be set
     */
    public void setController(GameController controller) {
        this.controller = controller;
    }

    /**
     * This method ask the player if he wants to challenge an action.
     *
     * @param message the message to be displayed in the confirmation dialog
     * @return true if the player chooses to challenge, false otherwise
     */
    public boolean promptForChallenge(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Challenge");
        alert.setHeaderText(null); // No header
        alert.setContentText(message);
        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeYes;
    }

    /**
     * This method ask the player if he wants to block an action.
     *
     * @param message the message to be displayed in the confirmation dialog
     * @return true if the player chooses to block, false otherwise
     */
    public boolean promptForBlock(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Block Action");
        alert.setHeaderText(null); // No header text
        alert.setContentText(message);
        ButtonType buttonTypeYes = new ButtonType("Block");
        ButtonType buttonTypeNo = new ButtonType("Do Not Block");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeYes;
    }


    /**
     * This method asks the player which cards they want to keep.
     * If the user cancels the selection dialog, this method returns the number of cards that were not yet selected
     * randomly or based on the remaining cards available for selection.
     *
     * @param options the list of available cards
     * @param numberOfCardsToSelect the number of cards the player can select
     * @return a list of selected cards
     */
    public List<Card> promptForCardSelection(List<Card> options, int numberOfCardsToSelect) {
        List<String> choices = options.stream().map(Card::toString).collect(Collectors.toList());
        List<Card> selectedCards = new ArrayList<>();

        while (selectedCards.size() < numberOfCardsToSelect && !choices.isEmpty()) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
            dialog.setTitle("Select Cards");
            dialog.setHeaderText("Select a card:");
            dialog.setContentText("Available cards:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String selected = result.get();
                // Add the corresponding Card object to the selectedCards list
                options.stream()
                        .filter(card -> card.toString().equals(selected))
                        .findFirst()
                        .ifPresent(card -> {
                            selectedCards.add(card);
                            choices.remove(selected); // Remove the selected item from the choices list
                        });
            } else {
                // User canceled the selection, add the remaining needed cards randomly or based on your criteria
                int remainingCards = numberOfCardsToSelect - selectedCards.size();
                List<Card> remainingOptions = options.stream()
                        .filter(card -> !selectedCards.contains(card))
                        .collect(Collectors.toList());
                Collections.shuffle(remainingOptions); // Shuffle to randomize or apply your own logic here
                selectedCards.addAll(remainingOptions.subList(0, Math.min(remainingCards, remainingOptions.size())));
                return selectedCards;
            }
        }
        return selectedCards;
    }



    /**
     * This method ask the player which card he wants to give up.
     *
     * @param player the player whose card will be selected
     * @return the selected card, or null if the player cancels the operation
     */
    public Card promptPlayerForCardToGiveUp(Player player) {
        List<String> cardChoices = player.getCards().stream()
                .map(Card::toString)
                .collect(Collectors.toList());
        ChoiceDialog<String> dialog = new ChoiceDialog<>(cardChoices.get(0), cardChoices);
        dialog.setTitle("Select a Card to Give Up");
        dialog.setHeaderText("You need to give up a card.");
        dialog.setContentText("Choose a card:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            for (Card card : player.getCards()) {
                if (card.toString().equals(result.get())) {
                    return card;
                }
            }
        }
        return null;
    }

    /**
     * This method updates the player info in the view.
     * It updates the coins and the cards in the view.
     *
     * @param players the list of players whose information will be updated in the view
     */
    public void updatePlayerInfo(List<Player> players) {
        Platform.runLater(() -> {
            gameContent.getChildren().removeIf(node -> node instanceof VBox && node.getStyleClass().contains("player-area"));
            players.forEach(player -> {
                List<String> cardImages;
                if (player.equals(controller.getGame().getAIPlayer())) {
                    cardImages = player.getCards().stream()
                            .map(card -> "Screenshot_15.png")
                            .collect(Collectors.toList());
                } else {
                    cardImages = player.getCards().stream()
                            .map(this::getCardImage)
                            .collect(Collectors.toList());
                }
                createPlayerArea(player, cardImages);
            });
        });
    }


    /**
     * Updates the current player and calls the updateTurnTable method.
     *
     * @param currentPlayer the new current player
     */
    public void updateCurrentPlayer(Player currentPlayer) {
        Platform.runLater(() -> {
            this.currentPlayer = currentPlayer;
            updateTurnTable();
        });
    }

    /**
     * Updates the available actions in the view.
     *
     * @param availableActions the list of available actions
     */
    public void updateAvailableActions(List<Action> availableActions) {
        Platform.runLater(() -> {
            gameContent.getChildren().removeIf(node -> node instanceof HBox && node.getStyleClass().contains("actions-box"));
            HBox actionsBox = new HBox(10);
            actionsBox.setAlignment(Pos.CENTER);
            actionsBox.getStyleClass().add("actions-box");
            actionsComboBox = new ComboBox<>();
            actionsComboBox.setPromptText("Choose an action");
            actionsComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Action action, boolean empty) {
                    super.updateItem(action, empty);
                    setText(empty ? null : action.actionCodeToString());
                }
            });
            actionsComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Action action, boolean empty) {
                    super.updateItem(action, empty);
                    setText(empty ? null : action.actionCodeToString());
                }
            });
            actionsComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Action action) {
                    return action == null ? null : action.actionCodeToString();
                }

                @Override
                public Action fromString(String actionName) {
                    return null;
                }
            });
            actionsComboBox.getItems().addAll(availableActions);
            actionsComboBox.setOnAction(event -> Platform.runLater(() -> {
            Action selectedAction = actionsComboBox.getValue();
            if (selectedAction != null) {
                controller.executeAction(selectedAction);
                actionsComboBox.setValue(null);
            }
            }));
            actionsBox.getChildren().add(actionsComboBox);
            gameContent.getChildren().add(actionsBox);
        });
    }

    /**
     * Updates the turn label in the view.
     */
    private void updateTurnTable() {
        Platform.runLater(() -> {
            Label turnLabel = (Label) gameContent.lookup(".turn-label");
            if (turnLabel != null) {
                turnLabel.setText(currentPlayer.getName() + "'s turn");
            }
        });
    }

    /**
     * This method create the turn label in the view.
     */
    private void createTurnTable() {
        Label turnLabel = new Label("Turn: ");
        turnLabel.setFont(new Font("Arial", 24));
        turnLabel.getStyleClass().add("turn-label");
        VBox.setMargin(turnLabel, new Insets(10, 10, 10, 10));
        turnLabel.setAlignment(Pos.CENTER);
        gameContent.getChildren().add(turnLabel);
    }

    /**
     * This method create the Player area in the view.
     *
     * @param player the player whose information will be displayed
     * @param cardImages the list of images representing the player's cards
     */
    private void createPlayerArea(Player player, List<String> cardImages) {
        VBox playerArea = new VBox(10);
        playerArea.setAlignment(Pos.CENTER);
        playerArea.getStyleClass().add("player-area");

        // Create a label for the player's name
        Label nameLabel = new Label(player.getName());
        nameLabel.setFont(new Font("Arial", 20));

        // Create a horizontal box for the player's cards
        HBox cardsArea = new HBox(10);
        cardsArea.setAlignment(Pos.CENTER);

        // Add the player's cards to the cards area
        for (String cardImageName : cardImages) {
            ImageView cardView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/demo6/" + cardImageName))));
            cardView.setFitHeight(100);
            cardView.setFitWidth(70);
            cardsArea.getChildren().add(cardView);
        }

        // Create a label for the player's coins
        Label coinsLabel = new Label("Coins: " + player.getCoins());
        coinsLabel.setFont(new Font("Arial", 16));
        coinsLabel.getStyleClass().add("coins-label");

        // Add the player's name, cards, and coins to the player area
        playerCardsMap.put(player.getName(), cardsArea);
        playerArea.getChildren().addAll(nameLabel, cardsArea, coinsLabel);

        // Add the player area to the game content
        gameContent.getChildren().addAll(playerArea);
    }

    /**
     * This method create the Deck area in the view.
     *
     * @param deck the deck to be displayed
     */
    public void createCardStackArea(Deck deck) {
        cardStackArea.getChildren().clear();
        Image cardStackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/demo6/Stack.png")));
        ImageView cardStackImageView = new ImageView(cardStackImage);
        cardStackImageView.setFitHeight(200);
        cardStackImageView.setFitWidth(150);
        cardStackImageView.setPreserveRatio(true);
        cardStackCount = deck.getSize();
        cardStackCountLabel = new Label("Number of cards: " + cardStackCount);
        cardStackCountLabel.setAlignment(Pos.CENTER);
        cardStackArea.getChildren().addAll(cardStackImageView, cardStackCountLabel);
    }

    /**
     * Updates the deck information in the view.
     *
     * @param deck the deck to be displayed
     */
    public void updateDeckInfo(Deck deck) {
        Platform.runLater(() -> {
            int numberOfRemainingCards = deck.getSize();
            cardStackCountLabel.setText("Number of cards: " + numberOfRemainingCards);
        });
    }

    /**
     * Creates a VBox with control buttons.
     *
     * @return the VBox containing the control buttons
     */
    private VBox createControlButtonsVBox() {
        // Create a 'Show Rules' button
        Button showRulesButton = new Button("Show Rules");
        showRulesButton.setFont(new Font("Arial", 16));
        showRulesButton.setOnAction(event -> {
            // Display the rules in an alert dialog
            Alert rulesAlert = new Alert(Alert.AlertType.INFORMATION);
            rulesAlert.setTitle("Game Rules");
            rulesAlert.setHeaderText("Rules of the Game");
            rulesAlert.setContentText("1. Income: Take one coin from the bank. This cannot be Challenged or Blocked.\n" +
                    "2. Foreign Aid: Take two coins from the bank. This cannot be Challenged but it can be Blocked by the Duke.\n" +
                    "3. Coup: Costs seven coins. Cause a player to give up an Influence card. Cannot be Challenged or Blocked. If you start your turn with 10+ coins, you must take this action.\n" +
                    "4. Taxes (the Duke): Take three coins from the bank. Can be Challenged.\n" +
                    "5. Assassinate (the Assassin): Costs three coins. Force one player to give up an Influence card of their choice. Can be Challenged. Can be Blocked by the Contessa.\n" +
                    "6. Steal (the Captain): Take two coins from another player. Can be Challenged. Can be Blocked by another Captain or an Ambassador.\n" +
                    "7. Swap Influence (the Ambassador): Draw two Influence cards from the deck, look at them and mix them with your current Influence card(s). Place two cards back in the deck and shuffle the deck. Can be Challenged. Cannot be Blocked.\n\n" +
                    "Blocking: If another player takes an action that can be Blocked, any other player may Block it by claiming to have the proper character on one of their Influence cards. The acting player cannot perform the action and takes no other action this turn. The acting player MAY choose to Challenge the Blocking player. If they win the Challenge, the action goes through as normal.\n\n" +
                    "Challenge: When the acting player declares their action, any other player may Challenge their right to take the action. They are saying “I don't believe you have the proper character to do that.” The acting player now must prove they have the power to take the action or lose the Challenge. If they have the right character, they reveal it and place the revealed card back in the deck. They then shuffle the deck and draw a new card. The Challenging player has lost the Challenge. If they do NOT have the proper character, they lose the Challenge.");
            rulesAlert.showAndWait();
        });

        // Create 'New Game' button
        Button newGameButton = createNewGameButton();
        Button revealAICardsButton = createRevealAICardsButton();
        VBox controlButtons = new VBox(10); // Spacing of 10 between buttons
        controlButtons.getChildren().addAll(showRulesButton, newGameButton,revealAICardsButton);
        controlButtons.setAlignment(Pos.BOTTOM_LEFT); // Align the buttons to the bottom left

        // Add padding or any additional styling as needed
        controlButtons.setPadding(new Insets(10));

        return controlButtons;
    }

    /**
     * Creates a 'New Game' button.
     *
     * @return the 'New Game' button
     */
    private Button createNewGameButton() {
        newGameButton = new Button("New Game");
        newGameButton.setFont(new Font("Arial", 16));
        newGameButton.setOnAction(event -> {
            // Reset the game state and initialize a new game
            controller = new GameController(this, new Game(new Deck(EnumSet.allOf(Deck.CardType.class), Deck.NUMBER_OF_COPIES)));
            Platform.runLater(() -> {
                controller.initializeGame();
            });
        });
        // Return the button instead of adding it directly to the gameContent
        return newGameButton;
    }

    private Button createRevealAICardsButton() {
        Button revealAICardsButton = new Button("Reveal AI Cards");
        revealAICardsButton.setFont(new Font("Arial", 16));
        revealAICardsButton.setOnAction(event -> {
            // Get the AI player
            Player aiPlayer = controller.getGame().getAIPlayer();
            Player humanPlayer = controller.getGame().getHumanPlayer();
            // Map cards to images, assuming aiPlayer is always available and valid
            List<String> cardImagesAI = aiPlayer.getCards().stream()
                    .map(this::getCardImage)
                    .toList();
            List<String> cardImagesHuman = humanPlayer.getCards().stream().map(this::getCardImage).toList();
            Platform.runLater(() -> {
                // Clear and recreate the AI player's card area
                // Remove all nodes from gameContent that are player areas, then recreate them
                gameContent.getChildren().removeIf(node -> node instanceof VBox && node.getStyleClass().contains("player-area"));
                createPlayerArea(humanPlayer, cardImagesHuman); // Recreate the human player area with the revealed cards
                createPlayerArea(aiPlayer, cardImagesAI); // Recreate the AI player area with the revealed cards
            });
        });
        return revealAICardsButton;
    }



    /**
     * Displays the winner in the alert box.
     *
     * @param winner the winner of the game
     */
    public void displayWinner(Player winner) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText("The game is over. The winner is " + winner.getName() + "!");

            alert.showAndWait();
        });
    }

    /**
     * Displays a message in an alert box.
     *
     * @param message the message to be displayed
     */
    public void displayMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }


    /**
     * Returns the name of the image file to be used for the specified card.
     *
     * @param card the card for which the image file name is to be obtained
     * @return the name of the image file to be used for the specified card
     */
    private String getCardImage(Card card) {
        Map<Deck.CardType, String> cardImageMap = new HashMap<>();
        cardImageMap.put(Deck.CardType.DUKE, "duke.png");
        cardImageMap.put(Deck.CardType.ASSASSIN, "assassin.png");
        cardImageMap.put(Deck.CardType.CAPTAIN, "captain.png");
        cardImageMap.put(Deck.CardType.AMBASSADOR, "ambassador.png");
        cardImageMap.put(Deck.CardType.CONTESSA, "contessa.png");
        return cardImageMap.getOrDefault(card.getType(), "screen_.png");
    }

    /**
     * Disables or enables the controls in the game view.
     *
     * @param disable true to disable the controls, false to enable them
     */
    public void setControlsDisable(boolean disable) {
        Platform.runLater(() -> {
            if (actionsComboBox != null) {
                actionsComboBox.setDisable(disable);
            }
            if (newGameButton != null) {
                newGameButton.setDisable(disable);
            }
        });
    }
}