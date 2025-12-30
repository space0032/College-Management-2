package com.college.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class for managing database connections
 * Provides a single connection instance throughout the application
 */
public class DatabaseConnection {

    // Database credentials - modify these according to your MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/college_management";
    private static final String USERNAME = "collegeapp";
    private static final String PASSWORD = "college123"; // Application user password

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
