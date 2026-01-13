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
        // Use EnvConfig utility
        String envUrl = EnvConfig.get("DB_URL");
        String envUser = EnvConfig.get("DB_USER");
        String envPass = EnvConfig.get("DB_PASSWORD");

        if (envUrl != null)
            URL = envUrl;
        if (envUser != null)
            USERNAME = envUser;
        if (envPass != null)
            PASSWORD = envPass;

        System.out.println("Database Config Loaded:");
        System.out.println("  URL: " + URL);
        System.out.println("  User: " + USERNAME);
        // Do not print password
    }

    private static void initDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USERNAME);
            config.setPassword(PASSWORD);

            // Pool settings optimized for desktop/remote usage (Conservative for Supabase
            // Session Mode)
            config.setMaximumPoolSize(15); // Reduced to 2 (Minimum viable)
            config.setMinimumIdle(1);
            config.setIdleTimeout(60000); // 1 minute
            config.setConnectionTimeout(30000); // 30 seconds
            config.setMaxLifetime(600000); // 10 minutes (Reduced to cycle connections faster)
            config.setKeepaliveTime(30000); // Keepalive every 30s to prevent server closure
            config.setLeakDetectionThreshold(10000); // Detect leaks > 10s (relaxed)

            // Driver
            config.setDriverClassName("org.postgresql.Driver");

            dataSource = new HikariDataSource(config);
            // Logger might not be initialized yet, so use stderr if needed or basic sysout
            System.out.println("HikariCP Connection Pool initialized.");

            // Add Shutdown Hook to close pool cleanly
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (dataSource != null && !dataSource.isClosed()) {
                    System.out.println("Closing HikariCP Connection Pool...");
                    dataSource.close();
                    System.out.println("HikariCP Connection Pool closed.");
                }
            }));

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
