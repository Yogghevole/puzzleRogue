package model.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import model.domain.RunFrozenBuffs;
import model.domain.SudokuGrid;
import org.junit.jupiter.api.Test;


public class SudokuGeneratorTest {

    static class FakeGameDataService extends GameDataService {
        public FakeGameDataService() { super(null); }

        @Override
        public String getBaseDifficultyByLevel(int levelNumber) {
            return levelNumber <= 3 ? "EASY" : "MEDIUM";
        }

        @Override
        public Map<String, Number> getBuffLevelData(String buffId, int level) {
            Map<String, Number> m = new HashMap<>();
            m.put("value", level == 0 ? 0.0 : 5.0);
            return m;
        }
    }

    private int countNonZero(int[][] grid) {
        int count = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] != 0) count++;
            }
        }
        return count;
    }

    @Test
    void generates_puzzle_with_expected_number_of_clues_and_full_solution() {
        FakeGameDataService ds = new FakeGameDataService();
        SudokuGenerator gen = new SudokuGenerator(ds);

        RunFrozenBuffs frozenNone = new RunFrozenBuffs(Map.of());
        SudokuGrid g1 = gen.generateNewPuzzle(1, frozenNone);
        assertEquals("EASY", g1.getDifficultyTier());
        assertEquals(40, countNonZero(g1.getInitialGrid()));
        assertEquals(81, countNonZero(g1.getSolvedGrid()));

        RunFrozenBuffs frozenBuff = new RunFrozenBuffs(Map.of("STARTING_CELLS", 1));
        SudokuGrid g2 = gen.generateNewPuzzle(1, frozenBuff);
        assertEquals(45, countNonZero(g2.getInitialGrid()));
        assertEquals(81, countNonZero(g2.getSolvedGrid()));
    }
}