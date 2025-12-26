package model.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import model.dao.RunDAO;
import model.db.DatabaseManager;
import model.domain.BuffType;
import model.domain.RunLevelState;
import model.domain.SudokuGrid;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the application of buffs within RunService,
 * specifically focusing on point calculation modifiers.
 */
public class RunServiceBuffsTest {

    static class FakeGameDataService extends GameDataService {
        public FakeGameDataService() { super(DatabaseManager.getInstance()); }
        @Override public int getCharacterBaseLives(String characterId) { return 3; }
        @Override public String getBaseDifficultyByLevel(int levelNumber) { return "EASY"; }
    }

    static class SimpleSudokuGenerator extends SudokuGenerator {
        public SimpleSudokuGenerator(GameDataService gds) { super(gds); }
        @Override public SudokuGrid generateNewPuzzle(int levelNumber, model.domain.RunFrozenBuffs frozenBuffs) {
            int[][] solved = new int[9][9];
            int[][] initial = new int[9][9];
            return new SudokuGrid(initial, solved, "EASY");
        }
    }

    @BeforeAll
    static void initDb() {
        DatabaseManager.getInstance().initializeDatabase();
    }

    @Test
    void pointBonusDoublesLevelScore() {
        var dao = new RunDAO(DatabaseManager.getInstance());
        var gds = new FakeGameDataService();
        var gen = new SimpleSudokuGenerator(gds);
        var service = new RunService(dao, gds, gen);

        Map<String, Integer> buffs = new HashMap<>();
        buffs.put(BuffType.POINT_BONUS.name(), 1);
        var user = new model.domain.User("tester", null, 0, 0, 0, 0, buffs);

        assertTrue(service.startNewRun(user, "DEFAULT"));
        RunLevelState st = service.getCurrentRun().getCurrentLevelState();
        assertEquals(1, st.getCurrentLevel());
        service.endLevel(true);
        assertTrue(service.getCurrentRun().getScore() >= 80);
    }

    @Test
    void firstErrorProtectionConsumed() {
        var dao = new RunDAO(DatabaseManager.getInstance());
        var gds = new FakeGameDataService();
        var gen = new SimpleSudokuGenerator(gds);
        var service = new RunService(dao, gds, gen);

        Map<String, Integer> buffs = new HashMap<>();
        buffs.put("FIRST_ERROR_PROTECT", 1);
        var user = new model.domain.User("tester2", null, 0, 0, 0, 0, buffs);

        assertTrue(service.startNewRun(user, "DEFAULT"));
        RunLevelState st = service.getCurrentRun().getCurrentLevelState();
        assertFalse(st.isProtectionUsed());
        service.consumeFirstErrorProtection();
        assertTrue(service.getCurrentRun().getCurrentLevelState().isProtectionUsed());
    }

    @Test
    void level3BuffsDoNotCrash() {
        var dao = new RunDAO(DatabaseManager.getInstance());
        var gds = new FakeGameDataService();
        var gen = new SimpleSudokuGenerator(gds);
        var service = new RunService(dao, gds, gen);

        Map<String, Integer> buffs = new HashMap<>();
        buffs.put("POINT_BONUS", 3);
        buffs.put("EXTRA_LIVES", 3);
        var user = new model.domain.User("tester3", null, 0, 0, 0, 0, buffs);
        
        assertTrue(service.startNewRun(user, "DEFAULT"));
        RunLevelState st = service.getCurrentRun().getCurrentLevelState();
        assertEquals(1, st.getCurrentLevel());
        
        assertEquals(3, service.getFrozenBuffLevel(BuffType.POINT_BONUS.name()));
        assertEquals(3, service.getFrozenBuffLevel(BuffType.EXTRA_LIVES.name()));
        
        service.endLevel(true);
        assertTrue(service.getCurrentRun().getScore() >= 80);
    }
}
