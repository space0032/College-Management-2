package com.college.dao;

import com.college.models.Student;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;
import com.college.utils.EnrollmentGenerator;
import java.sql.Connection;
import java.sql.SQLException;

public class EnrollmentDAO {

    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    /**
     * Enrolls a new student with transaction safety.
     * 1. Generates Enrollment Number.
     * 2. Creates User Account.
     * 3. Creates Student Record.
     * 
     * @param student  The student object containing personal details.
     * @param password The password for the student account.
     * @return The created Student object with ID and Username populated, or null on
     *         failure.
     */
    public Student enrollStudent(Student student, String password) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Generate Enrollment Number
            String enrollmentNumber = EnrollmentGenerator.generateStudentEnrollment(student.getDepartment());
            student.setUsername(enrollmentNumber);

            // 2. Create User Account
            int userId = userDAO.addUser(conn, enrollmentNumber, password, "STUDENT");
            if (userId == -1) {
                throw new SQLException("Failed to create user account.");
            }
            student.setUserId(userId);

            // 3. Create Student Record
            int studentId = studentDAO.addStudent(conn, student, userId);
            if (studentId == -1) {
                throw new SQLException("Failed to create student record.");
            }
            student.setId(studentId);

            conn.commit(); // Commit Transaction
            return student;

        } catch (SQLException e) {
            Logger.error("Failed to enroll student", e);
            // Try to rollback, but don't fail if connection is already closed
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception rollbackEx) {
                // Log but don't throw - connection might already be closed
                Logger.error("Could not rollback transaction", rollbackEx);
            }
            return null;
        } finally {
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.setAutoCommit(true); // Reset auto-commit
                    }
                    conn.close();
                } catch (SQLException e) {
                    Logger.error("Failed to close connection", e);
                }
            }
        }
    }
}
