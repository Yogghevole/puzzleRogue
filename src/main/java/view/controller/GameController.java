package view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.geometry.Pos;
import model.service.RunService;
import model.service.GameDataService;
import model.db.DatabaseManager;
import model.service.SudokuGenerator;
import model.domain.SudokuGrid;
import model.domain.RunFrozenBuffs;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import view.manager.BackgroundManager;

public class GameController {
    
    @FXML private StackPane mainGameArea; 
    @FXML private ImageView backgroundImageView;
    @FXML private StackPane modalContainer;
    @FXML private Label levelLabel;
    @FXML private Label difficultyLabel;
    @FXML private Button menuButton;
    @FXML private ImageView characterSpriteView;
    @FXML private ImageView enemySpriteView;
    @FXML private GridPane sudokuGridContainer;
    @FXML private HBox livesHBox;
    @FXML private HBox inputControlHBox;
    
    private RunService runService;
    private static final int GRID_SIZE = 9;
    
    private Label[][] cellLabels = new Label[GRID_SIZE][GRID_SIZE]; 
    private Label[][][] noteLabels = new Label[GRID_SIZE][GRID_SIZE][GRID_SIZE];
    private GridPane[][] noteGrids = new GridPane[GRID_SIZE][GRID_SIZE];
    private VBox[][] sudokuCells = new VBox[GRID_SIZE][GRID_SIZE];
    private VBox selectedCellVBox = null;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private int currentLevel = 1;
    private int totalLevels = 10;
    private final GameDataService gameDataService = new GameDataService(DatabaseManager.getInstance());
    private final Set<String> usedEnemyGlobal = new HashSet<>();
    private final Random rng = new Random();
    private boolean characterSelected = false;
    private final SudokuGenerator sudokuGenerator = new SudokuGenerator(gameDataService);
    private model.engine.SudokuEngine sudokuEngine;
    private final BackgroundManager backgroundManager = new BackgroundManager();

    private static class CharDef {
        final String id;
        final String name;
        final String portrait;
        final String sprite;
        CharDef(String id, String name, String portrait, String sprite) {
            this.id = id; this.name = name; this.portrait = portrait; this.sprite = sprite;
        }
    }

    public void setRunService(RunService runService) {
        this.runService = runService;
    }
    
    @FXML
    public void initialize() {
        try {
            Font.loadFont(getClass().getResourceAsStream("/assets/fonts/dark_font_Regular.ttf"), 10);  
            Font.loadFont(getClass().getResourceAsStream("/assets/fonts/dark_font_DemiBold.ttf"), 10);
            if (backgroundImageView != null) {
                backgroundImageView.setPreserveRatio(false);
                backgroundImageView.setFitWidth(1920);
                backgroundImageView.setFitHeight(800);
                StackPane.setAlignment(backgroundImageView, Pos.CENTER);
            }
            backgroundManager.preloadAll();
            
        } catch (Exception e) {
            System.err.println("Errore nel caricamento del font o delle immagini: " + e.getMessage());
        } 
        
        buildSudokuGrid();

        if (menuButton != null) {
            menuButton.setText("COMPLETA LIVELLO");
            menuButton.setOnAction(e -> completeLevelAndAdvance());
        }

        int count = gameDataService.getTotalLevels();
        if (count > 0) {
            totalLevels = count;
        }

        showCharacterSelectionModal();
        hideGameUIForSelection(true);

        javafx.application.Platform.runLater(this::applyCustomCursor);
    }
    
    private void buildSudokuGrid() {
        double cellSize = Math.floor(650.0 / GRID_SIZE);
        sudokuGridContainer.setHgap(0);
        sudokuGridContainer.setVgap(0);
        sudokuGridContainer.setPadding(new javafx.geometry.Insets(0));
        
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                VBox cellContainer = new VBox();
                cellContainer.setMinSize(cellSize, cellSize);
                cellContainer.setPrefSize(cellSize, cellSize);
                cellContainer.setMaxSize(cellSize, cellSize);
                cellContainer.getStyleClass().add("sudoku-cell");
                cellContainer.getStyleClass().add("unselected-cell");
                int leftW = (c == 0 || c == 3 || c == 6) ? 4 : 1;
                int rightW = (c == 8) ? 4 : 1;
                int topW = (r == 0 || r == 3 || r == 6) ? 4 : 1;
                int bottomW = (r == 8) ? 4 : 1;
                String thin = "#444444";
                String heavy = "#777777";
                String topColor = (topW > 1) ? heavy : thin;
                String rightColor = (rightW > 1) ? heavy : thin;
                String bottomColor = (bottomW > 1) ? heavy : thin;
                String leftColor = (leftW > 1) ? heavy : thin;
                cellContainer.setStyle(String.format(
                    "-fx-border-color: %s %s %s %s; -fx-border-width: %d %d %d %d;",
                    topColor, rightColor, bottomColor, leftColor,
                    topW, rightW, bottomW, leftW
                ));
                
                GridPane noteGrid = new GridPane();
                noteGrid.setPrefSize(cellSize, cellSize);
                noteGrid.setVisible(false);
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int noteValue = i * 3 + j + 1;
                        Label noteLabel = new Label(String.valueOf(noteValue));
                        noteLabel.getStyleClass().add("note-label");
                        noteLabels[r][c][noteValue - 1] = noteLabel; 
                        
                        noteGrid.add(noteLabel, j, i);
                    }
                }
                
                Label valueLabel = new Label("");
                valueLabel.getStyleClass().add("initial-number"); 
                valueLabel.setVisible(false);

                StackPane contentPane = new StackPane(noteGrid, valueLabel);
                cellContainer.getChildren().add(contentPane);
                
                cellLabels[r][c] = valueLabel;
                noteGrids[r][c] = noteGrid;
                sudokuCells[r][c] = cellContainer;
                
                final int finalR = r;
                final int finalC = c;
                cellContainer.setOnMouseClicked(e -> handleCellClick(finalR, finalC));
                
                sudokuGridContainer.add(cellContainer, c, r);
            }
        }
        sudokuGridContainer.setPrefSize(650, 650);
        sudokuGridContainer.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        sudokuGridContainer.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        buildInputControls();
    }
    
    private void buildInputControls() {
        for (int i = 1; i <= 9; i++) {
            Button numButton = new Button(String.valueOf(i));
            numButton.getStyleClass().add("input-number-button");
            final int value = i;
            numButton.setOnAction(e -> handleNumberInput(value));
            inputControlHBox.getChildren().add(numButton);
        }
        
        Button noteModeButton = new Button("NOTE");
        noteModeButton.setOnAction(e -> toggleNoteMode());
        inputControlHBox.getChildren().add(noteModeButton);
        
        Button clearButton = new Button("CLEAR");
        clearButton.setOnAction(e -> handleClearCell());
        inputControlHBox.getChildren().add(clearButton);
        
    }
    
    private void handleCellClick(int r, int c) {
        if (selectedCellVBox != null) {
            selectedCellVBox.getStyleClass().remove("selected-cell");
            selectedCellVBox.getStyleClass().add("unselected-cell");
        }
        
        VBox newSelection = sudokuCells[r][c];
        newSelection.getStyleClass().remove("unselected-cell");
        newSelection.getStyleClass().add("selected-cell");
        
        selectedCellVBox = newSelection;
        selectedRow = r;
        selectedCol = c;
        
    }
    
    

    private void toggleNoteMode() {
        System.out.println("Modalità Note attivata/disattivata.");
    }

    private void handleClearCell() {
        if (selectedRow != -1 && selectedCol != -1) {
            System.out.println("Cancellazione cella (" + selectedRow + "," + selectedCol + ")");
        }
    }
    
    @FXML
    private void handleMenuClick() {
        System.out.println("Mostra Menu Opzioni.");
    }
    
    private void showCharacterSelectionModal() {
        backgroundManager.resetRun();
        modalContainer.getStyleClass().clear();
        modalContainer.getStyleClass().add("modal-overlay");
        modalContainer.setVisible(true);

        javafx.scene.layout.StackPane selectionPanel = new javafx.scene.layout.StackPane();
        javafx.scene.image.ImageView panelBackground = new javafx.scene.image.ImageView(
            new javafx.scene.image.Image(getClass().getResourceAsStream("/assets/menu/character_selection.png"))
        );
        panelBackground.setFitWidth(1400);
        panelBackground.setFitHeight(800);
        panelBackground.setPreserveRatio(false);

        javafx.scene.layout.VBox contentBox = new javafx.scene.layout.VBox();
        contentBox.setPickOnBounds(false);
        contentBox.setSpacing(12);
        contentBox.setAlignment(javafx.geometry.Pos.CENTER);
        contentBox.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));
        contentBox.setTranslateY(30);


        javafx.scene.layout.HBox charactersRow = new javafx.scene.layout.HBox();
        charactersRow.setSpacing(5);
        charactersRow.setAlignment(javafx.geometry.Pos.CENTER);

        java.util.List<CharDef> defs = java.util.List.of(
            new CharDef("CRUSADER", "Crusader", "/assets/icons/characters/crusader_portrait.png", "/assets/characters/crusader.png"),
            new CharDef("HIGHWAYMAN", "Highwayman", "/assets/icons/characters/highwayman_portrait.png", "/assets/characters/highwayman.png"),
            new CharDef("JESTER", "Jester", "/assets/icons/characters/jester_portrait.png", "/assets/characters/jester.png"),
            new CharDef("OCCULTIST", "Occultist", "/assets/icons/characters/occultist_portrait.png", "/assets/characters/occultist.png"),
            new CharDef("PLAGUEDOCTOR", "Plaguedoctor", "/assets/icons/characters/plague_doctor_portrait.png", "/assets/characters/plague_doctor.png")
        );

        for (CharDef d : defs) {
            javafx.scene.image.ImageView portrait = new javafx.scene.image.ImageView(
                new javafx.scene.image.Image(getClass().getResourceAsStream(d.portrait))
            );
            portrait.setFitWidth(70);
            portrait.setFitHeight(70);
            portrait.setPreserveRatio(true);
            portrait.getStyleClass().add("character-portrait");

            javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(d.name);
            nameLabel.getStyleClass().add("character-name-label");

            javafx.scene.layout.VBox option = new javafx.scene.layout.VBox(portrait, nameLabel);
            option.getStyleClass().add("character-option");
            option.setAlignment(javafx.geometry.Pos.CENTER);
            option.setSpacing(8);
            option.setPrefWidth(160);
            option.setMinWidth(160);
            option.setMaxWidth(160);

            option.setOnMouseEntered(ev -> {
                portrait.setFitWidth(76);
                portrait.setFitHeight(76);
            });
            option.setOnMouseExited(ev -> {
                portrait.setFitWidth(70);
                portrait.setFitHeight(70);
            });

            option.setOnMouseClicked(e -> selectCharacter(d));
            portrait.setOnMouseClicked(e -> selectCharacter(d));
            nameLabel.setOnMouseClicked(e -> selectCharacter(d));

            charactersRow.getChildren().add(option);
        }

        contentBox.getChildren().add(charactersRow);
        selectionPanel.getChildren().addAll(panelBackground, contentBox);

        modalContainer.getChildren().clear();
        modalContainer.getChildren().add(selectionPanel);
        javafx.scene.layout.StackPane.setAlignment(selectionPanel, javafx.geometry.Pos.CENTER);
        System.out.println("Mostra Selezione Personaggio.");
    }

    private void selectCharacter(CharDef def) {
        try {
            characterSpriteView.setImage(new Image(getClass().getResourceAsStream(def.sprite)));
            modalContainer.setVisible(false);
            modalContainer.getStyleClass().clear();
            modalContainer.getChildren().clear();
            characterSelected = true;

            hideGameUIForSelection(false);
            updateLevelAndDifficultyUI();
            applyBackgroundForCurrentLevel();
            updateSkipButtonState();

            generateSudokuForCurrentLevel();
            spawnEnemyForCurrentLevel();
        } catch (Exception e) {
            System.err.println("Errore nel set del personaggio selezionato: " + e.getMessage());
        }
    }

    private String pickEnemyForDifficulty(String difficultyTier) {
        if (difficultyTier == null) return null;
        String dirName = difficultyTier.toLowerCase();
        List<String> all = listEnemyFiles(dirName);
        if (all.isEmpty()) return null;
        List<String> available = all.stream()
            .map(name -> "/assets/enemies/" + dirName + "/" + name)
            .filter(path -> !usedEnemyGlobal.contains(path))
            .collect(Collectors.toList());
        if (available.isEmpty()) {
            System.err.println("Nessun nemico disponibile nella difficoltà '" + difficultyTier + "' non ancora usato in questa run.");
            return null;
        }
        String chosenPath = available.get(rng.nextInt(available.size()));
        usedEnemyGlobal.add(chosenPath);
        return chosenPath;
    }

    private List<String> listEnemyFiles(String difficultyDir) {
        try {
            URL url = getClass().getResource("/assets/enemies/" + difficultyDir);
            if (url == null) return Collections.emptyList();
            Path dir = Paths.get(url.toURI());
            try (Stream<Path> stream = Files.list(dir)) {
                return stream
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".png"))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Impossibile elencare i file nemici per '" + difficultyDir + "': " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private String getDifficultyFallbackByLevel(int level) {
        if (level >= 10) return "NIGHTMARE";
        if (level >= 7) return "HARD";
        if (level >= 4) return "MEDIUM";
        return "EASY";
    }

    private void hideGameUIForSelection(boolean hide) {
        if (levelLabel != null) levelLabel.setVisible(!hide);
        if (difficultyLabel != null) difficultyLabel.setVisible(!hide);
        if (menuButton != null) menuButton.setVisible(!hide);
        if (characterSpriteView != null) characterSpriteView.setVisible(!hide);
        if (enemySpriteView != null) enemySpriteView.setVisible(!hide);
        if (sudokuGridContainer != null) sudokuGridContainer.setVisible(!hide);
        if (livesHBox != null) livesHBox.setVisible(!hide);
        if (inputControlHBox != null) inputControlHBox.setVisible(!hide);
    }

    private void updateLevelAndDifficultyUI() {
        if (levelLabel != null) levelLabel.setText("Livello " + currentLevel);
        String difficulty = gameDataService.getBaseDifficultyByLevel(currentLevel);
        if (difficulty == null || "UNKNOWN".equalsIgnoreCase(difficulty)) {
            difficulty = getDifficultyFallbackByLevel(currentLevel);
        }
        if (difficultyLabel != null) difficultyLabel.setText(difficulty);
    }

    private void spawnEnemyForCurrentLevel() {
        String difficulty = (difficultyLabel != null) ? difficultyLabel.getText() : null;
        if (difficulty == null || difficulty.isEmpty()) {
            difficulty = gameDataService.getBaseDifficultyByLevel(currentLevel);
            if (difficulty == null || "UNKNOWN".equalsIgnoreCase(difficulty)) {
                difficulty = getDifficultyFallbackByLevel(currentLevel);
            }
        }
        String enemySpritePath = pickEnemyForDifficulty(difficulty);
        if (enemySpritePath != null && enemySpriteView != null) {
            enemySpriteView.setImage(new Image(getClass().getResourceAsStream(enemySpritePath)));
            enemySpriteView.setVisible(true);
            enemySpriteView.setScaleX(-1); // Specchia l'immagine del nemico
        } else if (enemySpriteView != null) {
            enemySpriteView.setVisible(false);
        }
    }

    private void completeLevelAndAdvance() {
        if (!characterSelected) {
            return;
        }
        if (currentLevel >= totalLevels) {
            updateSkipButtonState();
            System.out.println("Run completata: ultimo livello raggiunto.");
            return;
        }
        currentLevel++;
        updateLevelAndDifficultyUI();
        applyBackgroundForCurrentLevel();
        generateSudokuForCurrentLevel();
        spawnEnemyForCurrentLevel();
        updateSkipButtonState();
        System.out.println("Livello " + currentLevel + " avviato (skip).");
    }

    private void applyBackgroundForCurrentLevel() {
        try {
            boolean isBoss = gameDataService.isBossLevel(currentLevel);
            Image img = backgroundManager.selectRandomUnique(isBoss);
            if (img != null && backgroundImageView != null) {
                backgroundImageView.setImage(img);
            } else {
                System.err.println("Background non disponibile per livello " + currentLevel + ".");
            }
        } catch (Exception e) {
            System.err.println("Errore nell'applicazione del background: " + e.getMessage());
        }
    }

    private void updateSkipButtonState() {
        if (menuButton != null) {
            boolean disable = !characterSelected || currentLevel >= totalLevels;
            menuButton.setDisable(disable);
        }
    }

    private void generateSudokuForCurrentLevel() {
        RunFrozenBuffs frozen = new RunFrozenBuffs(Collections.emptyMap());
        SudokuGrid puzzle = sudokuGenerator.generateNewPuzzle(currentLevel, frozen);
        sudokuEngine = new model.engine.SudokuEngine(puzzle);
        applyPuzzleToUI(puzzle);
    }

    private void applyPuzzleToUI(SudokuGrid puzzle) {
        int[][] initial = puzzle.getInitialGrid();
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                int v = initial[r][c];
                if (v != 0) {
                    cellLabels[r][c].setText(String.valueOf(v));
                    cellLabels[r][c].setVisible(true);
                    if (noteGrids[r][c] != null) noteGrids[r][c].setVisible(false);
                } else {
                    cellLabels[r][c].setText("");
                    cellLabels[r][c].setVisible(false);
                    if (noteGrids[r][c] != null) noteGrids[r][c].setVisible(false);
                }
            }
        }
    }

    private void handleNumberInput(int value) {
        if (selectedRow != -1 && selectedCol != -1 && sudokuEngine != null) {
            boolean ok = sudokuEngine.insertValue(selectedRow, selectedCol, value);
            if (ok) {
                cellLabels[selectedRow][selectedCol].setText(String.valueOf(value));
                cellLabels[selectedRow][selectedCol].setVisible(true);
                if (noteGrids[selectedRow][selectedCol] != null) noteGrids[selectedRow][selectedCol].setVisible(false);
                if (sudokuEngine.checkWin()) {
                    System.out.println("Sudoku completato!");
                }
            } else {
                System.out.println("Valore errato o cella bloccata.");
            }
        }
    }

    private void applyCustomCursor() {
        try {
            Image img = new Image(getClass().getResourceAsStream("/assets/icons/cursor/arrow.png"));
            javafx.scene.ImageCursor cursor = new javafx.scene.ImageCursor(img);
            if (mainGameArea.getScene() != null) {
                mainGameArea.getScene().setCursor(cursor);
            }
        } catch (Exception e) {
            System.err.println("Impossibile impostare il cursore custom: " + e.getMessage());
        }
    }

    public void updateSudokuView() {

    }
}