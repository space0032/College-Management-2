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
        String sql = "INSERT INTO courses (name, code, credits, department, department_id, semester, course_type, capacity, faculty_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            pstmt.setInt(6, course.getSemester());
            pstmt.setString(7, course.getCourseType());
            pstmt.setInt(8, course.getCapacity());
            if (course.getFacultyId() > 0) {
                pstmt.setInt(9, course.getFacultyId());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, d.name as dept_name, f.name as faculty_name FROM courses c " +
                "LEFT JOIN departments d ON c.department_id = d.id " +
                "LEFT JOIN faculty f ON c.faculty_id = f.id " +
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
        String sql = "SELECT c.*, d.name as dept_name, f.name as faculty_name FROM courses c " +
                "LEFT JOIN departments d ON c.department_id = d.id " +
                "LEFT JOIN faculty f ON c.faculty_id = f.id " +
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
        // Faculty
        int fid = rs.getInt("faculty_id");
        if (fid > 0) {
            course.setFacultyId(fid);
            try {
                course.setFacultyName(rs.getString("faculty_name"));
            } catch (SQLException e) {
                // Column might not exist in all queries
            }
        }

        // Handle new fields gracefully if they don't exist (though migration should
        // ensure they do)
        try {
            course.setCourseType(rs.getString("course_type"));
            course.setCapacity(rs.getInt("capacity"));
            course.setEnrolledCount(rs.getInt("enrolled_count"));
        } catch (SQLException e) {
            // Might happen if column doesn't exist yet, default to safe values
            course.setCourseType("CORE");
        }
        return course;
    }

    public boolean updateCourse(Course course) {
        String sql = "UPDATE courses SET name=?, code=?, credits=?, department=?, semester=?, department_id=?, course_type=?, capacity=?, faculty_id=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getCode());
            pstmt.setInt(3, course.getCredits());
            pstmt.setString(4, course.getDepartment());
            pstmt.setInt(5, course.getSemester());
            if (course.getDepartmentId() > 0) {
                pstmt.setInt(6, course.getDepartmentId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            pstmt.setString(7, course.getCourseType());
            pstmt.setInt(8, course.getCapacity());
            if (course.getFacultyId() > 0) {
                pstmt.setInt(9, course.getFacultyId());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }
            pstmt.setInt(10, course.getId());

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

    // Faculty Assignment
    public boolean assignFaculty(int courseId, int facultyId) {
        String sql = "UPDATE courses SET faculty_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (facultyId > 0) {
                pstmt.setInt(1, facultyId);
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setInt(2, courseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Assign faculty failed", e);
            return false;
        }
    }

    // Workload
    public static class WorkloadStats {
        public int count;
        public int credits;
    }

    public WorkloadStats getFacultyWorkload(int facultyId) {
        WorkloadStats stats = new WorkloadStats();
        String sql = "SELECT COUNT(*) as cnt, SUM(credits) as total_credits FROM courses WHERE faculty_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, facultyId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.count = rs.getInt("cnt");
                stats.credits = rs.getInt("total_credits");
            }
        } catch (SQLException e) {
            Logger.error("Workload fetch failed", e);
        }
        return stats;
    }

    public List<Course> getCoursesByFaculty(int facultyId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, d.name as dept_name, f.name as faculty_name FROM courses c " +
                "LEFT JOIN departments d ON c.department_id = d.id " +
                "LEFT JOIN faculty f ON c.faculty_id = f.id " +
                "WHERE c.faculty_id = ? ORDER BY c.code";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facultyId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courses.add(extractCourseFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Fetch faculty courses failed", e);
        }
        return courses;
    }
}
