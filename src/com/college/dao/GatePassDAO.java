package com.college.dao;

import com.college.models.GatePass;
import com.college.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * GatePassDAO - Data Access Object for Gate Pass operations
 */
public class GatePassDAO {

    /**
     * Create a new gate pass request
     */
    public static boolean createRequest(GatePass gatePass) {
        String sql = "INSERT INTO gate_passes (student_id, from_date, to_date, reason, " +
                "destination, parent_contact) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gatePass.getStudentId());
            pstmt.setDate(2, Date.valueOf(gatePass.getFromDate()));
            pstmt.setDate(3, Date.valueOf(gatePass.getToDate()));
            pstmt.setString(4, gatePass.getReason());
            pstmt.setString(5, gatePass.getDestination());
            pstmt.setString(6, gatePass.getParentContact());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Approve a gate pass request
     */
    public static boolean approveRequest(int gatePassId, int approvedBy, String comment) {
        String sql = "UPDATE gate_passes SET status = 'APPROVED', approved_by = ?, " +
                "approved_at = NOW(), approval_comment = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, approvedBy);
            pstmt.setString(2, comment);
            pstmt.setInt(3, gatePassId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reject a gate pass request
     */
    public static boolean rejectRequest(int gatePassId, int rejectedBy, String comment) {
        String sql = "UPDATE gate_passes SET status = 'REJECTED', approved_by = ?, " +
                "approved_at = NOW(), approval_comment = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rejectedBy);
            pstmt.setString(2, comment);
            pstmt.setInt(3, gatePassId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all gate passes for a student
     */
    public static List<GatePass> getStudentPasses(int studentId) {
        List<GatePass> passes = new ArrayList<>();
        String sql = "SELECT gp.*, s.name as student_name, u.username as approved_by_name " +
                "FROM gate_passes gp " +
                "JOIN students s ON gp.student_id = s.id " +
                "LEFT JOIN users u ON gp.approved_by = u.id " +
                "WHERE gp.student_id = ? " +
                "ORDER BY gp.requested_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                passes.add(mapResultSetToGatePass(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return passes;
    }

    /**
     * Get all pending gate pass requests
     */
    public static List<GatePass> getPendingPasses() {
        List<GatePass> passes = new ArrayList<>();
        String sql = "SELECT gp.*, s.name as student_name, u.username as approved_by_name " +
                "FROM gate_passes gp " +
                "JOIN students s ON gp.student_id = s.id " +
                "LEFT JOIN users u ON gp.approved_by = u.id " +
                "WHERE gp.status = 'PENDING' " +
                "ORDER BY gp.requested_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                passes.add(mapResultSetToGatePass(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return passes;
    }

    /**
     * Get all gate passes
     */
    public static List<GatePass> getAllPasses() {
        List<GatePass> passes = new ArrayList<>();
        String sql = "SELECT gp.*, s.name as student_name, u.username as approved_by_name " +
                "FROM gate_passes gp " +
                "JOIN students s ON gp.student_id = s.id " +
                "LEFT JOIN users u ON gp.approved_by = u.id " +
                "ORDER BY gp.requested_at DESC " +
                "LIMIT 500";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                passes.add(mapResultSetToGatePass(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return passes;
    }

    /**
     * Get gate passes by status
     */
    public static List<GatePass> getPassesByStatus(String status) {
        List<GatePass> passes = new ArrayList<>();
        String sql = "SELECT gp.*, s.name as student_name, u.username as approved_by_name " +
                "FROM gate_passes gp " +
                "JOIN students s ON gp.student_id = s.id " +
                "LEFT JOIN users u ON gp.approved_by = u.id " +
                "WHERE gp.status = ? " +
                "ORDER BY gp.requested_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                passes.add(mapResultSetToGatePass(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return passes;
    }

    /**
     * Map ResultSet to GatePass object
     */
    private static GatePass mapResultSetToGatePass(ResultSet rs) throws SQLException {
        GatePass gatePass = new GatePass();

        gatePass.setId(rs.getInt("id"));
        gatePass.setStudentId(rs.getInt("student_id"));
        gatePass.setStudentName(rs.getString("student_name"));

        Date fromDate = rs.getDate("from_date");
        if (fromDate != null) {
            gatePass.setFromDate(fromDate.toLocalDate());
        }

        Date toDate = rs.getDate("to_date");
        if (toDate != null) {
            gatePass.setToDate(toDate.toLocalDate());
        }

        gatePass.setReason(rs.getString("reason"));
        gatePass.setDestination(rs.getString("destination"));
        gatePass.setParentContact(rs.getString("parent_contact"));
        gatePass.setStatus(rs.getString("status"));

        Timestamp requestedAt = rs.getTimestamp("requested_at");
        if (requestedAt != null) {
            gatePass.setRequestedAt(requestedAt.toLocalDateTime());
        }

        int approvedBy = rs.getInt("approved_by");
        gatePass.setApprovedBy(rs.wasNull() ? null : approvedBy);
        gatePass.setApprovedByName(rs.getString("approved_by_name"));

        Timestamp approvedAt = rs.getTimestamp("approved_at");
        if (approvedAt != null) {
            gatePass.setApprovedAt(approvedAt.toLocalDateTime());
        }

        gatePass.setApprovalComment(rs.getString("approval_comment"));

        return gatePass;
    }

    /**
     * Delete a gate pass (admin only)
     */
    public static boolean deleteGatePass(int gatePassId) {
        String sql = "DELETE FROM gate_passes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gatePassId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
