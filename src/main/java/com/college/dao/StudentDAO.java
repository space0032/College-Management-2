package com.college.dao;

import com.college.models.Course;
import com.college.models.Student;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student entity
 * Handles all database operations for students
 */
public class StudentDAO {

    /**
     * Add a new student to the database
     * 
     * @param student Student object to add
     * @return generated student ID if successful, -1 otherwise
     */
    public int addStudent(Student student, int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return addStudent(conn, student, userId);
        } catch (SQLException e) {
            Logger.error("Failed to add student: " + student.getName(), e);
            return -1;
        }
    }

    public int addStudent(Connection conn, Student student, int userId) throws SQLException {
        String sql = "INSERT INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, "
                +
                "dob, gender, blood_group, category, nationality, father_name, mother_name, guardian_contact, previous_school, tenth_percentage, twelfth_percentage, extracurricular_activities, profile_photo_path, user_id, enrollment_id, specialization) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getEmail());
            pstmt.setString(3, student.getPhone());
            pstmt.setString(4, student.getCourse());
            pstmt.setString(5, student.getBatch());
            pstmt.setDate(6, enrollmentDateOrToday(student.getEnrollmentDate()));
            pstmt.setString(7, student.getAddress());
            pstmt.setString(8, student.getDepartment() != null ? student.getDepartment() : "General");
            pstmt.setInt(9, student.getSemester() > 0 ? student.getSemester() : 1);
            pstmt.setBoolean(10, student.isHostelite());

            // New fields
            pstmt.setDate(11, student.getDob() != null ? new java.sql.Date(student.getDob().getTime()) : null);
            pstmt.setString(12, student.getGender());
            pstmt.setString(13, student.getBloodGroup());
            pstmt.setString(14, student.getCategory());
            pstmt.setString(15, student.getNationality());
            pstmt.setString(16, student.getFatherName());
            pstmt.setString(17, student.getMotherName());
            pstmt.setString(18, student.getGuardianContact());
            pstmt.setString(19, student.getPreviousSchool());
            pstmt.setDouble(20, student.getTenthPercentage());
            pstmt.setDouble(21, student.getTwelfthPercentage());
            pstmt.setString(22, student.getExtracurricularActivities());
            pstmt.setString(23, student.getProfilePhotoPath());

            if (userId > 0) {
                pstmt.setInt(24, userId);
            } else {
                pstmt.setNull(24, Types.INTEGER);
            }
            // 25. Enrollment ID (from username field)
            pstmt.setString(25, student.getUsername());
            pstmt.setString(26, student.getSpecialization());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Update an existing student
     * 
     * @param student Student object with updated data
     * @return true if successful, false otherwise
     */
    public boolean updateStudent(Student student) {
        String sql = "UPDATE students SET name=?, email=?, phone=?, course=?, batch=?, " +
                "enrollment_date=?, address=?, department=?, semester=?, is_hostelite=?, " +
                "dob=?, gender=?, blood_group=?, category=?, nationality=?, father_name=?, mother_name=?, guardian_contact=?, previous_school=?, tenth_percentage=?, twelfth_percentage=?, extracurricular_activities=?, profile_photo_path=?, specialization=? "
                +
                "WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getEmail());
            pstmt.setString(3, student.getPhone());
            pstmt.setString(4, student.getCourse());
            pstmt.setString(5, student.getBatch());
            pstmt.setDate(6, student.getEnrollmentDate() == null
                    ? null
                    : new java.sql.Date(student.getEnrollmentDate().getTime()));
            pstmt.setString(7, student.getAddress());
            pstmt.setString(8, student.getDepartment() != null ? student.getDepartment() : "General");
            pstmt.setInt(9, student.getSemester() > 0 ? student.getSemester() : 1);
            pstmt.setBoolean(10, student.isHostelite());
            pstmt.setDate(11, student.getDob() != null ? new java.sql.Date(student.getDob().getTime()) : null); // null
                                                                                                                // if
                                                                                                                // missing
            pstmt.setString(12, student.getGender());
            pstmt.setString(13, student.getBloodGroup());
            pstmt.setString(14, student.getCategory());
            pstmt.setString(15, student.getNationality());
            pstmt.setString(16, student.getFatherName());
            pstmt.setString(17, student.getMotherName());
            pstmt.setString(18, student.getGuardianContact());
            pstmt.setString(19, student.getPreviousSchool());
            pstmt.setDouble(20, student.getTenthPercentage());
            pstmt.setDouble(21, student.getTwelfthPercentage());
            pstmt.setString(22, student.getExtracurricularActivities());
            pstmt.setString(23, student.getProfilePhotoPath());
            pstmt.setString(24, student.getSpecialization());

            pstmt.setInt(25, student.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            Logger.error("Failed to update student: " + student.getName(), e);
            return false;
        }
    }

    /**
     * Update an existing student, throwing a RuntimeException with the
     * underlying SQL message on failure so callers (e.g. the API) can surface a
     * useful error to the user (e.g. duplicate email).
     */
    public boolean updateStudentChecked(Student student) {
        try {
            String sql = "UPDATE students SET name=?, email=?, phone=?, course=?, batch=?, " +
                    "enrollment_date=?, address=?, department=?, semester=?, is_hostelite=?, " +
                    "dob=?, gender=?, blood_group=?, category=?, nationality=?, father_name=?, mother_name=?, guardian_contact=?, previous_school=?, tenth_percentage=?, twelfth_percentage=?, extracurricular_activities=?, profile_photo_path=?, specialization=? "
                    +
                    "WHERE id=?";

            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, student.getName());
                pstmt.setString(2, student.getEmail());
                pstmt.setString(3, student.getPhone());
                pstmt.setString(4, student.getCourse());
                pstmt.setString(5, student.getBatch());
                pstmt.setDate(6, student.getEnrollmentDate() == null
                        ? null
                        : new java.sql.Date(student.getEnrollmentDate().getTime()));
                pstmt.setString(7, student.getAddress());
                pstmt.setString(8, student.getDepartment() != null ? student.getDepartment() : "General");
                pstmt.setInt(9, student.getSemester() > 0 ? student.getSemester() : 1);
                pstmt.setBoolean(10, student.isHostelite());
                pstmt.setDate(11, student.getDob() != null ? new java.sql.Date(student.getDob().getTime()) : null);
                pstmt.setString(12, student.getGender());
                pstmt.setString(13, student.getBloodGroup());
                pstmt.setString(14, student.getCategory());
                pstmt.setString(15, student.getNationality());
                pstmt.setString(16, student.getFatherName());
                pstmt.setString(17, student.getMotherName());
                pstmt.setString(18, student.getGuardianContact());
                pstmt.setString(19, student.getPreviousSchool());
                pstmt.setDouble(20, student.getTenthPercentage());
                pstmt.setDouble(21, student.getTwelfthPercentage());
                pstmt.setString(22, student.getExtracurricularActivities());
                pstmt.setString(23, student.getProfilePhotoPath());
                pstmt.setString(24, student.getSpecialization());
                pstmt.setInt(25, student.getId());

                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            Logger.error("Failed to update student: " + student.getName(), e);
            throw new RuntimeException(friendlyUpdateMessage(e), e);
        }
    }

    private String friendlyUpdateMessage(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        String cause = e.getCause() != null && e.getCause().getMessage() != null
                ? e.getCause().getMessage().toLowerCase()
                : "";
        String combined = msg + " " + cause;
        if (combined.contains("students_email_key") || (combined.contains("duplicate key") && combined.contains("email"))) {
            return "Email is already registered.";
        }
        return "Failed to update student: " + (e.getMessage() == null ? "unknown error" : e.getMessage());
    }

    static java.sql.Date enrollmentDateOrToday(java.util.Date enrollmentDate) {
        long timestamp = enrollmentDate == null ? System.currentTimeMillis() : enrollmentDate.getTime();
        return new java.sql.Date(timestamp);
    }

    /**
     * Delete a student by ID and their associated user account
     *
     * @param studentId ID of the student to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteStudent(int studentId) {
        String deleteUserSql = "DELETE FROM users WHERE id = (SELECT user_id FROM students WHERE id = ?)";
        String deleteSql = "DELETE FROM students WHERE id=?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(deleteUserSql)) {
                ps1.setInt(1, studentId);
                ps1.executeUpdate();
            }

            int rowsAffected;
            try (PreparedStatement ps2 = conn.prepareStatement(deleteSql)) {
                ps2.setInt(1, studentId);
                rowsAffected = ps2.executeUpdate();
            }

            conn.commit();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { Logger.error("Rollback failed", ex); }
            }
            Logger.error("Database operation failed", e);
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { Logger.error("Connection close failed", e); }
            }
        }
    }

    /**
     * Get a student by ID
     * 
     * @param studentId ID of the student
     * @return Student object or null if not found
     */
    public Student getStudentById(int studentId) {
        String sql = "SELECT s.*, u.username FROM students s LEFT JOIN users u ON s.user_id = u.id WHERE s.id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return null;
    }

    /**
     * Get all students from the database
     * 
     * @return List of all students
     */
    public List<Student> getAllStudents() {
        return getAllStudentsPaginated(1, Integer.MAX_VALUE);
    }

    public List<Student> getAllStudentsPaginated(int page, int size) {
        List<Student> students = new ArrayList<>();
        int offset = (page - 1) * size;
        String sql = "SELECT s.*, u.username FROM students s LEFT JOIN users u ON s.user_id = u.id ORDER BY s.name LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, size);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(extractStudentFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return students;
    }

    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return 0;
    }

    /**
     * Get only students who are in hostel (is_hostelite = true)
     */
    public List<Student> getHostelStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, u.username FROM students s LEFT JOIN users u ON s.user_id = u.id WHERE s.is_hostelite = true ORDER BY s.name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return students;
    }

    /**
     * Search students by name or email
     * 
     * @param keyword Search keyword
     * @return List of matching students
     */
    public List<Student> searchStudents(String keyword) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, u.username FROM students s LEFT JOIN users u ON s.user_id = u.id WHERE s.name ILIKE ? OR s.email ILIKE ? OR u.username ILIKE ? ORDER BY s.name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return students;
    }

    /**
     * Search hostel students only
     */
    public List<Student> searchHostelStudents(String keyword) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, u.username FROM students s LEFT JOIN users u ON s.user_id = u.id WHERE s.is_hostelite = true AND (s.name ILIKE ? OR s.email ILIKE ? OR u.username ILIKE ?) ORDER BY s.name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return students;
    }

    /**
     * Helper method to extract Student object from ResultSet
     * 
     * @param rs ResultSet from query
     * @return Student object
     */
    private Student extractStudentFromResultSet(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("id"));
        student.setName(rs.getString("name"));
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));
        student.setCourse(rs.getString("course"));
        student.setBatch(rs.getString("batch"));
        student.setEnrollmentDate(rs.getDate("enrollment_date"));
        student.setAddress(rs.getString("address"));

        // Extended Profile Fields
        try {
            student.setDob(rs.getDate("dob"));
            student.setGender(rs.getString("gender"));
            student.setBloodGroup(rs.getString("blood_group"));
            student.setCategory(rs.getString("category"));
            student.setNationality(rs.getString("nationality"));
            student.setFatherName(rs.getString("father_name"));
            student.setMotherName(rs.getString("mother_name"));
            student.setGuardianContact(rs.getString("guardian_contact"));
            student.setPreviousSchool(rs.getString("previous_school"));
            student.setTenthPercentage(rs.getDouble("tenth_percentage"));
            student.setTwelfthPercentage(rs.getDouble("twelfth_percentage"));
            student.setExtracurricularActivities(rs.getString("extracurricular_activities"));
            student.setProfilePhotoPath(rs.getString("profile_photo_path"));
        } catch (SQLException e) {
            // Field might not exist in old queries or tables
        }

        // Handle new fields with defaults
        try {
            student.setDepartment(rs.getString("department"));
            student.setSemester(rs.getInt("semester"));
            student.setHostelite(rs.getBoolean("is_hostelite"));
        } catch (SQLException e) {
            // Fields might not exist in older schemas
            student.setDepartment("General");
            student.setSemester(1);
            student.setHostelite(false);
        }

        try {
            student.setUsername(rs.getString("username"));
        } catch (SQLException e) {
            // Ignore if username not present in result set
        }

        try {
            student.setSpecialization(rs.getString("specialization"));
        } catch (SQLException e) {
            // Column added by V62; ignore on older schemas
        }

        try {
            student.setEnrollmentId(rs.getString("enrollment_id"));
        } catch (SQLException e) {
            // Fallback to username if enrollment_id column not present
            student.setEnrollmentId(student.getUsername());
        }

        return student;
    }

    /**
     * Get student by user ID
     */
    public Student getStudentByUserId(int userId) {
        String sql = "SELECT s.*, u.username FROM students s LEFT JOIN users u ON s.user_id = u.id WHERE s.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return null;
    }

    /**
     * Resolve a student entity id (students.id) from an enrollment number.
     * Returns -1 if no student matches the enrollment number.
     */
    public int getStudentIdByEnrollment(String enrollmentId) {
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            return -1;
        }
        String sql = "SELECT id FROM students WHERE enrollment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, enrollmentId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return -1;
    }

    /**
     * Resolve a user id (users.id) from an enrollment number.
     * Returns -1 if no matching student/user is found.
     */
    public int getUserIdByEnrollment(String enrollmentId) {
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            return -1;
        }
        String sql = "SELECT user_id FROM students WHERE enrollment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, enrollmentId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return -1;
    }

    /**
     * Get count of students by department code and year for enrollment generation
     */
    public int getCountByDepartmentAndYear(String deptCode, int year) {        String sql = "SELECT COUNT(*) as count FROM students s " +
                "JOIN users u ON s.user_id = u.id " +
                "WHERE u.username LIKE ? AND EXTRACT(YEAR FROM s.enrollment_date) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, deptCode + year + "%");
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return 0;
    }

    /**
     * Get list of subjects registered by the student.
     * Single source of truth is course_registrations; student_courses is
     * included for backward compatibility with legacy rows.
     */
    public List<Course> getRegisteredCourses(int studentId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT DISTINCT c.id, c.name, c.code, c.credits, c.department_id, c.department, c.semester, "
                + "c.course_type, c.specialization FROM courses c "
                + "LEFT JOIN course_registrations cr ON c.id = cr.course_id AND cr.student_id = ? "
                + "AND (cr.status = 'ENROLLED' OR cr.status = 'REGISTERED' OR cr.status = 'APPROVED') "
                + "LEFT JOIN student_courses sc ON c.id = sc.course_id AND sc.student_id = ? "
                + "WHERE (cr.student_id IS NOT NULL OR sc.student_id IS NOT NULL) ORDER BY c.code";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Course c = new Course();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setCode(rs.getString("code"));
                c.setCredits(rs.getInt("credits"));
                try {
                    c.setDepartmentId(rs.getInt("department_id"));
                } catch (SQLException ignored) {
                }
                try {
                    c.setDepartment(rs.getString("department"));
                } catch (SQLException ignored) {
                }
                try {
                    c.setSemester(rs.getInt("semester"));
                } catch (SQLException ignored) {
                }
                try {
                    c.setCourseType(rs.getString("course_type"));
                } catch (SQLException ignored) {
                }
                try {
                    c.setSpecialization(rs.getString("specialization"));
                } catch (SQLException ignored) {
                }
                courses.add(c);
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return courses;
    }

    /**
     * Register student for a subject. Writes to course_registrations (source of
     * truth) and mirrors to legacy student_courses on a best-effort basis.
     */
    public boolean registerCourse(int studentId, int courseId, int semester, int year) {
        boolean primary = false;
        String sql = "INSERT INTO course_registrations (student_id, course_id, registration_date, status) "
                + "VALUES (?, ?, CURRENT_DATE, 'ENROLLED') "
                + "ON CONFLICT (student_id, course_id) DO UPDATE SET status = 'ENROLLED'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            primary = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Fall back to plain insert for DBs without the unique constraint
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement retry = conn.prepareStatement(
                            "INSERT INTO course_registrations (student_id, course_id, registration_date, status) VALUES (?, ?, CURRENT_DATE, 'ENROLLED')")) {
                retry.setInt(1, studentId);
                retry.setInt(2, courseId);
                primary = retry.executeUpdate() > 0;
            } catch (SQLException ex) {
                Logger.error("Failed to register subject " + courseId + " for student " + studentId, ex);
            }
        }
        // Legacy mirror (ignore failures - table may not exist on some schemas)
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO student_courses (student_id, course_id, semester, academic_year, status) VALUES (?, ?, ?, ?, 'ENROLLED')")) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            pstmt.setInt(3, semester);
            pstmt.setInt(4, year);
            pstmt.executeUpdate();
        } catch (SQLException ignored) {
        }
        return primary;
    }

    /**
     * Get count of students enrolled in the last 7 days
     */
    public int getWeeklyCount() {
        String sql = "SELECT COUNT(*) as count FROM students WHERE enrollment_date >= CURRENT_DATE - INTERVAL '7 days'";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return 0;
    }
}
