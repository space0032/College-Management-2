package com.college.dao;

import com.college.models.Course;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Course entity
 */
public class CourseDAO {

    public boolean addCourse(Course course) {
        String sql = "INSERT INTO courses (name, code, credits, department, department_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getCode());
            pstmt.setInt(3, course.getCredits());
            pstmt.setString(4, course.getDepartment());
            if (course.getDepartmentId() > 0) {
                pstmt.setInt(5, course.getDepartmentId());
            } else {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, d.name as dept_name FROM courses c " +
                "LEFT JOIN departments d ON c.department_id = d.id " +
                "ORDER BY c.code";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                courses.add(extractCourseFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return courses;
    }

    public Course getCourseById(int id) {
        String sql = "SELECT c.*, d.name as dept_name FROM courses c " +
                "LEFT JOIN departments d ON c.department_id = d.id " +
                "WHERE c.id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractCourseFromResultSet(rs);
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return null;
    }

    private Course extractCourseFromResultSet(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getInt("id"));
        course.setName(rs.getString("name"));
        course.setCode(rs.getString("code"));
        course.setCredits(rs.getInt("credits"));
        course.setDepartment(rs.getString("department"));
        course.setSemester(rs.getInt("semester"));
        course.setDepartmentId(rs.getInt("department_id"));
        course.setDepartmentName(rs.getString("dept_name"));
        return course;
    }

    public boolean updateCourse(Course course) {
        String sql = "UPDATE courses SET name=?, code=?, credits=?, department=?, semester=?, department_id=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getCode());
            pstmt.setInt(3, course.getCredits());
            pstmt.setString(4, course.getDepartment());
            pstmt.setInt(5, course.getSemester());
            pstmt.setInt(6, course.getDepartmentId());
            pstmt.setInt(7, course.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public List<Course> getCoursesByDepartment(int departmentId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, d.name as dept_name FROM courses c " +
                "LEFT JOIN departments d ON c.department_id = d.id " +
                "WHERE c.department_id = ? ORDER BY c.code";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courses.add(extractCourseFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return courses;
    }

    /**
     * Delete a course by ID
     */
    /**
     * Delete a course by ID
     */
    public boolean deleteCourse(int courseId) {
        String deleteAssignmentsSql = "DELETE FROM assignments WHERE course_id = ?";
        String deleteCourseSql = "DELETE FROM courses WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Delete assignments linked to this course
            try (PreparedStatement pstmt1 = conn.prepareStatement(deleteAssignmentsSql)) {
                pstmt1.setInt(1, courseId);
                pstmt1.executeUpdate();
            }

            // 2. Delete the course itself
            int rowsAffected = 0;
            try (PreparedStatement pstmt2 = conn.prepareStatement(deleteCourseSql)) {
                pstmt2.setInt(1, courseId);
                rowsAffected = pstmt2.executeUpdate();
            }

            conn.commit(); // Commit transaction
            return rowsAffected > 0;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.error("Could not rollback", ex);
                }
            }
            Logger.error("Database operation failed", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.error("Database operation failed", e);
                }
            }
        }
    }
}
