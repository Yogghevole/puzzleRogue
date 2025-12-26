package model.dao;

import model.domain.Run;
import model.domain.RunLevelState;
import model.db.DatabaseManager;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for Run entities.
 * Handles persistence of run progress, including level state, inventory, and active buffs.
 */
public class RunDAO {
    private final DatabaseManager dbManager;

    public RunDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    /**
     * Saves the run to the database.
     * Inserts a new record if the ID is null, otherwise updates the existing record.
     */
    public void save(Run run) {
        if (run.getId() != null) {
            update(run);
        } else {
            insert(run);
        }
    }

    /**
     * Inserts a new run and its associated components (level state, inventory).
     */
    private void insert(Run run) {
        String sql = """
            INSERT INTO Run (user_nick, character_selected, lives_remaining, total_errors, score, levels_completed, zero_error_levels, score_item_points, used_enemies)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, run.getUserNick());
            stmt.setString(2, run.getCharacterId());
            stmt.setInt(3, run.getLivesRemaining());
            stmt.setInt(4, run.getTotalErrors());
            stmt.setInt(5, run.getScore());
            stmt.setInt(6, run.getLevelsCompleted());
            stmt.setInt(7, run.getZeroErrorLevels());
            stmt.setInt(8, run.getScoreItemPoints());
            stmt.setString(9, String.join(",", run.getUsedEnemies()));

            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    run.setId(rs.getInt(1));
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

    /**
     * Saves the temporary buffs associated with the current run.
     */
    public void saveFrozenBuffs(Integer runId, Map<String, Integer> buffs) {
        String deleteSql = "DELETE FROM Run_Frozen_Buffs WHERE run_id = ?";
        String insertSql = "INSERT INTO Run_Frozen_Buffs (run_id, buff_id, buff_level) VALUES (?, ?, ?)";

        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, runId);
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                for (Map.Entry<String, Integer> entry : buffs.entrySet()) {
                    insertStmt.setInt(1, runId);
                    insertStmt.setString(2, entry.getKey());
                    insertStmt.setInt(3, entry.getValue());
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving frozen buffs", e);
        }
    }

    public Map<String, Integer> getFrozenBuffs(Integer runId) {
        String sql = "SELECT buff_id, buff_level FROM Run_Frozen_Buffs WHERE run_id = ?";
        Map<String, Integer> buffs = new HashMap<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, runId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                buffs.put(rs.getString("buff_id"), rs.getInt("buff_level"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading frozen buffs", e);
        }
        return buffs;
    }

    /**
     * Updates an existing run, including level state and inventory.
     */
    private void update(Run run) {
        String sql = """
            UPDATE Run 
            SET lives_remaining = ?, total_errors = ?, score = ?, levels_completed = ?, zero_error_levels = ?, score_item_points = ?, used_enemies = ?
            WHERE run_id = ?
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, run.getLivesRemaining());
            stmt.setInt(2, run.getTotalErrors());
            stmt.setInt(3, run.getScore());
            stmt.setInt(4, run.getLevelsCompleted());
            stmt.setInt(5, run.getZeroErrorLevels());
            stmt.setInt(6, run.getScoreItemPoints());
            stmt.setString(7, String.join(",", run.getUsedEnemies()));
            stmt.setInt(8, run.getId());

            stmt.executeUpdate();
            
            if (run.getCurrentLevelState() != null) {
                saveLevelState(run.getId(), run.getCurrentLevelState());
            }
            
            saveInventory(run.getId(), run.getInventory());
        } catch (SQLException e) {
            throw new RuntimeException("Error updating run", e);
        }
    }

    /**
     * Saves detailed state of the current level (grids, errors, notes).
     * Uses UPSERT (INSERT ... ON CONFLICT DO UPDATE) to handle both new and existing states.
     */
    private void saveLevelState(Integer runId, RunLevelState state) {
        String sql = """
            INSERT INTO Run_Level_State 
            (run_id, current_level, enemy_sprite_id, difficulty_tier, 
             initial_grid, solved_grid, user_grid, notes_data, errors_in_level, protection_used, bonus_cells_data, background_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(run_id) DO UPDATE SET
                current_level = ?,
                enemy_sprite_id = ?,
                difficulty_tier = ?,
                user_grid = ?,
                notes_data = ?,
                errors_in_level = ?,
                protection_used = ?,
                initial_grid = ?,
                solved_grid = ?,
                bonus_cells_data = ?,
                background_id = ?
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, runId);
            stmt.setInt(2, state.getCurrentLevel());
            stmt.setString(3, state.getEnemySpriteId());
            stmt.setString(4, state.getDifficultyTier());
            stmt.setString(5, state.getInitialGridData());
            stmt.setString(6, state.getSolvedGridData());
            stmt.setString(7, state.getUserGridData());
            stmt.setString(8, state.getNotesData());
            stmt.setInt(9, state.getErrorsInLevel());
            stmt.setBoolean(10, state.isProtectionUsed());
            stmt.setString(11, state.getBonusCellsData());
            stmt.setString(12, state.getBackgroundId());
            stmt.setInt(13, state.getCurrentLevel());
            stmt.setString(14, state.getEnemySpriteId());
            stmt.setString(15, state.getDifficultyTier());
            stmt.setString(16, state.getUserGridData());
            stmt.setString(17, state.getNotesData());
            stmt.setInt(18, state.getErrorsInLevel());
            stmt.setBoolean(19, state.isProtectionUsed());
            stmt.setString(20, state.getInitialGridData());
            stmt.setString(21, state.getSolvedGridData());
            stmt.setString(22, state.getBonusCellsData());
            stmt.setString(23, state.getBackgroundId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving level state", e);
        }
    }

    /**
     * Replaces the inventory for a given run.
     */
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

    /**
     * Retrieves the active run for a user, fully reconstructing the object graph.
     * Joins with level state and inventory tables to populate all fields.
     */
    public Optional<Run> findActiveRunByUser(String userNick) {
        String sql = """
            SELECT r.*, 
                   ls.initial_grid,
                   ls.solved_grid,
                   ls.user_grid,
                   ls.current_level, ls.enemy_sprite_id, ls.background_id, ls.difficulty_tier, ls.notes_data, ls.errors_in_level, ls.protection_used, ls.bonus_cells_data,
                   i.item_type_id AS item_id, i.capacity AS quantity
            FROM Run r
            JOIN User u ON u.current_run_id = r.run_id
            LEFT JOIN Run_Level_State ls ON r.run_id = ls.run_id
            LEFT JOIN Run_Inventory i ON r.run_id = i.run_id
            WHERE u.nick = ?
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
                    
                    if (run.getCurrentLevelState() == null && rs.getString("initial_grid") != null) {
                        run.setCurrentLevelState(mapLevelState(rs));
                    }
                } catch (Exception e) {
                    System.err.println("Error processing run data: " + e.getMessage());
                }
            }
            
            return runs.values().stream().findFirst();
            
        } catch (SQLException e) {
            System.err.println("Error finding active run: " + e.getMessage());
            return Optional.empty();
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
        run.setScore(rs.getInt("score"));
        
        try { run.setLevelsCompleted(rs.getInt("levels_completed")); } catch (SQLException e) { }
        try { run.setZeroErrorLevels(rs.getInt("zero_error_levels")); } catch (SQLException e) { }
        try { run.setScoreItemPoints(rs.getInt("score_item_points")); } catch (SQLException e) { }
        
        try {
            String used = rs.getString("used_enemies");
            if (used != null && !used.isEmpty()) {
                run.setUsedEnemies(new java.util.HashSet<>(java.util.Arrays.asList(used.split(","))));
            }
        } catch (SQLException e) {}

        return run;
    }

    private RunLevelState mapLevelState(ResultSet rs) throws SQLException {
        return new RunLevelState(
            rs.getInt("run_id"),
            rs.getInt("current_level"),
            rs.getString("enemy_sprite_id"),
            rs.getString("background_id"),
            rs.getString("difficulty_tier"),
            rs.getString("initial_grid"),
            rs.getString("solved_grid"),
            rs.getString("user_grid"),
            rs.getString("notes_data"),
            rs.getInt("errors_in_level"),
            rs.getBoolean("protection_used"),
            rs.getString("bonus_cells_data")
        );
    }
}
