package com.college.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class for managing database connections
 * Uses HikariCP for high-performance connection pooling
 */
public class DatabaseConnection {

    private static HikariDataSource dataSource;

    // Database credentials
    private static String URL = "jdbc:postgresql://localhost:5432/college_db";
    private static String USERNAME = "postgres";
    private static String PASSWORD = "password";

    static {
        loadEnv();
        initDataSource();
    }

    private static void loadEnv() {
        // 1. Try Environment Variables first
        if (System.getenv("DB_URL") != null)
            URL = System.getenv("DB_URL");
        if (System.getenv("DB_USER") != null)
            USERNAME = System.getenv("DB_USER");
        if (System.getenv("DB_PASSWORD") != null)
            PASSWORD = System.getenv("DB_PASSWORD");

        // 2. Try .env file if Env Vars are missing
        java.io.File envFile = new java.io.File(".env");

        if (envFile.exists()) {
            try (java.util.Scanner scanner = new java.util.Scanner(envFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty() || line.startsWith("#"))
                        continue;
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        if (key.equals("DB_USER")) {
                            USERNAME = value;
                        } else if (key.equals("DB_PASSWORD")) {
                            PASSWORD = value;
                        } else if (key.equals("DB_URL")) {
                            URL = value;
                        }
                    }
                }
            } catch (java.io.FileNotFoundException e) {
                System.err.println("Error reading .env file: " + e.getMessage());
            }
        }
    }

    private static void initDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USERNAME);
            config.setPassword(PASSWORD);

            // Pool settings optimized for desktop/remote usage
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000); // 30 seconds
            config.setConnectionTimeout(30000); // 30 seconds
            config.setLeakDetectionThreshold(2000); // Detect leaks > 2s

            // Driver
            config.setDriverClassName("org.postgresql.Driver");

            dataSource = new HikariDataSource(config);
            // Logger might not be initialized yet, so use stderr if needed or basic sysout
            System.out.println("HikariCP Connection Pool initialized.");

        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to initialize Connection Pool: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    private DatabaseConnection() {
    }

    /**
     * Get database connection from the pool
     * 
     * @return Connection object
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized. Check logs for startup errors.");
        }
        return dataSource.getConnection();
    }

    /**
     * Test database connection
     * 
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Test connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Shutdown the pool
     */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
