package com.college.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections
 * Provides database connection instances with environment-based configuration
 */
public class DatabaseConnection {

    // Database credentials
    private static String URL = "jdbc:mysql://localhost:3306/college_db";
    private static String USERNAME = "root";
    private static String PASSWORD = "";

    static {
        loadEnv();
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

    /**
     * Private constructor to prevent instantiation
     */
    private DatabaseConnection() {
    }

    /**
     * Get database connection instance
     * Creates a new connection for each request to avoid concurrency issues
     * 
     * @return Connection object
     */
    public static Connection getConnection() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create new connection for each request
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
        return null;
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
            return false;
        }
    }
}
