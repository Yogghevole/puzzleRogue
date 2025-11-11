package model.dao;

import model.domain.Run;
import model.domain.RunLevelState;
import model.db.DatabaseManager;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RunDAO {
    private final DatabaseManager dbManager;

    public RunDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void save(Run run) {
        String sql = """
            INSERT INTO Run (run_id, user_nick, character_selected, lives_remaining, 
                            total_errors, score, is_completed)
            VALUES (?, ?, ?, ?, ?, ?, false)
            ON CONFLICT(run_id) DO UPDATE SET 
                lives_remaining = ?,
                total_errors = ?,
                score = ?,
                is_completed = ?
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setObject(1, run.getId());
            stmt.setString(2, run.getUserNick());
            stmt.setString(3, run.getCharacterId());
            stmt.setInt(4, run.getLivesRemaining());
            stmt.setInt(5, run.getTotalErrors());
            stmt.setInt(6, run.getScore());
            stmt.setInt(7, run.getLivesRemaining());
            stmt.setInt(8, run.getTotalErrors());
            stmt.setInt(9, run.getScore());
            stmt.setBoolean(10, false);

            stmt.executeUpdate();
            
            if (run.getId() == null) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        run.setId(rs.getInt(1));
                    }
                }
            }
            
            if (run.getCurrentLevelState() != null) {
                saveLevelState(run.getId(), run.getCurrentLevelState());
            }
            
            saveInventory(run.getId(), run.getInventory());
        } catch (SQLException e) {
            throw new RuntimeException("Error saving run", e);
        }
    }

    private void saveLevelState(Integer runId, RunLevelState state) {
        String sql = """
            INSERT INTO Run_Level_State 
            (run_id, current_level, enemy_sprite_id, difficulty_tier, 
             initial_grid_data, user_grid_data, notes_data, errors_in_level, protection_used)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(run_id) DO UPDATE SET
                current_level = ?,
                enemy_sprite_id = ?,
                difficulty_tier = ?,
                user_grid_data = ?,
                notes_data = ?,
                errors_in_level = ?,
                protection_used = ?
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, runId);
            stmt.setInt(2, state.getCurrentLevel());
            stmt.setString(3, state.getEnemySpriteId());
            stmt.setString(4, state.getDifficultyTier());
            stmt.setString(5, state.getInitialGridData());
            stmt.setString(6, state.getUserGridData());
            stmt.setString(7, state.getNotesData());
            stmt.setInt(8, state.getErrorsInLevel());
            stmt.setBoolean(9, state.isProtectionUsed());
            stmt.setInt(10, state.getCurrentLevel());
            stmt.setString(11, state.getEnemySpriteId());
            stmt.setString(12, state.getDifficultyTier());
            stmt.setString(13, state.getUserGridData());
            stmt.setString(14, state.getNotesData());
            stmt.setInt(15, state.getErrorsInLevel());
            stmt.setBoolean(16, state.isProtectionUsed());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving level state", e);
        }
    }

    private void saveInventory(Integer runId, Map<String, Integer> inventory) {
        String deleteSql = "DELETE FROM Run_Inventory WHERE run_id = ?";
        String insertSql = "INSERT INTO Run_Inventory (run_id, item_type_id, capacity) VALUES (?, ?, ?)";

        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, runId);
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
                    insertStmt.setInt(1, runId);
                    insertStmt.setString(2, entry.getKey());
                    insertStmt.setInt(3, entry.getValue());
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving inventory", e);
        }
    }

    public Optional<Run> findActiveRunByUser(String userNick) {
        String sql = """
            SELECT r.*, ls.*, i.item_id, i.quantity
            FROM Run r
            LEFT JOIN Run_Level_State ls ON r.run_id = ls.run_id
            LEFT JOIN Run_Inventory i ON r.run_id = i.run_id
            WHERE r.user_nick = ? AND NOT r.is_completed
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userNick);
            
            ResultSet rs = stmt.executeQuery();
            Map<Integer, Run> runs = new HashMap<>();
            
            while (rs.next()) {
                Integer runId = rs.getInt("run_id");
                try {
                    Run run = runs.computeIfAbsent(runId, k -> {
                        try {
                            return mapBaseRunData(rs);
                        } catch (SQLException e) {
                            throw new RuntimeException("Error mapping run data", e);
                        }
                    });
                    
                    String itemId = rs.getString("item_id");
                    if (itemId != null) {
                        run.addItemToInventory(itemId, rs.getInt("quantity"));
                    }
                    
                    if (run.getCurrentLevelState() == null && rs.getString("initial_grid_data") != null) {
                        run.setCurrentLevelState(mapLevelState(rs));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException("Error processing run data", e);
                }
            }
            
            return runs.values().stream().findFirst();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding active run", e);
        }
    }

    private Run mapBaseRunData(ResultSet rs) throws SQLException {
        Run run = new Run(
            rs.getString("user_nick"),
            rs.getInt("lives_remaining"),
            rs.getString("character_selected")
        );
        run.setId(rs.getInt("run_id"));
        run.setTotalErrors(rs.getInt("total_errors"));
        return run;
    }

    private RunLevelState mapLevelState(ResultSet rs) throws SQLException {
        return new RunLevelState(
            rs.getInt("run_id"),
            rs.getInt("current_level"),
            rs.getString("enemy_sprite_id"),
            rs.getString("difficulty_tier"),
            rs.getString("initial_grid"),
            rs.getString("user_grid"),
            rs.getString("notes_data"),
            rs.getInt("errors_in_level"),
            rs.getBoolean("protection_used")
        );
    }
}