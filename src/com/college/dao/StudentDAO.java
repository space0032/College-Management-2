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
    public int addStudent(Student student) {
        String sql = "INSERT INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                "enrollment_date=?, address=?, department=?, semester=? WHERE id=?";

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
            pstmt.setInt(10, student.getId());

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
        String sql = "SELECT * FROM students ORDER BY name";

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
        String sql = "SELECT * FROM students WHERE name LIKE ? OR email LIKE ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

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
        } catch (SQLException e) {
            // Fields might not exist in older schemas
            student.setDepartment("General");
            student.setSemester(1);
        }

        return student;
    }
}
