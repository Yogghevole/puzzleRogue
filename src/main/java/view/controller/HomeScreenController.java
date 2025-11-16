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
    @FXML private StackPane optionsModalContainer;
    
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
            if (runService == null) runService = new RunService();
            boolean ok = runService.resumeLastRun();
            if (ok && runService.getCurrentRun() != null && runService.getCurrentRun().getCurrentLevelState() == null) {
                runService.startLevel(1);
            }
            return ok;
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
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/AncestorsLegacy.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception e) {
            System.err.println("Errore apertura Legacy: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showSettingsMenu() {
        try {
            optionsModalContainer.getChildren().clear();
            optionsModalContainer.setVisible(true);
            AnchorPane panel = new AnchorPane();
            panel.setPrefSize(mainContainer.getWidth(), mainContainer.getHeight());
            javafx.scene.image.Image bg = new javafx.scene.image.Image(getClass().getResourceAsStream("/assets/icons/help/help.background.png"));
            javafx.scene.image.ImageView bgView = new javafx.scene.image.ImageView(bg);
            bgView.setFitWidth(mainContainer.getWidth());
            bgView.setFitHeight(mainContainer.getHeight());
            bgView.setPreserveRatio(false);
            panel.getChildren().add(bgView);

            Button closeButton = new Button("X");
            closeButton.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-font-size: 16; -fx-background-radius: 16;");
            AnchorPane.setTopAnchor(closeButton, 20.0);
            AnchorPane.setRightAnchor(closeButton, 20.0);
            closeButton.setOnAction(e -> { optionsModalContainer.setVisible(false); optionsModalContainer.getChildren().clear(); });
            panel.getChildren().add(closeButton);

            VBox content = new VBox(12.0);
            content.setAlignment(javafx.geometry.Pos.CENTER);
            Button logoutBtn = new Button("Esci dall'account");
            Button quitBtn = new Button("Esci dal gioco");
            logoutBtn.setOnAction(e -> { optionsModalContainer.setVisible(false); optionsModalContainer.getChildren().clear(); handleLogout(); });
            quitBtn.setOnAction(e -> handleQuit());
            content.getChildren().addAll(logoutBtn, quitBtn);
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
            panel.getChildren().add(content);

            optionsModalContainer.getChildren().add(panel);
        } catch (Exception ex) {
            System.err.println("Errore apertura menu impostazioni: " + ex.getMessage());
        }
    }

    private void handleContinueExpedition() {
        String lastChar = model.service.SessionService.getLastSelectedCharacter();
        if (lastChar != null && !lastChar.isEmpty()) {
            if (runService == null) runService = new RunService();
            boolean ok = runService.startNewRunWithCharacter(lastChar);
            if (ok) {
                navigateToGameView();
                return;
            }
        }
        boolean success = resumeLastRun();
        if (success) {
            navigateToGameScreen();
        } else {
            navigateToCharacterSelection();
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
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/GameView.fxml"));
            javafx.scene.layout.BorderPane gameRoot = loader.load();
            GameController gameController = loader.getController();
            gameController.setRunService(runService);
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            Scene gameScene = new Scene(gameRoot, stage.getWidth(), stage.getHeight());
            String cssPath = getClass().getResource("/style.css").toExternalForm();
            gameScene.getStylesheets().add(cssPath);
            stage.setScene(gameScene);
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

    private void handleLogout() {
        try {
            model.service.SessionService.clear();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/Nickname.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception e) {
            System.err.println("Errore nel logout: " + e.getMessage());
        }
    }

    private void handleQuit() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.close();
    }
}