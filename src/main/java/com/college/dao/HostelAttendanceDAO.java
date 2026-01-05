package com.college.dao;

import com.college.models.HostelAttendance;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Hostel Attendance
 */
public class HostelAttendanceDAO {

    /**
     * Mark attendance for a single student
     */
    public boolean markAttendance(HostelAttendance attendance) {
        String sql = "INSERT INTO hostel_attendance (student_id, hostel_id, date, status, remarks, marked_by) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status = ?, remarks = ?, marked_by = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, attendance.getStudentId());
            pstmt.setInt(2, attendance.getHostelId());
            pstmt.setDate(3, new java.sql.Date(attendance.getDate().getTime()));
            pstmt.setString(4, attendance.getStatus());
            pstmt.setString(5, attendance.getRemarks());
            if (attendance.getMarkedBy() > 0) {
                pstmt.setInt(6, attendance.getMarkedBy());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            // Update part
            pstmt.setString(7, attendance.getStatus());
            pstmt.setString(8, attendance.getRemarks());
            if (attendance.getMarkedBy() > 0) {
                pstmt.setInt(9, attendance.getMarkedBy());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error marking hostel attendance", e);
            return false;
        }
    }

    /**
     * Get attendance records for a specific date and hostel
     */
    public List<HostelAttendance> getAttendanceByDateAndHostel(java.util.Date date, int hostelId) {
        List<HostelAttendance> records = new ArrayList<>();
        String sql = "SELECT ha.*, s.name as student_name, u.username as enrollment_id FROM hostel_attendance ha " +
                "JOIN students s ON ha.student_id = s.id " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "WHERE ha.date = ? AND ha.hostel_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, new java.sql.Date(date.getTime()));
            pstmt.setInt(2, hostelId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                HostelAttendance ha = new HostelAttendance();
                ha.setId(rs.getInt("id"));
                ha.setStudentId(rs.getInt("student_id"));
                ha.setHostelId(rs.getInt("hostel_id"));
                ha.setDate(rs.getDate("date"));
                ha.setStatus(rs.getString("status"));
                ha.setRemarks(rs.getString("remarks"));
                ha.setMarkedBy(rs.getInt("marked_by"));
                ha.setStudentName(rs.getString("student_name"));
                ha.setEnrollmentId(rs.getString("enrollment_id"));
                records.add(ha);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching hostel attendance", e);
        }
        return records;
    }

    /**
     * Get attendance history for a student
     */
    public List<HostelAttendance> getAttendanceByStudent(int studentId) {
        List<HostelAttendance> records = new ArrayList<>();
        String sql = "SELECT * FROM hostel_attendance WHERE student_id = ? ORDER BY date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                HostelAttendance ha = new HostelAttendance();
                ha.setId(rs.getInt("id"));
                ha.setStudentId(rs.getInt("student_id"));
                ha.setHostelId(rs.getInt("hostel_id"));
                ha.setDate(rs.getDate("date"));
                ha.setStatus(rs.getString("status"));
                ha.setRemarks(rs.getString("remarks"));
                ha.setMarkedBy(rs.getInt("marked_by"));
                records.add(ha);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching student hostel attendance", e);
        }
        return records;
    }

    /**
     * Get attendance record for a specific student and date
     */
    public HostelAttendance getAttendanceByStudentAndDate(int studentId, java.util.Date date) {
        String sql = "SELECT * FROM hostel_attendance WHERE student_id = ? AND date = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setDate(2, new java.sql.Date(date.getTime()));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                HostelAttendance ha = new HostelAttendance();
                ha.setId(rs.getInt("id"));
                ha.setStudentId(rs.getInt("student_id"));
                ha.setHostelId(rs.getInt("hostel_id"));
                ha.setDate(rs.getDate("date"));
                ha.setStatus(rs.getString("status"));
                ha.setRemarks(rs.getString("remarks"));
                ha.setMarkedBy(rs.getInt("marked_by"));
                return ha;
            }
        } catch (SQLException e) {
            Logger.error("Error fetching specific hostel attendance", e);
        }
        return null;
    }
}
