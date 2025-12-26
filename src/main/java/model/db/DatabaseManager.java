package model.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Singleton class managing SQLite database connection and initialization.
 * Handles schema creation, data insertion, and migrations.
 */
public class DatabaseManager {

    private static final String DB_FILE_NAME = "puzzle_rogue.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE_NAME;
    
    private static final String CREATE_SCRIPT = "/db/create_tables.sql";
    private static final String INSERT_SCRIPT = "/db/insert_static_data.sql";

    private static DatabaseManager instance = null;

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Returns the singleton instance of DatabaseManager.
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Establishes a new connection to the SQLite database.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initializes the database by executing create/insert scripts and applying migrations.
     */
    public void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            executeSqlScript(conn, CREATE_SCRIPT);
            executeSqlScript(conn, INSERT_SCRIPT);
            performSchemaMigrations(conn);
        } catch (SQLException e) {
            System.err.println("Fatal error during Database initialization: " + e.getMessage());
        }
    }
    
    /**
     * Applies schema changes (ALTER TABLE) to update existing databases.
     * Swallows exceptions for columns that already exist.
     */
    private void performSchemaMigrations(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            try { stmt.execute("ALTER TABLE Run ADD COLUMN levels_completed INTEGER DEFAULT 0"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE Run ADD COLUMN zero_error_levels INTEGER DEFAULT 0"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE Run ADD COLUMN score_item_points INTEGER DEFAULT 0"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE Run ADD COLUMN score INTEGER DEFAULT 0"); } catch (SQLException e) {}
            
            try { stmt.execute("ALTER TABLE Run_Level_State ADD COLUMN bonus_cells_data TEXT"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE Run_Level_State ADD COLUMN background_id TEXT"); } catch (SQLException e) {}
            
            try { stmt.execute("ALTER TABLE Run ADD COLUMN used_enemies TEXT"); } catch (SQLException e) {}

        } catch (SQLException e) {
            System.err.println("Error performing schema migrations: " + e.getMessage());
        }
    }
    
    /**
     * Reads and executes a SQL script from the classpath resources.
     */
    private void executeSqlScript(Connection conn, String resourcePath) throws SQLException {
        try (InputStream is = getClass().getResourceAsStream(resourcePath);
             Statement stmt = conn.createStatement()) {

            if (is == null) {
            System.err.println("SQL script not found: " + resourcePath);
                return;
            }

            try (Scanner scanner = new Scanner(is).useDelimiter(";")) {
                while (scanner.hasNext()) {
                    String sql = scanner.next().trim();
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading/executing SQL script: " + e.getMessage());
            throw new SQLException("Failed to execute script " + resourcePath, e);
        }
    }
}
