package model.service;

import model.db.DatabaseManager;
import model.dao.UserDAO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LegacyServiceTest {
    @Test
    void selectBuffPersistsToUser() {
        DatabaseManager.getInstance().initializeDatabase();
        SessionService.setCurrentNick("legacy_user");
        UserDAO dao = new UserDAO(DatabaseManager.getInstance());
        if (dao.getUserByNick("legacy_user") == null) {
            assertTrue(dao.createUser("legacy_user"));
        }
        var user = dao.getUserByNick("legacy_user");
        assertNotNull(user);
        user.upgradeBuff("EXTRA_LIVES", 1);
        assertTrue(dao.updateUser(user));
        var reloaded = dao.getUserByNick("legacy_user");
        assertEquals(1, reloaded.getBuffLevel("EXTRA_LIVES"));
    }
}