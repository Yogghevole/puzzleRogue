package view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import model.db.DatabaseManager;
import model.dao.UserDAO;
import model.service.SessionService;

public class NicknameController {
    @FXML private TextField nickField;
    @FXML private Button continueButton;
    @FXML private AnchorPane rootPane;

    @FXML
    public void initialize() {
        continueButton.setOnAction(e -> handleContinue());
        applyBackground();
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
        }
        SessionService.setCurrentNick(nick);
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
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception ex) {
            System.err.println("Errore nella navigazione alla Home: " + ex.getMessage());
        }
    }
}