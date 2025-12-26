package view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import model.service.GameDataService;
import model.service.RunService;
import view.util.StageUtils;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import view.util.ModalUtils;

/**
 * Controller for the main menu screen.
 * Provides access to new runs, legacy upgrades, and settings.
 */
public class HomeScreenController {
    
    @FXML private StackPane mainContainer;
    @FXML private ImageView continueButton;
    @FXML private ImageView newExpeditionButton;
    @FXML private ImageView reliquaryButton;
    @FXML private ImageView settingsIcon;
    @FXML private StackPane optionsModalContainer;
    @FXML private javafx.scene.control.Label nickLabel;
    @FXML private javafx.scene.control.Label pointsLabel;
    
    private final view.manager.UserInfoManager userInfoManager = new view.manager.UserInfoManager();
    
    private GameDataService gameDataService;
    private RunService runService;
    
    @FXML
    public void initialize() {
        gameDataService = new GameDataService();
        runService = new RunService();
        
        setupUI();
        userInfoManager.setup(nickLabel, pointsLabel);
        checkContinueButtonState();
        setupButtonListeners();

        if (mainContainer.getScene() != null) {
            StageUtils.enforceFullscreen((Stage) mainContainer.getScene().getWindow());
        } else {
            mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) StageUtils.enforceFullscreen((Stage) newScene.getWindow());
            });
        }
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
        settingsIcon.setFitWidth(75);
        settingsIcon.setFitHeight(75);
        settingsIcon.setPreserveRatio(true);
        settingsIcon.setOnMouseEntered(e -> {
            Glow glow = new Glow(0.35);
            DropShadow shadow = new DropShadow();
            shadow.setRadius(6.0);
            shadow.setSpread(0.1);
            shadow.setColor(Color.web("#ffffff88"));
            settingsIcon.setEffect(new Blend(BlendMode.SRC_OVER, glow, shadow));
        });
        settingsIcon.setOnMouseExited(e -> settingsIcon.setEffect(null));
        
        addHoverEffect(continueButton);
        addHoverEffect(newExpeditionButton);
        addHoverEffect(reliquaryButton);

        try { view.manager.SoundManager.getInstance().playHomeMusic(); } catch (Exception ignore) {}
    }

    private void addHoverEffect(ImageView view) {
        view.setOnMouseEntered(e -> {
            if (!view.isDisable()) {
                Glow glow = new Glow(0.3);
                DropShadow shadow = new DropShadow();
                shadow.setRadius(15.0);
                shadow.setSpread(0.2);
                shadow.setColor(Color.web("#ffffff66"));
                view.setEffect(new Blend(BlendMode.SRC_OVER, glow, shadow));
                view.setScaleX(1.05);
                view.setScaleY(1.05);
            }
        });
        view.setOnMouseExited(e -> {
            view.setEffect(null);
            view.setScaleX(1.0);
            view.setScaleY(1.0);
        });
    }
    
    private void checkContinueButtonState() {
        boolean hasActiveRun = resumeLastRun();
        
        if (hasActiveRun) {
            continueButton.setDisable(false);
            continueButton.setOpacity(1.0);
        } else {
            continueButton.setDisable(true);
            continueButton.setOpacity(0.6);
        }
    }
    
    private void setupButtonListeners() {
        continueButton.setOnMouseClicked(event -> {
            if (!continueButton.isDisable()) {
                handleContinueExpedition();
            }
        });

        newExpeditionButton.setOnMouseClicked(event -> {
            handleNewExpedition();
        });

        reliquaryButton.setOnMouseClicked(event -> handleReliquary());
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
            runService = new RunService();
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
            StageUtils.setSceneRoot(stage, root);
        } catch (Exception e) {
            System.err.println("Errore apertura Legacy: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showSettingsMenu() {
        try {
            ModalUtils.show(optionsModalContainer, ModalUtils.Type.DEFAULT);
            try { view.manager.SoundManager.getInstance().playSettingsToggle(); } catch (Exception ignore) {}
            
            try {
                optionsModalContainer.prefWidthProperty().unbind();
                optionsModalContainer.prefHeightProperty().unbind();
            } catch (Exception ignore) {}
            
            if (mainContainer.getScene() != null) {
                optionsModalContainer.prefWidthProperty().bind(mainContainer.getScene().widthProperty());
                optionsModalContainer.prefHeightProperty().bind(mainContainer.getScene().heightProperty());
            }

            optionsModalContainer.setPickOnBounds(true);
            try { optionsModalContainer.setMouseTransparent(false); } catch (Exception ignore) {}

            StackPane panel = new StackPane();
            panel.setPickOnBounds(true);
            panel.prefWidthProperty().bind(optionsModalContainer.widthProperty());
            panel.prefHeightProperty().bind(optionsModalContainer.heightProperty());

            Image bg = new Image(getClass().getResourceAsStream("/assets/icons/help/help.background.png"));
            ImageView bgView = new ImageView(bg);
            bgView.setPreserveRatio(true);
            double bgW = bg.getWidth() * 0.8;
            double bgH = bg.getHeight() * 0.8;
            bgView.setFitWidth(bgW);
            bgView.setFitHeight(bgH);

            Group bgGroup = new Group();
            bgGroup.getChildren().add(bgView);

            Image closeImg = new Image(getClass().getResourceAsStream("/assets/icons/utils/x.png"));
            ImageView closeIcon = new ImageView(closeImg);
            closeIcon.setPreserveRatio(true);
            closeIcon.setFitWidth(36);
            try { closeIcon.setCursor(null); } catch (Exception ignore) {}
            closeIcon.setOnMouseEntered(ev -> {
                Glow glow = new Glow(0.6);
                DropShadow shadow = new DropShadow();
                shadow.setRadius(8);
                shadow.setSpread(0.18);
                shadow.setColor(Color.web("#ff2b2baa"));
                closeIcon.setEffect(new Blend(BlendMode.SRC_OVER, glow, shadow));
            });
            closeIcon.setOnMouseExited(ev -> closeIcon.setEffect(null));
            closeIcon.setOnMouseClicked(ev -> { 
                try { view.manager.SoundManager.getInstance().playSettingsToggle(); } catch (Exception ignore) {}
                ModalUtils.hideAndClear(optionsModalContainer); 
            });
            
            double margin = 16;
            double offsetLeft = 63; 
            double offsetDown = 83; 
            StackPane.setAlignment(closeIcon, Pos.CENTER);
            closeIcon.setTranslateX((bgW / 2.0) - closeIcon.getFitWidth() - margin - offsetLeft);
            closeIcon.setTranslateY((-bgH / 2.0) + margin + offsetDown);

            StackPane.setAlignment(bgGroup, Pos.CENTER);
            panel.getChildren().add(bgGroup);

            VBox content = new VBox(20.0);
            content.setAlignment(Pos.CENTER);

            HBox sfxRow = new HBox(16.0);
            sfxRow.setAlignment(Pos.CENTER);
            Label sfxLabel = new Label("SFX Volume");
            sfxLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
            sfxLabel.setPrefWidth(170);
            sfxLabel.setMinWidth(170);
            sfxLabel.setMaxWidth(170);
            Slider sfxSlider = new Slider(0.0, 1.0, view.manager.SoundManager.getInstance().getSfxVolume());
            sfxSlider.setPrefWidth(320);
            sfxSlider.setMinWidth(320);
            sfxSlider.setMaxWidth(320);
            sfxSlider.setStyle("-fx-accent: #FFCC00;");
            sfxSlider.setShowTickLabels(false);
            sfxSlider.setShowTickMarks(false);
            sfxSlider.valueProperty().addListener((obs, o, n) -> view.manager.SoundManager.getInstance().setSfxVolume(n.doubleValue()));
            sfxRow.getChildren().addAll(sfxLabel, sfxSlider);

            HBox musicRow = new HBox(16.0);
            musicRow.setAlignment(Pos.CENTER);
            Label musicLabel = new Label("Music Volume");
            musicLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
            musicLabel.setPrefWidth(170);
            musicLabel.setMinWidth(170);
            musicLabel.setMaxWidth(170);
            Slider musicSlider = new Slider(0.0, 1.0, view.manager.SoundManager.getInstance().getMusicVolume());
            musicSlider.setPrefWidth(320);
            musicSlider.setMinWidth(320);
            musicSlider.setMaxWidth(320);
            musicSlider.setStyle("-fx-accent: #FFCC00;");
            musicSlider.setShowTickLabels(false);
            musicSlider.setShowTickMarks(false);
            musicSlider.valueProperty().addListener((obs, o, n) -> view.manager.SoundManager.getInstance().setMusicVolume(n.doubleValue()));
            musicSlider.valueChangingProperty().addListener((obs, was, is) -> view.manager.SoundManager.getInstance().setMusicVolume(musicSlider.getValue()));
            musicRow.getChildren().addAll(musicLabel, musicSlider);

            ImageView logoutIcon = createMenuIcon("/assets/menu/logOut.png", () -> { 
                ModalUtils.hideAndClear(optionsModalContainer); 
                handleLogout(); 
            });
            ImageView exitIcon = createMenuIcon("/assets/menu/exitGame.png", () -> { 
                handleQuit(); 
            });
            
            VBox buttonsBox = new VBox(25.0);
            buttonsBox.setAlignment(Pos.CENTER);
            buttonsBox.getChildren().addAll(logoutIcon, exitIcon);
            
            try { VBox.setMargin(buttonsBox, new Insets(60, 0, 0, 0)); } catch (Exception ignore) {}

            content.getChildren().addAll(sfxRow, musicRow, buttonsBox);
            content.setPickOnBounds(false);
            StackPane.setAlignment(content, Pos.CENTER);
            panel.getChildren().add(content);

            double shiftX = 15; 
            double shiftY = -30; 
            bgGroup.setTranslateX(shiftX);
            bgGroup.setTranslateY(shiftY);
            content.setTranslateX(shiftX);
            content.setTranslateY(shiftY);
            closeIcon.setTranslateX(closeIcon.getTranslateX() + shiftX);
            closeIcon.setTranslateY(closeIcon.getTranslateY() + shiftY);

            panel.getChildren().add(closeIcon);

            optionsModalContainer.getChildren().add(panel);
        } catch (Exception ex) {
            System.err.println("Errore apertura menu impostazioni: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private ImageView createMenuIcon(String resourcePath, Runnable onClick) {
        Image img = new Image(getClass().getResourceAsStream(resourcePath));
        ImageView icon = new ImageView(img);
        icon.setPreserveRatio(true);
        icon.setFitWidth(240);
        try { icon.setCursor(null); } catch (Exception ignore) {}
        icon.setOnMouseEntered(ev -> {
            Glow glow = new Glow(0.6);
            DropShadow shadow = new DropShadow();
            shadow.setRadius(9);
            shadow.setSpread(0.2);
            shadow.setColor(Color.web("#ffffffaa"));
            icon.setEffect(new Blend(BlendMode.SRC_OVER, glow, shadow));
        });
        icon.setOnMouseExited(ev -> icon.setEffect(null));
        icon.setOnMouseClicked(ev -> { if (onClick != null) onClick.run(); });
        return icon;
    }

    private void handleContinueExpedition() {
        boolean success = resumeLastRun();
        if (success) {
            navigateToGameScreen();
        } else {
            
            navigateToCharacterSelection();
        }
    }
    
    private void handleNewExpedition() {
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
            try { view.manager.SoundManager.getInstance().fadeOutMusic(400); } catch (Exception ignore) {}
            StageUtils.setSceneRoot(stage, root);
            try {
                PauseTransition pt = new PauseTransition(Duration.millis(300));
                pt.setOnFinished(ev -> { try { view.manager.SoundManager.getInstance().playCharacterSelection(); } catch (Exception ignore) {} });
                pt.play();
            } catch (Exception ignore) {}
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
            gameController.startGame();
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            StageUtils.setSceneRoot(stage, gameRoot);
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
            gameController.startGame();
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            StageUtils.setSceneRoot(stage, gameRoot);
            
        } catch (java.io.IOException e) {
            System.err.println("Errore nella navigazione alla GameView: " + e.getMessage());
            e.printStackTrace();
            showAlert("Errore", "Impossibile caricare la schermata di gioco.");
        }
    }
    
    private void showAlert(String title, String message) {
    }

    private void handleLogout() {
        try {
            model.service.SessionService.clear();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/Nickname.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            StageUtils.setSceneRoot(stage, root);
        } catch (Exception e) {
            System.err.println("Errore nel logout: " + e.getMessage());
        }
    }

    private void handleQuit() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.close();
    }
}
