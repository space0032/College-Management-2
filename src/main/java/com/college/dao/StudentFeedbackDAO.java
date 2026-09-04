package com.college.dao;

import com.college.models.StudentFeedback;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentFeedbackDAO {

    public boolean addFeedback(StudentFeedback feedback) {
        String sql = "INSERT INTO student_feedback (student_id, faculty_id, feedback_text, category, is_private) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, feedback.getStudentId());
            pstmt.setInt(2, feedback.getFacultyId());
            pstmt.setString(3, feedback.getFeedbackText());
            pstmt.setString(4, feedback.getCategory());
            pstmt.setBoolean(5, feedback.isPrivate());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error adding feedback: " + e.getMessage());
            return false;
        }
    }

    public List<StudentFeedback> getFeedbackByStudent(int studentId) {
        List<StudentFeedback> list = new ArrayList<>();
        String sql = "SELECT sf.*, s.name as student_name, fc.name as faculty_name, u.username AS enrollment_id " +
                "FROM student_feedback sf " +
                "JOIN students s ON sf.student_id = s.id " +
                "LEFT JOIN users fu ON sf.faculty_id = fu.id " +
                "LEFT JOIN faculty fc ON fc.user_id = fu.id " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "WHERE sf.student_id = ? " +
                "ORDER BY sf.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extract(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching feedback: " + e.getMessage());
        }
        return list;
    }

    public List<Map<String, Object>> getFeedbackByFacultyId(int facultyId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT sf.*, s.name as student_name, u.username as enrollment_id " +
                "FROM student_feedback sf " +
                "JOIN students s ON sf.student_id = s.id " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "WHERE sf.faculty_id = (SELECT id FROM users WHERE id = (SELECT user_id FROM faculty WHERE id = ?)) " +
                "OR sf.faculty_id = ? " +
                "ORDER BY sf.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, facultyId);
            pstmt.setInt(2, facultyId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("studentId", rs.getInt("student_id"));
                row.put("facultyId", rs.getInt("faculty_id"));
                row.put("feedbackText", rs.getString("feedback_text"));
                row.put("category", rs.getString("category"));
                row.put("isPrivate", rs.getBoolean("is_private"));
                row.put("createdAt", rs.getTimestamp("created_at"));
                row.put("studentName", rs.getString("student_name"));
                row.put("enrollmentId", rs.getString("enrollment_id"));
                list.add(row);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching faculty feedback: " + e.getMessage());
        }
        return list;
    }

    private StudentFeedback extract(ResultSet rs) throws SQLException {
        StudentFeedback sf = new StudentFeedback();
        sf.setId(rs.getInt("id"));
        sf.setStudentId(rs.getInt("student_id"));
        sf.setFacultyId(rs.getInt("faculty_id"));
        sf.setFeedbackText(rs.getString("feedback_text"));
        sf.setCategory(rs.getString("category"));
        sf.setPrivate(rs.getBoolean("is_private"));
        sf.setCreatedAt(rs.getTimestamp("created_at"));

        sf.setStudentName(rs.getString("student_name"));
        sf.setFacultyName(rs.getString("faculty_name"));
        sf.setEnrollmentId(rs.getString("enrollment_id"));
        return sf;
    }
}
