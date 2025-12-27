package view.controller;

import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;
import model.engine.SudokuEngine;
import model.service.GameDataService;
import model.service.RunService;
import view.manager.InventorySlotsUIManager;
import view.manager.LivesUIManager;
import view.manager.SoundManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

/**
 * Manages the usage of consumable items within the game.
 * Handles the logic and effects of different inventory items.
 */
public class ItemUseManager {
    private final Supplier<SudokuEngine> sudokuSupplier;
    private final LivesUIManager livesUIManager;
    private final InventorySlotsUIManager inventorySlotsUIManager;
    private final RunService runService;
    private final GameDataService gameDataService;
    private final IntSupplier currentLevelSupplier;
    private final IntSupplier selectedRowSupplier;
    private final IntSupplier selectedColSupplier;
    private final IntSupplier maxLivesSupplier;
    private final Label[][] cellLabels;
    private final GridPane[][] noteGrids;
    private final Runnable refreshHighlightsRunnable;
    private final BiConsumer<Integer, Integer> regionHighlighter;
    private final IntConsumer strongNumberHighlighter;
    private final Runnable clearSelectedCellRunnable;
    private final Runnable onWinBossRunnable;
    private final Runnable onWinNonBossRunnable;
    private final java.util.function.BiConsumer<String, Color> sudokuToastShower;

    public ItemUseManager(
            Supplier<SudokuEngine> sudokuSupplier,
            LivesUIManager livesUIManager,
            InventorySlotsUIManager inventorySlotsUIManager,
            RunService runService,
            GameDataService gameDataService,
            IntSupplier currentLevelSupplier,
            IntSupplier selectedRowSupplier,
            IntSupplier selectedColSupplier,
            IntSupplier maxLivesSupplier,
            Label[][] cellLabels,
            GridPane[][] noteGrids,
            Runnable refreshHighlightsRunnable,
            BiConsumer<Integer, Integer> regionHighlighter,
            IntConsumer strongNumberHighlighter,
            Runnable clearSelectedCellRunnable,
            Runnable onWinBossRunnable,
            Runnable onWinNonBossRunnable,
            java.util.function.BiConsumer<String, Color> sudokuToastShower
    ) {
        this.sudokuSupplier = sudokuSupplier;
        this.livesUIManager = livesUIManager;
        this.inventorySlotsUIManager = inventorySlotsUIManager;
        this.runService = runService;
        this.gameDataService = gameDataService;
        this.currentLevelSupplier = currentLevelSupplier;
        this.selectedRowSupplier = selectedRowSupplier;
        this.selectedColSupplier = selectedColSupplier;
        this.maxLivesSupplier = maxLivesSupplier;
        this.cellLabels = cellLabels;
        this.noteGrids = noteGrids;
        this.refreshHighlightsRunnable = refreshHighlightsRunnable;
        this.regionHighlighter = regionHighlighter;
        this.strongNumberHighlighter = strongNumberHighlighter;
        this.clearSelectedCellRunnable = clearSelectedCellRunnable;
        this.onWinBossRunnable = onWinBossRunnable;
        this.onWinNonBossRunnable = onWinNonBossRunnable;
        this.sudokuToastShower = sudokuToastShower;
    }

    public void handleItemClick(int index, String resourcePath, MouseEvent event) {
        if (resourcePath == null) return;
        if (resourcePath.endsWith("/hint_item.png") || resourcePath.endsWith("hint_item.png")) {
            handleHintItem(index);
        } else if (resourcePath.endsWith("/missing_heart_item.png") || resourcePath.endsWith("missing_heart_item.png")) {
            handleHeartItem(index);
        } else if (resourcePath.endsWith("/sacrifice_item.png") || resourcePath.endsWith("sacrifice_item.png")) {
            handleSacrificeItem(index);
        } else if (resourcePath.endsWith("/score_item.png") || resourcePath.endsWith("score_item.png")) {
            handleScoreItem(index);
        }
    }

    private void handleHintItem(int slotIndex) {
        SudokuEngine sudokuEngine = sudokuSupplier.get();
        if (inventorySlotsUIManager == null || sudokuEngine == null) {
            if (inventorySlotsUIManager != null) inventorySlotsUIManager.flashFailureOnSlot(slotIndex);
            SoundManager.getInstance().playInvalidClick();
            return;
        }
        int selectedRow = selectedRowSupplier.getAsInt();
        int selectedCol = selectedColSupplier.getAsInt();
        if (selectedRow < 0 || selectedCol < 0) {
            inventorySlotsUIManager.flashFailureOnSlot(slotIndex);
            SoundManager.getInstance().playInvalidClick();
            return;
        }
        if (sudokuEngine.isInitialCell(selectedRow, selectedCol)) {
            inventorySlotsUIManager.flashFailureOnSlot(slotIndex);
            SoundManager.getInstance().playInvalidClick();
            return;
        }

        Label lbl = cellLabels[selectedRow][selectedCol];
        if (lbl == null) {
            inventorySlotsUIManager.flashFailureOnSlot(slotIndex);
            return;
        }

        Optional<Integer> inserted = sudokuEngine.revealHintAt(selectedRow, selectedCol);
        if (!inserted.isPresent()) {
            inventorySlotsUIManager.flashFailureOnSlot(slotIndex);
            SoundManager.getInstance().playInvalidClick();
            return;
        }

        int value = inserted.get();
        lbl.setText(String.valueOf(value));
        lbl.setVisible(true);
        lbl.getStyleClass().remove("initial-number");
        lbl.getStyleClass().remove("user-number-error");
        if (!lbl.getStyleClass().contains("user-number-correct")) {
            lbl.getStyleClass().add("user-number-correct");
        }
        GridPane ng = noteGrids[selectedRow][selectedCol];
        if (ng != null) ng.setVisible(false);

        inventorySlotsUIManager.clearSlot(slotIndex);
        inventorySlotsUIManager.flashSuccessOnSlot(slotIndex);
        SoundManager.getInstance().playHint();
        try { runService.registerItemUse("HINT_ITEM"); } catch (Exception ignore) {}

        refreshHighlightsRunnable.run();
        regionHighlighter.accept(selectedRow, selectedCol);
        strongNumberHighlighter.accept(value);

        if (sudokuEngine.checkWin()) {
            int lvl = currentLevelSupplier.getAsInt();
            boolean isBoss = gameDataService.isBossLevel(lvl);
            int total = gameDataService.getTotalLevels();
            boolean isFinalLevel = lvl >= total;
            if (isBoss || isFinalLevel) {
                onWinBossRunnable.run();
            } else {
                onWinNonBossRunnable.run();
            }
        }
    }

    private void handleHeartItem(int slotIndex) {
        if (livesUIManager == null) {
            if (inventorySlotsUIManager != null) inventorySlotsUIManager.flashFailureOnSlot(slotIndex);
            SoundManager.getInstance().playInvalidClick();
            return;
        }
        int currentLives = livesUIManager.getLives();
        int maxLives = maxLivesSupplier.getAsInt();
        if (currentLives >= maxLives) {
            inventorySlotsUIManager.flashFailureOnSlot(slotIndex);
            SoundManager.getInstance().playInvalidClick();
            return;
        }
        livesUIManager.gainLifeWithAnimation(maxLives);
        inventorySlotsUIManager.clearSlot(slotIndex);
        inventorySlotsUIManager.flashSuccessOnSlot(slotIndex);
        SoundManager.getInstance().playHeal();
        try { runService.registerItemUse("LIFE_BOOST_ITEM"); } catch (Exception ignore) {}
    }

    private void handleSacrificeItem(int slotIndex) {
        SudokuEngine sudokuEngine = sudokuSupplier.get();
        if (livesUIManager == null || sudokuEngine == null) {
            if (inventorySlotsUIManager != null) inventorySlotsUIManager.flashFailureOnSlot(slotIndex);
            return;
        }
        int lives = livesUIManager.getLives();
        if (lives <= 1) {
            inventorySlotsUIManager.flashFailureOnSlot(slotIndex);
            SoundManager.getInstance().playInvalidClick();
            return;
        }

        ArrayList<int[]> empties = new ArrayList<>();
        int gridSize = cellLabels.length;
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (!sudokuEngine.isInitialCell(r, c)) {
                    boolean emptyByUI = (cellLabels[r][c] != null && !cellLabels[r][c].isVisible());
                    boolean emptyByEngine = sudokuEngine.getCellValue(r, c) == 0;
                    if (emptyByUI && emptyByEngine) {
                        empties.add(new int[]{r, c});
                    }
                }
            }
        }
        if (empties.isEmpty()) {
            inventorySlotsUIManager.flashFailureOnSlot(slotIndex);
            SoundManager.getInstance().playInvalidClick();
            return;
        }
        Collections.shuffle(empties, new Random());
        int revealCount = Math.min(2, empties.size());

        livesUIManager.loseLifeWithAnimation();
        SoundManager.getInstance().playSacrifice();

        for (int i = 0; i < revealCount; i++) {
            int[] pos = empties.get(i);
            int r = pos[0], c = pos[1];
            Optional<Integer> inserted = sudokuEngine.revealHintAt(r, c);
            if (inserted.isPresent()) {
                int value = inserted.get();
                Label lbl = cellLabels[r][c];
                if (lbl != null) {
                    lbl.setText(String.valueOf(value));
                    lbl.setVisible(true);
                    lbl.getStyleClass().remove("initial-number");
                    lbl.getStyleClass().remove("user-number-error");
                    if (!lbl.getStyleClass().contains("user-number-correct")) {
                        lbl.getStyleClass().add("user-number-correct");
                    }
                    playSacrificeRevealAnimation(lbl);
                }
                GridPane ng = noteGrids[r][c];
                if (ng != null) ng.setVisible(false);
            }
        }

        inventorySlotsUIManager.clearSlot(slotIndex);
        inventorySlotsUIManager.flashSuccessOnSlot(slotIndex);
        try { runService.registerItemUse("SACRIFICE_ITEM"); } catch (Exception ignore) {}

        refreshHighlightsRunnable.run();
        clearSelectedCellRunnable.run();
        if (sudokuEngine.checkWin()) {
            int lvl = currentLevelSupplier.getAsInt();
            boolean isBoss = gameDataService.isBossLevel(lvl);
            int total = gameDataService.getTotalLevels();
            boolean isFinalLevel = lvl >= total;
            if (isBoss || isFinalLevel) {
                onWinBossRunnable.run();
            } else {
                onWinNonBossRunnable.run();
            }
        }
    }

    private void handleScoreItem(int slotIndex) {
        if (inventorySlotsUIManager == null) {
            return;
        }
        try { runService.getCurrentRun(); } catch (Exception ignore) {}
        boolean used = false;
        try { used = runService.registerItemUse("SCORE_ITEM"); } catch (Exception e) { }
        if (used) {
            inventorySlotsUIManager.clearSlot(slotIndex);
            inventorySlotsUIManager.flashSuccessOnSlot(slotIndex);
            SoundManager.getInstance().playScore();
            try {
                int lvl = currentLevelSupplier.getAsInt();
                int pts = Math.max(1, lvl) * 10;
                if (sudokuToastShower != null) {
                    sudokuToastShower.accept("Score Bonus! +" + pts + " points", Color.WHITE);
                }
            } catch (Exception ignore) {}
        } else {
            inventorySlotsUIManager.flashFailureOnSlot(slotIndex);
            SoundManager.getInstance().playInvalidClick();
        }
    }

    private void playSacrificeRevealAnimation(Label lbl) {
        DropShadow goldGlow = new DropShadow();
        goldGlow.setRadius(18);
        goldGlow.setSpread(0.25);
        goldGlow.setColor(Color.rgb(255, 210, 80, 0.85));
        lbl.setEffect(goldGlow);

        lbl.setOpacity(0.0);
        lbl.setScaleX(0.6);
        lbl.setScaleY(0.6);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), lbl);
        fadeIn.setToValue(1.0);
        ScaleTransition pop = new ScaleTransition(Duration.millis(220), lbl);
        pop.setToX(1.0);
        pop.setToY(1.0);
        SequentialTransition seq = new SequentialTransition(fadeIn, pop);
        seq.setOnFinished(e -> lbl.setEffect(null));
        seq.play();
    }
}
