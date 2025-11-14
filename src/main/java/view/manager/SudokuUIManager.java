package view.manager;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.function.BiConsumer;

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
}