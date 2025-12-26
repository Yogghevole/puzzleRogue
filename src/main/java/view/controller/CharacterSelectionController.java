package view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.Node;
import model.service.RunService;
import view.util.StageUtils;

/**
 * Controller for the character selection screen.
 * Allows the player to choose a character class before starting a run.
 */
public class CharacterSelectionController {
    
    @FXML private HBox charactersContainer;
    @FXML private Button backButton;
    @FXML private Button startButton;
    
    private String selectedCharacter = null;
    private RunService runService;
    
    @FXML
    public void initialize() {
        runService = new RunService();
        setupCharacters();
        setupButtonListeners();
    }
    
    private void setupCharacters() {
        String[] characters = {
            "CRUSADER",
            "HIGHWAYMAN", 
            "JESTER",
            "OCCULTIST",
            "PLAGUEDOCTOR"
        };
        
        String[] characterNames = {
            "Crusader",
            "Highwayman",
            "Jester", 
            "Occultist",
            "Plague Doctor"
        };
        
        for (int i = 0; i < characters.length; i++) {
            VBox characterBox = createCharacterBox(characters[i], characterNames[i]);
            charactersContainer.getChildren().add(characterBox);
        }
    }
    
    private VBox createCharacterBox(String characterId, String characterName) {
        VBox characterBox = new VBox(10);
        characterBox.setAlignment(javafx.geometry.Pos.CENTER);
        characterBox.setStyle("-fx-background-color: #333333; -fx-background-radius: 8; -fx-padding: 16;");
        characterBox.setPrefWidth(120);
        characterBox.setPrefHeight(160);
        
        try {
            String imagePath = "/assets/icons/characters/" + characterId.toLowerCase() + "_portrait.png";
            Image characterImage = new Image(getClass().getResourceAsStream(imagePath));
            ImageView characterImageView = new ImageView(characterImage);
            characterImageView.setFitWidth(70);
            characterImageView.setFitHeight(70);
            characterImageView.setPreserveRatio(true);
            characterBox.getChildren().add(characterImageView);
        } catch (Exception e) {
            Label placeholder = new Label(characterId.substring(0, 1));
            placeholder.setFont(Font.font("Roboto Bold", 36));
            placeholder.setTextFill(Color.WHITE);
            characterBox.getChildren().add(placeholder);
        }
        
        Label nameLabel = new Label(characterName);
        nameLabel.setFont(Font.font("Roboto Medium", 14));
        nameLabel.setTextFill(Color.WHITE);
        characterBox.getChildren().add(nameLabel);
        characterBox.setOnMouseClicked(event -> selectCharacter(characterId, characterBox));
        characterBox.setOnMouseEntered(event -> {
            if (selectedCharacter == null || !selectedCharacter.equals(characterId)) {
                characterBox.setStyle("-fx-background-color: #444444; -fx-background-radius: 8; -fx-padding: 16;");
            }
        });
        characterBox.setOnMouseExited(event -> {
            if (selectedCharacter == null || !selectedCharacter.equals(characterId)) {
                characterBox.setStyle("-fx-background-color: #333333; -fx-background-radius: 8; -fx-padding: 16;");
            }
        });
        
        return characterBox;
    }
    
    private void selectCharacter(String characterId, VBox characterBox) {
        for (Node node : charactersContainer.getChildren()) {
            if (node instanceof VBox) {
                ((VBox) node).setStyle("-fx-background-color: #333333; -fx-background-radius: 8; -fx-padding: 16;");
            }
        }

        selectedCharacter = characterId;
        characterBox.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 8; -fx-padding: 16;");
        startButton.setDisable(false);
    }
    
    private void setupButtonListeners() {
        backButton.setOnAction(event -> goBack());
        startButton.setOnAction(event -> startExpedition());
    }
    
    private void goBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/view/HomeScreenSimple.fxml")
            );
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            StageUtils.setSceneRoot(stage, root);
        } catch (Exception e) {
            System.err.println("Errore nel tornare alla home: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void startExpedition() {
        if (selectedCharacter == null) {
            return;
        }
        
        try {
            runService.startNewRun();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/view/GameView.fxml")
            );
            javafx.scene.Parent root = loader.load();
            view.controller.GameController gameController = loader.getController();
            gameController.setRunService(runService);
            Stage stage = (Stage) startButton.getScene().getWindow();
            StageUtils.setSceneRoot(stage, root);
            
        } catch (Exception e) {
            System.err.println("Errore nell'avviare l'espedizione: " + e.getMessage());
            e.printStackTrace();
        }
    }
}