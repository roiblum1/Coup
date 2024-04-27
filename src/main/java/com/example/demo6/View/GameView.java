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

        Scene scene = new Scene(root, 800, 600);
        String css = Objects.requireNonNull(this.getClass().getResource("/style.css")).toExternalForm();
        scene.getStylesheets().add(css);
        primaryStage.setTitle("Coup Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize the game controller and game state
        controller = new GameController(this, new Game(new Deck(EnumSet.allOf(Deck.CardType.class), 3)));
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
     * This method ask the player which cards he wants to keep.
     *
     * @param options the list of available cards
     * @param numberOfCardsToSelect the number of cards the player can select
     * @return a list of selected cards
     */
    public List<Card> promptForCardSelection(List<Card> options, int numberOfCardsToSelect) {
        List<String> choices = options.stream().map(Card::toString).collect(Collectors.toList());
        List<Card> selectedCards = new ArrayList<>();
        for (int i = 0; i < numberOfCardsToSelect; i++) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
            dialog.setTitle("Select Cards");
            dialog.setHeaderText("Select a card:");
            dialog.setContentText("Available cards:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(cardDescription -> {
                for (Card card : options) {
                    if (card.toString().equals(cardDescription)) {
                        selectedCards.add(card);
                        choices.remove(cardDescription);
                        break;
                    }
                }
            });
            if (result.isEmpty()) break;
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
                if (players.indexOf(player) == 1) { // Assuming index 1 is always the opponent
                    cardImages = player.getCards().stream()
                            .map(card -> "Screenshot_15.png") // Use the back image for all cards of the opponent
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
            ComboBox<Action> actionsComboBox = new ComboBox<>();
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

    //* This method create the Player area in the view. */
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

    //* This method create the Deck area in the view. */
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

    //* This method updates the deck information in the view. */
    public void updateDeckInfo(Deck deck) {
        Platform.runLater(() -> {
            int numberOfRemainingCards = deck.getSize();
            cardStackCountLabel.setText("Number of cards: " + numberOfRemainingCards);
        });
    }

    private void createNewGameButton() {
        Button newGameButton = new Button("New Game");
        newGameButton.setFont(new Font("Arial", 16));
        newGameButton.setOnAction(event -> {
            // Reset the game state and initialize a new game
            controller = new GameController(this, new Game(new Deck(EnumSet.allOf(Deck.CardType.class), 2)));
            Platform.runLater(() -> {
                controller.initializeGame();
            });
        });

        gameContent.getChildren().add(newGameButton);
    }

    private void animateCardReveal(Player player, ImageView cardView) {
        TranslateTransition translate = new TranslateTransition(Duration.seconds(0.5), cardView);
        translate.setFromX(-200);  // Start from left (adjust as needed)
        translate.setToX(0);       // End at the card's final position

        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), cardView);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition animation = new ParallelTransition(translate, fade);
        animation.setOnFinished(event -> {
            // Update any necessary state or perform further actions after the animation completes
        });
        animation.play();
    }
    private void animateCoinUpdate(Label coinsLabel, int oldCount, int newCount) {
        Duration duration = Duration.millis(500);  // Total duration of the animation
        int steps = Math.abs(newCount - oldCount); // Calculate the number of steps to increment or decrement

        // Use an array to hold the mutable integer
        final int[] count = {oldCount};

        Timeline timeline = new Timeline();
        timeline.setCycleCount(steps);
        timeline.setAutoReverse(false);

        KeyFrame keyFrame = new KeyFrame(duration.divide(steps), event -> {
            if (newCount > oldCount) {
                count[0]++;  // Increment coin count
            } else {
                count[0]--;  // Decrement coin count
            }
            coinsLabel.setText("Coins: " + count[0]);  // Update the label text
        });

        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
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


    //* Returns the name of the image file to be used for the specified card. */
    private String getCardImage(Card card) {
        Map<Deck.CardType, String> cardImageMap = new HashMap<>();
        cardImageMap.put(Deck.CardType.DUKE, "duke.png");
        cardImageMap.put(Deck.CardType.ASSASSIN, "assassin.png");
        cardImageMap.put(Deck.CardType.CAPTAIN, "captain.png");
        cardImageMap.put(Deck.CardType.AMBASSADOR, "ambassador.png");
        cardImageMap.put(Deck.CardType.CONTESSA, "contessa.png");
        return cardImageMap.getOrDefault(card.getType(), "screen_.png");
    }
}