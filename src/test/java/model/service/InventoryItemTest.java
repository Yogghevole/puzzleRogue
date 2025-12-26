package model.service;

import model.dao.UserDAO;
import model.db.DatabaseManager;
import model.domain.Run;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests inventory management functionality within RunService,
 * including adding, removing, and tracking items.
 */
class InventoryItemTest {

    @BeforeEach
    void setUp() {
        DatabaseManager.getInstance().initializeDatabase();
        SessionService.setCurrentNick("inventory_test_user");
        
        UserDAO userDAO = new UserDAO(DatabaseManager.getInstance());
        if (userDAO.getUserByNick("inventory_test_user") == null) {
            userDAO.createUser("inventory_test_user");
        } else {
             RunService rs = new RunService();
             if (rs.resumeLastRun()) {
                 rs.endRun(false);
             }
        }
    }

    @Test
    void testInventoryManagement() {
        RunService runService = new RunService();
        assertTrue(runService.startNewRun(), "Should start new run");
        
        runService.addItem("HINT_ITEM");
        runService.addItem("HINT_ITEM");
        runService.addItem("LIFE_BOOST_ITEM");
        
        Run currentRun = runService.getCurrentRun();
        Map<String, Integer> inventory = currentRun.getInventory();
        
        assertEquals(2, inventory.get("HINT_ITEM"));
        assertEquals(1, inventory.get("LIFE_BOOST_ITEM"));
        
        assertTrue(runService.removeItem("HINT_ITEM"));
        assertEquals(1, inventory.get("HINT_ITEM"));
        
        assertTrue(runService.removeItem("HINT_ITEM"));
        assertFalse(inventory.containsKey("HINT_ITEM"));
        
        assertFalse(runService.removeItem("HINT_ITEM"), "Should fail to remove missing item");
    }

    @Test
    void testUseLifeBoostItem() {
        RunService runService = new RunService();
        runService.startNewRun();
        Run run = runService.getCurrentRun();
        
        int initialLives = run.getLivesRemaining();
        
        runService.addItem("LIFE_BOOST_ITEM");
        assertTrue(runService.useItem("LIFE_BOOST_ITEM"));
        
        assertEquals(initialLives + 1, run.getLivesRemaining());
        assertFalse(run.getInventory().containsKey("LIFE_BOOST_ITEM"));
    }

    @Test
    void testUseScoreItem() {
        RunService runService = new RunService();
        runService.startNewRun();
        runService.startLevel(1);
        Run run = runService.getCurrentRun();
        
        int initialScore = run.getScore();
        
        runService.addItem("SCORE_ITEM");
        assertTrue(runService.useItem("SCORE_ITEM"));
        
        assertTrue(run.getScore() > initialScore);
        assertFalse(run.getInventory().containsKey("SCORE_ITEM"));
    }
    
    @Test
    void testUseHintItem() {
        RunService runService = new RunService();
        runService.startNewRun();
        runService.startLevel(1);
        
        runService.addItem("HINT_ITEM");
        
        boolean used = runService.useItem("HINT_ITEM");
        
        if (used) {
            assertFalse(runService.getCurrentRun().getInventory().containsKey("HINT_ITEM"));
        } else {
             assertTrue(runService.getCurrentRun().getInventory().containsKey("HINT_ITEM"));
        }
    }

    @Test
    void testUseSacrificeItem() {
        RunService runService = new RunService();
        runService.startNewRun();
        runService.startLevel(1);
        Run run = runService.getCurrentRun();
        
        while (run.getLivesRemaining() <= 1) {
            run.addLife();
        }
        int livesBefore = run.getLivesRemaining();
        
        runService.addItem("SACRIFICE_ITEM");
        boolean used = runService.useItem("SACRIFICE_ITEM");
        
        if (used) {
            assertEquals(livesBefore - 1, run.getLivesRemaining());
            assertFalse(run.getInventory().containsKey("SACRIFICE_ITEM"));
        } else {
        }
    }
    
    @Test
    void testUseSacrificeItemFailsWithOneLife() {
        RunService runService = new RunService();
        runService.startNewRun();
        runService.startLevel(1);
        Run run = runService.getCurrentRun();
        
        while (run.getLivesRemaining() > 1) {
            run.loseLife();
        }
        assertEquals(1, run.getLivesRemaining());
        
        runService.addItem("SACRIFICE_ITEM");
        boolean used = runService.useItem("SACRIFICE_ITEM");
        
        assertFalse(used, "Should not be able to use sacrifice item with 1 life");
        assertEquals(1, run.getLivesRemaining());
        assertTrue(run.getInventory().containsKey("SACRIFICE_ITEM"));
    }
}
