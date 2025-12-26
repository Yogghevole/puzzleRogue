package view.manager;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.util.function.BiConsumer;
import model.domain.SudokuGrid;

/**
 * Manages the construction and update of the Sudoku grid UI.
 * Handles cell creation, styling, and displaying values/notes.
 */
public class SudokuUIManager {

    public void build(GridPane sudokuGridContainer,
                      int gridSize,
                      Label[][] cellLabels,
                      Label[][][] noteLabels,
                      GridPane[][] noteGrids,
                      VBox[][] sudokuCells,
                      BiConsumer<Integer, Integer> onCellClick) {
        double cellSize = Math.floor(650.0 / gridSize);
        sudokuGridContainer.setHgap(0);
        sudokuGridContainer.setVgap(0);
        sudokuGridContainer.setPadding(new Insets(0));

        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
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
                noteGrid.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                noteGrid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                noteGrid.setVisible(false);
                noteGrid.setHgap(0);
                noteGrid.setVgap(0);
                noteGrid.setTranslateX(36);
                noteGrid.setTranslateY(2);
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
                cellContainer.setOnMouseClicked(e -> onCellClick.accept(finalR, finalC));

                sudokuGridContainer.add(cellContainer, c, r);
            }
        }
        sudokuGridContainer.setPrefSize(650, 650);
        sudokuGridContainer.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        sudokuGridContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    public void applyPuzzleToUI(SudokuGrid puzzle,
                                int gridSize,
                                Label[][] cellLabels,
                                GridPane[][] noteGrids) {
        if (puzzle == null || cellLabels == null || noteGrids == null) return;
        int[][] initial = puzzle.getInitialGrid();
        java.util.Set<String> bonusCells = puzzle.getBonusCells();

        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                int v = initial[r][c];
                if (v != 0) {
                    cellLabels[r][c].setText(String.valueOf(v));
                    cellLabels[r][c].setVisible(true);
                    cellLabels[r][c].getStyleClass().remove("user-number-error");
                    cellLabels[r][c].getStyleClass().remove("user-number-correct");
                    if (!cellLabels[r][c].getStyleClass().contains("initial-number")) {
                        cellLabels[r][c].getStyleClass().add("initial-number");
                    }
                    
                    if (bonusCells != null && bonusCells.contains(r + "," + c)) {
                        if (!cellLabels[r][c].getStyleClass().contains("buff-revealed-cell")) {
                            cellLabels[r][c].getStyleClass().add("buff-revealed-cell");
                        }
                    } else {
                        cellLabels[r][c].getStyleClass().remove("buff-revealed-cell");
                    }

                    if (noteGrids[r][c] != null) noteGrids[r][c].setVisible(false);
                } else {
                    cellLabels[r][c].setText("");
                    cellLabels[r][c].setVisible(false);
                    cellLabels[r][c].getStyleClass().remove("initial-number");
                    cellLabels[r][c].getStyleClass().remove("user-number-error");
                    cellLabels[r][c].getStyleClass().remove("user-number-correct");
                    cellLabels[r][c].getStyleClass().remove("buff-revealed-cell");
                    if (noteGrids[r][c] != null) noteGrids[r][c].setVisible(false);
                }
            }
        }
    }

    public void applyInitialGridToUI(int[][] initial,
                                     int gridSize,
                                     Label[][] cellLabels,
                                     GridPane[][] noteGrids) {
        if (initial == null || cellLabels == null || noteGrids == null) return;
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
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
    }

    public void clearSelectedCellStyle(VBox selectedCellVBox) {
        if (selectedCellVBox == null) return;
        selectedCellVBox.getStyleClass().remove("selected-cell");
        if (!selectedCellVBox.getStyleClass().contains("unselected-cell")) {
            selectedCellVBox.getStyleClass().add("unselected-cell");
        }
    }

    public VBox selectCell(VBox[][] sudokuCells,
                           int r,
                           int c,
                           VBox previousSelected) {
        if (previousSelected != null) {
            clearSelectedCellStyle(previousSelected);
        }
        VBox newSelection = sudokuCells[r][c];
        newSelection.getStyleClass().remove("unselected-cell");
        newSelection.getStyleClass().add("selected-cell");
        return newSelection;
    }

    public void applyUserCorrect(int r,
                                 int c,
                                 int value,
                                 Label[][] cellLabels,
                                 GridPane[][] noteGrids) {
        Label lbl = (cellLabels != null && cellLabels[r] != null) ? cellLabels[r][c] : null;
        if (lbl != null) {
            lbl.setText(String.valueOf(value));
            lbl.setVisible(true);
            lbl.getStyleClass().remove("initial-number");
            lbl.getStyleClass().remove("user-number-error");
            if (!lbl.getStyleClass().contains("user-number-correct")) {
                lbl.getStyleClass().add("user-number-correct");
            }
        }
        if (noteGrids != null && noteGrids[r] != null && noteGrids[r][c] != null) {
            noteGrids[r][c].setVisible(false);
        }
    }

    public void applyUserError(int r,
                               int c,
                               int value,
                               Label[][] cellLabels,
                               GridPane[][] noteGrids) {
        Label lbl = (cellLabels != null && cellLabels[r] != null) ? cellLabels[r][c] : null;
        if (lbl != null) {
            lbl.setText(String.valueOf(value));
            lbl.setVisible(true);
            lbl.getStyleClass().remove("initial-number");
            lbl.getStyleClass().remove("user-number-correct");
            if (!lbl.getStyleClass().contains("user-number-error")) {
                lbl.getStyleClass().add("user-number-error");
            }
        }
        if (noteGrids != null && noteGrids[r] != null && noteGrids[r][c] != null) {
            noteGrids[r][c].setVisible(false);
        }
    }

    public void showNoteForValue(int r,
                                 int c,
                                 int value,
                                 Label[][][] noteLabels,
                                 GridPane[][] noteGrids,
                                 Label[][] cellLabels) {
        GridPane ng = (noteGrids != null && noteGrids[r] != null) ? noteGrids[r][c] : null;
        if (ng != null) {
            ng.setVisible(true);
            for (int k = 0; k < 9; k++) {
                Label nl = (noteLabels != null && noteLabels[r] != null && noteLabels[r][c] != null) ? noteLabels[r][c][k] : null;
                if (nl != null) {
                    nl.setVisible(k == 2);
                }
            }
            Label target = (noteLabels != null && noteLabels[r] != null && noteLabels[r][c] != null) ? noteLabels[r][c][2] : null;
            if (target != null) target.setText(String.valueOf(value));
        }
        Label lbl = (cellLabels != null && cellLabels[r] != null) ? cellLabels[r][c] : null;
        if (lbl != null) {
            lbl.setText("");
            lbl.setVisible(false);
            lbl.getStyleClass().remove("user-number-error");
            lbl.getStyleClass().remove("user-number-correct");
        }
    }

    public void refreshHighlightsAfterSelection(int r,
                                                int c,
                                                Label[][] cellLabels,
                                                SudokuHighlightManager highlightManager) {
        if (highlightManager == null) return;
        highlightManager.clearRegionHighlights();
        highlightManager.clearNumberHighlights();
        highlightManager.applyRegionHighlights(r, c);
        Label lbl = (cellLabels != null && cellLabels[r] != null) ? cellLabels[r][c] : null;
        if (lbl != null && lbl.isVisible()) {
            String t = lbl.getText();
            if (t != null && !t.isEmpty()) {
                try {
                    int v = Integer.parseInt(t);
                    highlightManager.highlightMatchingNumbersStrong(v);
                } catch (NumberFormatException ignore) {}
            }
        }
    }

    public void refreshHighlightsAfterInput(int r,
                                            int c,
                                            int value,
                                            SudokuHighlightManager highlightManager) {
        if (highlightManager == null) return;
        highlightManager.clearNumberHighlights();
        highlightManager.clearRegionHighlights();
        highlightManager.applyRegionHighlights(r, c);
        highlightManager.highlightMatchingNumbersStrong(value);
    }

    public void refreshHighlightsForNumberOnly(int value,
                                               SudokuHighlightManager highlightManager) {
        if (highlightManager == null) return;
        highlightManager.clearRegionHighlights();
        highlightManager.clearNumberHighlights();
        highlightManager.highlightMatchingNumbersStrong(value);
    }

    public void hideNotes(int r,
                          int c,
                          Label[][][] noteLabels,
                          GridPane[][] noteGrids) {
        GridPane ng = (noteGrids != null && noteGrids[r] != null) ? noteGrids[r][c] : null;
        if (ng != null) ng.setVisible(false);
        if (noteLabels != null && noteLabels[r] != null && noteLabels[r][c] != null) {
            for (int k = 0; k < 9; k++) {
                Label nl = noteLabels[r][c][k];
                if (nl != null) nl.setVisible(false);
            }
        }
    }

    public void clearErrorLabel(int r,
                                int c,
                                Label[][] cellLabels) {
        Label lbl = (cellLabels != null && cellLabels[r] != null) ? cellLabels[r][c] : null;
        if (lbl != null) {
            lbl.setText("");
            lbl.setVisible(false);
            lbl.getStyleClass().remove("user-number-error");
        }
    }
}