package com.college;

import com.college.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminPasswordFixer {
    public static void main(String[] args) {
        System.out.println("Fixing Admin Password...");
        String username = "admin";
        // SHA-256 for 'admin123'
        String legacyHash = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check current value
            try (PreparedStatement check = conn.prepareStatement("SELECT password FROM users WHERE username = ?")) {
                check.setString(1, username);
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    System.out.println("Current password in DB: " + rs.getString("password"));
                }
            }

            // Update
            String sql = "UPDATE users SET password = ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, legacyHash);
                pstmt.setString(2, username);
                int rows = pstmt.executeUpdate();
                System.out.println("Updated rows: " + rows);
            }

            System.out.println("Password fixed to SHA-256 hash of 'admin123'.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
