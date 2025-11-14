package model.service;

import model.db.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servizio per accedere ai dati statici di configurazione del gioco .
 */
public class GameDataService {

    private final DatabaseManager dbManager;

    public GameDataService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public Map<String, Number> getBuffLevelData(String buffId, int level) {
        String sql = "SELECT cost_points, effect_value FROM Buff_Level_Cost WHERE buff_id = ? AND level = ?";
        Map<String, Number> data = new HashMap<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, buffId);
            stmt.setInt(2, level);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    data.put("cost", rs.getInt("cost_points"));
                    data.put("value", rs.getDouble("effect_value"));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error retrieving Buff data for " + buffId + " at level " + level + ": " + e.getMessage());
        }
        return data;
    }

    public String getBaseDifficultyByLevel(int levelNumber) {
        String sql = "SELECT base_difficulty FROM Level_Definition WHERE level_number = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, levelNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("base_difficulty");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error retrieving difficulty for level " + levelNumber + ": " + e.getMessage());
        }
        return "UNKNOWN";
    }

    public int getCharacterBaseLives(String charId) {
        String sql = "SELECT base_lives FROM Character_Definition WHERE char_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, charId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("base_lives");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error retrieving base lives for " + charId + ": " + e.getMessage());
        }
        return 0; 
    }

    public int getTotalLevels() {
        String sql = "SELECT COUNT(*) AS cnt FROM Level_Definition";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.err.println("SQL error counting levels: " + e.getMessage());
        }
        return 0;
    }

    public boolean isBossLevel(int levelNumber) {
        String sql = "SELECT is_boss_level FROM Level_Definition WHERE level_number = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, levelNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Object val = rs.getObject("is_boss_level");
                    if (val instanceof Boolean) return (Boolean) val;
                    if (val instanceof Number) return ((Number) val).intValue() != 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error retrieving boss flag for level " + levelNumber + ": " + e.getMessage());
        }
        return false;
    }
    
    public int getMaxBuffLevel(String buffId) {
        String sql = "SELECT MAX(level) as max_level FROM Buff_Level_Cost WHERE buff_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, buffId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("max_level");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error retrieving max buff level for " + buffId + ": " + e.getMessage());
        }
        return 0;
    }
}