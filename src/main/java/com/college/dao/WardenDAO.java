package com.college.dao;

import com.college.models.Warden;

import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Warden Management
 */
public class WardenDAO {

    /**
     * Add new warden
     */
    /**
     * Add new warden with auto-generated user account
     */
    public int addWarden(Warden warden) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // 1. Create User Account if not exists
                int userId = warden.getUserId();
                if (userId <= 0) {
                    // Generate unique username: WARDEN + 4 random digits
                    String username = "WARDEN" + (int) (Math.random() * 9000 + 1000);
                    UserDAO userDAO = new UserDAO();
                    RoleDAO roleDAO = new RoleDAO();

                    // Use SHARED connection to avoid pool deadlock
                    com.college.models.Role wardenRole = roleDAO.getRoleByCode(conn, "WARDEN");
                    int roleId = (wardenRole != null) ? wardenRole.getId() : 0;

                    // Ensure uniqueness (simple retry logic could be added here, but purely random
                    // is usually fine for low volume)
                    // Default password: password123
                    if (roleId > 0) {
                        userId = userDAO.addUser(conn, username, "password123", "WARDEN", roleId);
                    } else {
                        userId = userDAO.addUser(conn, username, "password123", "WARDEN");
                    }

                    if (userId <= 0) {
                        conn.rollback();
                        return -1;
                    }
                    warden.setUserId(userId);
                    warden.setUsername(username); // Set for display back to user
                }

                // 2. Insert Warden
                String sql = "INSERT INTO wardens (name, email, phone, hostel_id, user_id) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, warden.getName());
                    pstmt.setString(2, warden.getEmail());
                    pstmt.setString(3, warden.getPhone());

                    if (warden.getHostelId() > 0) {
                        pstmt.setInt(4, warden.getHostelId());
                    } else {
                        pstmt.setNull(4, Types.INTEGER);
                    }

                    pstmt.setInt(5, userId);

                    int affectedRows = pstmt.executeUpdate();

                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int wardenId = generatedKeys.getInt(1);
                                conn.commit(); // Commit transaction

                                // AUTO-CREATE EMPLOYEE RECORD WITH SALARY
                                try {
                                    createEmployeeRecord(warden, userId);
                                } catch (Exception e) {
                                    Logger.error("Failed to auto-create employee for warden", e);
                                    // Don't fail the operation
                                }

                                return wardenId;
                            }
                        }
                    }
                }
                conn.rollback();
                return -1;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return -1;
        }
    }

    /**
     * Get all wardens
     */
    public List<Warden> getAllWardens() {
        List<Warden> wardens = new ArrayList<>();
        String sql = "SELECT w.*, h.name as hostel_name, u.username FROM wardens w " +
                "LEFT JOIN hostels h ON w.hostel_id = h.id " +
                "LEFT JOIN users u ON w.user_id = u.id " +
                "ORDER BY w.name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                wardens.add(extractWardenFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return wardens;
    }

    /**
     * Update warden
     */
    public boolean updateWarden(Warden warden) {
        String sql = "UPDATE wardens SET name = ?, email = ?, phone = ?, hostel_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, warden.getName());
            pstmt.setString(2, warden.getEmail());
            pstmt.setString(3, warden.getPhone());

            if (warden.getHostelId() > 0) {
                pstmt.setInt(4, warden.getHostelId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.setInt(5, warden.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return false;
    }

    /**
     * Delete warden
     */
    public boolean deleteWarden(int id) {
        // First delete user account if exists
        Warden warden = getWardenById(id);
        if (warden != null && warden.getUserId() > 0) {
            deleteUser(warden.getUserId());
        }

        String sql = "DELETE FROM wardens WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return false;
    }

    private void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
    }

    /**
     * Get warden by ID
     */
    public Warden getWardenById(int id) {
        String sql = "SELECT w.*, h.name as hostel_name, u.username FROM wardens w " +
                "LEFT JOIN hostels h ON w.hostel_id = h.id " +
                "LEFT JOIN users u ON w.user_id = u.id " +
                "WHERE w.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractWardenFromResultSet(rs);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return null;
    }

    /**
     * Get warden by User ID
     */
    public Warden getWardenByUserId(int userId) {
        String sql = "SELECT w.*, h.name as hostel_name, u.username FROM wardens w " +
                "LEFT JOIN hostels h ON w.hostel_id = h.id " +
                "LEFT JOIN users u ON w.user_id = u.id " +
                "WHERE w.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractWardenFromResultSet(rs);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return null;
    }

    private Warden extractWardenFromResultSet(ResultSet rs) throws SQLException {
        Warden warden = new Warden();
        warden.setId(rs.getInt("id"));
        warden.setName(rs.getString("name"));
        warden.setEmail(rs.getString("email"));
        warden.setPhone(rs.getString("phone"));

        int hostelId = rs.getInt("hostel_id");
        if (!rs.wasNull()) {
            warden.setHostelId(hostelId);
        }

        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            warden.setUserId(userId);
        }

        // Check if hostel_name column exists (it might not if using simple select)
        try {
            warden.setHostelName(rs.getString("hostel_name"));
        } catch (SQLException e) {
            // Ignore if column not found
        }

        try {
            warden.setUsername(rs.getString("username"));
        } catch (SQLException e) {
            // Ignore
        }

        return warden;
    }

    private void createEmployeeRecord(Warden warden, int userId) {
        if (userId <= 0)
            return;

        UserDAO userDAO = new UserDAO();
        com.college.models.User user = userDAO.getUserById(userId);

        EmployeeDAO employeeDAO = new EmployeeDAO();
        com.college.models.Employee emp = new com.college.models.Employee();

        // Use username if available, otherwise fetch from user
        String username = warden.getUsername();
        if (username == null || username.isEmpty()) {
            if (user != null)
                username = user.getUsername();
            else
                return;
        }

        emp.setEmployeeId(username);
        emp.setFirstName(warden.getName());
        emp.setLastName("");
        emp.setEmail(warden.getEmail());
        emp.setPhone(warden.getPhone());
        emp.setDesignation("WARDEN");
        emp.setSalary(new java.math.BigDecimal("40000")); // Warden Salary
        emp.setStatus(com.college.models.Employee.Status.ACTIVE);
        emp.setJoinDate(java.time.LocalDate.now());

        employeeDAO.addEmployee(emp);
    }
}
