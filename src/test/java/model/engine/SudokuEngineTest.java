package model.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import model.domain.SudokuGrid;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the SudokuEngine class.
 * Verifies core game mechanics including value insertion, win checking,
 * note toggling, and hint system functionality.
 */
public class SudokuEngineTest {

    private int[][] solved() {
        return new int[][]{
            {5,3,4,6,7,8,9,1,2},
            {6,7,2,1,9,5,3,4,8},
            {1,9,8,3,4,2,5,6,7},
            {8,5,9,7,6,1,4,2,3},
            {4,2,6,8,5,3,7,9,1},
            {7,1,3,9,2,4,8,5,6},
            {9,6,1,5,3,7,2,8,4},
            {2,8,7,4,1,9,6,3,5},
            {3,4,5,2,8,6,1,7,9}
        };
    }

    private int[][] initialFromSolved(int[][] solved) {
        int[][] init = new int[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                init[r][c] = (r % 2 == 0 && c % 3 == 0) ? solved[r][c] : 0;
            }
        }
        return init;
    }

    private SudokuEngine newEngine() {
        int[][] s = solved();
        int[][] i = initialFromSolved(s);
        SudokuGrid grid = new SudokuGrid(i, s, "EASY");
        return new SudokuEngine(grid);
    }

    @Test
    void insert_and_check_win_behaviour() {
        SudokuEngine engine = newEngine();
        assertEquals(0, engine.getCellValue(0, 1));

        assertTrue(engine.insertValue(0, 1, solved()[0][1]));
        assertEquals(solved()[0][1], engine.getCellValue(0, 1));

        assertFalse(engine.insertValue(0, 2, 1));

        assertFalse(engine.checkWin());
    }

    @Test
    void notes_toggle_and_clear_work() {
        SudokuEngine engine = newEngine();
        engine.toggleNote(0, 1, 5);
        Set<Integer> notes = engine.getCellNotes(0, 1);
        assertTrue(notes.contains(5));
        engine.toggleNote(0, 1, 5);
        assertFalse(engine.getCellNotes(0, 1).contains(5));

        engine.toggleNote(0, 1, 7);
        engine.clearNotes(0, 1);
        assertTrue(engine.getCellNotes(0, 1).isEmpty());
    }

    @Test
    void reveal_hint_places_correct_value_and_returns_coordinates() {
        SudokuEngine engine = newEngine();
        var hint = engine.revealHint();
        assertTrue(hint.isPresent());
        int[] data = hint.get();
        int r = data[0], c = data[1], v = data[2];
        assertEquals(v, engine.getCellValue(r, c));
        assertTrue(v >= 1 && v <= 9);
    }
}
