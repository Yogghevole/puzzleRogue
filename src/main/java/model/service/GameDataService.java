package model.service;

import model.db.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servizio per accedere ai dati statici di configurazione del gioco (regole, costi, definizioni).
 */
public class GameDataService {

    private final DatabaseManager dbManager;

    public GameDataService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Recupera il costo e il valore dell'effetto di un buff specifico per un dato livello.
     */
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

    /**
     * Restituisce la difficoltà base di un livello (es. Livello 5 -> HARD).
     */
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

    /**
     * Restituisce le vite base di un personaggio.
     */
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

    /**
     * Restituisce il livello massimo disponibile per un buff specifico.
     */
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
}