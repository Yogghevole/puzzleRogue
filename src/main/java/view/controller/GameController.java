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
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import view.manager.LivesUIManager;
import javafx.stage.Stage;
import view.manager.InventorySlotsUIManager;
import javafx.geometry.Pos;
import model.service.RunService;
import model.service.GameDataService;
import model.db.DatabaseManager;
import model.service.SudokuGenerator;
import model.domain.SudokuGrid;
import model.domain.RunFrozenBuffs;
import model.domain.RunLevelState;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import view.manager.BackgroundManager;
import view.manager.EnemySpriteManager;
import view.manager.PlayerSpriteManager;
import view.manager.CursorManager;
import view.manager.CharacterSelectionManager;
import view.manager.ItemSelectionManager;
import view.manager.SudokuUIManager;
import view.manager.GameInputManager;
import view.manager.HudManager;
import view.manager.EndGameManager;

public class GameController {
    
    @FXML private StackPane mainGameArea; 
    @FXML private ImageView backgroundImageView;
    @FXML private StackPane modalContainer;
    @FXML private Label levelLabel;
    @FXML private Label difficultyLabel;
    @FXML private Button menuButton;
    @FXML private Button skipButton;
    @FXML private Button itemSelectionButton;
    @FXML private ImageView characterSpriteView;
    @FXML private ImageView enemySpriteView;
    @FXML private GridPane sudokuGridContainer;
    @FXML private HBox livesHBox;
    @FXML private HBox inputControlHBox;
    @FXML private HBox inventorySlotsHBox;
    @FXML private HBox selectedBuffsHBox;
    @FXML private VBox buffInfoBox;
    
    private RunService runService;
    private static final int GRID_SIZE = 9;
    private Label[][] cellLabels = new Label[GRID_SIZE][GRID_SIZE]; 
    private Label[][][] noteLabels = new Label[GRID_SIZE][GRID_SIZE][GRID_SIZE];
    private GridPane[][] noteGrids = new GridPane[GRID_SIZE][GRID_SIZE];
    private VBox[][] sudokuCells = new VBox[GRID_SIZE][GRID_SIZE];
    private VBox selectedCellVBox = null;
    private int selectedRow = -1;
    private int selectedCol = -1;
    @SuppressWarnings("unused")
    private Integer lastHighlightedNumber = null;
    private int currentLevel = 1;
    private int totalLevels = 10;
    private final GameDataService gameDataService = new GameDataService(DatabaseManager.getInstance());
    private final Set<String> usedEnemyGlobal = new HashSet<>();
    private final Random rng = new Random();
    private boolean characterSelected = false;
    private final SudokuGenerator sudokuGenerator = new SudokuGenerator(gameDataService);
    private model.engine.SudokuEngine sudokuEngine;
    private final BackgroundManager backgroundManager = new BackgroundManager();
    private final EnemySpriteManager enemySpriteManager = new EnemySpriteManager();
    private final PlayerSpriteManager playerSpriteManager = new PlayerSpriteManager();
    private final CursorManager cursorManager = new CursorManager();
    private final CharacterSelectionManager characterSelectionManager = new CharacterSelectionManager();
    private final ItemSelectionManager itemSelectionManager = new ItemSelectionManager();
    private final SudokuUIManager sudokuUIManager = new SudokuUIManager();
    private final GameInputManager gameInputManager = new GameInputManager();
    private final HudManager hudManager = new HudManager();
    private final EndGameManager endGameManager = new EndGameManager();
    private boolean firstErrorProtectionActive = false;
    private boolean firstErrorProtectionUsed = false;
    private boolean noteModeActive = false;
    @SuppressWarnings("unused")
    private String currentTheme = null;
    private int initialMaxLives = 0;

    public void setRunService(RunService runService) {
        this.runService = runService;
    }
    
    @FXML
    public void initialize() {
        try {
            if (backgroundImageView != null) {
                backgroundImageView.setPreserveRatio(false);
                backgroundImageView.setFitWidth(1920);
                backgroundImageView.setFitHeight(800);
                StackPane.setAlignment(backgroundImageView, Pos.CENTER);
                backgroundImageView.setPickOnBounds(true);
                backgroundImageView.setOnMouseClicked(e -> {
                    clearSelectedCell();
                    clearNumberHighlights();
                    clearRegionHighlights();
                });
            }
            if (mainGameArea != null) {
                mainGameArea.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                    Node target = (Node) e.getTarget();
                    boolean outsideSudoku = (sudokuGridContainer != null && !isDescendantOf(target, sudokuGridContainer));
                    boolean onInputs = (inputControlHBox != null && isDescendantOf(target, inputControlHBox))
                                     || (inventorySlotsHBox != null && isDescendantOf(target, inventorySlotsHBox));
                    if (outsideSudoku && !onInputs) {
                        clearSelectedCell();
                        clearNumberHighlights();
                        clearRegionHighlights();
                    }
                });
            }
            backgroundManager.preloadAll();
            
        } catch (Exception e) {
        System.err.println("Error loading font or images: " + e.getMessage());
        } 
        
        buildSudokuGrid();

        if (menuButton != null) {
            menuButton.setText("Salva ed esci");
            menuButton.setOnAction(e -> saveAndExitToHome());
        }

        if (skipButton != null) {
            skipButton.setOnAction(e -> completeLevelAndAdvance());
            skipButton.setOnMouseEntered(e -> {
                if (skipButton.getGraphic() instanceof ImageView) {
                    ImageView iv = (ImageView) skipButton.getGraphic();
                    javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.35);
                    javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
                    shadow.setRadius(6.0);
                    shadow.setSpread(0.1);
                    shadow.setColor(javafx.scene.paint.Color.web("#ffffff88"));
                    iv.setEffect(new javafx.scene.effect.Blend(
                        javafx.scene.effect.BlendMode.SRC_OVER,
                        glow,
                        shadow
                    ));
                }
            });
            skipButton.setOnMouseExited(e -> {
                if (skipButton.getGraphic() instanceof ImageView) {
                    ImageView iv = (ImageView) skipButton.getGraphic();
                    iv.setEffect(null);
                }
            });
        }

        int count = gameDataService.getTotalLevels();
        if (count > 0) {
            totalLevels = count;
        }

        backgroundManager.resetRun();
        if (runService != null && runService.getCurrentRun() != null) {
            hideGameUIForSelection(false);
            characterSelected = true;
            RunLevelState st = runService.getCurrentRun().getCurrentLevelState();
            if (st == null) {
                runService.startLevel(1);
                st = runService.getCurrentRun().getCurrentLevelState();
            }
            currentLevel = st.getCurrentLevel();
            applyBackgroundForCurrentLevel();
            try {
                int[][] initial = RunLevelState.convertStringToGrid(st.getInitialGridData());
                int[][] user = RunLevelState.convertStringToGrid(st.getUserGridData());
                SudokuGrid grid = new SudokuGrid(initial, user, st.getDifficultyTier());
                sudokuEngine = new model.engine.SudokuEngine(grid);
                applyPuzzleToUI(grid);
                updateLevelAndDifficultyUI();
                spawnEnemyForCurrentLevel();
                renderSelectedBuffs();
                renderBuffInfo();
            } catch (Exception e) {
                System.err.println("Errore nel ripristino griglia utente: " + e.getMessage());
            }
        } else {
            hideGameUIForSelection(true);
            applyBackgroundForCurrentLevel();
            showCharacterSelectionModal();
        }

        javafx.application.Platform.runLater(this::applyCustomCursor);

        if (this.runService != null) {
            this.runService.getCurrentRun();
        }

        livesUIManager = new LivesUIManager(livesHBox, "/assets/icons/utils/heart.png");
        livesUIManager.setLives(0);

        inventorySlotsUIManager = new InventorySlotsUIManager(inventorySlotsHBox,
                "/assets/icons/items/placeholder.png");
        inventorySlotsUIManager.setCapacityLevel(0);
        initializeInventoryInteraction();

        if (itemSelectionButton != null) {
            itemSelectionButton.setVisible(false);
        }

        if (mainGameArea != null) {
            mainGameArea.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                Object tgt = e.getTarget();
                if (tgt instanceof Node) {
                    Node n = (Node) tgt;
                    boolean insideGrid = isDescendantOf(n, sudokuGridContainer);
                    boolean insideControls = isDescendantOf(n, inputControlHBox) || isDescendantOf(n, inventorySlotsHBox);
                    if (!insideGrid && !insideControls) {
                        clearSelectedCell();
                        clearNumberHighlights();
                    }
                } else {
                    clearSelectedCell();
                    clearNumberHighlights();
                }
            });
        }
    }

    private void renderSelectedBuffs() {
        try {
            String nick = model.service.SessionService.getCurrentNick();
            if (nick == null || nick.isEmpty() || selectedBuffsHBox == null) return;
            var user = new model.dao.UserDAO(model.db.DatabaseManager.getInstance()).getUserByNick(nick);
            if (user == null) return;
            Map<String, Integer> buffs = user.getPermanentBuffLevels();
            selectedBuffsHBox.getChildren().clear();
            for (Map.Entry<String, Integer> e : buffs.entrySet()) {
                if (e.getValue() != null && e.getValue() > 0) {
                    String path = mapBuffIcon(e.getKey());
                    if (path != null) {
                        ImageView badge = new ImageView(new Image(getClass().getResourceAsStream(path), 28, 28, true, true));
                        selectedBuffsHBox.getChildren().add(badge);
                    }
                }
            }
        } catch (Exception ignore) {}
    }

    private String mapBuffIcon(String id) {
        if (id == null) return null;
        switch (id) {
            case "EXTRA_LIVES": return "/assets/icons/buffs/extra_lives.png";
            case "FIRST_ERROR_PROTECT": return "/assets/icons/buffs/first_error_protection.png";
            case "STARTING_HINTS": return "/assets/icons/buffs/extra_hints.png";
            case "POINT_BONUS": return "/assets/icons/buffs/point_bonus.png";
            case "INVENTORY_CAPACITY": return "/assets/icons/buffs/inventory_capacity.png";
            case "STARTING_CELLS": return "/assets/icons/buffs/starting_cells.png";
            default: return null;
        }
    }

    private void renderBuffInfo() {
        try {
            if (buffInfoBox == null) return;
            String id = model.service.SessionService.getLastSelectedBuff();
            buffInfoBox.getChildren().clear();
            if (id == null || id.isEmpty()) return;
            javafx.scene.control.Label name = new javafx.scene.control.Label(id.replace('_',' '));
            name.getStyleClass().add("heading-small");
            int level = 1;
            try {
                String nick = model.service.SessionService.getCurrentNick();
                if (nick != null) {
                    var user = new model.dao.UserDAO(model.db.DatabaseManager.getInstance()).getUserByNick(nick);
                    if (user != null) level = user.getBuffLevel(id);
                }
            } catch (Exception ignore) {}
            double value = new model.service.GameDataService(model.db.DatabaseManager.getInstance())
                    .getBuffLevelData(id, Math.max(level,1))
                    .getOrDefault("value", 0).doubleValue();
            javafx.scene.control.Label desc = new javafx.scene.control.Label("Level " + Math.max(level,1) + " • Value " + value);
            desc.getStyleClass().add("hint-label");
            buffInfoBox.getChildren().addAll(name, desc);
        } catch (Exception ignore) {}
    }

    
    private void buildSudokuGrid() {
        sudokuUIManager.build(
            sudokuGridContainer,
            GRID_SIZE,
            cellLabels,
            noteLabels,
            noteGrids,
            sudokuCells,
            (r, c) -> handleCellClick(r, c)
        );
        gameInputManager.build(
            inputControlHBox,
            this::handleNumberInput,
            this::toggleNoteMode,
            this::handleClearCell
        );
        gameInputManager.setNoteModeActive(false);
    }
    
    
    
    private void handleCellClick(int r, int c) {
        clearRegionHighlights();
        clearNumberHighlights();

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

        applyRegionHighlights(r, c);

        Label clickedLbl = cellLabels[r][c];
        if (clickedLbl != null && clickedLbl.isVisible()) {
            String t = clickedLbl.getText();
            if (t != null && !t.isEmpty()) {
                try {
                    int v = Integer.parseInt(t);
                    highlightMatchingNumbersStrong(v);
                } catch (NumberFormatException ignore) { }
            }
        }
        
    }
    
    

    private void toggleNoteMode() {
        noteModeActive = !noteModeActive;
        gameInputManager.setNoteModeActive(noteModeActive);
        System.out.println("Note Mode " + (noteModeActive ? "ON" : "OFF"));
        clearNumberHighlights();
    }

    private void handleClearCell() {
        if (selectedRow == -1 || selectedCol == -1 || sudokuEngine == null) return;
        clearNumberHighlights();

        GridPane ng = noteGrids[selectedRow][selectedCol];
        if (ng != null && ng.isVisible()) {
            sudokuEngine.clearNotes(selectedRow, selectedCol);
            ng.setVisible(false);
            for (int k = 0; k < GRID_SIZE; k++) {
                if (noteLabels[selectedRow][selectedCol][k] != null) {
                    noteLabels[selectedRow][selectedCol][k].setVisible(false);
                }
            }
            return;
        }

        Label lbl = cellLabels[selectedRow][selectedCol];
        if (lbl != null && lbl.isVisible() && lbl.getStyleClass().contains("user-number-error")) {
            sudokuEngine.clearCell(selectedRow, selectedCol);
            lbl.setText("");
            lbl.setVisible(false);
            lbl.getStyleClass().remove("user-number-error");
            refreshNumberButtonsAvailability();
        }
    }
    
    @FXML
    private void handleMenuClick() {
        saveAndExitToHome();
    }
    
    private void showCharacterSelectionModal() {
        characterSelectionManager.show(modalContainer, opt -> selectCharacterFromOption(opt));
    }

    @FXML
    private void handleItemSelectionClick() {
        hideGameUIForSelection(true);
        showItemSelectionModal();
    }
    private void showItemSelectionModal() {
        if (modalContainer == null) return;
        itemSelectionManager.show(modalContainer, this::onItemSelectedFromOption);
    }

    private void onItemSelectedFromOption(view.manager.ItemSelectionManager.ItemOption opt) {
        try {
            if (modalContainer != null) {
                modalContainer.getChildren().clear();
                modalContainer.setVisible(false);
            }
            hideGameUIForSelection(false);

            if (opt != null && opt.id != null && !"NO_ITEM".equals(opt.id)) {
                if (runService != null) {
                    runService.addItem(opt.id);
                }
                if (inventorySlotsUIManager != null) {
                    inventorySlotsUIManager.addItemImage(opt.iconPath);
                }
            }

            completeLevelAndAdvance();
        } catch (Exception e) {
        System.err.println("Error handling item selection: " + e.getMessage());
            completeLevelAndAdvance();
        }
    }

    private void saveAndExitToHome() {
        try {
            if (runService != null && runService.getCurrentRun() != null) {
                if (sudokuEngine != null && runService.getCurrentRun().getCurrentLevelState() != null) {
                    int[][] ug = sudokuEngine.getUserGrid();
                    String data = model.domain.RunLevelState.convertGridToString(ug);
                    runService.getCurrentRun().getCurrentLevelState().setUserGridData(data);
                }
                runService.save();
            }
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/HomeScreen.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) mainGameArea.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio e ritorno alla Home: " + e.getMessage());
        }
    }
    

    private void selectCharacterFromOption(view.manager.CharacterSelectionManager.Option opt) {
        try {
            Image img = new Image(getClass().getResourceAsStream(opt.sprite));
            playerSpriteManager.applyTo(characterSpriteView, img, opt.id);
            modalContainer.setVisible(false);
            modalContainer.getStyleClass().clear();
            modalContainer.getChildren().clear();
            characterSelected = true;

            model.service.SessionService.setLastSelectedCharacter(opt.id);

            applyThemeForCharacter(opt.id);

            hideGameUIForSelection(false);
            updateLevelAndDifficultyUI();
            updateSkipButtonState();

            generateSudokuForCurrentLevel();
            spawnEnemyForCurrentLevel();

            int baseLives = gameDataService.getCharacterBaseLives(opt.id);
            if (baseLives < 0) baseLives = 0;
            if (livesUIManager == null) {
                livesUIManager = new LivesUIManager(livesHBox, "/assets/icons/utils/heart.png");
            }
            livesUIManager.setLives(baseLives);
            initialMaxLives = baseLives;
        } catch (Exception e) {
        System.err.println("Error setting selected character: " + e.getMessage());
        }
    }

    private void applyThemeForCharacter(String characterId) {
        if (mainGameArea == null) return;
        mainGameArea.getStyleClass().remove("theme-crusader");
        mainGameArea.getStyleClass().remove("theme-highwayman");
        mainGameArea.getStyleClass().remove("theme-jester");
        mainGameArea.getStyleClass().remove("theme-occultist");
        mainGameArea.getStyleClass().remove("theme-plague-doctor");

        String toAdd = null;
        if ("CRUSADER".equalsIgnoreCase(characterId)) {
            toAdd = "theme-crusader";
        } else if ("HIGHWAYMAN".equalsIgnoreCase(characterId)) {
            toAdd = "theme-highwayman";
        } else if ("JESTER".equalsIgnoreCase(characterId)) {
            toAdd = "theme-jester";
        } else if ("OCCULTIST".equalsIgnoreCase(characterId)) {
            toAdd = "theme-occultist";
        } else if ("PLAGUEDOCTOR".equalsIgnoreCase(characterId) || "PLAGUE_DOCTOR".equalsIgnoreCase(characterId)) {
            toAdd = "theme-plague-doctor";
        }
        if (toAdd != null) {
            mainGameArea.getStyleClass().add(toAdd);
            currentTheme = toAdd;
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
        System.err.println("No enemy available in difficulty '" + difficultyTier + "' not yet used in this run.");
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
        System.err.println("Unable to list enemy files for '" + difficultyDir + "': " + e.getMessage());
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
        hudManager.hideGameUIForSelection(hide, levelLabel, difficultyLabel, menuButton, characterSpriteView, enemySpriteView, sudokuGridContainer, livesHBox, inputControlHBox, inventorySlotsHBox);
    }

    private void updateLevelAndDifficultyUI() {
        String difficulty = gameDataService.getBaseDifficultyByLevel(currentLevel);
        if (difficulty == null || "UNKNOWN".equalsIgnoreCase(difficulty)) {
            difficulty = getDifficultyFallbackByLevel(currentLevel);
        }
        hudManager.updateLevelAndDifficultyUI(levelLabel, difficultyLabel, currentLevel, difficulty);
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
            Image img = new Image(getClass().getResourceAsStream(enemySpritePath));
            enemySpriteManager.applyTo(enemySpriteView, img, enemySpritePath);
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
        System.out.println("Run completed: last level reached.");
            return;
        }
        clearSelectedCell();
        clearRegionHighlights();
        clearNumberHighlights();
        currentLevel++;
        updateLevelAndDifficultyUI();
        applyBackgroundForCurrentLevel();
        generateSudokuForCurrentLevel();
        spawnEnemyForCurrentLevel();
        updateSkipButtonState();
        System.out.println("Level " + currentLevel + " started (skip).");
    }

    private void applyBackgroundForCurrentLevel() {
        try {
            boolean isBoss = gameDataService.isBossLevel(currentLevel);
            Image img = backgroundManager.selectRandomUnique(isBoss);
            if (img != null && backgroundImageView != null) {
                backgroundImageView.setImage(img);
            } else {
        System.err.println("Background not available for level " + currentLevel + ".");
            }
        } catch (Exception e) {
        System.err.println("Error applying background: " + e.getMessage());
        }
    }

    private void updateSkipButtonState() {
        if (menuButton != null) {
            boolean disable = !characterSelected || currentLevel >= totalLevels;
            menuButton.setDisable(disable);
        }
    }

    private void generateSudokuForCurrentLevel() {
        clearSelectedCell();
        clearRegionHighlights();
        clearNumberHighlights();
        RunFrozenBuffs frozen = new RunFrozenBuffs(Collections.emptyMap());
        SudokuGrid puzzle = sudokuGenerator.generateNewPuzzle(currentLevel, frozen);
        sudokuEngine = new model.engine.SudokuEngine(puzzle);
        applyPuzzleToUI(puzzle);
        clearRegionHighlights();
        clearNumberHighlights();
    }

    private void applyPuzzleToUI(SudokuGrid puzzle) {
        int[][] initial = puzzle.getInitialGrid();
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                int v = initial[r][c];
                if (v != 0) {
                    cellLabels[r][c].setText(String.valueOf(v));
                    cellLabels[r][c].setVisible(true);
                    cellLabels[r][c].getStyleClass().remove("user-number-error");
                    cellLabels[r][c].getStyleClass().remove("user-number-correct");
                    if (!cellLabels[r][c].getStyleClass().contains("initial-number")) {
                        cellLabels[r][c].getStyleClass().add("initial-number");
                    }
                    if (noteGrids[r][c] != null) noteGrids[r][c].setVisible(false);
                } else {
                    cellLabels[r][c].setText("");
                    cellLabels[r][c].setVisible(false);
                    cellLabels[r][c].getStyleClass().remove("initial-number");
                    cellLabels[r][c].getStyleClass().remove("user-number-error");
                    cellLabels[r][c].getStyleClass().remove("user-number-correct");
                    if (noteGrids[r][c] != null) noteGrids[r][c].setVisible(false);
                }
            }
        }
        refreshNumberButtonsAvailability();
    }

    private void handleNumberInput(int value) {
        if (sudokuEngine == null) return;

        if (selectedRow == -1 || selectedCol == -1) {
            highlightMatchingNumbersStrong(value);
            return;
        }

        Label currentLbl = cellLabels[selectedRow][selectedCol];
        if (currentLbl != null && currentLbl.isVisible()) {
            String ct = currentLbl.getText();
            if (ct != null && !ct.isEmpty()) {
                if (selectedCellVBox != null) {
                    selectedCellVBox.getStyleClass().remove("selected-cell");
                    if (!selectedCellVBox.getStyleClass().contains("unselected-cell")) {
                        selectedCellVBox.getStyleClass().add("unselected-cell");
                    }
                }
                clearRegionHighlights();
                clearNumberHighlights();
                highlightMatchingNumbersStrong(value);
                return;
            }
        }

        if (noteModeActive) {
            if (!sudokuEngine.isInitialCell(selectedRow, selectedCol) && sudokuEngine.getCellValue(selectedRow, selectedCol) == 0) {
                sudokuEngine.clearNotes(selectedRow, selectedCol);
                sudokuEngine.toggleNote(selectedRow, selectedCol, value);
                GridPane ng = noteGrids[selectedRow][selectedCol];
                if (ng != null) {
                    ng.setVisible(true);
                    for (int k = 0; k < GRID_SIZE; k++) {
                        if (noteLabels[selectedRow][selectedCol][k] != null) {
                            noteLabels[selectedRow][selectedCol][k].setVisible(k == 2);
                        }
                    }
                    noteLabels[selectedRow][selectedCol][2].setText(String.valueOf(value));
                }
                Label lbl = cellLabels[selectedRow][selectedCol];
                if (lbl != null) {
                    lbl.setText("");
                    lbl.setVisible(false);
                    lbl.getStyleClass().remove("user-number-error");
                    lbl.getStyleClass().remove("user-number-correct");
                }
                refreshNumberButtonsAvailability();
            }
            return;
        }

        boolean ok = sudokuEngine.insertValue(selectedRow, selectedCol, value);
        Label lbl = cellLabels[selectedRow][selectedCol];
        if (ok) {
            if (lbl != null) {
                lbl.setText(String.valueOf(value));
                lbl.setVisible(true);
                lbl.getStyleClass().remove("initial-number");
                lbl.getStyleClass().remove("user-number-error");
                if (!lbl.getStyleClass().contains("user-number-correct")) {
                    lbl.getStyleClass().add("user-number-correct");
                }
            }
            GridPane ng = noteGrids[selectedRow][selectedCol];
            if (ng != null) ng.setVisible(false);
            clearNumberHighlights();
            clearRegionHighlights();
            applyRegionHighlights(selectedRow, selectedCol);
            highlightMatchingNumbersStrong(value);
            if (sudokuEngine.checkWin()) {
                boolean isBoss = gameDataService.isBossLevel(currentLevel);
                if (isBoss) {
                    completeLevelAndAdvance();
                } else {
                    hideGameUIForSelection(true);
                    showItemSelectionModal();
                }
            }
        } else {
            if (!sudokuEngine.isInitialCell(selectedRow, selectedCol)) {
                if (lbl != null) {
                    lbl.setText(String.valueOf(value));
                    lbl.setVisible(true);
                    lbl.getStyleClass().remove("initial-number");
                    lbl.getStyleClass().remove("user-number-correct");
                    if (!lbl.getStyleClass().contains("user-number-error")) {
                        lbl.getStyleClass().add("user-number-error");
                    }
                }
                GridPane ng = noteGrids[selectedRow][selectedCol];
                if (ng != null) ng.setVisible(false);
                clearNumberHighlights();
                clearRegionHighlights();
                applyRegionHighlights(selectedRow, selectedCol);
                highlightMatchingNumbersStrong(value);
            }
            handleUserError();
        }
        refreshNumberButtonsAvailability();
    }

    private void applyCustomCursor() {
        cursorManager.apply(mainGameArea);
    }

    private LivesUIManager livesUIManager;
    private InventorySlotsUIManager inventorySlotsUIManager;
    private ItemUseManager itemUseManager;

    private void initializeInventoryInteraction() {
        if (inventorySlotsUIManager == null) return;
        itemUseManager = new ItemUseManager(
                () -> sudokuEngine,
                livesUIManager,
                inventorySlotsUIManager,
                runService,
                gameDataService,
                () -> currentLevel,
                () -> selectedRow,
                () -> selectedCol,
                () -> initialMaxLives,
                cellLabels,
                noteGrids,
                () -> { clearNumberHighlights(); clearRegionHighlights(); refreshNumberButtonsAvailability(); },
                (r, c) -> applyRegionHighlights(r, c),
                (value) -> highlightMatchingNumbersStrong(value),
                this::clearSelectedCell,
                () -> completeLevelAndAdvance(),
                () -> { hideGameUIForSelection(true); showItemSelectionModal(); }
        );
        inventorySlotsUIManager.setOnItemClicked(itemUseManager::handleItemClick);
    }

    

    private void handleUserError() {
        if (firstErrorProtectionActive && !firstErrorProtectionUsed) {
            firstErrorProtectionUsed = true;
        System.out.println("First error protection active: no life lost.");
            return;
        }
        if (livesUIManager != null && livesUIManager.getLives() > 0) {
            livesUIManager.loseLifeWithAnimation();
            if (livesUIManager.getLives() <= 0) {
                hideGameUIForSelection(true);
                endGameManager.showDefeat(modalContainer);
            }
        }
    }


    private void refreshNumberButtonsAvailability() {
        int[] counts = new int[10];
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (cellLabels[r][c].isVisible()) {
                    String t = cellLabels[r][c].getText();
                    if (t != null && !t.isEmpty()) {
                        try {
                            int v = Integer.parseInt(t);
                            if (v >= 1 && v <= 9) counts[v]++;
                        } catch (NumberFormatException ignore) { }
                    }
                }
            }
        }
        for (int i = 1; i <= 9; i++) {
            boolean enabled = counts[i] < 9;
            gameInputManager.setNumberEnabled(i, enabled);
        }
    }

    private void clearNumberHighlights() {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                VBox cell = sudokuCells[r][c];
                if (cell != null) {
                    cell.getStyleClass().remove("number-highlight");
                    cell.getStyleClass().remove("match-number-strong");
                }
            }
        }
        lastHighlightedNumber = null;
    }

    @SuppressWarnings("unused")
    private void highlightNumbers(int value) {
        clearNumberHighlights();

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Label lbl = cellLabels[r][c];
                if (lbl != null && lbl.isVisible()) {
                    String t = lbl.getText();
                    if (t != null && !t.isEmpty()) {
                        try {
                            int v = Integer.parseInt(t);
                            if (v == value) {
                                VBox cell = sudokuCells[r][c];
                                if (cell != null && !cell.getStyleClass().contains("number-highlight")) {
                                    cell.getStyleClass().add("number-highlight");
                                }
                            }
                        } catch (NumberFormatException ignore) { }
                    }
                }
            }
        }
        lastHighlightedNumber = value;
    }

    private void highlightMatchingNumbersStrong(int value) {
        clearNumberHighlights();
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Label lbl = cellLabels[r][c];
                if (lbl != null && lbl.isVisible()) {
                    String t = lbl.getText();
                    if (t != null && !t.isEmpty()) {
                        try {
                            int v = Integer.parseInt(t);
                            if (v == value) {
                                VBox cell = sudokuCells[r][c];
                                if (cell != null && !cell.getStyleClass().contains("match-number-strong")) {
                                    cell.getStyleClass().add("match-number-strong");
                                }
                            }
                        } catch (NumberFormatException ignore) { }
                    }
                }
            }
        }
        lastHighlightedNumber = value;
    }

    private void clearRegionHighlights() {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                VBox cell = sudokuCells[r][c];
                if (cell != null) {
                    cell.getStyleClass().remove("peer-highlight");
                }
            }
        }
    }

    private void applyRegionHighlights(int r, int c) {
        for (int cc = 0; cc < GRID_SIZE; cc++) {
            if (cc == c) continue;
            VBox cell = sudokuCells[r][cc];
            if (cell != null && !cell.getStyleClass().contains("peer-highlight")) {
                cell.getStyleClass().add("peer-highlight");
            }
        }
        for (int rr = 0; rr < GRID_SIZE; rr++) {
            if (rr == r) continue;
            VBox cell = sudokuCells[rr][c];
            if (cell != null && !cell.getStyleClass().contains("peer-highlight")) {
                cell.getStyleClass().add("peer-highlight");
            }
        }
        int boxStartR = (r / 3) * 3;
        int boxStartC = (c / 3) * 3;
        for (int rr = boxStartR; rr < boxStartR + 3; rr++) {
            for (int cc = boxStartC; cc < boxStartC + 3; cc++) {
                if (rr == r && cc == c) continue;
                VBox cell = sudokuCells[rr][cc];
                if (cell != null && !cell.getStyleClass().contains("peer-highlight")) {
                    cell.getStyleClass().add("peer-highlight");
                }
            }
        }
    }

    private void clearSelectedCell() {
        if (selectedCellVBox != null) {
            selectedCellVBox.getStyleClass().remove("selected-cell");
            if (!selectedCellVBox.getStyleClass().contains("unselected-cell")) {
                selectedCellVBox.getStyleClass().add("unselected-cell");
            }
            selectedCellVBox = null;
        }
        selectedRow = -1;
        selectedCol = -1;
    }

    private boolean isDescendantOf(Node node, Node ancestor) {
        if (node == null || ancestor == null) return false;
        Node cur = node;
        while (cur != null) {
            if (cur == ancestor) return true;
            cur = cur.getParent();
        }
        return false;
    }


}