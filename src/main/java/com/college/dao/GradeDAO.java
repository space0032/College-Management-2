package com.college.dao;

import com.college.models.Grade;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

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
        String sql = "INSERT INTO grades (student_id, course_id, exam_type, marks, grade, semester) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE marks=?, grade=?, semester=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, grade.getStudentId());
            pstmt.setInt(2, grade.getCourseId());
            pstmt.setString(3, grade.getExamType());
            pstmt.setDouble(4, grade.getMarksObtained());
            pstmt.setString(5, grade.getGrade());
            pstmt.setInt(6, grade.getSemester() != 0 ? grade.getSemester() : 1); // Default semester 1 if not set

            // For duplicate key update
            pstmt.setDouble(7, grade.getMarksObtained());
            pstmt.setString(8, grade.getGrade());
            pstmt.setInt(9, grade.getSemester() != 0 ? grade.getSemester() : 1);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
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
        String sql = "SELECT g.id, g.student_id, g.course_id, g.exam_type, g.marks, g.grade, g.semester, " +
                "c.name as course_name, c.credits FROM grades g " +
                "JOIN courses c ON g.course_id = c.id " +
                "WHERE g.student_id = ? ORDER BY g.exam_type DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                grades.add(extractGradeFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
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
        String sql = "SELECT g.id, g.student_id, g.course_id, g.exam_type, g.marks, g.grade, g.semester, " +
                "s.name as student_name, c.name as course_name " +
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
            Logger.error("Database operation failed", e);
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
        String sql = "SELECT g.id, g.student_id, g.course_id, g.exam_type, g.marks, g.grade, g.semester, " +
                "s.name as student_name, c.name as course_name " +
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
            Logger.error("Database operation failed", e);
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
            Logger.error("Database operation failed", e);
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
        String sql = "SELECT grade, COUNT(*) as count FROM grades " +
                "WHERE course_id = ? GROUP BY grade";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                distribution.put(rs.getString("grade"), rs.getInt("count"));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return distribution;
    }

    /**
     * Get ALL grades with full details (Admin/Faculty view)
     */
    public List<Grade> getAllGrades() {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.id, g.student_id, g.course_id, g.exam_type, g.marks, g.grade, g.semester, " +
                "s.name as student_name, u.username as enrollment_no, " +
                "c.name as course_name, c.credits, d.name as dept_name " +
                "FROM grades g " +
                "JOIN students s ON g.student_id = s.id " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "JOIN courses c ON g.course_id = c.id " +
                "LEFT JOIN departments d ON c.department_id = d.id " +
                "ORDER BY s.name, c.name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                grades.add(extractGradeFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Fetch all grades failed", e);
        }
        return grades;
    }

    /**
     * Get grades by Faculty (only courses taught by them)
     */
    public List<Grade> getGradesByFaculty(int facultyId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.id, g.student_id, g.course_id, g.exam_type, g.marks, g.grade, g.semester, " +
                "s.name as student_name, u.username as enrollment_no, " +
                "c.name as course_name, c.credits, d.name as dept_name " +
                "FROM grades g " +
                "JOIN students s ON g.student_id = s.id " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "JOIN courses c ON g.course_id = c.id " +
                "LEFT JOIN departments d ON c.department_id = d.id " +
                "WHERE c.faculty_id = ? " +
                "ORDER BY s.name, c.name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facultyId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                grades.add(extractGradeFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Fetch faculty grades failed", e);
        }
        return grades;
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
        grade.setMarksObtained(rs.getDouble("marks"));
        grade.setGrade(rs.getString("grade"));
        grade.setSemester(rs.getInt("semester"));

        try {
            grade.setStudentName(rs.getString("student_name"));
            grade.setCourseName(rs.getString("course_name"));
            grade.setCredits(rs.getInt("credits"));
            // Optional fields
            try {
                grade.setEnrollmentNumber(rs.getString("enrollment_no"));
            } catch (Exception e) {
            }
            try {
                grade.setDepartment(rs.getString("dept_name"));
            } catch (Exception e) {
            }

        } catch (SQLException e) {
            // Fields might not be in result set
        }

        return grade;
    }
}
