package view.controller;

import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.*;
import javafx.scene.input.*;
import view.manager.*;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.stage.*;
import javafx.geometry.*;
import model.service.*;
import model.db.*;
import model.domain.*;
import java.util.*;
import view.util.*;
 

public class GameController {
    
    @FXML private StackPane mainGameArea; 
    @FXML private ImageView backgroundImageView;
    @FXML private StackPane modalContainer;
    @FXML private AnchorPane gameContentPane;
    @FXML private Label levelLabel;
    @FXML private Label difficultyLabel;
    @FXML private Button skipButton;
    @FXML private Button itemSelectionButton;
    @FXML private ImageView characterSpriteView;
    @FXML private ImageView enemySpriteView;
    @FXML private GridPane sudokuGridContainer;
    @FXML private StackPane sudokuAreaStack;
    @FXML private HBox livesHBox;
    @FXML private HBox inputControlHBox;
    @FXML private HBox inventorySlotsHBox;
    @FXML private HBox selectedBuffsHBox;
    @FXML private VBox buffInfoBox;
    @FXML private VBox characterSpriteBox;
    @FXML private VBox enemySpriteBox;
    
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
    private final EnemySpriteManager enemySpriteManager = new EnemySpriteManager();
    private final PlayerSpriteManager playerSpriteManager = new PlayerSpriteManager();
    private final CursorManager cursorManager = new CursorManager();
    private final CharacterSelectionManager characterSelectionManager = new CharacterSelectionManager();
    private final ItemSelectionManager itemSelectionManager = new ItemSelectionManager();
    private final SudokuUIManager sudokuUIManager = new SudokuUIManager();
    private final GameInputManager gameInputManager = new GameInputManager();
    private final HudManager hudManager = new HudManager();
    private final EndGameManager endGameManager = new EndGameManager();
    private final SudokuHighlightManager sudokuHighlightManager = new SudokuHighlightManager(GRID_SIZE, cellLabels, sudokuCells);
    private final LayoutBindingManager layoutBindingManager = new LayoutBindingManager();
    private InGameSettingsManager settingsManager;
    private boolean firstErrorProtectionActive = false;
    private boolean firstErrorProtectionUsed = false;
    private boolean noteModeActive = false;
    @SuppressWarnings("unused")
    private String currentTheme = null;
    private int initialMaxLives = 0;
    private boolean characterSelectInProgress = false;

    public void setRunService(RunService runService) {
        this.runService = runService;
        try {
            if (inventorySlotsUIManager != null) {
                initializeInventoryInteraction();
            }
        } catch (Exception ignore) {}
    }
    
    @FXML private ImageView headerImageView;
    @FXML private ImageView footerImageView;
    @FXML private Pane headerDimmer;
    @FXML private Pane footerDimmer;
    
    @FXML
    public void initialize() {
        try {
            if (backgroundImageView != null) {
                backgroundImageView.setPreserveRatio(true);
                StackPane.setAlignment(backgroundImageView, Pos.CENTER);
                backgroundImageView.setPickOnBounds(true);
                backgroundImageView.setOnMouseClicked(e -> {
                    clearSelectedCell();
                    sudokuHighlightManager.clearNumberHighlights();
                    sudokuHighlightManager.clearRegionHighlights();
                });
            }
            if (mainGameArea != null) {
                mainGameArea.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                    Node target = (Node) e.getTarget();
                    boolean outsideSudoku = (sudokuGridContainer != null && !view.util.NodeUtils.isDescendantOf(target, sudokuGridContainer));
                    boolean onInputs = (inputControlHBox != null && view.util.NodeUtils.isDescendantOf(target, inputControlHBox))
                                     || (inventorySlotsHBox != null && view.util.NodeUtils.isDescendantOf(target, inventorySlotsHBox));
                    if (outsideSudoku && !onInputs) {
                        clearSelectedCell();
                        sudokuHighlightManager.clearNumberHighlights();
                        sudokuHighlightManager.clearRegionHighlights();
                    }
                });
                if (mainGameArea.getScene() != null) {
                    layoutBindingManager.bindToSceneSize(mainGameArea.getScene(), mainGameArea, headerImageView, footerImageView);
                } else {
                    mainGameArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
                        if (newScene != null) layoutBindingManager.bindToSceneSize(newScene, mainGameArea, headerImageView, footerImageView);
                    });
                }
                layoutBindingManager.bindBackgroundToMainArea(mainGameArea, backgroundImageView, gameContentPane);
            }
            backgroundManager.preloadAll();
            
        } catch (Exception e) {
        System.err.println("Error loading font or images: " + e.getMessage());
        } 
        
        buildSudokuGrid();

        gameInputManager.setupKeyboardShortcuts(
                mainGameArea,
                this::handleNumberInput,
                this::toggleNoteMode,
                this::handleClearCell
        );

        settingsManager = new InGameSettingsManager(
                modalContainer,
                mainGameArea,
                () -> {
                    try {
                        if (runService != null && runService.getCurrentRun() != null) {
                            if (sudokuEngine != null && runService.getCurrentRun().getCurrentLevelState() != null) {
                                int[][] ug = sudokuEngine.getUserGrid();
                                String data = model.domain.RunLevelState.convertGridToString(ug);
                                runService.getCurrentRun().getCurrentLevelState().setUserGridData(data);
                            }
                            runService.save();
                        }
                        try { if (settingsManager != null) settingsManager.hide(); else ModalUtils.hideAndClear(modalContainer); } catch (Exception ignore) {}
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/HomeScreen.fxml"));
                        javafx.scene.Parent root = loader.load();
                        Stage stage = (Stage) mainGameArea.getScene().getWindow();
                        StageUtils.setSceneRoot(stage, root);
                    } catch (Exception e) {
                        System.err.println("Errore nel salvataggio e ritorno alla Home: " + e.getMessage());
                    }
                },
                () -> {
                    try { if (settingsManager != null) settingsManager.hide(); else ModalUtils.hideAndClear(modalContainer); } catch (Exception ignore) {}
                    boolean isFinal = currentLevel >= totalLevels;
                    boolean isBoss = false;
                    try { if (gameDataService != null) isBoss = gameDataService.isBossLevel(currentLevel); } catch (Exception ignore) {}
                    hideGameUIForSelection(true);
                    if (isFinal || isBoss) {
                        completeLevelAndAdvance();
                    } else {
                        showItemSelectionModal();
                    }
                },
                () -> setGameInteractivity(false),
                () -> setGameInteractivity(true)
        );

        if (skipButton != null) {
            skipButton.setOnAction(e -> {
                setGameInteractivity(false);
                if (settingsManager != null) settingsManager.show();
            });
            hudManager.setupSkipButtonHoverEffects(skipButton);
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
                hudManager.renderSelectedBuffs(selectedBuffsHBox);
                hudManager.renderBuffInfo(buffInfoBox, gameDataService);
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
                    boolean insideGrid = view.util.NodeUtils.isDescendantOf(n, sudokuGridContainer);
                    boolean insideControls = view.util.NodeUtils.isDescendantOf(n, inputControlHBox) || view.util.NodeUtils.isDescendantOf(n, inventorySlotsHBox);
                    if (!insideGrid && !insideControls) {
                        clearSelectedCell();
                        sudokuHighlightManager.clearNumberHighlights();
                    }
                } else {
                    clearSelectedCell();
                    sudokuHighlightManager.clearNumberHighlights();
                }
            });
        }
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
        selectedCellVBox = sudokuUIManager.selectCell(sudokuCells, r, c, selectedCellVBox);
        selectedRow = r;
        selectedCol = c;
        sudokuUIManager.refreshHighlightsAfterSelection(r, c, cellLabels, sudokuHighlightManager);
        
    }
    
    

    private void toggleNoteMode() {
        noteModeActive = !noteModeActive;
        gameInputManager.setNoteModeActive(noteModeActive);
        sudokuHighlightManager.clearNumberHighlights();
    }

    private void handleClearCell() {
        if (selectedRow == -1 || selectedCol == -1 || sudokuEngine == null) return;
        sudokuHighlightManager.clearNumberHighlights();

        GridPane ng = noteGrids[selectedRow][selectedCol];
        if (ng != null && ng.isVisible()) {
            sudokuEngine.clearNotes(selectedRow, selectedCol);
            sudokuUIManager.hideNotes(selectedRow, selectedCol, noteLabels, noteGrids);
            return;
        }

        Label lbl = cellLabels[selectedRow][selectedCol];
        if (lbl != null && lbl.isVisible() && lbl.getStyleClass().contains("user-number-error")) {
            sudokuEngine.clearCell(selectedRow, selectedCol);
            sudokuUIManager.clearErrorLabel(selectedRow, selectedCol, cellLabels);
            gameInputManager.refreshNumberButtonsAvailability(cellLabels);
        }
    }
    
    private void showCharacterSelectionModal() {
        try { view.manager.SoundManager.getInstance().fadeOutMusic(400); } catch (Exception ignore) {}
        characterSelectionManager.show(modalContainer, opt -> selectCharacterFromOption(opt));
        try {
            PauseTransition pt = new PauseTransition(Duration.millis(300));
            pt.setOnFinished(ev -> { try { view.manager.SoundManager.getInstance().playCharacterSelection(); } catch (Exception ignore) {} });
            pt.play();
        } catch (Exception ignore) {}
    }

    @FXML
    private void handleItemSelectionClick() {
        boolean isFinal = currentLevel >= totalLevels;
        boolean isBoss = false;
        try { if (gameDataService != null) isBoss = gameDataService.isBossLevel(currentLevel); } catch (Exception ignore) {}
        hideGameUIForSelection(true);
        if (isFinal || isBoss) {
            completeLevelAndAdvance();
            return;
        }
        showItemSelectionModal();
    }
    private void showItemSelectionModal() {
        if (modalContainer == null) return;
        try {
            boolean isLastLevel = (currentLevel >= totalLevels);
            boolean isBoss = false;
            try { if (gameDataService != null) isBoss = gameDataService.isBossLevel(currentLevel); } catch (Exception ignore) {}
            if (isBoss || isLastLevel) {
                return;
            }
        } catch (Exception ignore) {}
        try { view.manager.SoundManager.getInstance().fadeOutMusic(400); } catch (Exception ignore) {}
        try { view.manager.SoundManager.getInstance().playWinItemSelection(); } catch (Exception ignore) {}
        itemSelectionManager.show(modalContainer, this::onItemSelectedFromOption);
    }

    private void onItemSelectedFromOption(view.manager.ItemSelectionManager.ItemOption opt) {
        try {
        if (modalContainer != null) {
            view.util.ModalUtils.hideAndClear(modalContainer);
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

    private void selectCharacterFromOption(view.manager.CharacterSelectionManager.Option opt) {
        if (characterSelectInProgress) return;
        characterSelectInProgress = true;
        try {
            try {
                view.manager.SoundManager.getInstance().playCharacterSelectFor(opt.id, () -> {
                    try {
                        String cat = backgroundManager.getLastSelectedCategory();
                        view.manager.SoundManager.getInstance().playLevelMusicForCategory(cat);
                    } catch (Exception ignore2) {}
                });
            } catch (Exception ignore) {}
            Image img = new Image(getClass().getResourceAsStream(opt.sprite));
            playerSpriteManager.applyTo(characterSpriteView, img, opt.id);
            view.util.ModalUtils.hideAndClear(modalContainer);
            characterSelected = true;

            model.service.SessionService.setLastSelectedCharacter(opt.id);

            applyThemeForCharacter(opt.id);

            hideGameUIForSelection(false);
            updateLevelAndDifficultyUI();
            hudManager.updateSkipButtonState(skipButton, gameDataService.isBossLevel(currentLevel), characterSelected);

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
        } finally {
            characterSelectInProgress = true;
        }
    }

    private void applyThemeForCharacter(String characterId) {
        String added = hudManager.applyThemeForCharacter(mainGameArea, characterId);
        currentTheme = added;
    }

    

    private void hideGameUIForSelection(boolean hide) {
        hudManager.hideGameUIForSelection(hide, levelLabel, difficultyLabel, characterSpriteView, enemySpriteView, sudokuGridContainer, livesHBox, inputControlHBox, inventorySlotsHBox, skipButton);
    }

    private void updateLevelAndDifficultyUI() {
        String difficulty = gameDataService.getBaseDifficultyByLevel(currentLevel);
        if (difficulty == null || "UNKNOWN".equalsIgnoreCase(difficulty)) {
            difficulty = gameDataService.getDifficultyFallbackByLevel(currentLevel);
        }
        hudManager.updateLevelAndDifficultyUI(levelLabel, difficultyLabel, currentLevel, difficulty);
    }

    private void spawnEnemyForCurrentLevel() {
        enemySpriteManager.spawnForLevel(enemySpriteView, difficultyLabel, gameDataService, currentLevel, usedEnemyGlobal, rng);
    }

    private void completeLevelAndAdvance() {
        if (!characterSelected) {
            return;
        }
        if (runService != null) {
            try {
                boolean isFinalLevel = false;
                try {
                    isFinalLevel = (currentLevel >= totalLevels);
                } catch (Exception ignore) {}

                if (isFinalLevel) {
                    int remainingVisible = 0;
                    try { if (inventorySlotsUIManager != null) remainingVisible = inventorySlotsUIManager.getVisibleItemCount(); } catch (Exception ignore) {}
                    runService.endLevelWithRemainingOverride(true, remainingVisible);
                } else {
                    runService.endLevel(true);
                }
            } catch (Exception e) {
                System.err.println("Errore nell'avanzamento livello: " + e.getMessage());
            }
        }
        clearSelectedCell();
        sudokuHighlightManager.clearRegionHighlights();
        sudokuHighlightManager.clearNumberHighlights();
        try {
            if (runService != null && runService.getCurrentRun() != null && runService.getCurrentRun().getCurrentLevelState() != null) {
                currentLevel = runService.getCurrentRun().getCurrentLevelState().getCurrentLevel();
                updateLevelAndDifficultyUI();
                applyBackgroundForCurrentLevel();
                sudokuEngine = runService.getCurrentEngine();
                int[][] init = model.domain.RunLevelState.convertStringToGrid(runService.getCurrentRun().getCurrentLevelState().getInitialGridData());
                sudokuUIManager.applyInitialGridToUI(init, GRID_SIZE, cellLabels, noteGrids);
                spawnEnemyForCurrentLevel();
                hudManager.updateSkipButtonState(skipButton, gameDataService.isBossLevel(currentLevel), characterSelected);
            } else {
                try { view.manager.SoundManager.getInstance().fadeOutMusic(400); } catch (Exception ignore) {}
                hudManager.updateSkipButtonState(skipButton, gameDataService.isBossLevel(currentLevel), characterSelected);
                try {
                    hideGameUIForSelection(true);
                    if (headerDimmer != null) headerDimmer.setVisible(true);
                    if (footerDimmer != null) footerDimmer.setVisible(true);
                    if (skipButton != null) skipButton.setVisible(false);
                    model.service.PointService.ScoreBreakdown bd = null;
                    try {
                        if (runService != null) {
                            bd = runService.getLastRunBreakdown();
                        }
                    } catch (Exception ignore) {}
                    try { view.manager.SoundManager.getInstance().playWin(); } catch (Exception ignore) {}
                    endGameManager.showVictory(modalContainer, bd);
                } catch (Exception e) {
                    System.err.println("Errore nel mostrare la schermata di vittoria: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel refresh UI livello: " + e.getMessage());
        }
    }

    private void applyBackgroundForCurrentLevel() {
        try {
            boolean isBoss = gameDataService.isBossLevel(currentLevel);
            backgroundManager.applyRandomForLevel(backgroundImageView, isBoss);
            try {
                String cat = backgroundManager.getLastSelectedCategory();
                if (characterSelected) {
                    view.manager.SoundManager.getInstance().playLevelMusicForCategory(cat);
                }
            } catch (Exception ignore) {}
        } catch (Exception e) {
        System.err.println("Error applying background: " + e.getMessage());
        }
    }

    

    private void generateSudokuForCurrentLevel() {
        clearSelectedCell();
        sudokuHighlightManager.clearRegionHighlights();
        sudokuHighlightManager.clearNumberHighlights();
        RunFrozenBuffs frozen = new RunFrozenBuffs(Collections.emptyMap());
        SudokuGrid puzzle = sudokuGenerator.generateNewPuzzle(currentLevel, frozen);
        sudokuEngine = new model.engine.SudokuEngine(puzzle);
        applyPuzzleToUI(puzzle);
        sudokuHighlightManager.clearRegionHighlights();
        sudokuHighlightManager.clearNumberHighlights();
    }

    private void applyPuzzleToUI(SudokuGrid puzzle) {
        sudokuUIManager.applyPuzzleToUI(puzzle, GRID_SIZE, cellLabels, noteGrids);
        gameInputManager.refreshNumberButtonsAvailability(cellLabels);
    }

    private void handleNumberInput(int value) {
        if (sudokuEngine == null) return;

        if (selectedRow == -1 || selectedCol == -1) {
            sudokuHighlightManager.highlightMatchingNumbersStrong(value);
            return;
        }

        Label currentLbl = cellLabels[selectedRow][selectedCol];
        if (currentLbl != null && currentLbl.isVisible()) {
            String ct = currentLbl.getText();
            if (ct != null && !ct.isEmpty()) {
                if (selectedCellVBox != null) {
                    sudokuUIManager.clearSelectedCellStyle(selectedCellVBox);
                }
                sudokuUIManager.refreshHighlightsForNumberOnly(value, sudokuHighlightManager);
                return;
            }
        }

        if (noteModeActive) {
            if (!sudokuEngine.isInitialCell(selectedRow, selectedCol) && sudokuEngine.getCellValue(selectedRow, selectedCol) == 0) {
                sudokuEngine.clearNotes(selectedRow, selectedCol);
                sudokuEngine.toggleNote(selectedRow, selectedCol, value);
                sudokuUIManager.showNoteForValue(selectedRow, selectedCol, value, noteLabels, noteGrids, cellLabels);
                gameInputManager.refreshNumberButtonsAvailability(cellLabels);
            }
            return;
        }

        boolean ok = sudokuEngine.insertValue(selectedRow, selectedCol, value);
        if (ok) {
            sudokuUIManager.applyUserCorrect(selectedRow, selectedCol, value, cellLabels, noteGrids);
            try { view.manager.SoundManager.getInstance().playCorrect(); } catch (Exception ignore) {}
            sudokuHighlightManager.clearNumberHighlights();
            sudokuHighlightManager.clearRegionHighlights();
            sudokuHighlightManager.applyRegionHighlights(selectedRow, selectedCol);
            sudokuHighlightManager.highlightMatchingNumbersStrong(value);
            if (sudokuEngine.checkWin()) {
                try { view.manager.SoundManager.getInstance().fadeOutMusic(300); } catch (Exception ignore) {}
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
                sudokuUIManager.applyUserError(selectedRow, selectedCol, value, cellLabels, noteGrids);
                try { view.manager.SoundManager.getInstance().playError(); } catch (Exception ignore) {}
                sudokuHighlightManager.clearNumberHighlights();
                sudokuHighlightManager.clearRegionHighlights();
                sudokuHighlightManager.applyRegionHighlights(selectedRow, selectedCol);
                sudokuHighlightManager.highlightMatchingNumbersStrong(value);
            }
            handleUserError();
        }
        gameInputManager.refreshNumberButtonsAvailability(cellLabels);
    }

    private void applyCustomCursor() {
        cursorManager.apply(mainGameArea);
    }

    private LivesUIManager livesUIManager;
    private InventorySlotsUIManager inventorySlotsUIManager;
    private ItemUseManager itemUseManager;

    private void initializeInventoryInteraction() {
        if (inventorySlotsUIManager == null) return;
        if (runService == null) {
            inventorySlotsUIManager.setOnItemClicked((i, p, e) -> {
                try {
                    inventorySlotsUIManager.flashFailureOnSlot(i);
                    view.manager.SoundManager.getInstance().playInvalidClick();
                } catch (Exception ignore) {}
            });
            return;
        }
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
                () -> { sudokuHighlightManager.clearNumberHighlights(); sudokuHighlightManager.clearRegionHighlights(); gameInputManager.refreshNumberButtonsAvailability(cellLabels); },
                (r, c) -> sudokuHighlightManager.applyRegionHighlights(r, c),
                (value) -> sudokuHighlightManager.highlightMatchingNumbersStrong(value),
                this::clearSelectedCell,
                () -> completeLevelAndAdvance(),
                () -> { hideGameUIForSelection(true); showItemSelectionModal(); },
                this::showSudokuToast
        );
        inventorySlotsUIManager.setOnItemClicked(itemUseManager::handleItemClick);
    }

    private void showSudokuToast(String text, javafx.scene.paint.Color color) {
        view.util.ToastUtils.showSudokuToast(sudokuAreaStack, text, color);
    }

    private void handleUserError() {
        if (firstErrorProtectionActive && !firstErrorProtectionUsed) {
            firstErrorProtectionUsed = true;
            try { if (runService != null) runService.registerErrorEvent("protezione primo errore attiva"); } catch (Exception ignore) {}
        return;
        }
        if (livesUIManager != null && livesUIManager.getLives() > 0) {
            if (livesUIManager.getLives() > 1) {
                try { if (runService != null) runService.registerErrorEvent("errore dell'utente"); } catch (Exception ignore) {}
            }
            livesUIManager.loseLifeWithAnimation();
            if (livesUIManager.getLives() <= 0) {
                try { view.manager.SoundManager.getInstance().stopMusic(); } catch (Exception ignore) {}
                hideGameUIForSelection(true);
                if (headerDimmer != null) headerDimmer.setVisible(true);
                if (footerDimmer != null) footerDimmer.setVisible(true);
                if (skipButton != null) skipButton.setVisible(false);
                model.service.PointService.ScoreBreakdown bd = null;
                try {
                    if (runService != null) {
                        int remainingVisible = 0;
                        try {
                            if (inventorySlotsUIManager != null) remainingVisible = inventorySlotsUIManager.getVisibleItemCount();
                        } catch (Exception ignore) {}
                        runService.endRunWithRemainingItems(false, remainingVisible);
                        bd = runService.getLastRunBreakdown();
                    }
                } catch (Exception ignore) {}
                try { view.manager.SoundManager.getInstance().playLoss(); } catch (Exception ignore) {}
                endGameManager.showDefeat(modalContainer, bd);
            }
        }
    }



    private void clearSelectedCell() {
        if (selectedCellVBox != null) {
            sudokuUIManager.clearSelectedCellStyle(selectedCellVBox);
            selectedCellVBox = null;
        }
        selectedRow = -1;
        selectedCol = -1;
    }

    private void setGameInteractivity(boolean enabled) {
        try {
            if (sudokuGridContainer != null) sudokuGridContainer.setDisable(!enabled);
            if (inputControlHBox != null) inputControlHBox.setMouseTransparent(!enabled);
            if (gameInputManager != null) gameInputManager.setInteractivity(enabled);
            if (inventorySlotsHBox != null) inventorySlotsHBox.setDisable(!enabled);
            if (skipButton != null) skipButton.setMouseTransparent(!enabled);
            if (itemSelectionButton != null) itemSelectionButton.setDisable(!enabled);
            if (enabled) {
                gameInputManager.setNoteModeActive(noteModeActive);
            }
        } catch (Exception ignore) {}
    }
}
