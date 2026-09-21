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
    /**
     * Add or update a grade
     * 
     * @param grade Grade object
     * @return true if successful
     */
    public boolean saveGrade(Grade grade) {
        // Removed 'semester' from queries as it is not in the grades table schema
        String sql = "INSERT INTO grades (student_id, course_id, exam_type, marks_obtained, grade, max_marks) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (student_id, course_id, exam_type) DO UPDATE SET " +
                "marks_obtained = EXCLUDED.marks_obtained, " +
                "grade = EXCLUDED.grade, " +
                "max_marks = EXCLUDED.max_marks";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, grade.getStudentId());
            pstmt.setInt(2, grade.getCourseId());
            pstmt.setString(3, grade.getExamType());
            pstmt.setDouble(4, grade.getMarksObtained());
            pstmt.setString(5, grade.getGrade());
            double max = grade.getMaxMarks();
            if (max > 0)
                pstmt.setDouble(6, max);
            else
                pstmt.setNull(6, Types.DOUBLE);
            // Semester is inferred from Course/Student, not stored in grades

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
        // Changed g.semester to c.semester
        String sql = "SELECT g.id, g.student_id, g.course_id, g.exam_type, g.marks_obtained as marks, COALESCE(g.max_marks, 100) as max_marks, g.grade, c.semester as semester, "
                +
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
        // Changed g.semester to c.semester
        String sql = "SELECT g.id, g.student_id, g.course_id, g.exam_type, g.marks_obtained as marks, COALESCE(g.max_marks, 100) as max_marks, g.grade, c.semester as semester, "
                +
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
        // Changed g.semester to c.semester
        String sql = "SELECT g.id, g.student_id, g.course_id, g.exam_type, g.marks_obtained as marks, COALESCE(g.max_marks, 100) as max_marks, g.grade, c.semester as semester, "
                +
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
        String sql = "SELECT AVG(marks_obtained) as avg_marks FROM grades WHERE student_id = ?"; // percentage ->
                                                                                                 // marks_obtained

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double avgPercentage = rs.getDouble("avg_marks");
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
        // Changed g.semester to c.semester
        String sql = "SELECT g.id, g.student_id, g.course_id, g.exam_type, g.marks_obtained as marks, COALESCE(g.max_marks, 100) as max_marks, g.grade, c.semester as semester, "
                +
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
        // Changed g.semester to c.semester
        String sql = "SELECT g.id, g.student_id, g.course_id, g.exam_type, g.marks_obtained as marks, COALESCE(g.max_marks, 100) as max_marks, g.grade, c.semester as semester, "
                +
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
     * Delete a grade row by id
     */
    public boolean deleteGrade(int gradeId) {
        String sql = "DELETE FROM grades WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gradeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Delete grade failed", e);
            return false;
        }
    }

    /**
     * Fetch a single grade (used to authorize edits/delete by course ownership).
     */
    public Grade getGradeById(int gradeId) {
        String sql = "SELECT g.id, g.student_id, g.course_id, g.exam_type, g.marks_obtained as marks, "
                + "COALESCE(g.max_marks, 100) as max_marks, g.grade, c.semester as semester, " +
                "c.name as course_name FROM grades g " +
                "JOIN courses c ON g.course_id = c.id " +
                "WHERE g.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gradeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return extractGradeFromResultSet(rs);
        } catch (SQLException e) {
            Logger.error("Fetch grade by id failed", e);
        }
        return null;
    }

    /**
     * True when the given faculty teaches this course.
     */
    public boolean isCourseInstructor(int facultyId, int courseId) {
        String sql = "SELECT 1 FROM courses WHERE id = ? AND faculty_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, facultyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.error("Instructor check failed", e);
            return false;
        }
    }

    /**
     * Distinct exam types that already have grades recorded for a course
     * (used to drive the assignment filter).
     */
    public List<String> getCourseExamTypes(int courseId) {
        List<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT exam_type FROM grades WHERE course_id = ? AND exam_type IS NOT NULL ORDER BY exam_type";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                types.add(rs.getString("exam_type"));
            }
        } catch (SQLException e) {
            Logger.error("Fetch course exam types failed", e);
        }
        return types;
    }

    /**
     * Row of the assignment grid for a single enrolled student.
     * marksObtained/grade are null when that student has no grade for the
     * course + exam type yet (ungraded).
     */
    public static class AssignmentRow {
        private int studentId;
        private String studentName;
        private String enrollmentNo;
        private Double marksObtained;
        private Double maxMarks;
        private String grade;

        public int getStudentId() {
            return studentId;
        }

        public void setStudentId(int studentId) {
            this.studentId = studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public String getEnrollmentNo() {
            return enrollmentNo;
        }

        public void setEnrollmentNo(String enrollmentNo) {
            this.enrollmentNo = enrollmentNo;
        }

        public Double getMarksObtained() {
            return marksObtained;
        }

        public void setMarksObtained(Double marksObtained) {
            this.marksObtained = marksObtained;
        }

        public Double getMaxMarks() {
            return maxMarks;
        }

        public void setMaxMarks(Double maxMarks) {
            this.maxMarks = maxMarks;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }
    }

    /**
     * Assignment matrix: every enrolled student for a course, joined with their
     * grade for the given exam type (if any). Supports the "preload + confirm"
     * bulk workflow and the pending-students analytics.
     */
    public List<AssignmentRow> getAssignmentMatrix(int courseId, String examType) {
        List<AssignmentRow> rows = new ArrayList<>();
        String sql = "SELECT DISTINCT s.id as student_id, s.name as student_name, u.username as enrollment_no, " +
                "g.marks_obtained as marks, g.max_marks as max_marks, g.grade " +
                "FROM students s " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "LEFT JOIN course_registrations cr ON s.id = cr.student_id AND cr.course_id = ? " +
                "LEFT JOIN student_courses sc ON s.id = sc.student_id AND sc.course_id = ? " +
                "LEFT JOIN grades g ON g.student_id = s.id AND g.course_id = ? AND g.exam_type = ? " +
                "WHERE (cr.status IN ('ENROLLED', 'REGISTERED') OR sc.status IN ('ENROLLED', 'REGISTERED')) " +
                "ORDER BY s.name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, courseId);
            pstmt.setInt(3, courseId);
            pstmt.setString(4, examType);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                AssignmentRow row = new AssignmentRow();
                row.setStudentId(rs.getInt("student_id"));
                row.setStudentName(rs.getString("student_name"));
                row.setEnrollmentNo(rs.getString("enrollment_no"));
                double marks = rs.getDouble("marks");
                if (rs.wasNull()) {
                    row.setMarksObtained(null);
                    row.setMaxMarks(null);
                    row.setGrade(null);
                } else {
                    row.setMarksObtained(marks);
                    double max = rs.getDouble("max_marks");
                    row.setMaxMarks(rs.wasNull() ? null : max);
                    row.setGrade(rs.getString("grade"));
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            Logger.error("Fetch assignment matrix failed", e);
        }
        return rows;
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
        try {
            grade.setMaxMarks(rs.getDouble("max_marks"));
        } catch (SQLException e) {
            // max_marks column may not exist in older schemas
        }
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
