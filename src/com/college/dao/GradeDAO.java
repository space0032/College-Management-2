package com.college.dao;

import com.college.models.Grade;
import com.college.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Grade entity
 * Handles all database operations for grades
 */
public class GradeDAO {

    /**
     * Add or update a grade
     * 
     * @param grade Grade object
     * @return true if successful
     */
    public boolean saveGrade(Grade grade) {
        String sql = "INSERT INTO grades (student_id, course_id, exam_type, marks_obtained, max_marks, " +
                "grade_letter, percentage, exam_date) VALUES (?, ?, ?, ?, ?, ?, ?, CURDATE()) " +
                "ON DUPLICATE KEY UPDATE marks_obtained=?, max_marks=?, grade_letter=?, percentage=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, grade.getStudentId());
            pstmt.setInt(2, grade.getCourseId());
            pstmt.setString(3, grade.getExamType());
            pstmt.setDouble(4, grade.getMarksObtained());
            pstmt.setDouble(5, grade.getMaxMarks());
            pstmt.setString(6, grade.getGrade());
            pstmt.setDouble(7, grade.getPercentage());

            // For duplicate key update
            pstmt.setDouble(8, grade.getMarksObtained());
            pstmt.setDouble(9, grade.getMaxMarks());
            pstmt.setString(10, grade.getGrade());
            pstmt.setDouble(11, grade.getPercentage());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all grades for a student
     * 
     * @param studentId Student ID
     * @return List of grades
     */
    public List<Grade> getGradesByStudent(int studentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, c.name as course_name FROM grades g " +
                "JOIN courses c ON g.course_id = c.id " +
                "WHERE g.student_id = ? ORDER BY g.exam_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                grades.add(extractGradeFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grades;
    }

    /**
     * Get all grades for a course
     * 
     * @param courseId Course ID
     * @return List of grades
     */
    public List<Grade> getGradesByCourse(int courseId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, s.name as student_name, c.name as course_name " +
                "FROM grades g " +
                "JOIN students s ON g.student_id = s.id " +
                "JOIN courses c ON g.course_id = c.id " +
                "WHERE g.course_id = ? ORDER BY s.name, g.exam_type";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                grades.add(extractGradeFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grades;
    }

    /**
     * Get grades for a specific student and course
     * 
     * @param studentId Student ID
     * @param courseId  Course ID
     * @return List of grades
     */
    public List<Grade> getGrades(int studentId, int courseId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, s.name as student_name, c.name as course_name " +
                "FROM grades g " +
                "JOIN students s ON g.student_id = s.id " +
                "JOIN courses c ON g.course_id = c.id " +
                "WHERE g.student_id = ? AND g.course_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                grades.add(extractGradeFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grades;
    }

    /**
     * Calculate CGPA for a student
     * 
     * @param studentId Student ID
     * @return CGPA (0-10 scale)
     */
    public double calculateCGPA(int studentId) {
        String sql = "SELECT AVG(percentage) as avg_percentage FROM grades WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double avgPercentage = rs.getDouble("avg_percentage");
                return (avgPercentage / 100) * 10; // Convert to 10-point scale
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Get grade distribution for a course
     * 
     * @param courseId Course ID
     * @return Map of grade letter to count
     */
    public Map<String, Integer> getGradeDistribution(int courseId) {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT grade_letter, COUNT(*) as count FROM grades " +
                "WHERE course_id = ? GROUP BY grade_letter";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                distribution.put(rs.getString("grade_letter"), rs.getInt("count"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return distribution;
    }

    /**
     * Extract Grade object from ResultSet
     */
    private Grade extractGradeFromResultSet(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setId(rs.getInt("id"));
        grade.setStudentId(rs.getInt("student_id"));
        grade.setCourseId(rs.getInt("course_id"));
        grade.setExamType(rs.getString("exam_type"));
        grade.setMarksObtained(rs.getDouble("marks_obtained"));
        grade.setMaxMarks(rs.getDouble("max_marks"));
        grade.setGrade(rs.getString("grade_letter"));
        grade.setPercentage(rs.getDouble("percentage"));

        try {
            grade.setStudentName(rs.getString("student_name"));
            grade.setCourseName(rs.getString("course_name"));
        } catch (SQLException e) {
            // Fields might not be in result set
        }

        return grade;
    }
}
