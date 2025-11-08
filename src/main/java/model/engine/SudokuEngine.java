package model.engine;

import model.domain.SudokuGrid;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Implementazione concreta della logica del Sudoku.
 */
public class SudokuEngine implements PuzzleEngine {

    private final int[][] initialGrid;
    private final int[][] solvedGrid;
    private int[][] userGrid;

    @SuppressWarnings("unchecked")
    private final Set<Integer>[][] notes = new Set[SIZE][SIZE];
    
    private final Random random = new Random();
    private static final int SIZE = 9;

    public SudokuEngine(SudokuGrid puzzle) {
        this.initialGrid = puzzle.getInitialGrid();
        this.solvedGrid = puzzle.getSolvedGrid();
        this.userGrid = Arrays.stream(initialGrid).map(int[]::clone).toArray(int[][]::new);
        
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                this.notes[i][j] = new HashSet<>();
            }
        }
        
        IntStream.range(0, SIZE).forEach(r ->
            IntStream.range(0, SIZE).filter(c -> initialGrid[r][c] != 0).forEach(c -> notes[r][c].clear())
        );
    }
    
    // --- Operazioni Base ---
    
    @Override
    public boolean insertValue(int row, int col, int value) {
        if (initialGrid[row][col] != 0) {
            return false;
        }

        userGrid[row][col] = value;
        boolean correct = isCorrect(row, col, value);
        
        if (correct) {
            notes[row][col].clear();
            removeCandidateFromPeers(row, col, value);
        }
        return correct;
    }

    @Override
    public boolean isCorrect(int row, int col, int value) {
        return solvedGrid[row][col] == value;
    }

    @Override
    public void clearCell(int row, int col) {
        if (initialGrid[row][col] == 0) {
            userGrid[row][col] = 0;
        }
    }

    @Override
    public boolean checkWin() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (userGrid[r][c] != solvedGrid[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // --- Operazioni Note ---

    @Override
    public void toggleNote(int row, int col, int candidate) {
        if (initialGrid[row][col] == 0 && userGrid[row][col] == 0) {
            if (notes[row][col].contains(candidate)) {
                notes[row][col].remove(candidate);
            } else {
                notes[row][col].add(candidate);
            }
        }
    }

    @Override
    public void clearNotes(int row, int col) {
        notes[row][col].clear();
    }
    
    @Override
    public void removeCandidateFromPeers(int row, int col, int value) {
        IntStream.range(0, SIZE).forEach(i -> {
            notes[row][i].remove(value);
            notes[i][col].remove(value);
        });

        int boxRowStart = row - row % 3;
        int boxColStart = col - col % 3;
        for (int r = boxRowStart; r < boxRowStart + 3; r++) {
            for (int c = boxColStart; c < boxColStart + 3; c++) {
                notes[r][c].remove(value);
            }
        }
    }

    // --- Operazioni Oggetti/Buff  ---

    @Override
    public Optional<int[]> revealHint() {
        Set<int[]> emptyCells = new HashSet<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (initialGrid[r][c] == 0 && userGrid[r][c] == 0) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }

        if (emptyCells.isEmpty()) {
            return Optional.empty();
        }

        int randomIndex = random.nextInt(emptyCells.size());
        int[] coords = emptyCells.stream().skip(randomIndex).findFirst().get();
        int r = coords[0];
        int c = coords[1];
        
        int correctValue = solvedGrid[r][c];
        userGrid[r][c] = correctValue;
        
        notes[r][c].clear();
        removeCandidateFromPeers(r, c, correctValue);

        return Optional.of(new int[]{r, c, correctValue});
    }
    
    // --- Getters per l'UI/ViewModel ---

    @Override
    public int getCellValue(int row, int col) {
        return userGrid[row][col];
    }
    
    @Override
    public Set<Integer> getCellNotes(int row, int col) {
        return notes[row][col];
    }
    
    public boolean isInitialCell(int row, int col) {
        return initialGrid[row][col] != 0;
    }
    
    public int[][] getUserGrid() {
        return userGrid;
    }
}