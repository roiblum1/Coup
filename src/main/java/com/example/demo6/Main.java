package com.example.demo6;

import com.example.demo6.Controller.GameController;
import com.example.demo6.Model.Game;
import com.example.demo6.View.GameView;
import javafx.application.Application;

public class Main {
    GameController gameController;
    public static void main(String[] args) {
        Application.launch(GameView.class, args);
    }
}
