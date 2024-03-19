package com.example.demo6.View;

import com.example.demo6.Controller.GameController;
import com.example.demo6.Model.Actions.Action;
import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Game;
import com.example.demo6.Model.Player;
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
import javafx.util.StringConverter;

import java.util.*;
import java.util.stream.Collectors;

public class GameView extends Application {
    private VBox gameContent;
    private Map<String, HBox> playerCardsMap = new HashMap<>();
    private Label cardStackCountLabel;
    private int cardStackCount;
    private VBox cardStackArea;
    private Player currentPlayer;
    private GameController controller;
    public static void main(String[] args) {
        launch(args);
    }

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
        // VBox for the card stack area
        cardStackArea = new VBox(10);
        cardStackArea.setAlignment(Pos.CENTER_RIGHT);

        // Add gameContent and cardStackArea to mainContent
        mainContent.getChildren().addAll(gameContent, cardStackArea);

        // Use a BorderPane as the root for scene for flexibility
        BorderPane root = new BorderPane();
        root.setCenter(mainContent);  // Set the HBox in the center of the BorderPane

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Coup Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize the game controller and game state
        controller = new GameController(this, new Game(new Deck(EnumSet.allOf(Deck.CardType.class), 3)));
        Platform.runLater(() -> controller.initializeGame());
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    public boolean promptForChallenge(String message) {
        // Create a confirmation alert dialog.
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Challenge");
        alert.setHeaderText(null); // No header
        alert.setContentText(message);

        // Customize the button text.
        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        // Show the alert and wait for the user's response.
        Optional<ButtonType> result = alert.showAndWait();

        // Return true if the user clicks "Yes", indicating they want to challenge.
        return result.isPresent() && result.get() == buttonTypeYes;
    }

    public boolean promptForBlock(String message) {
        // Create a confirmation alert dialog for blocking.
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Block Action");
        alert.setHeaderText(null); // No header text
        alert.setContentText(message);

        // Customize the button text.
        ButtonType buttonTypeYes = new ButtonType("Block");
        ButtonType buttonTypeNo = new ButtonType("Do Not Block");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        // Show the alert and wait for the user's response.
        Optional<ButtonType> result = alert.showAndWait();

        // Return true if the user clicks "Block", indicating they want to block the action.
        return result.isPresent() && result.get() == buttonTypeYes;
    }
    public List<Card> promptForCardSelection(Player player, int numberOfCards) {
        // Fetch the list of cards from the player
        List<Card> cards = player.getCards();

        // Map the Card objects to their string representations for display
        List<String> choices = cards.stream().map(Card::toString).collect(Collectors.toList());

        // List to hold the selected Card objects
        List<Card> selectedCards = new ArrayList<>();

        for (int i = 0; i < numberOfCards; i++) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
            dialog.setTitle("Select Cards");
            dialog.setHeaderText("Select a card:");
            dialog.setContentText("Available cards:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(cardDescription -> {
                // Find the selected Card object based on the selection string
                for (Card card : cards) {
                    if (card.toString().equals(cardDescription)) {
                        selectedCards.add(card);
                        // Remove the selected card from the list of choices to avoid reselection
                        choices.remove(cardDescription);
                        break;
                    }
                }
            });
            // If the user closes the dialog without making a selection, break the loop to prevent further selections
            if (result.isEmpty()) break;
        }
        return selectedCards;
    }

    public Card promptPlayerForCardToGiveUp(Player player) {
        // Convert the player's cards to a list of string representations
        List<String> cardChoices = player.getCards().stream()
                .map(Card::toString)
                .collect(Collectors.toList());

        // Create and configure the choice dialog
        ChoiceDialog<String> dialog = new ChoiceDialog<>(cardChoices.get(0), cardChoices);
        dialog.setTitle("Select a Card to Give Up");
        dialog.setHeaderText("You need to give up a card.");
        dialog.setContentText("Choose a card:");

        // Show the dialog and capture the result
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            // Find the Card object corresponding to the chosen string representation
            for (Card card : player.getCards()) {
                if (card.toString().equals(result.get())) {
                    return card; // Return the selected card
                }
            }
        }

        // If no card was selected (dialog was canceled), handle accordingly
        // This might mean returning null or throwing an exception depending on your game's rules
        return null; // or throw new SomeException("A card must be selected.");
    }

    public void updatePlayerInfo(List<Player> players) {
        Platform.runLater(() -> {
            // Clear the existing player information
            gameContent.getChildren().removeIf(node -> node instanceof VBox && node.getStyleClass().contains("player-area"));

            // Create updated player areas for each player
            for (Player player : players) {
                List<String> cardImages = player.getCards().stream()
                        .map(this::getCardImage)
                        .collect(Collectors.toList());
                createPlayerArea(player, cardImages);
            }
        });
    }

    public void updateCurrentPlayer(Player currentPlayer) {
        Platform.runLater(() -> {
            this.currentPlayer = currentPlayer;
            updateTurnTable();
        });
    }

    public void updateAvailableActions(List<Action> availableActions) {
        Platform.runLater(() -> {
            gameContent.getChildren().removeIf(node -> node instanceof HBox && node.getStyleClass().contains("actions-box"));

            HBox actionsBox = new HBox(10);
            actionsBox.setAlignment(Pos.CENTER);
            actionsBox.getStyleClass().add("actions-box");

            ComboBox<Action> actionsComboBox = new ComboBox<>();
            actionsComboBox.setPromptText("Choose an action");

            // Set a cell factory to use for rendering the action names in the dropdown list
            actionsComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Action action, boolean empty) {
                    super.updateItem(action, empty);
                    setText(empty ? null : action.getNameOfAction());
                }
            });

            // Similarly, set the button cell for displaying the selected item
            actionsComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Action action, boolean empty) {
                    super.updateItem(action, empty);
                    setText(empty ? null : action.getNameOfAction());
                }
            });

            // Provide a StringConverter to correctly handle the conversion between the string representation and the Action object
            actionsComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Action action) {
                    return action == null ? null : action.getNameOfAction();
                }

                @Override
                public Action fromString(String actionName) {
                    // This method is not needed for the ComboBox's functionality in this context
                    return null;
                }
            });

            actionsComboBox.getItems().addAll(availableActions);
            actionsComboBox.setOnAction(event -> {
                Action selectedAction = actionsComboBox.getValue();
                if (selectedAction != null) {
                    controller.executeAction(selectedAction);
                    actionsComboBox.setValue(null); // Reset the selection
                }
            });

            actionsBox.getChildren().add(actionsComboBox);
            gameContent.getChildren().add(actionsBox);
        });
    }

    private void updateTurnTable() {
        Platform.runLater(() -> {
            Label turnLabel = (Label) gameContent.lookup(".turn-label");
            if (turnLabel != null) {
                turnLabel.setText(currentPlayer.getName() + "'s turn");
            }
        });
    }


    private void createTurnTable() {
        Label turnLabel = new Label("Turn: ");
        turnLabel.setFont(new Font("Arial", 24));
        turnLabel.getStyleClass().add("turn-label"); // Adding a style class for potential CSS styling
        VBox.setMargin(turnLabel, new Insets(10, 10, 10, 10));
        turnLabel.setAlignment(Pos.CENTER);

        gameContent.getChildren().add(turnLabel); // Assuming gameContent is your main VBox container
    }


    private void createPlayerArea(Player player, List<String> cardImages) {
        VBox playerArea = new VBox(10);
        playerArea.setAlignment(Pos.CENTER);
        playerArea.getStyleClass().add("player-area");

        Label nameLabel = new Label(player.getName());
        nameLabel.setFont(new Font("Arial", 20));

        HBox cardsArea = new HBox(10);
        cardsArea.setAlignment(Pos.CENTER);

        for (String cardImageName : cardImages) {
            ImageView cardView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/demo6/" + cardImageName))));
            cardView.setFitHeight(100);
            cardView.setFitWidth(70);
            cardsArea.getChildren().add(cardView);
        }

        Label coinsLabel = new Label("Coins: " + player.getCoins());
        coinsLabel.setFont(new Font("Arial", 16));
        coinsLabel.getStyleClass().add("coins-label");

        playerCardsMap.put(player.getName(), cardsArea);

        playerArea.getChildren().addAll(nameLabel, cardsArea, coinsLabel);
        gameContent.getChildren().addAll(playerArea);
    }

    public void createCardStackArea(Deck deck) {
        // cardStackArea is a member variable of type VBox
        cardStackArea.getChildren().clear();  // Clear it first in case it's being re-initialized

        Image cardStackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/demo6/Stack.png")));
        ImageView cardStackImageView = new ImageView(cardStackImage);
        cardStackImageView.setFitHeight(200);
        cardStackImageView.setFitWidth(150);
        cardStackImageView.setPreserveRatio(true);

        cardStackCount = deck.getSize();
        cardStackCountLabel = new Label("Number of cards: " + cardStackCount);
        cardStackCountLabel.setAlignment(Pos.CENTER); // Center the label

        cardStackArea.getChildren().addAll(cardStackImageView, cardStackCountLabel);
    }

    // This method updates the deck information in the view.
    public void updateDeckInfo(Deck deck) {
        Platform.runLater(() -> {
            int numberOfRemainingCards = deck.getSize();
            cardStackCountLabel.setText("Number of cards: " + numberOfRemainingCards);
        });
    }


    public void displayWinner(Player winner) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText("The game is over. The winner is " + winner.getName() + "!");

            alert.showAndWait();
        });
    }

    private String getCardImage(Card card) {
        // Map card names to image file names
        Map<String, String> cardImageMap = new HashMap<>();
        cardImageMap.put("Duke", "duke.png");
        cardImageMap.put("Assassin", "assassin.png");
        cardImageMap.put("Captain", "captain.png");
        cardImageMap.put("Ambassador", "ambassador.png");
        cardImageMap.put("Contessa", "contessa.png");
        return cardImageMap.getOrDefault(card.getName(), "s.png");
    }
}