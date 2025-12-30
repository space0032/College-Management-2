package com.college.dao;

import com.college.models.Course;
import com.college.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Course entity
 */
public class CourseDAO {

    public boolean addCourse(Course course) {
        String sql = "INSERT INTO courses (name, code, credits, department, semester, department_id) VALUES (?, ?, ?, ?, ?, ?)?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getCode());
            pstmt.setInt(3, course.getCredits());
            pstmt.setString(4, course.getDepartment());
            pstmt.setInt(5, course.getSemester());
            pstmt.setInt(6, course.getDepartmentId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return courses;
    }
}
