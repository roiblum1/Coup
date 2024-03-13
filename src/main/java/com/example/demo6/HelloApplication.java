package com.example.demo6;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HelloApplication extends Application {
    private VBox gameContent;
    private Map<String, HBox> playerCardsMap = new HashMap<>();
    private Label cardStackCountLabel;
    private int cardStackCount;
    private VBox cardStackArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        HBox root = new HBox(20);
        root.setAlignment(Pos.CENTER);

        gameContent = new VBox(20);
        gameContent.setAlignment(Pos.CENTER);

        createPlayerArea("AI", 2, Arrays.asList("Screenshot_15.png", "Screenshot_15.png"));
        createTurnTable("Player 1's turn");
        createPlayerArea("Player 1", 3, Arrays.asList("Screenshot_16.png", "Screenshot_16.png"));

        addPossibleAction(Arrays.asList("Action 1", "Action 2", "Action 3"));

        cardStackArea = createCardStackArea("Stack.png", 15);
        root.getChildren().addAll(gameContent, cardStackArea);

        HBox.setHgrow(gameContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 800, 650);
        primaryStage.setTitle("Coup Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createTurnTable(String text) {
        Label turnLabel = new Label(text);
        turnLabel.setFont(new Font("Arial", 24));
        VBox.setMargin(turnLabel, new Insets(20, 0, 20, 0));
        gameContent.getChildren().addAll(turnLabel);
    }

    private void createPlayerArea(String playerName, int coinCount, List<String> cardImages) {
        VBox playerArea = new VBox(10);
        playerArea.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(playerName);
        nameLabel.setFont(new Font("Arial", 20));

        HBox cardsArea = new HBox(10);
        cardsArea.setAlignment(Pos.CENTER);

        for (String cardImageName : cardImages) {
            ImageView cardView = new ImageView(new Image("C:\\Users\\Roi Blum\\IdeaProjects\\demo6\\src\\main\\resources\\com\\example\\demo6\\" + cardImageName));
            cardView.setFitHeight(100);
            cardView.setFitWidth(70);
            cardsArea.getChildren().add(cardView);
        }

        Label coinsLabel = new Label("Coins: " + coinCount);
        coinsLabel.setFont(new Font("Arial", 16));

        playerCardsMap.put(playerName, cardsArea);

        playerArea.getChildren().addAll(nameLabel, cardsArea, coinsLabel);
        gameContent.getChildren().addAll(playerArea);
    }

    public void addPossibleAction(List<String> actions) {
        ComboBox<String> actionsComboBox = new ComboBox<>();
        actionsComboBox.getItems().addAll(actions);
        actionsComboBox.setPromptText("Choose an action");
        actionsComboBox.setOnAction(event -> {
            String selectedAction = actionsComboBox.getValue();
            if (selectedAction != null) {
                System.out.println("Player selected: " + selectedAction);
                if (selectedAction.equals("Action 1")) {
                    removeCard("Player 1", "Screenshot_16.png");
                } else if (selectedAction.equals("Action 2")) {
                    addNewCard("Player 1", "Screenshot_16.png");
                }

                // Reset the ComboBox selection later
                Platform.runLater(() -> actionsComboBox.setValue(null));
            }
        });
        gameContent.getChildren().add(actionsComboBox);
    }

    private VBox createCardStackArea(String imagePath, int cardCount) {
        cardStackArea = new VBox();
        cardStackArea.setAlignment(Pos.CENTER);

        Image cardStackImage = new Image("C:\\Users\\Roi Blum\\IdeaProjects\\demo6\\src\\main\\resources\\com\\example\\demo6\\" + imagePath);
        ImageView cardStackImageView = new ImageView(cardStackImage);
        cardStackImageView.setFitHeight(200);
        cardStackImageView.setFitWidth(150);
        cardStackImageView.setPreserveRatio(true);

        cardStackCount = cardCount;
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
                ImageView newCardView = new ImageView(new Image("C:\\Users\\Roi Blum\\IdeaProjects\\demo6\\src\\main\\resources\\com\\example\\demo6\\" + newCardImageName));
                newCardView.setFitHeight(100);
                newCardView.setFitWidth(70);
                if (!playerCards.getChildren().isEmpty()) {
                    playerCards.getChildren().set(0, newCardView);
                } else {
                    System.out.println(playerName + " has no cards to switch.");
                }
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
                ImageView newCardView = new ImageView(new Image("C:\\Users\\Roi Blum\\IdeaProjects\\demo6\\src\\main\\resources\\com\\example\\demo6\\" + newCardImageName));
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