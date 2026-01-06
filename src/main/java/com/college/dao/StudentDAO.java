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
                "dob, gender, blood_group, category, nationality, father_name, mother_name, guardian_contact, previous_school, tenth_percentage, twelfth_percentage, extracurricular_activities, profile_photo_path, user_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getEmail());
            pstmt.setString(3, student.getPhone());
            pstmt.setString(4, student.getCourse());
            pstmt.setString(5, student.getBatch());
            pstmt.setDate(6, new java.sql.Date(student.getEnrollmentDate().getTime()));
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
                "dob=?, gender=?, blood_group=?, category=?, nationality=?, father_name=?, mother_name=?, guardian_contact=?, previous_school=?, tenth_percentage=?, twelfth_percentage=?, extracurricular_activities=?, profile_photo_path=? "
                +
                "WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getEmail());
            pstmt.setString(3, student.getPhone());
            pstmt.setString(4, student.getCourse());
            pstmt.setString(5, student.getBatch());
            pstmt.setDate(6, new java.sql.Date(student.getEnrollmentDate().getTime()));
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

            pstmt.setInt(24, student.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            Logger.error("Failed to update student: " + student.getName(), e);
            return false;
        }
    }

    /**
     * Delete a student by ID
     * 
     * @param studentId ID of the student to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteStudent(int studentId) {
        String sql = "DELETE FROM students WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
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
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, u.username FROM students s LEFT JOIN users u ON s.user_id = u.id ORDER BY s.name";

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
     * Get count of students by department code and year for enrollment generation
     */
    public int getCountByDepartmentAndYear(String deptCode, int year) {
        String sql = "SELECT COUNT(*) as count FROM students s " +
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
     * Get list of courses registered by the student
     */
    public List<Course> getRegisteredCourses(int studentId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, sc.status as enrollment_status FROM courses c " +
                "JOIN student_courses sc ON c.id = sc.course_id " +
                "WHERE sc.student_id = ? ORDER BY c.code";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Course c = new Course();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setCode(rs.getString("code"));
                c.setCredits(rs.getInt("credits"));
                c.setDepartmentId(rs.getInt("department_id"));
                courses.add(c);
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return courses;
    }

    /**
     * Register student for a course
     */
    public boolean registerCourse(int studentId, int courseId, int semester, int year) {
        String sql = "INSERT INTO student_courses (student_id, course_id, semester, academic_year, status) VALUES (?, ?, ?, ?, 'ENROLLED')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            pstmt.setInt(3, semester);
            pstmt.setInt(4, year);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
