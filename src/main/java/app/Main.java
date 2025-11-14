package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import model.db.DatabaseManager;

/**
 * Classe di avvio dell'applicazione JavaFX. 
 */
public class Main extends Application {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1000;

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            DatabaseManager.getInstance().initializeDatabase();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeScreenSimple.fxml"));
            javafx.scene.layout.VBox root = loader.load();
            
            Scene scene = new Scene(root, WIDTH, HEIGHT);
            
            String cssPath = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            String homeScreenCssPath = getClass().getResource("/home_screen.css").toExternalForm();
            if (homeScreenCssPath != null) {
                scene.getStylesheets().add(homeScreenCssPath);
            }
            
            primaryStage.setTitle("Puzzle Rogue - Darkest Sudoku");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Impossibile caricare HomeScreen.fxml o le sue risorse.");
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}