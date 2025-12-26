package model.domain.buff;

import model.domain.BuffType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the BuffFactory class.
 * Verifies that buffs are correctly created by type and string identifier,
 * and ensures all BuffType enum values have corresponding implementations.
 */
public class BuffFactoryTest {

    @Test
    void testGetBuffByType() {
        Buff extraLives = BuffFactory.getBuff(BuffType.EXTRA_LIVES);
        assertNotNull(extraLives);
        assertTrue(extraLives instanceof ExtraLivesBuff);
        assertEquals(BuffType.EXTRA_LIVES.name(), extraLives.getId());
    }

    @Test
    void testGetBuffByString() {
        Buff pointBonus = BuffFactory.getBuff("POINT_BONUS");
        assertNotNull(pointBonus);
        assertTrue(pointBonus instanceof PointBonusBuff);
        
        Buff invalid = BuffFactory.getBuff("NON_EXISTENT");
        assertNull(invalid);
    }
    
    @Test
    void testAllEnumTypesHaveBuffs() {
        for (BuffType type : BuffType.values()) {
            assertNotNull(BuffFactory.getBuff(type), "Missing implementation for " + type);
        }
    }
}
