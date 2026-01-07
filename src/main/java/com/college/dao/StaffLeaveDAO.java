package com.college.dao;

import com.college.models.StaffLeave;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffLeaveDAO {

    public boolean createLeaveRequest(StaffLeave leave) {
        String sql = "INSERT INTO staff_leaves (user_id, leave_type, start_date, end_date, reason, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, leave.getUserId());
            pstmt.setString(2, leave.getLeaveType());
            pstmt.setDate(3, Date.valueOf(leave.getStartDate()));
            pstmt.setDate(4, Date.valueOf(leave.getEndDate()));
            pstmt.setString(5, leave.getReason());
            pstmt.setString(6, leave.getStatus());
            pstmt.setTimestamp(7, Timestamp.valueOf(leave.getCreatedAt()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to create staff leave request", e);
            return false;
        }
    }

    public List<StaffLeave> getLeavesByUser(int userId) {
        List<StaffLeave> leaves = new ArrayList<>();
        String sql = "SELECT * FROM staff_leaves WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                leaves.add(mapResultSetToLeave(rs));
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch staff leaves", e);
        }
        return leaves;
    }

    public List<StaffLeave> getAllPendingLeaves() {
        List<StaffLeave> leaves = new ArrayList<>();
        // Join with users table to get staff name (assuming users table has name
        // otherwise join with faculty/staff tables)
        // Since we unified users, let's try to get name from profile tables if
        // possible, or just username.
        // For now, let's assume we can get a display name from users or related tables.
        // Actually, users table doesn't have name (it has username). Faculty table has
        // name.
        // Let's do a left join with faculty table.
        String sql = "SELECT sl.*, COALESCE(f.name, u.username) as staff_name " +
                "FROM staff_leaves sl " +
                "JOIN users u ON sl.user_id = u.id " +
                "LEFT JOIN faculty f ON u.id = f.user_id " +
                "WHERE sl.status = 'PENDING' ORDER BY sl.created_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                StaffLeave leave = mapResultSetToLeave(rs);
                leave.setStaffName(rs.getString("staff_name"));
                leaves.add(leave);
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch pending staff leaves", e);
        }
        return leaves;
    }

    public boolean updateLeaveStatus(int leaveId, String status, int approvedBy, String comments) {
        String sql = "UPDATE staff_leaves SET status = ?, approved_by = ?, approval_date = CURRENT_TIMESTAMP, comments = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, approvedBy);
            pstmt.setString(3, comments);
            pstmt.setInt(4, leaveId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to update staff leave status", e);
            return false;
        }
    }

    private StaffLeave mapResultSetToLeave(ResultSet rs) throws SQLException {
        StaffLeave leave = new StaffLeave();
        leave.setId(rs.getInt("id"));
        leave.setUserId(rs.getInt("user_id"));
        leave.setLeaveType(rs.getString("leave_type"));
        leave.setStartDate(rs.getDate("start_date").toLocalDate());
        leave.setEndDate(rs.getDate("end_date").toLocalDate());
        leave.setReason(rs.getString("reason"));
        leave.setStatus(rs.getString("status"));
        leave.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("approval_date") != null) {
            leave.setApprovalDate(rs.getTimestamp("approval_date").toLocalDateTime());
        }
        leave.setApprovedBy(rs.getInt("approved_by"));
        leave.setComments(rs.getString("comments"));
        return leave;
    }
}
