package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.db.DatabaseManager;
import model.dao.UserDAO;
import model.service.SessionService;
import model.service.RunService;
import javafx.scene.layout.BorderPane;
import view.controller.GameController;

public class Main extends Application {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1000;
    private static final boolean DEV_SKIP_TO_SUDOKU = false;

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.getInstance().initializeDatabase();
        try {
            Scene scene;
            if (DEV_SKIP_TO_SUDOKU) {
                String nick = "dev";
                UserDAO userDAO = new UserDAO(DatabaseManager.getInstance());
                var user = userDAO.getUserByNick(nick);
                if (user == null) {
                    userDAO.createUser(nick);
                }
                SessionService.setCurrentNick(nick);

                RunService runService = new RunService();
                runService.startNewRun();

                FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/view/GameView.fxml"));
                BorderPane gameRoot = gameLoader.load();
                GameController controller = gameLoader.getController();
                controller.setRunService(runService);
                scene = new Scene(gameRoot, WIDTH, HEIGHT);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Nickname.fxml"));
                javafx.scene.Parent root = loader.load();
                scene = new Scene(root, WIDTH, HEIGHT);
            }
            String cssPath = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            primaryStage.setTitle("Puzzle Rogue - Darkest Sudoku");
            primaryStage.setScene(scene);
            primaryStage.initStyle(javafx.stage.StageStyle.DECORATED);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
            primaryStage.show();
            primaryStage.setFullScreen(true);
        } catch (Exception e) {
            System.err.println("Unable to load Nickname.fxml: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
