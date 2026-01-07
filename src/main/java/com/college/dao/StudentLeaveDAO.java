package com.college.dao;

import com.college.models.StudentLeave;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentLeaveDAO {

    public boolean createLeaveRequest(StudentLeave leave) {
        String sql = "INSERT INTO student_leaves (student_id, leave_type, start_date, end_date, reason) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, leave.getStudentId());
            stmt.setString(2, leave.getLeaveType());
            stmt.setDate(3, leave.getStartDate());
            stmt.setDate(4, leave.getEndDate());
            stmt.setString(5, leave.getReason());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to create leave request", e);
            return false;
        }
    }

    public List<StudentLeave> getLeavesByStudent(int studentId) {
        List<StudentLeave> leaves = new ArrayList<>();
        String sql = "SELECT * FROM student_leaves WHERE student_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                leaves.add(mapResultSetToLeave(rs));
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch student leaves", e);
        }
        return leaves;
    }

    public List<StudentLeave> getPendingLeaves() {
        List<StudentLeave> leaves = new ArrayList<>();
        String sql = "SELECT sl.*, s.name as student_name FROM student_leaves sl " +
                "JOIN students s ON sl.student_id = s.id " +
                "WHERE sl.status = 'PENDING' ORDER BY sl.created_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                StudentLeave leave = mapResultSetToLeave(rs);
                leave.setStudentName(rs.getString("student_name"));
                leaves.add(leave);
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch pending leaves", e);
        }
        return leaves;
    }

    public boolean updateLeaveStatus(int leaveId, String status, int approvedBy) {
        String sql = "UPDATE student_leaves SET status = ?, approved_by = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, approvedBy);
            stmt.setInt(3, leaveId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to update leave status", e);
            return false;
        }
    }

    private StudentLeave mapResultSetToLeave(ResultSet rs) throws SQLException {
        StudentLeave leave = new StudentLeave();
        leave.setId(rs.getInt("id"));
        leave.setStudentId(rs.getInt("student_id"));
        leave.setLeaveType(rs.getString("leave_type"));
        leave.setStartDate(rs.getDate("start_date"));
        leave.setEndDate(rs.getDate("end_date"));
        leave.setReason(rs.getString("reason"));
        leave.setStatus(rs.getString("status"));
        leave.setApprovedBy(rs.getInt("approved_by"));
        leave.setCreatedAt(rs.getTimestamp("created_at"));

        // Ensure approvedBy is 0 if null, which getInt does automatically for SQL NULL
        // if not checked with wasNull
        // But for clarity, it's fine.
        return leave;
    }
}
