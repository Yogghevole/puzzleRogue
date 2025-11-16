package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.db.DatabaseManager;

public class Main extends Application {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1000;

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.getInstance().initializeDatabase();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Nickname.fxml"));
            javafx.scene.Parent root = loader.load();
            Scene scene = new Scene(root, WIDTH, HEIGHT);
            String cssPath = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            primaryStage.setTitle("Puzzle Rogue - Darkest Sudoku");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Unable to load Nickname.fxml: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
