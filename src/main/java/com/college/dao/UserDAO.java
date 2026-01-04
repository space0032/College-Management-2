package com.college.dao;

import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;
import com.college.utils.PasswordUtils;
import java.sql.*;

public class UserDAO {

    public int addUser(String username, String password, String role) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return addUser(conn, username, password, role);
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return -1;
        }
    }

    public int addUser(Connection conn, String username, String password, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, PasswordUtils.hashPasswordLegacy(password)); // Use legacy for compatibility
            pstmt.setString(3, role);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public boolean updateUserRole(int userId, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }
}
