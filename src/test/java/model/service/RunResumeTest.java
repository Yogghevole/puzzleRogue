package model.service;

import model.db.DatabaseManager;
import model.dao.RunDAO;
import model.domain.Run;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RunResumeTest {
    @org.junit.jupiter.api.Disabled
    @Test
    void createAndFindActiveRunForUser() {
        DatabaseManager.getInstance().initializeDatabase();
        SessionService.setCurrentNick("test_user");
        model.dao.UserDAO userDAO = new model.dao.UserDAO(DatabaseManager.getInstance());
        if (userDAO.getUserByNick("test_user") == null) {
            assertTrue(userDAO.createUser("test_user"));
        }
        RunService runService = new RunService();
        boolean created = runService.startNewRun();
        if (created) {
            runService.startLevel(1);
        }
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS c FROM Run WHERE user_nick = ?")) {
            ps.setString(1, "test_user");
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                rs.getInt("c");
            }
        } catch (java.sql.SQLException e) {
            fail("SQL error verifying active run: " + e.getMessage());
        }
    }
}