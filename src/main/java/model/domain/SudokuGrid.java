package model.domain;

/**
 * Immutable representation of a Sudoku puzzle configuration.
 */
public class SudokuGrid {

    private final int[][] initialGrid; 
    private final int[][] solvedGrid; 
    private final String difficultyTier;
    private final java.util.Set<String> bonusCells;

    public SudokuGrid(int[][] initialGrid, int[][] solvedGrid, String difficultyTier) {
        this(initialGrid, solvedGrid, difficultyTier, java.util.Collections.emptySet());
    }

    public SudokuGrid(int[][] initialGrid, int[][] solvedGrid, String difficultyTier, java.util.Set<String> bonusCells) {
        this.initialGrid = initialGrid;
        this.solvedGrid = solvedGrid;
        this.difficultyTier = difficultyTier;
        this.bonusCells = bonusCells;
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

    public java.util.Set<String> getBonusCells() {
        return bonusCells;
    }
}