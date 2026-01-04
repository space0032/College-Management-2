package com.college.dao;

import com.college.models.Warden;
import com.college.utils.DatabaseConnection;

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
    public int addWarden(Warden warden) {
        String sql = "INSERT INTO wardens (name, email, phone, hostel_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, warden.getName());
            pstmt.setString(2, warden.getEmail());
            pstmt.setString(3, warden.getPhone());

            if (warden.getHostelId() > 0) {
                pstmt.setInt(4, warden.getHostelId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Get all wardens
     */
    public List<Warden> getAllWardens() {
        List<Warden> wardens = new ArrayList<>();
        String sql = "SELECT w.*, h.name as hostel_name FROM wardens w " +
                "LEFT JOIN hostels h ON w.hostel_id = h.id " +
                "ORDER BY w.name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                wardens.add(extractWardenFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    /**
     * Get warden by ID
     */
    public Warden getWardenById(int id) {
        String sql = "SELECT w.*, h.name as hostel_name FROM wardens w " +
                "LEFT JOIN hostels h ON w.hostel_id = h.id WHERE w.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractWardenFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get warden by User ID
     */
    public Warden getWardenByUserId(int userId) {
        String sql = "SELECT w.*, h.name as hostel_name FROM wardens w " +
                "LEFT JOIN hostels h ON w.hostel_id = h.id WHERE w.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractWardenFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
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

        return warden;
    }
}
