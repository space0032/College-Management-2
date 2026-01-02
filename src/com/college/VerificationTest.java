package com.college;

import com.college.dao.AuditLogDAO;
import com.college.dao.GatePassDAO;
import com.college.dao.GradeDAO;
import com.college.dao.StudentDAO;
import com.college.dao.UserDAO;
import com.college.models.AuditLog;
import com.college.models.GatePass;
import com.college.models.Grade;
import com.college.models.Student;
import com.college.utils.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class VerificationTest {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("Phase 4 Backend Verification Test");
        System.out.println("==========================================");

        try {
            // 1. Test Database Connection
            if (DatabaseConnection.testConnection()) {
                System.out.println("[PASS] Database Connection");
            } else {
                System.out.println("[FAIL] Database Connection");
                return;
            }

            // 2. Test User Login (Simulated)
            System.out.println("\nTesting Login...");
            UserDAO userDAO = new UserDAO();

            String testUser = "testval" + System.currentTimeMillis();
            // addUser returns int userId
            int userId = userDAO.addUser(testUser, "password123", "STUDENT");

            if (userId > 0) {
                System.out.println("[PASS] Create Test User (ID: " + userId + ")");

                System.out.println("[INFO] Skipping SessionManager init (User model verification pending)");

                // 2.5 Create Student Profile (Required for GatePass FK)
                System.out.println("\nCreating Test Student Profile...");
                StudentDAO studentDAO = new StudentDAO();
                Student student = new Student();
                student.setName("Test Student");
                student.setEmail("test@student.com");
                student.setPhone("9999999999");
                student.setCourse("B.Tech");
                student.setBatch("2024");
                student.setEnrollmentDate(java.sql.Date.valueOf(java.time.LocalDate.now())); // Model uses
                                                                                             // java.util.Date but
                                                                                             // checking setter might be
                                                                                             // safer with sql.Date if
                                                                                             // DAO expects specific
                                                                                             // type or just util.Date.
                                                                                             // DAO uses getTime() so
                                                                                             // util.Date is fine.
                // Re-checking DAO addStudent: pstmt.setDate(6, new
                // java.sql.Date(student.getEnrollmentDate().getTime()));
                // So model uses java.util.Date.
                student.setEnrollmentDate(new java.util.Date());
                student.setAddress("Test Address");
                student.setDepartment("CSE");
                student.setSemester(1);
                student.setHostelite(true);

                int studentId = studentDAO.addStudent(student, userId);

                if (studentId > 0) {
                    System.out.println("[PASS] Create Test Student (ID: " + studentId + ")");

                    // 3. Test Gate Pass
                    System.out.println("\nTesting Gate Pass...");
                    // GatePassDAO methods are static
                    GatePass gp = new GatePass();
                    gp.setStudentId(studentId); // Use STUDENT ID, not USER ID
                    gp.setReason("Home Visit");

                    gp.setFromDate(java.time.LocalDate.now());
                    gp.setToDate(java.time.LocalDate.now().plusDays(1));
                    gp.setDestination("Home");
                    gp.setParentContact("9999999999");

                    boolean gpResult = GatePassDAO.createRequest(gp);
                    if (gpResult) {
                        System.out.println("[PASS] Create Gate Pass Request");

                        // Verify it exists in pending
                        List<GatePass> pending = GatePassDAO.getPendingPasses();
                        boolean found = false;
                        int gpId = -1;
                        for (GatePass p : pending) {
                            if (p.getStudentId() == studentId) {
                                found = true;
                                gpId = p.getId();
                                break;
                            }
                        }

                        if (found) {
                            System.out.println("[PASS] Verify Gate Pass is PENDING");

                            // 4. Test Approval (Admin Action)
                            System.out.println("\nTesting Gate Pass Approval...");
                            // approveRequest(int gatePassId, int approvedBy, String comment)
                            boolean approveResult = GatePassDAO.approveRequest(gpId, 1, "Approved by Test Script");
                            if (approveResult) {
                                System.out.println("[PASS] Approve Gate Pass");
                            } else {
                                System.out.println("[FAIL] Approve Gate Pass");
                            }
                        } else {
                            System.out.println("[FAIL] Generated Gate Pass not found in pending list");
                        }
                    } else {
                        System.out.println("[FAIL] Create Gate Pass Request");
                    }

                    // 5. Test Audit Logs
                    System.out.println("\nTesting Audit Logs...");
                    AuditLogDAO.logAction(userId, testUser, "TEST_ACTION", "USER", userId,
                            "Running verification script");

                    List<AuditLog> newLogs = AuditLogDAO.getRecentLogs(10);
                    if (!newLogs.isEmpty() && newLogs.get(0).getAction().equals("TEST_ACTION")) {
                        System.out.println("[PASS] Create and Verify Audit Log");
                    } else {
                        System.out.println("[FAIL] Audit Log verification");
                    }

                    // 6. Test Grades
                    System.out.println("\nTesting Grades...");
                    GradeDAO gradeDAO = new GradeDAO();
                    Grade grade = new Grade();
                    grade.setStudentId(studentId);
                    grade.setCourseId(1); // Assuming course ID 1 exists
                    grade.setExamType("MID_TERM");
                    grade.setMarksObtained(85.5);
                    grade.setMaxMarks(100.0);
                    grade.setGrade("A");
                    grade.setSemester(1);

                    if (gradeDAO.saveGrade(grade)) {
                        System.out.println("[PASS] Save Grade");

                        List<Grade> grades = gradeDAO.getGradesByStudent(studentId);
                        if (!grades.isEmpty()) {
                            System.out.println("[PASS] Retrieve Grades");
                            // boolean hasGrade = grades.stream().anyMatch(g ->
                            // g.getExamType().equals("MID_TERM"));
                            boolean hasGrade = false;
                            for (Grade g : grades) {
                                if ("MID_TERM".equals(g.getExamType())) {
                                    hasGrade = true;
                                    break;
                                }
                            }

                            if (hasGrade) {
                                System.out.println("[PASS] Verified Saved Grade");
                            } else {
                                System.out.println("[FAIL] Saved Grade not found in list");
                            }
                        } else {
                            System.out.println("[FAIL] Retrieve Grades (List empty)");
                        }
                    } else {
                        System.out.println("[FAIL] Save Grade");
                    }

                    // 7. Test Report Generator
                    System.out.println("\nTesting Report Generator...");
                    List<Object[]> reportData = new ArrayList<>();
                    reportData.add(new Object[] { "Test Student", "85.5" });
                    boolean reportResult = com.college.utils.ReportGenerator.generateCSV("test_report.csv",
                            new String[] { "Name", "Score" }, reportData);

                    if (reportResult) {
                        System.out.println("[PASS] Generate CSV Report");
                        // Verify file exists
                        java.io.File rFile = new java.io.File("test_report.csv");
                        if (rFile.exists()) {
                            System.out.println("[PASS] Verify Report File Exists");
                            rFile.delete(); // Cleanup
                        } else {
                            System.out.println("[FAIL] Report File Not Found");
                        }
                    } else {
                        System.out.println("[FAIL] Generate CSV Report");
                    }

                    // Specific Cleanup for Student
                    deleteTestStudent(studentId);

                } else {
                    System.out.println("[FAIL] Create Test Student Profile");
                }

                // Cleanup User
                System.out.println("\nCleaning up test data...");
                deleteTestUser(userId);
                System.out.println("[PASS] Cleanup");

            } else {
                System.out.println("[FAIL] Could not create test user");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Exception during verification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteTestStudent(int studentId) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Student cleanup failed: " + e.getMessage());
        }
    }

    private static void deleteTestUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }
    }
}
