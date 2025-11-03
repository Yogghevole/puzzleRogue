package it.unibo.samplejavafx.base;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        final Label message = new Label("Puzzle Rogue");
        StackPane root = new StackPane(message);
        stage.setTitle("Puzzle Rogue");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    public static void run(String[] args) {
        launch(args);
    }
}