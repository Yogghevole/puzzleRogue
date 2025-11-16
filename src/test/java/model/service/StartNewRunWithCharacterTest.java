package model.service;

import model.db.DatabaseManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StartNewRunWithCharacterTest {
    @Test
    void startNewRunUsesPersistedCharacter() {
        DatabaseManager.getInstance().initializeDatabase();
        SessionService.setCurrentNick("run_user");
        model.dao.UserDAO userDAO = new model.dao.UserDAO(DatabaseManager.getInstance());
        if (userDAO.getUserByNick("run_user") == null) {
            assertTrue(userDAO.createUser("run_user"));
        }
        SessionService.setLastSelectedCharacter("HIGHWAYMAN");
        RunService runService = new RunService();
        assertTrue(runService.startNewRunWithCharacter(SessionService.getLastSelectedCharacter()));
    }
}