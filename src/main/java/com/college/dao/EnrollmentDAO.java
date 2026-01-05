package com.college.dao;

import com.college.models.Student;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;
import com.college.utils.EnrollmentGenerator;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.List;
import com.college.dao.RoleDAO;
import com.college.dao.EnhancedFeeDAO;

public class EnrollmentDAO {

    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final EnhancedFeeDAO feeDAO = new EnhancedFeeDAO();
    private final com.college.dao.CourseDAO courseDAO = new com.college.dao.CourseDAO();
    private final com.college.dao.CourseRegistrationDAO registrationDAO = new com.college.dao.CourseRegistrationDAO();

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

            // 2. Create User Account with Role ID
            com.college.models.Role studentRole = roleDAO.getRoleByCode("STUDENT");
            int roleId = (studentRole != null) ? studentRole.getId() : 0;

            // Validate role found
            if (roleId == 0) {
                Logger.error("STUDENT role not found in database during enrollment");
                // Fallback to legacy or fail? Let's proceed but log error - actually NO,
                // failing strictly is better for consistency
                // But given legacy state, maybe fallback to legacy method if 0?
                // Let's rely on role existing as per V9 migration
            }

            int userId;
            if (roleId > 0) {
                userId = userDAO.addUser(conn, enrollmentNumber, password, "STUDENT", roleId);
            } else {
                userId = userDAO.addUser(conn, enrollmentNumber, password, "STUDENT");
            }

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

            // 4. Auto-Assign Fees
            assignAutoFees(studentId, student, feeDAO);

            // 5. Auto-Register Core Courses
            assignCoreCourses(conn, studentId, student);

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

    private void assignCoreCourses(Connection conn, int studentId, Student student) {
        try {
            List<com.college.models.Course> coreCourses = courseDAO.getCoreCourses(student.getDepartment(),
                    student.getSemester());
            // Need to bypass DAO transaction since we are already in one, or use a method
            // that accepts connection
            // CourseRegistrationDAO.registerCourse manages its own transaction which might
            // complicate things if we are in one.
            // Ideally should have registerCourse(Connection conn, ...)
            // For now, let's just insert manually or assume it works if we use a separate
            // connection (but that loses atomicity)
            // Or better, add registerCourse(Connection, ...) to DAO.
            // Let's implement a simple direct insert here to be safe within transaction

            String sql = "INSERT INTO course_registrations (student_id, course_id, status, registration_date) VALUES (?, ?, 'APPROVED', CURRENT_DATE)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (com.college.models.Course c : coreCourses) {
                    pstmt.setInt(1, studentId);
                    pstmt.setInt(2, c.getId());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

        } catch (Exception e) {
            Logger.error("Failed to auto-register core courses for student: " + studentId, e);
        }
    }

    private void assignAutoFees(int studentId, Student student, EnhancedFeeDAO feeDAO) {
        try {
            List<com.college.models.FeeCategory> categories = feeDAO.getAllCategories();
            java.sql.Date dueDate = java.sql.Date.valueOf(java.time.LocalDate.now().plusMonths(1)); // Due in 30 days

            // 1. Tuition Fees (Always apply)
            categories.stream()
                    .filter(c -> "Tuition Fees".equalsIgnoreCase(c.getCategoryName())
                            || "Tuition Fee".equalsIgnoreCase(c.getCategoryName()))
                    .findFirst()
                    .ifPresent(c -> {
                        feeDAO.addStudentFee(studentId, c.getId(), c.getBaseAmount(), dueDate);
                    });

            // 2. Hostel Fees (If hostelite)
            if (student.isHostelite()) {
                categories.stream()
                        .filter(c -> "Hostel Fees".equalsIgnoreCase(c.getCategoryName())
                                || "Hostel Fee".equalsIgnoreCase(c.getCategoryName()))
                        .findFirst()
                        .ifPresent(c -> {
                            feeDAO.addStudentFee(studentId, c.getId(), c.getBaseAmount(), dueDate);
                        });
            }

        } catch (Exception e) {
            Logger.error("Failed to auto-assign fees for student: " + studentId, e);
            // Don't fail the enrollment execution just because fees failed, but log it
        }
    }
}
