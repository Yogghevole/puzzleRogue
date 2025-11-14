package model.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Gestisce la connessione al database SQLite e l'inizializzazione dello schema.
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

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void initializeDatabase() {
        System.out.println("Initializing Database (" + DB_FILE_NAME + ")...");
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            executeSqlScript(conn, CREATE_SCRIPT);
            executeSqlScript(conn, INSERT_SCRIPT);
        System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Fatal error during Database initialization: " + e.getMessage());
        }
    }
    
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