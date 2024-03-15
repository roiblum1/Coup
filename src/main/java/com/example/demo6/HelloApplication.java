package com.example.demo6;

import com.example.demo6.Model.Actions.Action;
import com.example.demo6.Model.Card;
import com.example.demo6.Model.Deck;
import com.example.demo6.Model.Game;
import com.example.demo6.Model.Player;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HelloApplication extends Application {
    private VBox gameContent;
    private Map<String, HBox> playerCardsMap = new HashMap<>();
    private Label cardStackCountLabel;
    private int cardStackCount;
    private VBox cardStackArea;

    private Game game;
    private Player currentPlayer;
    private Player opponent;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        HBox root = new HBox(20);
        root.setAlignment(Pos.CENTER);

        gameContent = new VBox(20);
        gameContent.setAlignment(Pos.CENTER);

        // Initialize the game and players
        Set<Deck.CardType> allCardTypes = EnumSet.allOf(Deck.CardType.class);
        game = new Game(new Deck(allCardTypes, 2)); // Assuming 3 copies of each card
        currentPlayer = new Player("Player 1");
        opponent = new Player("Player 2");
        game.addPlayer(currentPlayer);
        game.addPlayer(opponent);
        updatePlayerInfo();

        // Create player areas for both players
        createPlayerArea(opponent, Arrays.asList(getCardImage(opponent.getCards().get(0)), getCardImage(opponent.getCards().get(1))));
        createPlayerArea(currentPlayer, Arrays.asList(getCardImage(currentPlayer.getCards().get(0)), getCardImage(currentPlayer.getCards().get(1))));

        createTurnTable(game.getCurrentPlayer().getName() + "'s turn");

        List<Action> currentPlayerActions = game.getPossibleActions(currentPlayer);
        addPossibleAction(currentPlayer, currentPlayerActions);

        cardStackArea = createCardStackArea(game.getDeck());
        root.getChildren().addAll(gameContent, cardStackArea);

        HBox.setHgrow(gameContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 800, 650);
        primaryStage.setTitle("Coup Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void updateTurnTable() {
        Label turnLabel = (Label) gameContent.lookup(".turn-label");
        if (turnLabel != null) {
            turnLabel.setText(game.getCurrentPlayer().getName() + "'s turn");
        }
    }


    private void createTurnTable(String text) {
        Label turnLabel = new Label(text);
        turnLabel.setFont(new Font("Arial", 24));
        turnLabel.getStyleClass().add("turn-label");
        VBox.setMargin(turnLabel, new Insets(20, 0, 20, 0));
        gameContent.getChildren().addAll(turnLabel);
    }

    private void createPlayerArea(Player player, List<String> cardImages) {
        VBox playerArea = new VBox(10);
        playerArea.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(player.getName());
        nameLabel.setFont(new Font("Arial", 20));

        HBox cardsArea = new HBox(10);
        cardsArea.setAlignment(Pos.CENTER);

        for (String cardImageName : cardImages) {
            ImageView cardView = new ImageView(new Image("C:\\Users\\Roi Blum\\IdeaProjects\\demo6\\src\\main\\resources\\com\\example\\demo6\\" + cardImageName));
            cardView.setFitHeight(100);
            cardView.setFitWidth(70);
            cardsArea.getChildren().add(cardView);
        }

        Label coinsLabel = new Label("Coins: " + player.getCoins());
        coinsLabel.setFont(new Font("Arial", 16));
        coinsLabel.getStyleClass().add("coins-label"); // Add the CSS class to the label

        playerCardsMap.put(player.getName(), cardsArea);

        playerArea.getChildren().addAll(nameLabel, cardsArea, coinsLabel);
        gameContent.getChildren().addAll(playerArea);
    }

    public void addPossibleAction(Player player, List<Action> actions) {
        ComboBox<String> actionsComboBox = new ComboBox<>();
        actionsComboBox.getItems().addAll(actions.stream().map(Action::getNameOfAction).collect(Collectors.toList()));
        actionsComboBox.setPromptText("Choose an action");
        actionsComboBox.setOnAction(event -> {
            String selectedActionName = actionsComboBox.getValue();
            if (selectedActionName != null) {
                Action selectedAction = actions.stream()
                        .filter(action -> action.getNameOfAction().equals(selectedActionName))
                        .findFirst()
                        .orElse(null);
                if (selectedAction != null) {
                    System.out.println("Player selected: " + selectedActionName);
                    performAction(selectedAction);
                    // Reset the ComboBox selection later
                    Platform.runLater(() -> actionsComboBox.setValue(null));
                }
            }
        });
        gameContent.getChildren().add(actionsComboBox);
    }

    private void performAction(Action action) {
        action.execute();
        game.switchTurns();
        updatePlayerInfo();
        updateActionButtons();
        updateTurnTable();
    }

    private VBox createCardStackArea(Deck deck) {
        cardStackArea = new VBox();
        cardStackArea.setAlignment(Pos.CENTER);

        Image cardStackImage = new Image("C:\\Users\\Roi Blum\\IdeaProjects\\demo6\\src\\main\\resources\\com\\example\\demo6\\" + "Stack.png");
        ImageView cardStackImageView = new ImageView(cardStackImage);
        cardStackImageView.setFitHeight(200);
        cardStackImageView.setFitWidth(150);
        cardStackImageView.setPreserveRatio(true);

        cardStackCount = deck.getSize();
        cardStackCountLabel = new Label("Number of cards: " + cardStackCount);

        cardStackArea.getChildren().addAll(cardStackImageView, cardStackCountLabel);
        return cardStackArea;
    }

    public void switchCard(String playerName, String newCardImageName) {
        if (cardStackCount > 0) {
            cardStackCount--;
            cardStackCountLabel.setText("Number of cards: " + cardStackCount);

            HBox playerCards = playerCardsMap.get(playerName);
            if (playerCards != null && !playerCards.getChildren().isEmpty()) {
                ImageView oldCardView = (ImageView) playerCards.getChildren().get(0);
                ImageView newCardView = new ImageView(new Image("C:\\Users\\Roi Blum\\IdeaProjects\\demo6\\src\\main\\resources\\com\\example\\demo6\\" + newCardImageName));
                newCardView.setFitHeight(100);
                newCardView.setFitWidth(70);

                // Create a fade out transition for the old card
                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), oldCardView);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);

                fadeOut.setOnFinished(event -> {
                    // Replace the old card with the new one after the fade out is complete
                    playerCards.getChildren().set(0, newCardView);

                    // Create a fade in transition for the new card
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(500), newCardView);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });

                fadeOut.play();
            } else {
                System.out.println(playerName + " has no cards to switch.");
            }
        } else {
            System.out.println("No cards left in the stack to switch.");
        }
    }

    public void removeCard(String playerName, String cardToRemove) {
        HBox cardsArea = playerCardsMap.get(playerName);
        if (cardsArea != null) {
            List<Node> toRemove = cardsArea.getChildren().stream()
                    .filter(node -> {
                        if (node instanceof ImageView) {
                            ImageView imageView = (ImageView) node;
                            String imageUrl = imageView.getImage().getUrl();
                            return imageUrl.endsWith(cardToRemove);
                        }
                        return false;
                    })
                    .toList();

            if (!toRemove.isEmpty()) {
                for (Node node : toRemove) {
                    FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), node);
                    fadeTransition.setFromValue(1); // Start fully opaque
                    fadeTransition.setToValue(0); // Fade to fully transparent
                    fadeTransition.setOnFinished(event -> cardsArea.getChildren().remove(node));
                    fadeTransition.play();
                }
            }
        }
    }

    public boolean askToBlockAction() {
        final boolean[] answer = {false};
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Block Action");
            alert.setHeaderText("Do you want to block the action?");

            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.getItems().addAll("Yes", "No");

            VBox content = new VBox();
            content.getChildren().add(comboBox);
            alert.getDialogPane().setContent(content);

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK && "Yes".equals(comboBox.getValue())) {
                    answer[0] = true;
                }
            });
        });
        return answer[0];
    }

    private void updatePlayerInfo() {
        // Update player information labels
        for (Player player : game.getPlayers()) {
            HBox playerCards = playerCardsMap.get(player.getName());
            if (playerCards != null) {
                // Update player's card images
                playerCards.getChildren().clear();
                for (Card card : player.getCards()) {
                    ImageView cardView = new ImageView(new Image("C:\\Users\\Roi Blum\\IdeaProjects\\demo6\\src\\main\\resources\\com\\example\\demo6\\" + getCardImage(card)));
                    cardView.setFitHeight(100);
                    cardView.setFitWidth(70);
                    playerCards.getChildren().add(cardView);
                }

                // Update player's coin count
                VBox playerArea = (VBox) playerCards.getParent();
                for (Node node : playerArea.getChildren()) {
                    if (node instanceof Label) {
                        Label label = (Label) node;
                        if (label.getText().startsWith("Coins:")) {
                            label.setText("Coins: " + player.getCoins());
                            break;
                        }
                    }
                }
            }
        }
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

    private void updateActionButtons() {
        // Update action buttons based on the current player's available actions
        List<Action> currentPlayerActions = game.getPossibleActions(currentPlayer);

        // Clear existing action buttons
        Node actionsNode = gameContent.lookup(".actions-box");
        if (actionsNode instanceof HBox) {
            HBox actionsBox = (HBox) actionsNode;
            actionsBox.getChildren().clear();

            // Add new action buttons
            for (Action action : currentPlayerActions) {
                Button actionButton = new Button(action.getNameOfAction());
                actionButton.setOnAction(event -> {
                    action.execute();
                    updatePlayerInfo();
                    updateActionButtons();
                });
                actionsBox.getChildren().add(actionButton);
            }
        }
    }
    public void addNewCard(String playerName, String newCardImageName) {
        if (cardStackCount > 0) {
            HBox playerCards = playerCardsMap.get(playerName);
            if (playerCards != null) {
                // Check if the player already has two cards
                if (playerCards.getChildren().size() >= 2) {
                    System.out.println("Player " + playerName + " already has too many cards.");
                    return;
                }

                // Decrease the number of cards in the card stack area
                cardStackCount--;
                cardStackCountLabel.setText("Number of cards: " + cardStackCount);

                // Create the new card ImageView
                ImageView newCardView = new ImageView(new Image("C:\\Users\\Roi Blum\\source\\javaInt\\demo6\\src\\main\\resources\\com\\example\\demo6\\" + newCardImageName));
                newCardView.setFitHeight(100);
                newCardView.setFitWidth(70);
                newCardView.setPreserveRatio(true);
                newCardView.setOpacity(0); // Start fully transparent

                // Add the new card to the player's hand
                playerCards.getChildren().add(newCardView);

                // Create and play the fade-in animation
                FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), newCardView);
                fadeTransition.setFromValue(0); // Start from transparent
                fadeTransition.setToValue(1); // Fade to fully opaque
                fadeTransition.play();
            }
        } else {
            System.out.println("No cards left in the stack to add.");
        }
    }
}