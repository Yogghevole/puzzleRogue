package model.service;

import model.db.DatabaseManager;
import model.domain.BuffType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GameDataService.
 * Verifies retrieval of game configuration data such as character lives, buff levels, and level properties.
 */
class GameDataServiceTest {

    private static GameDataService gameDataService;

    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance().initializeDatabase();
        gameDataService = new GameDataService(DatabaseManager.getInstance());
    }

    @Test
    void testGetCharacterBaseLives() {
        int lives = gameDataService.getCharacterBaseLives("CRUSADER");
        assertTrue(lives > 0, "Crusader character should have base lives > 0");
    }

    @Test
    void testGetMaxBuffLevel() {
        int maxLevel = gameDataService.getMaxBuffLevel(BuffType.EXTRA_LIVES.name());
        assertTrue(maxLevel > 0, "Extra Lives buff should have a max level");
        int maxLevelProtect = gameDataService.getMaxBuffLevel(BuffType.FIRST_ERROR_PROTECT.name());
        assertTrue(maxLevelProtect > 0);
    }

    @Test
    void testGetBuffLevelData() {
        Map<String, Number> data = gameDataService.getBuffLevelData(BuffType.EXTRA_LIVES.name(), 1);
        assertNotNull(data);
        assertTrue(data.containsKey("cost"));
        assertTrue(data.containsKey("value"));
        
        double cost = data.get("cost").doubleValue();
        assertTrue(cost > 0, "Buff cost should be positive");
    }

    @Test
    void testIsBossLevel() {
        assertFalse(gameDataService.isBossLevel(1), "Level 1 should not be a boss level");
    }
    
    @Test
    void testGetTotalLevels() {
        int total = gameDataService.getTotalLevels();
        assertTrue(total >= 10, "Should have at least 10 levels defined");
    }
}
