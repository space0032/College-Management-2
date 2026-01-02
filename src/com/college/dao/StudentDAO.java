package com.college.dao;

import com.college.models.Student;
import com.college.utils.DatabaseConnection;

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
        String sql = "INSERT INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
            if (userId > 0) {
                pstmt.setInt(11, userId);
            } else {
                pstmt.setNull(11, Types.INTEGER);
            }

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated student ID
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
                "enrollment_date=?, address=?, department=?, semester=?, is_hostelite=? WHERE id=?";

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
            pstmt.setInt(11, student.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
        String sql = "SELECT * FROM students WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
        String sql = "SELECT s.*, u.username FROM students s LEFT JOIN users u ON s.user_id = u.id WHERE s.name LIKE ? OR s.email LIKE ? OR u.username LIKE ? ORDER BY s.name";

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
            e.printStackTrace();
        }
        return students;
    }

    /**
     * Search hostel students only
     */
    public List<Student> searchHostelStudents(String keyword) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, u.username FROM students s LEFT JOIN users u ON s.user_id = u.id WHERE s.is_hostelite = true AND (s.name LIKE ? OR s.email LIKE ? OR u.username LIKE ?) ORDER BY s.name";

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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return null;
    }
}
