package model.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import model.domain.RunFrozenBuffs;
import model.domain.SudokuGrid;
import org.junit.jupiter.api.Test;

/**
 * Tests SudokuGenerator logic related to bonus effects,
 * such as increasing the number of starting cells based on active buffs.
 */
public class SudokuGeneratorBonusTest {

    static class MockGameDataService extends GameDataService {
        public MockGameDataService() { super(null); }

        @Override
        public String getBaseDifficultyByLevel(int levelNumber) {
            return "EASY";
        }

        @Override
        public Map<String, Number> getBuffLevelData(String buffId, int level) {
            Map<String, Number> m = new HashMap<>();
            if ("STARTING_CELLS".equals(buffId)) {
                if (level == 1) m.put("value", 1.0);
                else if (level == 2) m.put("value", 2.0);
                else if (level == 3) m.put("value", 3.0);
                else m.put("value", 0.0);
            } else {
                m.put("value", 0.0);
            }
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
    void testStartingCellsBuffLevel1() {
        MockGameDataService ds = new MockGameDataService();
        SudokuGenerator gen = new SudokuGenerator(ds);

        RunFrozenBuffs frozenNone = new RunFrozenBuffs(Map.of());
        SudokuGrid g0 = gen.generateNewPuzzle(1, frozenNone);
        int baseCount = countNonZero(g0.getInitialGrid());

        RunFrozenBuffs frozenBuff1 = new RunFrozenBuffs(Map.of("STARTING_CELLS", 1));
        SudokuGrid g1 = gen.generateNewPuzzle(1, frozenBuff1);
        
        assertEquals(1, g1.getBonusCells().size(), "Should have exactly 1 bonus cell at level 1");
    }

    @Test
    void testStartingCellsBuffLevel2() {
        MockGameDataService ds = new MockGameDataService();
        SudokuGenerator gen = new SudokuGenerator(ds);

        RunFrozenBuffs frozenBuff2 = new RunFrozenBuffs(Map.of("STARTING_CELLS", 2));
        SudokuGrid g2 = gen.generateNewPuzzle(1, frozenBuff2);
        
        assertEquals(2, g2.getBonusCells().size(), "Should have exactly 2 bonus cells at level 2");
    }

    @Test
    void testStartingCellsBuffLevel3() {
        MockGameDataService ds = new MockGameDataService();
        SudokuGenerator gen = new SudokuGenerator(ds);

        RunFrozenBuffs frozenBuff3 = new RunFrozenBuffs(Map.of("STARTING_CELLS", 3));
        SudokuGrid g3 = gen.generateNewPuzzle(1, frozenBuff3);
        
        assertEquals(3, g3.getBonusCells().size(), "Should have exactly 3 bonus cells at level 3");
    }
}
