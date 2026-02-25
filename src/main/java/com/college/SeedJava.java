package com.college;

import com.college.dao.AttendanceDAO;
import com.college.models.Attendance;
import com.college.utils.DatabaseConnection;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class SeedJava {
        public static void main(String[] args) throws Exception {
                try (Connection conn = DatabaseConnection.getConnection();
                                Statement stmt = conn.createStatement()) {

                        stmt.executeUpdate(
                                        "INSERT INTO departments (name, code, description) VALUES ('Computer Science', 'CS', 'Desc') ON CONFLICT DO NOTHING");
                        ResultSet rsD = stmt.executeQuery("SELECT id FROM departments LIMIT 1");
                        int dId = 1;
                        if (rsD.next())
                                dId = rsD.getInt(1);

                        stmt.executeUpdate(
                                        "INSERT INTO courses (name, code, credits, department_id, semester) VALUES ('Intro to CS', 'CS101', 4, "
                                                        + dId + ", 1) ON CONFLICT DO NOTHING");

                        ResultSet rsC = stmt.executeQuery("SELECT id FROM courses LIMIT 1");
                        int cId = 1;
                        if (rsC.next())
                                cId = rsC.getInt(1);

                        AttendanceDAO dao = new AttendanceDAO();
                        Date today = new SimpleDateFormat("yyyy-MM-dd").parse("2026-02-24");

                        Attendance a1 = new Attendance();
                        a1.setStudentId(1);
                        a1.setCourseId(cId);
                        a1.setDate(today);
                        a1.setStatus("PRESENT");
                        a1.setRemarks("Seeded");
                        boolean ok1 = dao.markAttendance(a1);

                        Attendance a2 = new Attendance();
                        a2.setStudentId(2);
                        a2.setCourseId(cId);
                        a2.setDate(today);
                        a2.setStatus("ABSENT");
                        a2.setRemarks("Seeded");
                        boolean ok2 = dao.markAttendance(a2);

                        stmt.executeUpdate(
                                        "INSERT INTO fee_categories (category_name, base_amount, description) VALUES ('Tuition', 5000, '') ON CONFLICT DO NOTHING");
                        ResultSet rsF = stmt.executeQuery("SELECT id FROM fee_categories LIMIT 1");
                        int fId = 1;
                        if (rsF.next())
                                fId = rsF.getInt(1);

                        com.college.dao.EnhancedFeeDAO feeDAO = new com.college.dao.EnhancedFeeDAO();
                        java.sql.Date sqlToday = new java.sql.Date(today.getTime());
                        feeDAO.addStudentFee(conn, 1, fId, 5000.0, sqlToday);
                        feeDAO.addStudentFee(conn, 2, fId, 5000.0, sqlToday);

                        System.out.println("Seeded Course ID: " + cId);
                        System.out.println("Seeded student 1 attendance: " + ok1);
                        System.out.println("Seeded student 2 attendance: " + ok2);
                        System.out.println("Seeded Fees");

                        stmt.executeUpdate(
                                        "INSERT INTO placement_companies (name, industry, contact_person, email, phone, website) "
                                                        +
                                                        "VALUES ('Tech Corp', 'IT', 'John Doe', 'john@techcorp.com', '1234567890', 'www.techcorp.com') ON CONFLICT DO NOTHING");

                        ResultSet rsComp = stmt.executeQuery("SELECT id FROM placement_companies LIMIT 1");
                        int compId = 1;
                        if (rsComp.next())
                                compId = rsComp.getInt(1);

                        stmt.executeUpdate(
                                        "INSERT INTO placement_drives (company_id, job_role, package_lpa, description, drive_date, deadline, eligibility_criteria) "
                                                        +
                                                        "VALUES (" + compId
                                                        + ", 'Software Engineer', 10.0, 'SDE Role', '2026-12-31', '2026-11-30', 'B.Tech') ON CONFLICT DO NOTHING");

                        System.out.println("Seeded Placement Data");

                        stmt.executeUpdate(
                                        "INSERT INTO clubs (name, description, category, member_count, status) " +
                                                        "VALUES ('Coding Club', 'For programming enthusiasts', 'Technical', 0, 'ACTIVE') ON CONFLICT DO NOTHING");

                        stmt.executeUpdate(
                                        "INSERT INTO clubs (name, description, category, member_count, status) " +
                                                        "VALUES ('Drama Club', 'Theatrical acts and plays', 'Cultural', 0, 'ACTIVE') ON CONFLICT DO NOTHING");

                        System.out.println("Seeded Clubs Data");

                        // Seed Events
                        stmt.executeUpdate(
                                        "INSERT INTO events (name, description, event_type, location, start_time, end_time, max_participants, registration_deadline, created_by, status) "
                                                        +
                                                        "VALUES ('Tech Symposium', 'Annual technology conference', 'SEMINAR', 'Main Auditorium', '2026-03-15 09:00:00', '2026-03-15 17:00:00', 500, '2026-03-10 23:59:59', 1, 'UPCOMING') ON CONFLICT DO NOTHING");

                        stmt.executeUpdate(
                                        "INSERT INTO events (name, description, event_type, location, start_time, end_time, max_participants, registration_deadline, created_by, status) "
                                                        +
                                                        "VALUES ('Hackathon 2026', '24-hour coding challenge', 'WORKSHOP', 'Lab 1 & 2', '2026-04-20 10:00:00', '2026-04-21 10:00:00', 100, '2026-04-15 23:59:59', 1, 'UPCOMING') ON CONFLICT DO NOTHING");

                        System.out.println("Seeded Events Data");

                        // Seed Grades
                        com.college.dao.GradeDAO gradeDAO = new com.college.dao.GradeDAO();
                        com.college.models.Grade grade1 = new com.college.models.Grade();
                        grade1.setStudentId(1);
                        grade1.setCourseId(cId);
                        grade1.setExamType("MID TERM");
                        grade1.setMarksObtained(85.5);
                        grade1.setGrade("A");
                        gradeDAO.saveGrade(grade1);

                        com.college.models.Grade grade2 = new com.college.models.Grade();
                        grade2.setStudentId(2);
                        grade2.setCourseId(cId);
                        grade2.setExamType("MID TERM");
                        grade2.setMarksObtained(75.0);
                        grade2.setGrade("B");
                        gradeDAO.saveGrade(grade2);

                        System.out.println("Seeded Grades Data");

                        // Seed Visitor Data
                        stmt.executeUpdate(
                                        "INSERT INTO visitor_logs (visitor_name, visitor_phone, purpose, person_to_meet, entry_time, status) "
                                                        +
                                                        "VALUES ('Alice Smith', '9876543210', 'Guest Lecture', 'Dr. Bob', '2026-02-24 10:00:00', 'IN') ON CONFLICT DO NOTHING");

                        stmt.executeUpdate(
                                        "INSERT INTO visitor_logs (visitor_name, visitor_phone, purpose, person_to_meet, entry_time, exit_time, status) "
                                                        +
                                                        "VALUES ('Charlie Brown', '5551234567', 'Delivery', 'Admin Office', '2026-02-24 11:30:00', '2026-02-24 11:45:00', 'OUT') ON CONFLICT DO NOTHING");

                        System.out.println("Seeded Visitor Data");

                        // Seed Gate Passes
                        stmt.executeUpdate(
                                        "INSERT INTO gate_passes (student_id, from_date, to_date, reason, destination, parent_contact, status) "
                                                        +
                                                        "VALUES (1, '2026-03-01', '2026-03-05', 'Family Function', 'Hometown', '9876543210', 'PENDING') ON CONFLICT DO NOTHING");

                        stmt.executeUpdate(
                                        "INSERT INTO gate_passes (student_id, from_date, to_date, reason, destination, parent_contact, status, approved_by, approved_at, approval_comment) "
                                                        +
                                                        "VALUES (2, '2026-02-15', '2026-02-16', 'Medical Issue', 'City Hospital', '5551234567', 'APPROVED', 1, '2026-02-14 10:00:00', 'Approved for health reasons') ON CONFLICT DO NOTHING");

                        System.out.println("Seeded Gate Pass Data");
                }
        }
}
