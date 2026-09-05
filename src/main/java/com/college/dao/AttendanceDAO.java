package com.college.dao;

import com.college.models.Attendance;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Attendance entity
 * Handles all database operations for attendance
 */
public class AttendanceDAO {

    /**
     * Mark attendance for a student
     * 
     * @param attendance Attendance record to mark
     * @return true if successful, false otherwise
     */
    public boolean markAttendance(Attendance attendance) {
        return markAttendance(attendance, 1);
    }

    /**
     * Mark attendance for a student, attributing to the acting user.
     */
    public boolean markAttendance(Attendance attendance, int markedBy) {
        if (attendance == null || attendance.getDate() == null) {
            Logger.error("Cannot mark attendance: record or date is null");
            return false;
        }
        if (attendance.getStudentId() <= 0 || attendance.getCourseId() <= 0) {
            Logger.error("Cannot mark attendance: invalid student/course id");
            return false;
        }
        String status = normalizeStatus(attendance.getStatus());
        if (status == null) {
            Logger.error("Cannot mark attendance: invalid status " + attendance.getStatus());
            return false;
        }
        attendance.setStatus(status);
        String sql = "INSERT INTO attendance (student_id, course_id, date, status, remarks, marked_by) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (student_id, course_id, date) DO UPDATE SET " +
                "status = EXCLUDED.status, " +
                "remarks = EXCLUDED.remarks, " +
                "marked_by = EXCLUDED.marked_by"; // updated_at is auto-handled by trigger slightly later but we can
                                                  // ignore or set it if needed, but schema doesn't show updated_at
                                                  // column in V1.

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, attendance.getStudentId());
            pstmt.setInt(2, attendance.getCourseId());
            pstmt.setDate(3, new java.sql.Date(attendance.getDate().getTime()));
            pstmt.setString(4, attendance.getStatus());
            pstmt.setString(5, attendance.getRemarks());
            if (markedBy > 0) pstmt.setInt(6, markedBy); else pstmt.setNull(6, java.sql.Types.INTEGER);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Mark attendance for multiple students (bulk)
     * 
     * @param attendanceList List of attendance records
     * @return number of records marked successfully
     */
    public int markBulkAttendance(List<Attendance> attendanceList) {
        return markBulkAttendance(attendanceList, 1);
    }

    /**
     * Bulk mark with acting-user attribution. Skips invalid rows so one bad
     * record does not fail the whole batch.
     */
    public int markBulkAttendance(List<Attendance> attendanceList, int markedBy) {
        if (attendanceList == null || attendanceList.isEmpty()) return 0;
        String sql = "INSERT INTO attendance (student_id, course_id, date, status, remarks, marked_by) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (student_id, course_id, date) DO UPDATE SET " +
                "status = EXCLUDED.status, " +
                "remarks = EXCLUDED.remarks, " +
                "marked_by = EXCLUDED.marked_by";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement pstmt = conn.prepareStatement(sql);
            int count = 0;

            for (Attendance attendance : attendanceList) {
                if (attendance == null || attendance.getDate() == null
                        || attendance.getStudentId() <= 0 || attendance.getCourseId() <= 0)
                    continue;
                String status = normalizeStatus(attendance.getStatus());
                if (status == null) continue;
                pstmt.setInt(1, attendance.getStudentId());
                pstmt.setInt(2, attendance.getCourseId());
                pstmt.setDate(3, new java.sql.Date(attendance.getDate().getTime()));
                pstmt.setString(4, status);
                pstmt.setString(5, attendance.getRemarks());
                if (markedBy > 0) pstmt.setInt(6, markedBy); else pstmt.setNull(6, java.sql.Types.INTEGER);

                pstmt.addBatch();
                count++;
            }
            if (count == 0) return 0;

            pstmt.executeBatch();
            conn.commit();
            pstmt.close();
            return count;

        } catch (SQLException e) {
            Logger.error("Failed to mark bulk attendance", e);
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ex) {
            }
            return 0;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Get attendance records for a specific course and date
     * 
     * @param courseId Course ID
     * @param date     Date
     * @return List of attendance records
     */
    public List<Attendance> getAttendanceByCourseAndDate(int courseId, java.util.Date date) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT a.*, s.name as student_name, c.name as course_name, u.username AS enrollment_id " +
                "FROM attendance a " +
                "JOIN students s ON a.student_id = s.id " +
                "JOIN courses c ON a.course_id = c.id " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "WHERE a.course_id = ? AND a.date = ? " +
                "ORDER BY s.name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            pstmt.setDate(2, new java.sql.Date(date.getTime()));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return attendanceList;
    }

    /**
     * Get attendance records for a specific student
     * 
     * @param studentId Student ID
     * @return List of attendance records
     */
    public List<Attendance> getAttendanceByStudent(int studentId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT a.*, s.name as student_name, c.name as course_name, u.username AS enrollment_id " +
                "FROM attendance a " +
                "JOIN students s ON a.student_id = s.id " +
                "JOIN courses c ON a.course_id = c.id " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "WHERE a.student_id = ? " +
                "ORDER BY a.date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return attendanceList;
    }

    /**
     * Calculate attendance percentage for a student in a course
     * 
     * @param studentId Student ID
     * @param courseId  Course ID
     * @return Attendance percentage (0-100)
     */
    public double getAttendancePercentage(int studentId, int courseId) {
        String sql = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as present_count " +
                "FROM attendance " +
                "WHERE student_id = ? AND course_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                int present = rs.getInt("present_count");

                if (total > 0) {
                    return (double) present / total * 100.0;
                }
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return 0.0;
    }

    /**
     * Get attendance statistics for all students in a course
     * 
     * @param courseId Course ID
     * @return Map of student ID to attendance percentage
     */
    public Map<Integer, Double> getCourseAttendanceStats(int courseId) {
        Map<Integer, Double> stats = new HashMap<>();
        String sql = "SELECT " +
                "student_id, " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as present_count " +
                "FROM attendance " +
                "WHERE course_id = ? " +
                "GROUP BY student_id";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int studentId = rs.getInt("student_id");
                int total = rs.getInt("total");
                int present = rs.getInt("present_count");

                double percentage = total > 0 ? (double) present / total * 100.0 : 0.0;
                stats.put(studentId, percentage);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return stats;
    }

    /**
     * Get students with low attendance (below threshold)
     * 
     * @param courseId  Course ID
     * @param threshold Threshold percentage (e.g., 75.0)
     * @return List of student IDs with low attendance
     */
    public List<Integer> getLowAttendanceStudents(int courseId, double threshold) {
        List<Integer> studentIds = new ArrayList<>();
        Map<Integer, Double> stats = getCourseAttendanceStats(courseId);

        for (Map.Entry<Integer, Double> entry : stats.entrySet()) {
            if (entry.getValue() < threshold) {
                studentIds.add(entry.getKey());
            }
        }

        return studentIds;
    }

    /**
     * Normalize status to PRESENT / ABSENT / LATE. Returns null if invalid.
     * LATE intentionally does NOT count as present in percentage queries.
     */
    public static String normalizeStatus(String status) {
        if (status == null) return null;
        String s = status.trim().toUpperCase();
        if ("PRESENT".equals(s) || "ABSENT".equals(s) || "LATE".equals(s)) return s;
        return null;
    }

    /**
     * Helper method to extract Attendance object from ResultSet
     * 
     * @param rs ResultSet from query
     * @return Attendance object
     */
    private Attendance extractAttendanceFromResultSet(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getInt("id"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setCourseId(rs.getInt("course_id"));
        attendance.setDate(rs.getDate("date"));
        attendance.setStatus(rs.getString("status"));
        attendance.setRemarks(rs.getString("remarks"));

        // Set display fields if available
        try {
            attendance.setStudentName(rs.getString("student_name"));
            attendance.setCourseName(rs.getString("course_name"));
        } catch (SQLException e) {
            // Fields might not be in result set
        }
        attendance.setEnrollmentId(rs.getString("enrollment_id"));

        return attendance;
    }
}
