package view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.application.Platform;
import model.db.DatabaseManager;
import model.dao.UserDAO;
import model.service.SessionService;
import view.util.StageUtils;
import javafx.scene.image.ImageView;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;

/**
 * Controller for the nickname entry screen.
 * Handles user login and profile creation.
 */
public class NicknameController {
    @FXML private TextField nickField;
    @FXML private Button continueButton;
    @FXML private AnchorPane rootPane;

    @FXML
    public void initialize() {
        continueButton.setOnAction(e -> handleContinue());
        
        nickField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() > 15) {
                return null;
            }
            return change;
        }));
        
        applyBackground();
        setupButtonEffects();
        rootPane.setFocusTraversable(true);
        Platform.runLater(() -> rootPane.requestFocus());
    }

    private void setupButtonEffects() {
        continueButton.setOnMouseEntered(e -> {
            if (continueButton.getGraphic() instanceof ImageView) {
                ImageView iv = (ImageView) continueButton.getGraphic();
                Glow glow = new Glow(0.35);
                DropShadow shadow = new DropShadow();
                shadow.setRadius(6.0);
                shadow.setSpread(0.1);
                shadow.setColor(Color.web("#ffffff88"));
                iv.setEffect(new Blend(BlendMode.SRC_OVER, glow, shadow));
            }
        });
        continueButton.setOnMouseExited(e -> {
            if (continueButton.getGraphic() instanceof ImageView) {
                ImageView iv = (ImageView) continueButton.getGraphic();
                iv.setEffect(null);
            }
        });
    }

    private void handleContinue() {
        String nick = nickField.getText();
        if (nick == null || nick.trim().isEmpty()) {
            return;
        }
        nick = nick.trim();
        DatabaseManager db = DatabaseManager.getInstance();
        UserDAO userDAO = new UserDAO(db);
        var user = userDAO.getUserByNick(nick);
        if (user == null) {
            userDAO.createUser(nick);
            user = userDAO.getUserByNick(nick);
        }
        SessionService.setCurrentUser(user);
        navigateToHome();
    }
    private void applyBackground() {
        try {
            String imagePath = "/assets/menu/home_screen.png";
            Image backgroundImage = new Image(getClass().getResourceAsStream(imagePath));
            BackgroundImage bg = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, false, true)
            );
            if (rootPane != null) {
                rootPane.setBackground(new Background(bg));
            }
        } catch (Exception ignore) {}
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeScreen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nickField.getScene().getWindow();
            StageUtils.setSceneRoot(stage, root);
        } catch (Exception ex) {
            System.err.println("Errore nella navigazione alla Home: " + ex.getMessage());
        }
    }
}