package model.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import model.domain.SudokuGrid;
import model.domain.RunFrozenBuffs;

/**
 * Responsabile della generazione di una griglia Sudoku completa e della rimozione
 * delle celle per raggiungere una difficoltà target (derivata dal livello).
 */
public class SudokuGenerator {

    private final GameDataService gameDataService;
    private final Random random = new Random();
    private static final int GRID_SIZE = 9;
    private static final String STARTING_CELLS_BUFF_ID = "STARTING_CELLS"; 

    public SudokuGenerator(GameDataService gameDataService) {
        this.gameDataService = gameDataService;
    }

    public SudokuGrid generateNewPuzzle(int levelNumber, RunFrozenBuffs frozenBuffs) {
        
        String baseDifficulty = gameDataService.getBaseDifficultyByLevel(levelNumber);

        int cellsToKeep = determineCellsToKeep(baseDifficulty, frozenBuffs);

        int[][] solvedGrid = generateSolvedGrid();

        int[][] initialGrid = removeCells(solvedGrid, cellsToKeep);
        
        return new SudokuGrid(initialGrid, solvedGrid, baseDifficulty);
    }
    
    private int determineCellsToKeep(String baseDifficulty, RunFrozenBuffs frozenBuffs) {
        int targetClues;
        
        switch (baseDifficulty) {
            case "EASY": targetClues = 40; break;
            case "MEDIUM": targetClues = 35; break;
            case "HARD": targetClues = 30; break;
            case "EXPERT": targetClues = 25; break;
            case "NIGHTMARE": targetClues = 22; break;
            default: targetClues = 38;
        }
        
        int buffLevel = frozenBuffs.getBuffLevel(STARTING_CELLS_BUFF_ID); 
        
        if (buffLevel > 0) {
            double extraCells = gameDataService
                .getBuffLevelData(STARTING_CELLS_BUFF_ID, buffLevel)
                .getOrDefault("value", 0.0)
                .doubleValue();
            
            targetClues += (int) extraCells; 
        }

        return targetClues;
    }

    private int[][] generateSolvedGrid() {
        int[][] grid = new int[GRID_SIZE][GRID_SIZE];
        solveGrid(grid, 0, 0);
        return grid;
    }

    private boolean solveGrid(int[][] grid, int row, int col) {
        if (row == GRID_SIZE) {
            return true; 
        }

        int nextRow = col == GRID_SIZE - 1 ? row + 1 : row;
        int nextCol = col == GRID_SIZE - 1 ? 0 : col + 1;

        Integer[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        Collections.shuffle(Arrays.asList(numbers));

        for (int num : numbers) {
            if (isSafe(grid, row, col, num)) {
                grid[row][col] = num;
                if (solveGrid(grid, nextRow, nextCol)) {
                    return true;
                }
                grid[row][col] = 0;
            }
        }
        return false;
    }

    private boolean isSafe(int[][] grid, int row, int col, int num) {
        for (int d = 0; d < GRID_SIZE; d++) {
            if (grid[row][d] == num || grid[d][col] == num) return false;
        }
        int boxRowStart = row - row % 3;
        int boxColStart = col - col % 3;
        for (int r = boxRowStart; r < boxRowStart + 3; r++) {
            for (int d = boxColStart; d < boxColStart + 3; d++) {
                if (grid[r][d] == num) return false;
            }
        }
        return true;
    }

    private int[][] removeCells(int[][] solvedGrid, int cellsToKeep) {
        int[][] puzzleGrid = Arrays.stream(solvedGrid).map(int[]::clone).toArray(int[][]::new);
        int totalCells = GRID_SIZE * GRID_SIZE;
        int cellsToRemove = totalCells - cellsToKeep;
        
        for (int i = 0; i < cellsToRemove; i++) {
            int row, col;
            do {
                row = random.nextInt(GRID_SIZE);
                col = random.nextInt(GRID_SIZE);
            } while (puzzleGrid[row][col] == 0);

            puzzleGrid[row][col] = 0;
        }
        return puzzleGrid;
    }
}