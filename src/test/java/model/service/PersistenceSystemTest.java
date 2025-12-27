package model.service;

import model.dao.UserDAO;
import model.db.DatabaseManager;
import model.domain.Run;
import model.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the persistence system.
 * Verifies that user data and game state are correctly saved to and loaded from the database.
 */
class PersistenceSystemTest {

    private DatabaseManager dbManager;
    private UserDAO userDAO;
    private static final String TEST_USER_NICK = "persistence_tester";

    @BeforeEach
    void setUp() {
        File dbFile = new File("puzzle_rogue.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }

        dbManager = DatabaseManager.getInstance();
        dbManager.initializeDatabase();
        userDAO = new UserDAO(dbManager);
        
        try (var conn = dbManager.getConnection();
             var stmt = conn.prepareStatement("DELETE FROM User WHERE nick = ?")) {
            stmt.setString(1, TEST_USER_NICK);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        userDAO.createUser(TEST_USER_NICK);
        SessionService.setCurrentNick(TEST_USER_NICK);
    }

    @Test
    void testFullPersistenceCycle() {
        User initialUser = userDAO.getUserByNick(TEST_USER_NICK);
        initialUser.upgradeBuff("EXTRA_LIVES", 1);
        userDAO.updateUser(initialUser);

        RunService service1 = new RunService();
        User user = userDAO.getUserByNick(TEST_USER_NICK);
        assertEquals(1, user.getBuffLevel("EXTRA_LIVES"));
        
        boolean started = service1.startNewRun(user, "DEFAULT");
        assertTrue(started, "Should start new run successfully");
        
        Run run1 = service1.getCurrentRun();
        assertNotNull(run1, "Current run should not be null");
        
        assertEquals(1, service1.getFrozenBuffLevel("EXTRA_LIVES"), "Frozen buff should be 1 at start");
        
        Integer runId = run1.getId();
        assertNotNull(runId, "Run ID should be generated");
        
        User userAfterStart = userDAO.getUserByNick(TEST_USER_NICK);
        assertEquals(runId, userAfterStart.getCurrentRunId(), "User should have current_run_id set to new run");

        service1.startLevel(1);
        assertNotNull(service1.getCurrentEngine(), "Engine should be initialized after startLevel");
        
        service1.getCurrentEngine().insertValue(0, 0, 1);
        
        run1.getCurrentLevelState().setBackgroundId("/assets/backgrounds/levels/test_bg.png");
        run1.getCurrentLevelState().setEnemySpriteId("/assets/enemies/test_enemy.png");
        
        service1.save();
        
        run1.loseLife();
        service1.save();
        
        int livesAfterLoss = run1.getLivesRemaining();
        
        RunService service2 = new RunService();
        
        boolean resumed = service2.resumeLastRun();
        assertTrue(resumed, "Should resume existing run");
        
        Run run2 = service2.getCurrentRun();
        assertNotNull(run2, "Resumed run should not be null");
        assertEquals(runId, run2.getId(), "Resumed run ID should match original");
        assertEquals(livesAfterLoss, run2.getLivesRemaining(), "Resumed run should have updated lives count");
        
        assertNotNull(service2.getCurrentEngine(), "Engine should be restored after resume");
        
        assertNotNull(service2.getCurrentEngine().getSolvedGrid(), "Solved grid should be present in restored engine");
        
        assertEquals(1, service2.getFrozenBuffLevel("EXTRA_LIVES"), "Frozen buffs should persist on resume");
        
        assertEquals("/assets/backgrounds/levels/test_bg.png", run2.getCurrentLevelState().getBackgroundId(), "Background ID should persist");
        assertEquals("/assets/enemies/test_enemy.png", run2.getCurrentLevelState().getEnemySpriteId(), "Enemy Sprite ID should persist");
        
        User userBeforeEnd = userDAO.getUserByNick(TEST_USER_NICK);
        System.out.println("DEBUG: User runs completed before end: " + userBeforeEnd.getRunsCompleted());
        assertEquals(0, userBeforeEnd.getRunsCompleted());

        System.out.println("DEBUG: Calling endRun(true)...");
        service2.endRun(true);
        System.out.println("DEBUG: endRun(true) returned.");
        
        User userAfterEnd = userDAO.getUserByNick(TEST_USER_NICK);
        System.out.println("DEBUG: User runs completed after end: " + userAfterEnd.getRunsCompleted());
        
        if (userAfterEnd.getRunsCompleted() != 1) {
             fail("Runs completed is " + userAfterEnd.getRunsCompleted() + ", expected 1. RunsWon: " + userAfterEnd.getRunsWon());
        }
        
        assertNull(userAfterEnd.getCurrentRunId(), "User current_run_id should be null after endRun");
        assertEquals(1, userAfterEnd.getRunsCompleted(), "Runs completed should increment");
        assertEquals(1, userAfterEnd.getRunsWon(), "Runs won should increment");
    }

    
    @Test
    void testResumeFailsIfNoActiveRun() {
        RunService service = new RunService();
        boolean resumed = service.resumeLastRun();
        assertFalse(resumed, "Should not resume if no run exists");
    }
}
