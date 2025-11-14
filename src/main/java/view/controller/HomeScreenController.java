package view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Scene;
import model.service.GameDataService;
import model.service.RunService;

public class HomeScreenController {
    
    @FXML private VBox mainContainer;
    @FXML private Button continueButton;
    @FXML private Button newExpeditionButton;
    @FXML private Button reliquaryButton;
    @FXML private ImageView settingsIcon;
    
    private GameDataService gameDataService;
    private RunService runService;
    
    @FXML
    public void initialize() {
        gameDataService = new GameDataService();
        runService = new RunService();
        
        setupUI();
        checkContinueButtonState();
        setupButtonListeners();
    }
    
    private void setupUI() {
        String imagePath = "/assets/menu/home_screen.png";
        Image backgroundImage = new Image(getClass().getResourceAsStream(imagePath));
        BackgroundImage background = new BackgroundImage(
            backgroundImage,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(100, 100, true, true, false, true)
        );
        mainContainer.setBackground(new Background(background));
        String settingsIconPath = "/assets/icons/utils/settings.png";
        Image settingsImage = new Image(getClass().getResourceAsStream(settingsIconPath));
        settingsIcon.setImage(settingsImage);
        settingsIcon.setFitWidth(48);
        settingsIcon.setFitHeight(48);
        settingsIcon.setPreserveRatio(true);
    }
    
    private void checkContinueButtonState() {
        boolean hasActiveRun = resumeLastRun();
        
        if (hasActiveRun) {
            continueButton.setDisable(false);
            continueButton.setStyle("-fx-opacity: 1.0; -fx-text-fill: #FFFFFF;");
        } else {
            continueButton.setDisable(true);
            continueButton.setStyle("-fx-opacity: 0.5; -fx-text-fill: #808080;");
        }
    }
    
    private void setupButtonListeners() {
        continueButton.setOnAction(event -> {
            if (!continueButton.isDisable()) {
                handleContinueExpedition();
            }
        });

        newExpeditionButton.setOnAction(event -> {
            newExpeditionButton.setStyle(newExpeditionButton.getStyle() + "; -fx-scale-x: 0.95; -fx-scale-y: 0.95;");
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                javafx.application.Platform.runLater(() -> {
                    newExpeditionButton.setStyle(newExpeditionButton.getStyle().replace("; -fx-scale-x: 0.95; -fx-scale-y: 0.95;", ""));
                    handleNewExpedition();
                });
            }).start();
        });

        reliquaryButton.setOnAction(event -> handleReliquary());
        settingsIcon.setOnMouseClicked(event -> handleSettings());
    }
    private boolean resumeLastRun() {
        try {
            return gameDataService.hasActiveRun();
        } catch (Exception e) {
            System.err.println("Errore nel controllo run attiva: " + e.getMessage());
            return false;
        }
    }

    private void startNewRun() {
        try {
            runService.startNewRun();
            navigateToGameView();
        } catch (Exception e) {
            System.err.println("Errore nell'avvio di una nuova run: " + e.getMessage());
        }
    }
    
    private void navigateToReliquary() {
        System.out.println("Navigazione verso il Reliquiario...");
    }
    
    private void showSettingsMenu() {
        System.out.println("Apertura menu impostazioni...");
    }

    private void handleContinueExpedition() {
        System.out.println("Ripresa spedizione in corso...");
        boolean success = resumeLastRun();
        if (success) {
            navigateToGameScreen();
        }
    }
    
    private void handleNewExpedition() {
        System.out.println("Avvio nuova spedizione...");
        startNewRun();
    }
    
    private void handleReliquary() {
        navigateToReliquary();
    }
    
    private void handleSettings() {
        showSettingsMenu();
    }

    private void navigateToCharacterSelection() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/view/CharacterSelection.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Errore nella navigazione: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void navigateToGameScreen() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/view/GameView.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Errore nella navigazione al gioco: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void navigateToGameView() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/GameView.fxml"));
            javafx.scene.layout.BorderPane gameRoot = loader.load();
            GameController gameController = loader.getController();
            gameController.setRunService(runService);
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            Scene gameScene = new Scene(gameRoot, stage.getWidth(), stage.getHeight());
            String cssPath = getClass().getResource("/style.css").toExternalForm();
            gameScene.getStylesheets().add(cssPath);
            
            stage.setScene(gameScene);
            
        } catch (java.io.IOException e) {
            System.err.println("Errore nella navigazione alla GameView: " + e.getMessage());
            e.printStackTrace();
            showAlert("Errore", "Impossibile caricare la schermata di gioco.");
        }
    }
    
    private void showAlert(String title, String message) {
        System.out.println(title + ": " + message);
    }
}