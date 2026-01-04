package com.college.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class for managing database connections
 * Provides a single connection instance throughout the application
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
        System.out.println("[DEBUG] Loading environment variables...");

        // 1. Try Environment Variables first
        if (System.getenv("DB_URL") != null)
            URL = System.getenv("DB_URL");
        if (System.getenv("DB_USER") != null)
            USERNAME = System.getenv("DB_USER");
        if (System.getenv("DB_PASSWORD") != null)
            PASSWORD = System.getenv("DB_PASSWORD");

        // 2. Try .env file if Env Vars are missing
        java.io.File envFile = new java.io.File(".env");
        System.out.println("[DEBUG] Looking for .env at: " + envFile.getAbsolutePath());

        if (envFile.exists()) {
            System.out.println("[DEBUG] .env file found!");
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
                            System.out.println("[DEBUG] Loaded DB_USER: " + USERNAME);
                        } else if (key.equals("DB_PASSWORD")) {
                            PASSWORD = value;
                            System.out
                                    .println("[DEBUG] Loaded DB_PASSWORD: " + (value.isEmpty() ? "(empty)" : "******"));
                        } else if (key.equals("DB_URL")) {
                            URL = value;
                        }
                    }
                }
            } catch (java.io.FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[DEBUG] .env file NOT found.");
        }
        System.out.println("[DEBUG] Final DB User: " + USERNAME);
    }

    private static Connection connection = null;

    /**
     * Private constructor to prevent instantiation
     */
    private DatabaseConnection() {
    }

    /**
     * Get database connection instance
     * Creates a new connection if one doesn't exist
     * 
     * @return Connection object
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Create connection
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                // System.out.println("Database connected successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database!");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Close the database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection!");
            e.printStackTrace();
        }
    }

    /**
     * Test database connection
     * 
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
