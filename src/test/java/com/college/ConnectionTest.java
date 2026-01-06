package com.college;

import com.college.utils.DatabaseConnection;
import java.sql.Connection;

public class ConnectionTest {
    public static void main(String[] args) {
        System.out.println("Testing Database Connection...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("CONNECTION SUCCESS: " + conn.getMetaData().getURL());

                // Run Migration
                System.out.println("Running Migration...");
                com.college.utils.DatabaseMigrator.migrate();

                // Test Query
                System.out.println("Verifying Schema...");
                try (java.sql.Statement stmt = conn.createStatement();
                        java.sql.ResultSet rs = stmt.executeQuery("SELECT count(*) FROM roles")) {
                    if (rs.next()) {
                        System.out.println("Roles count: " + rs.getInt(1));
                    }
                }

            } else {
                System.out.println("CONNECTION FAILED: Connection is null or closed.");
            }
        } catch (Exception e) {
            System.out.println("CONNECTION EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
