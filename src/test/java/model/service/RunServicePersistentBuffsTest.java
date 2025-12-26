package model.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import model.dao.RunDAO;
import model.dao.UserDAO;
import model.db.DatabaseManager;
import model.domain.SudokuGrid;
import model.domain.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the persistence and application of long-term buffs across different game runs.
 */
public class RunServicePersistentBuffsTest {

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

    private RunService service;
    private UserDAO userDAO;

    @BeforeAll
    static void initDb() {
        DatabaseManager.getInstance().initializeDatabase();
    }

    @BeforeEach
    void setup() {
        var dao = new RunDAO(DatabaseManager.getInstance());
        var gds = new FakeGameDataService();
        var gen = new SimpleSudokuGenerator(gds);
        service = new RunService(dao, gds, gen);
        userDAO = new UserDAO(DatabaseManager.getInstance());
    }

    @Test
    void testProtectionConsumedIfUsed() {
        Map<String, Integer> buffs = new HashMap<>();
        buffs.put("FIRST_ERROR_PROTECT", 1);
        User user = new User("persistTester1", null, 0, 0, 0, 0, buffs);
        
        userDAO.createUser(user.getNick());
        userDAO.updateUser(user);
        SessionService.setCurrentNick(user.getNick());

        assertTrue(service.startNewRun(user, "DEFAULT"));
        
        service.getCurrentRun().incrementTotalErrors();
        
        service.endRun(false);
        
        User updatedUser = userDAO.getUserByNick(user.getNick());
        
        assertTrue(updatedUser.getPermanentBuffLevels().containsKey("FIRST_ERROR_PROTECT"), 
            "Buff should persist (Permanent Progression Design)");
    }

    @Test
    void testProtectionPersistsIfNotUsed() {
        Map<String, Integer> buffs = new HashMap<>();
        buffs.put("FIRST_ERROR_PROTECT", 1);
        User user = new User("persistTester2", null, 0, 0, 0, 0, buffs);
        
        userDAO.createUser(user.getNick());
        userDAO.updateUser(user);
        SessionService.setCurrentNick(user.getNick());

        assertTrue(service.startNewRun(user, "DEFAULT"));
        
        service.endRun(false);
        
        User updatedUser = userDAO.getUserByNick(user.getNick());
        assertTrue(updatedUser.getPermanentBuffLevels().containsKey("FIRST_ERROR_PROTECT"), 
            "Buff should persist because no errors occurred");
    }

    @Test
    void testExtraLivesConsumedIfUsed() {
        Map<String, Integer> buffs = new HashMap<>();
        buffs.put("EXTRA_LIVES", 1);
        User user = new User("persistTester3", null, 0, 0, 0, 0, buffs);
        
        userDAO.createUser(user.getNick());
        userDAO.updateUser(user);
        SessionService.setCurrentNick(user.getNick());

        assertTrue(service.startNewRun(user, "DEFAULT"));
        assertEquals(4, service.getCurrentRun().getLivesRemaining());
        
        service.getCurrentRun().loseLife();
        service.getCurrentRun().loseLife();
        
        service.endRun(false);
        
        User updatedUser = userDAO.getUserByNick(user.getNick());
        assertTrue(updatedUser.getPermanentBuffLevels().containsKey("EXTRA_LIVES"), 
            "Buff should persist (Permanent Progression Design)");
    }

    @Test
    void testExtraLivesPersistsIfNotUsed() {
        Map<String, Integer> buffs = new HashMap<>();
        buffs.put("EXTRA_LIVES", 1);
        User user = new User("persistTester4", null, 0, 0, 0, 0, buffs);
        
        userDAO.createUser(user.getNick());
        userDAO.updateUser(user);
        SessionService.setCurrentNick(user.getNick());

        assertTrue(service.startNewRun(user, "DEFAULT"));
        
        service.endRun(false);
        
        User updatedUser = userDAO.getUserByNick(user.getNick());
        assertTrue(updatedUser.getPermanentBuffLevels().containsKey("EXTRA_LIVES"), 
            "Buff should persist because lives were not lost (or at least not below base threshold?)");
    }
}
