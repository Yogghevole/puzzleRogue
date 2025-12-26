package model.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the User domain class.
 * Verifies point management (adding/spending) and buff upgrade logic.
 */
public class UserTest {

    @Test
    void testAddPoints() {
        User user = new User("test");
        assertEquals(0, user.getPointsAvailable());
        user.addPoints(100);
        assertEquals(100, user.getPointsAvailable());
        assertEquals(100, user.getPointsTotal());
    }

    @Test
    void testSpendPoints() {
        User user = new User("test");
        user.addPoints(100);
        
        boolean success = user.spendPoints(60);
        assertTrue(success);
        assertEquals(40, user.getPointsAvailable());
        
        boolean fail = user.spendPoints(50);
        assertFalse(fail);
        assertEquals(40, user.getPointsAvailable());
    }

    @Test
    void testBuffUpgrade() {
        User user = new User("test");
        String buffId = BuffType.EXTRA_LIVES.name();
        
        assertEquals(0, user.getBuffLevel(buffId));
        
        user.upgradeBuff(buffId, 1);
        assertEquals(1, user.getBuffLevel(buffId));
        
        user.upgradeBuff(buffId, 3);
        assertEquals(3, user.getBuffLevel(buffId));
    }
}
