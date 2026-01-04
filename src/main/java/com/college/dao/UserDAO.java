package com.college.dao;

import com.college.models.User;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;
import com.college.utils.PasswordUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, r.name as role_name FROM users u " +
                "LEFT JOIN roles r ON u.role_id = r.id " +
                "ORDER BY u.username";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setRoleId(rs.getInt("role_id"));
                user.setRoleName(rs.getString("role_name"));

                // If role_name is null (legacy user), show legacy role
                if (user.getRoleName() == null) {
                    user.setRoleName(user.getRole());
                }

                users.add(user);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching users", e);
        }
        return users;
    }

    public List<User> getSpecialUsers() {
        List<User> users = new ArrayList<>();
        // Exclude users with roles STUDENT or FACULTY
        // Assuming roles are stored as 'STUDENT' and 'FACULTY' in the 'role' column or
        // joined via role table
        String sql = "SELECT u.*, r.name as role_name, r.code as role_code FROM users u " +
                "LEFT JOIN roles r ON u.role_id = r.id " +
                "WHERE (r.code NOT IN ('STUDENT', 'FACULTY') OR r.code IS NULL) " +
                "AND (u.role NOT IN ('STUDENT', 'FACULTY')) " + // Legacy check
                "ORDER BY u.username";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setRoleId(rs.getInt("role_id"));
                user.setRoleName(rs.getString("role_name"));

                if (user.getRoleName() == null) {
                    user.setRoleName(user.getRole());
                }

                users.add(user);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching special users", e);
        }
        return users;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error deleting user", e);
            return false;
        }
    }

    public boolean isUsernameTaken(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.error("Error checking username", e);
            return true; // Fail safe
        }
    }
}
