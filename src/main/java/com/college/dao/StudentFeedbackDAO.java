package com.college.dao;

import com.college.models.StudentFeedback;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "SELECT sf.*, s.name as student_name, u.username as faculty_name " +
                "FROM student_feedback sf " +
                "JOIN students s ON sf.student_id = s.id " +
                "JOIN users u ON sf.faculty_id = u.id " +
                "WHERE sf.student_id = ? " +
                "ORDER BY sf.created_at DESC";
        // Note: Logic to curb 'is_private' viewing depends on caller. DAO returns all.
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
        return sf;
    }
}
