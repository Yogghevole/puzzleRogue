package view.manager;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SudokuHighlightManager {
    private final int gridSize;
    private final Label[][] cellLabels;
    private final VBox[][] sudokuCells;

    public SudokuHighlightManager(int gridSize, Label[][] cellLabels, VBox[][] sudokuCells) {
        this.gridSize = gridSize;
        this.cellLabels = cellLabels;
        this.sudokuCells = sudokuCells;
    }

    public void clearNumberHighlights() {
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                VBox cell = sudokuCells[r][c];
                if (cell != null) {
                    cell.getStyleClass().remove("number-highlight");
                    cell.getStyleClass().remove("match-number-strong");
                }
            }
        }
    }

    public void highlightNumbers(int value) {
        clearNumberHighlights();
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
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
    }

    public void highlightMatchingNumbersStrong(int value) {
        clearNumberHighlights();
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
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
    }

    public void clearRegionHighlights() {
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                VBox cell = sudokuCells[r][c];
                if (cell != null) {
                    cell.getStyleClass().remove("peer-highlight");
                }
            }
        }
    }

    public void applyRegionHighlights(int r, int c) {
        for (int cc = 0; cc < gridSize; cc++) {
            if (cc == c) continue;
            VBox cell = sudokuCells[r][cc];
            if (cell != null && !cell.getStyleClass().contains("peer-highlight")) {
                cell.getStyleClass().add("peer-highlight");
            }
        }
        for (int rr = 0; rr < gridSize; rr++) {
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
}