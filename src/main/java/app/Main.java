package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import view.controller.GameController;
import java.io.IOException;

/**
 * Classe di avvio dell'applicazione JavaFX. 
 */
public class Main extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GameView.fxml"));
            BorderPane root = loader.load();
            
            GameController controller = loader.getController();

            Scene scene = new Scene(root, WIDTH, HEIGHT);
            
            String cssPath = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            primaryStage.setTitle("Puzzle Rogue - Darkest Sudoku");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(WIDTH);
            primaryStage.setMinHeight(HEIGHT);
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Impossibile caricare GameView.fxml o le sue risorse.");
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}