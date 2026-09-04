package com.college.dao;

import com.college.models.Student;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;
import com.college.utils.EnrollmentGenerator;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.List;

public class EnrollmentDAO {

    @FunctionalInterface
    interface ConnectionProvider { Connection get() throws SQLException; }
    @FunctionalInterface
    interface EnrollmentNumberProvider { String generate(String department); }

    private final UserDAO userDAO;
    private final StudentDAO studentDAO;
    private final RoleDAO roleDAO;
    private final EnhancedFeeDAO feeDAO;
    private final CourseDAO courseDAO;
    private final ConnectionProvider connectionProvider;
    private final EnrollmentNumberProvider enrollmentNumberProvider;

    public EnrollmentDAO() {
        this(new UserDAO(), new StudentDAO(), new RoleDAO(), new EnhancedFeeDAO(), new CourseDAO(),
                DatabaseConnection::getConnection, EnrollmentGenerator::generateStudentEnrollment);
    }

    EnrollmentDAO(UserDAO userDAO, StudentDAO studentDAO, RoleDAO roleDAO,
            EnhancedFeeDAO feeDAO, CourseDAO courseDAO, ConnectionProvider connectionProvider,
            EnrollmentNumberProvider enrollmentNumberProvider) {
        this.userDAO = userDAO;
        this.studentDAO = studentDAO;
        this.roleDAO = roleDAO;
        this.feeDAO = feeDAO;
        this.courseDAO = courseDAO;
        this.connectionProvider = connectionProvider;
        this.enrollmentNumberProvider = enrollmentNumberProvider;
    }

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
            conn = connectionProvider.get();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Generate Enrollment Number
            String enrollmentNumber = enrollmentNumberProvider.generate(student.getDepartment());
            student.setUsername(enrollmentNumber);

            // 2. Create User Account with Role ID
            com.college.models.Role studentRole = roleDAO.getRoleByCode(conn, "STUDENT");
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
            assignAutoFees(conn, studentId, student, feeDAO);

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
            throw new EnrollmentException(friendlyEnrollmentMessage(e), e);
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
            if (coreCourses.isEmpty()) {
                return;
            }
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

    private void assignAutoFees(Connection conn, int studentId, Student student, EnhancedFeeDAO feeDAO) {
        try {
            java.sql.Date dueDate = java.sql.Date.valueOf(java.time.LocalDate.now().plusMonths(1)); // Due in 30 days
            String academicYear = java.time.Year.now().toString();

            // 1. Prefer the customizable per-program breakdown when it exists.
            // Only applies to new enrollments, existing student_fees rows are untouched.
            List<com.college.models.ProgramFeeStructure> programFees = new java.util.ArrayList<>();
            if (student.getDepartment() != null && !student.getDepartment().trim().isEmpty()) {
                try {
                    programFees = feeDAO.getProgramFees(conn, student.getDepartment(), academicYear);
                } catch (Exception e) {
                    Logger.error("Failed to load program fees, using base amounts", e);
                }
            }
            if (programFees != null && !programFees.isEmpty()) {
                for (com.college.models.ProgramFeeStructure item : programFees) {
                    if (item == null || item.getCategoryId() <= 0 || item.getAmount() <= 0) {
                        continue;
                    }
                    // Bus fees are not part of department program fees.
                    if (item.getCategoryName() != null
                            && item.getCategoryName().toLowerCase().contains("bus")) {
                        continue;
                    }
                    // Skip hostel charges for day scholars.
                    if (!student.isHostelite() && item.getCategoryName() != null
                            && item.getCategoryName().toLowerCase().contains("hostel")) {
                        continue;
                    }
                    feeDAO.addStudentFee(conn, studentId, item.getCategoryId(), item.getAmount(), dueDate);
                }
                return;
            }

            // 2. Fallback to global base amounts when no program customization exists.
            List<com.college.models.FeeCategory> categories = feeDAO.getAllCategories();

            // Tuition Fees (Always apply)
            categories.stream()
                    .filter(c -> "Tuition Fees".equalsIgnoreCase(c.getCategoryName())
                            || "Tuition Fee".equalsIgnoreCase(c.getCategoryName()))
                    .findFirst()
                    .ifPresent(c -> {
                        feeDAO.addStudentFee(conn, studentId, c.getId(), c.getBaseAmount(), dueDate);
                    });

            // Hostel Fees (If hostelite)
            if (student.isHostelite()) {
                categories.stream()
                        .filter(c -> "Hostel Fees".equalsIgnoreCase(c.getCategoryName())
                                || "Hostel Fee".equalsIgnoreCase(c.getCategoryName()))
                        .findFirst()
                        .ifPresent(c -> {
                            feeDAO.addStudentFee(conn, studentId, c.getId(), c.getBaseAmount(), dueDate);
                        });
            }

        } catch (Exception e) {
            Logger.error("Failed to auto-assign fees for student: " + studentId, e);
            // Don't fail the enrollment execution just because fees failed, but log it
        }
    }

    /**
     * Translate a raw database exception into a user-friendly message.
     */
    private String friendlyEnrollmentMessage(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        String cause = e.getCause() != null && e.getCause().getMessage() != null
                ? e.getCause().getMessage().toLowerCase()
                : "";
        String combined = msg + " " + cause;
        if (combined.contains("students_email_key") || combined.contains("duplicate key") && combined.contains("email")) {
            return "Email is already registered.";
        }
        if (combined.contains("users_username_key") || combined.contains("duplicate key") && combined.contains("username")) {
            return "Username (enrollment number) already exists. Try again.";
        }
        if (combined.contains("users_username_key")) {
            return "An account with this username already exists.";
        }
        return "Failed to enroll student: " + (e.getMessage() == null ? "unknown error" : e.getMessage());
    }
}
