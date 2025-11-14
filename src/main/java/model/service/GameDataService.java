package model.service;

import model.db.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
            System.err.println("Errore SQL nel recupero dati Buff per " + buffId + " al livello " + level + ": " + e.getMessage());
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
            System.err.println("Errore SQL nel recupero difficoltà livello " + levelNumber + ": " + e.getMessage());
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
            System.err.println("Errore SQL nel recupero vite base per " + charId + ": " + e.getMessage());
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
            System.err.println("Errore SQL nel conteggio dei livelli: " + e.getMessage());
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
            System.err.println("Errore SQL nel recupero flag boss per livello " + levelNumber + ": " + e.getMessage());
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
            System.err.println("Errore SQL nel recupero livello massimo buff " + buffId + ": " + e.getMessage());
        }
        return 0;
    }
    
    public boolean hasActiveRun() {
        String sql = "SELECT COUNT(*) as count FROM Run";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Errore SQL nel controllo run attiva: " + e.getMessage());
        }
        return false;
    }
    
    public int getActiveRunId() {
        String sql = "SELECT run_id FROM Run LIMIT 1";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("run_id");
            }
        } catch (SQLException e) {
            System.err.println("Errore SQL nel recupero ID run attiva: " + e.getMessage());
        }
        return -1;
    }
    
    public Map<String, Object> getActiveRunInfo() {
        String sql = "SELECT run_id, character_selected, current_level, lives_remaining " +
                     "FROM Run r LEFT JOIN Run_Level_State ls ON r.run_id = ls.run_id " +
                     "LIMIT 1";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                Map<String, Object> runInfo = new HashMap<>();
                runInfo.put("run_id", rs.getInt("run_id"));
                runInfo.put("character_id", rs.getString("character_selected"));
                runInfo.put("current_level", rs.getInt("current_level"));
                runInfo.put("current_lives", rs.getInt("lives_remaining"));
                return runInfo;
            }
        } catch (SQLException e) {
            System.err.println("Errore SQL nel recupero info run attiva: " + e.getMessage());
        }
        return null;
    }
    
    public GameDataService() {
        this.dbManager = DatabaseManager.getInstance();
    }
}