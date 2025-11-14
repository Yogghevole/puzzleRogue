package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import view.controller.GameController;
import model.service.RunService;
import model.dao.RunDAO;
import model.service.GameDataService;
import model.service.SudokuGenerator;
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GameView.fxml"));
            BorderPane root = loader.load();

            GameController controller = loader.getController();
            GameDataService gameDataService = new GameDataService(DatabaseManager.getInstance());
            RunDAO runDAO = new RunDAO(DatabaseManager.getInstance());
            SudokuGenerator sudokuGenerator = new SudokuGenerator(gameDataService);
            RunService runService = new RunService(runDAO, gameDataService, sudokuGenerator);
            controller.setRunService(runService);

            Scene scene = new Scene(root, WIDTH, HEIGHT);
            
            String cssPath = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            primaryStage.setTitle("Puzzle Rogue - Darkest Sudoku");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(WIDTH);
            primaryStage.setMinHeight(HEIGHT);
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Unable to load GameView.fxml or its resources.");
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
