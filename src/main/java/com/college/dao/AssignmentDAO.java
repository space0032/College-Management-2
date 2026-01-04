package com.college.dao;

import com.college.models.Assignment;
import com.college.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDAO {

    /**
     * Create a new assignment
     */
    /**
     * Create a new assignment
     */
    public boolean createAssignment(Assignment assignment) {
        String sql = "INSERT INTO assignments (course_id, title, description, due_date, created_by, semester) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, assignment.getCourseId());
            pstmt.setString(2, assignment.getTitle());
            pstmt.setString(3, assignment.getDescription());
            pstmt.setTimestamp(4, new Timestamp(assignment.getDueDate().getTime()));
            pstmt.setInt(5, assignment.getCreatedBy());
            pstmt.setInt(6, assignment.getSemester());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get assignments by course ID
     */
    public List<Assignment> getAssignmentsByCourse(int courseId) {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT a.*, c.name as course_name, u.username as faculty_name " +
                "FROM assignments a " +
                "JOIN courses c ON a.course_id = c.id " +
                "JOIN users u ON a.created_by = u.id " +
                "WHERE a.course_id = ? ORDER BY a.due_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                assignments.add(extractAssignmentFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assignments;
    }

    /**
     * Get assignments by course ID AND Semester
     */
    public List<Assignment> getAssignmentsByCourseAndSemester(int courseId, int semester) {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT a.*, c.name as course_name, u.username as faculty_name " +
                "FROM assignments a " +
                "JOIN courses c ON a.course_id = c.id " +
                "JOIN users u ON a.created_by = u.id " +
                "WHERE a.course_id = ? AND a.semester = ? ORDER BY a.due_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            pstmt.setInt(2, semester);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                assignments.add(extractAssignmentFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assignments;
    }

    /**
     * Get assignments by Semester (for all courses)
     */
    public List<Assignment> getAssignmentsBySemester(int semester) {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT a.*, c.name as course_name, u.username as faculty_name " +
                "FROM assignments a " +
                "JOIN courses c ON a.course_id = c.id " +
                "JOIN users u ON a.created_by = u.id " +
                "WHERE a.semester = ? ORDER BY a.due_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, semester);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                assignments.add(extractAssignmentFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assignments;
    }

    /**
     * Get assignments created by a faculty member
     */
    public List<Assignment> getAssignmentsByFaculty(int facultyId) {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT a.*, c.name as course_name, u.username as faculty_name " +
                "FROM assignments a " +
                "JOIN courses c ON a.course_id = c.id " +
                "JOIN users u ON a.created_by = u.id " +
                "WHERE a.created_by = ? ORDER BY a.due_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facultyId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                assignments.add(extractAssignmentFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assignments;
    }

    /**
     * Get assignment by ID
     */
    public Assignment getAssignmentById(int id) {
        String sql = "SELECT a.*, c.name as course_name, u.username as faculty_name " +
                "FROM assignments a " +
                "JOIN courses c ON a.course_id = c.id " +
                "JOIN users u ON a.created_by = u.id " +
                "WHERE a.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractAssignmentFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Update assignment
     */
    public boolean updateAssignment(Assignment assignment) {
        String sql = "UPDATE assignments SET title = ?, description = ?, due_date = ?, semester = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, assignment.getTitle());
            pstmt.setString(2, assignment.getDescription());
            pstmt.setTimestamp(3, new Timestamp(assignment.getDueDate().getTime()));
            pstmt.setInt(4, assignment.getSemester());
            pstmt.setInt(5, assignment.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete assignment
     */
    public boolean deleteAssignment(int id) {
        String sql = "DELETE FROM assignments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Assignment extractAssignmentFromResultSet(ResultSet rs) throws SQLException {
        Assignment assignment = new Assignment();
        assignment.setId(rs.getInt("id"));
        assignment.setCourseId(rs.getInt("course_id"));
        assignment.setTitle(rs.getString("title"));
        assignment.setDescription(rs.getString("description"));
        assignment.setDueDate(rs.getTimestamp("due_date"));
        assignment.setCreatedBy(rs.getInt("created_by"));
        assignment.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            // Check if column exists or default to 1
            assignment.setSemester(rs.getInt("semester"));
        } catch (SQLException e) {
            assignment.setSemester(1);
        }

        // Optional fields
        try {
            assignment.setCourseName(rs.getString("course_name"));
        } catch (SQLException e) {
        }
        try {
            assignment.setFacultyName(rs.getString("faculty_name"));
        } catch (SQLException e) {
        }

        return assignment;
    }
}
