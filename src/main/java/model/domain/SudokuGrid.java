package model.domain;

/**
 * Contiene i dati di un puzzle Sudoku appena generato, inclusa la soluzione.
 */
public class SudokuGrid {

    private final int[][] initialGrid; 
    private final int[][] solvedGrid; 
    private final String difficultyTier;

    public SudokuGrid(int[][] initialGrid, int[][] solvedGrid, String difficultyTier) {
        this.initialGrid = initialGrid;
        this.solvedGrid = solvedGrid;
        this.difficultyTier = difficultyTier;
    }

    public int[][] getInitialGrid() {
        return initialGrid;
    }

    public int[][] getSolvedGrid() {
        return solvedGrid;
    }

    public String getDifficultyTier() {
        return difficultyTier;
    }
}