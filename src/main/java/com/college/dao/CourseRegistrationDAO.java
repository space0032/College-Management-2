package com.college.dao;

import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseRegistrationDAO {

    public static class RegistrationRequest {
        private int id;
        private int studentId;
        private String studentName;
        private int courseId;
        private String courseName;
        private String courseCode;
        private String status;
        private Date date;

        // Getters
        public int getId() {
            return id;
        }

        public int getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public int getCourseId() {
            return courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getStatus() {
            return status;
        }

        public Date getDate() {
            return date;
        }

        // Setters for DAO population
        public void setId(int id) {
            this.id = id;
        }

        public void setStudentId(int sid) {
            this.studentId = sid;
        }

        public void setStudentName(String n) {
            this.studentName = n;
        }

        public void setCourseId(int cid) {
            this.courseId = cid;
        }

        public void setCourseName(String n) {
            this.courseName = n;
        }

        public void setCourseCode(String c) {
            this.courseCode = c;
        }

        public void setStatus(String s) {
            this.status = s;
        }

        public void setDate(Date d) {
            this.date = d;
        }
    }

    /**
     * Register a student for a course.
     * Core: Auto-registered (handled separately or implies simpler flow).
     * Elective: Request Status -> 'PENDING'.
     */
    public String registerCourse(int studentId, int courseId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Check if already registered or pending
            String dupSql = "SELECT id, status FROM course_registrations WHERE student_id = ? AND course_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(dupSql)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, courseId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("REGISTERED".equals(status) || "ENROLLED".equals(status))
                        return "Already registered.";
                    if ("PENDING".equals(status))
                        return "Request already pending.";
                    if ("REJECTED".equals(status)) {
                        // Allow re-request? For now, YES.
                    }
                }
            }

            // 2. Insert as PENDING
            String insertSql = "INSERT INTO course_registrations (student_id, course_id, registration_date, status) VALUES (?, ?, CURRENT_DATE, 'PENDING')";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, courseId);
                pstmt.executeUpdate();
            }

            conn.commit();
            return "SUCCESS";

        } catch (SQLException e) {
            Logger.error("Registration failed", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.error("Rollback failed", ex);
                }
            }
            return "Database Error: " + e.getMessage();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.error("Connection close failed", e);
                }
            }
        }
    }

    /**
     * Approve a registration request.
     */
    public boolean approveRequest(int requestId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Get course ID to update count
            int courseId = -1;
            String getSql = "SELECT course_id FROM course_registrations WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(getSql)) {
                pstmt.setInt(1, requestId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next())
                    courseId = rs.getInt("course_id");
            }

            if (courseId == -1)
                return false;

            // Update status
            String upSql = "UPDATE course_registrations SET status = 'ENROLLED' WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(upSql)) {
                pstmt.setInt(1, requestId);
                pstmt.executeUpdate();
            }

            // Update capacity
            String capSql = "UPDATE courses SET enrolled_count = enrolled_count + 1 WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(capSql)) {
                pstmt.setInt(1, courseId);
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            Logger.error("Approve failed", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.error("Rollback failed", ex);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.error("Connection close failed", e);
                }
            }
        }
    }

    /**
     * Reject a registration request.
     */
    public boolean rejectRequest(int requestId) {
        String sql = "UPDATE course_registrations SET status = 'REJECTED' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Reject failed", e);
            return false;
        }
    }

    /**
     * Get all pending requests for Faculty/Admin.
     */
    public List<RegistrationRequest> getPendingRequests() {
        List<RegistrationRequest> list = new ArrayList<>();
        String sql = "SELECT cr.id, cr.student_id, cr.course_id, cr.registration_date, cr.status, " +
                "s.name as student_name, c.name as course_name, c.code as course_code " +
                "FROM course_registrations cr " +
                "JOIN students s ON cr.student_id = s.id " +
                "JOIN courses c ON cr.course_id = c.id " +
                "WHERE cr.status = 'PENDING' ORDER BY cr.registration_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                RegistrationRequest r = new RegistrationRequest();
                r.setId(rs.getInt("id"));
                r.setStudentId(rs.getInt("student_id"));
                r.setStudentName(rs.getString("student_name"));
                r.setCourseId(rs.getInt("course_id"));
                r.setCourseName(rs.getString("course_name"));
                r.setCourseCode(rs.getString("course_code"));
                r.setStatus(rs.getString("status"));
                r.setDate(rs.getDate("registration_date"));
                list.add(r);
            }
        } catch (SQLException e) {
            Logger.error("Fetch pending failed", e);
        }
        return list;
    }

    /**
     * Drop a course (or withdraw request).
     */
    public boolean dropCourse(int studentId, int courseId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String status = null;
            String checkSql = "SELECT status FROM course_registrations WHERE student_id = ? AND course_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, studentId);
                ps.setInt(2, courseId);
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                    status = rs.getString("status");
            }

            if (status == null)
                return false;

            // Delete record
            String deleteSql = "DELETE FROM course_registrations WHERE student_id = ? AND course_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, courseId);
                pstmt.executeUpdate();
            }

            // If was ENROLLED, decrement count
            if ("ENROLLED".equalsIgnoreCase(status) || "REGISTERED".equalsIgnoreCase(status)) {
                String updateSql = "UPDATE courses SET enrolled_count = enrolled_count - 1 WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setInt(1, courseId);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            Logger.error("Drop failed", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.error("Rollback failed", ex);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.error("Connection close failed", e);
                }
            }
        }
    }

    public List<Integer> getRegisteredCourseIds(int studentId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT course_id FROM course_registrations WHERE student_id = ? AND (status = 'REGISTERED' OR status = 'ENROLLED')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("course_id"));
            }
        } catch (SQLException e) {
            Logger.error("Fetch registrations failed", e);
        }
        return ids;
    }

    public List<Integer> getPendingCourseIds(int studentId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT course_id FROM course_registrations WHERE student_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("course_id"));
            }
        } catch (SQLException e) {
            Logger.error("Fetch pending failed", e);
        }
        return ids;
    }

    /**
     * Get all students enrolled in a specific course.
     */
    public List<com.college.models.Student> getEnrolledStudents(int courseId) {
        List<com.college.models.Student> students = new ArrayList<>();
        String sql = "SELECT s.* FROM students s " +
                "JOIN course_registrations cr ON s.id = cr.student_id " +
                "WHERE cr.course_id = ? AND (cr.status = 'ENROLLED' OR cr.status = 'REGISTERED') " +
                "ORDER BY s.name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Reuse extraction logic if possible, or just fetch essential
                // For safety and completeness, let's use StudentDAO's extraction if visible or
                // just map manually
                // Since extractStudentFromResultSet is likely private, we'll rely on
                // StudentDAO.getStudentById
                // but that's N+1. Better to map essential fields here or make extraction
                // public.
                // Checking StudentDAO... for now, let's just map ID and Name as that's what we
                // need for grading.
                // Wait, StudentDAO is in same package. check visibility.

                // Better approach: use StudentDAO.extractStudentFromResultSet if
                // package-private.
                // Assuming it's private. Let's map manually what we need.
                com.college.models.Student s = new com.college.models.Student();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                // s.setRollNumber(rs.getString("roll_number"));
                students.add(s);
            }

        } catch (SQLException e) {
            Logger.error("Fetch enrolled students failed", e);
        }
        return students;
    }
}
