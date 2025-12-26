package model.dao;

import model.db.DatabaseManager;
import model.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for User entities.
 * Manages user profiles, statistics, and permanent buffs.
 */
public class UserDAO {

    private final DatabaseManager dbManager;

    public UserDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }


    public boolean createUser(String nick) {
        String sqlUser = "INSERT INTO User (nick, current_run_id, points_available, points_total, runs_completed, runs_won) VALUES (?, NULL, 0, 0, 0, 0)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlUser)) {

            stmt.setString(1, nick);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
        System.err.println("SQL error creating user " + nick + ": " + e.getMessage());
            return false;
        }
    }
    

    public User getUserByNick(String nick) {
        String sqlUser = "SELECT * FROM User WHERE nick = ?";

        try (Connection conn = dbManager.getConnection()) {
            
            User user = null;
            try (PreparedStatement stmt = conn.prepareStatement(sqlUser)) {
                stmt.setString(1, nick);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int currentRunIdInt = rs.getInt("current_run_id");
                        Integer currentRunId = rs.wasNull() ? null : currentRunIdInt;
                        
                        user = new User(
                            rs.getString("nick"),
                            currentRunId,
                            rs.getInt("points_available"),
                            rs.getInt("points_total"),
                            rs.getInt("runs_completed"),
                            rs.getInt("runs_won"),
                            null 
                        );
                    }
                }
            }

            if (user != null) {
                Map<String, Integer> buffs = loadUserBuffs(conn, nick);
                user.getPermanentBuffLevels().putAll(buffs);
            }
            return user;

        } catch (SQLException e) {
        System.err.println("SQL error loading user " + nick + ": " + e.getMessage());
            return null;
        }
    }

    private Map<String, Integer> loadUserBuffs(Connection conn, String nick) throws SQLException {
        String sqlBuffs = "SELECT buff_id, buff_level FROM User_Buffs WHERE user_nick = ?";
        Map<String, Integer> buffs = new HashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(sqlBuffs)) {
            stmt.setString(1, nick);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    buffs.put(rs.getString("buff_id"), rs.getInt("buff_level"));
                }
            }
        }
        return buffs;
    }


    /**
     * Updates user statistics and permanent buffs in a single transaction.
     */
    public boolean updateUser(User user) {
        String sqlUpdateUser = "UPDATE User SET current_run_id = ?, points_available = ?, points_total = ?, runs_completed = ?, runs_won = ? WHERE nick = ?";

        try (Connection conn = dbManager.getConnection()) {
            
            conn.setAutoCommit(false); 

            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateUser)) {
                stmt.setObject(1, user.getCurrentRunId());
                stmt.setInt(2, user.getPointsAvailable());
                stmt.setInt(3, user.getPointsTotal());
                stmt.setInt(4, user.getRunsCompleted());
                stmt.setInt(5, user.getRunsWon());
                stmt.setString(6, user.getNick());
                stmt.executeUpdate();
            }

            saveUserBuffs(conn, user.getNick(), user.getPermanentBuffLevels());
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
        System.err.println("SQL error updating user " + user.getNick() + ": " + e.getMessage());
            return false;
        }
    }
    
    private void saveUserBuffs(Connection conn, String nick, Map<String, Integer> buffs) throws SQLException {
        String sqlDelete = "DELETE FROM User_Buffs WHERE user_nick = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlDelete)) {
            stmt.setString(1, nick);
            stmt.executeUpdate();
        }

        String sqlInsert = "INSERT INTO User_Buffs (user_nick, buff_id, buff_level) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
            for (Map.Entry<String, Integer> entry : buffs.entrySet()) {
                if (entry.getValue() > 0) {
                    stmt.setString(1, nick);
                    stmt.setString(2, entry.getKey());
                    stmt.setInt(3, entry.getValue());
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        }
    }
}