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
                        System.out.println("Seeded Departments");

                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS resource_categories (" +
                                                        "id SERIAL PRIMARY KEY," +
                                                        "name VARCHAR(100) NOT NULL UNIQUE," +
                                                        "description TEXT" +
                                                        ")");

                        stmt.executeUpdate(
                                        "INSERT INTO resource_categories (name, description) VALUES ('Lecture Notes', 'PDFs and slides from class') ON CONFLICT DO NOTHING");
                        stmt.executeUpdate(
                                        "INSERT INTO resource_categories (name, description) VALUES ('Video Tutorials', 'Recorded lectures and tutorials') ON CONFLICT DO NOTHING");
                        stmt.executeUpdate(
                                        "INSERT INTO resource_categories (name, description) VALUES ('Past Papers', 'Previous year exam papers') ON CONFLICT DO NOTHING");

                        System.out.println("Seeded Resource Categories");

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

                        stmt.executeUpdate(
                                        "INSERT INTO calendar_events (title, event_date, event_type, description) "
                                                        +
                                                        "VALUES ('Spring Break', '2026-03-20', 'HOLIDAY', 'Campus closed for spring break') ON CONFLICT DO NOTHING");
                        stmt.executeUpdate(
                                        "INSERT INTO calendar_events (title, event_date, event_type, description) "
                                                        +
                                                        "VALUES ('Final Exams Begin', '2026-05-10', 'EXAM', 'All library hours extended') ON CONFLICT DO NOTHING");
                        stmt.executeUpdate(
                                        "INSERT INTO calendar_events (title, event_date, event_type, description) "
                                                        +
                                                        "VALUES ('Project Submission Deadline', '2026-04-15', 'DEADLINE', 'Final year projects due') ON CONFLICT DO NOTHING");

                        System.out.println("Seeded Calendar Event Data");

                        stmt.executeUpdate(
                                        "INSERT INTO scholarships (title, description, amount, donor_name, created_by, status) "
                                                        +
                                                        "VALUES ('Merit Scholarship 2026', 'For top performers', 50000.0, 'Alumni Assoc', 1, 'OPEN') ON CONFLICT DO NOTHING");
                        stmt.executeUpdate(
                                        "INSERT INTO scholarships (title, description, amount, donor_name, created_by, status) "
                                                        +
                                                        "VALUES ('Need-Based Grant', 'Financial assistance program', 25000.0, 'Global Trust', 1, 'OPEN') ON CONFLICT DO NOTHING");

                        ResultSet rsSchol = stmt.executeQuery("SELECT id FROM scholarships LIMIT 1");
                        int sId = 1;
                        if (rsSchol.next())
                                sId = rsSchol.getInt(1);

                        stmt.executeUpdate(
                                        "INSERT INTO scholarship_applications (scholarship_id, student_id, statement, status) "
                                                        +
                                                        "VALUES (" + sId
                                                        + ", 1, 'I maintain a 3.9 GPA across all subjects.', 'APPLIED') ON CONFLICT DO NOTHING");

                        System.out.println("Seeded Scholarship Data");

                        stmt.executeUpdate(
                                        "INSERT INTO assignments (course_id, title, description, due_date, created_by, semester) "
                                                        +
                                                        "VALUES (1, 'Data Structures Project', 'Implement an AVL Tree in Java', '2026-03-01 23:59:59', 1, 1) ON CONFLICT DO NOTHING");

                        ResultSet rsAssign = stmt.executeQuery("SELECT id FROM assignments LIMIT 1");
                        int aId = 1;
                        if (rsAssign.next())
                                aId = rsAssign.getInt(1);

                        stmt.executeUpdate(
                                        "INSERT INTO submissions (assignment_id, student_id, submission_text, submission_date, grade, feedback, is_graded) "
                                                        +
                                                        "VALUES (" + aId
                                                        + ", 1, 'github.com/student/avl-tree-java', '2026-02-24 12:00:00', NULL, NULL, FALSE) ON CONFLICT DO NOTHING");

                        System.out.println("Seeded Assignment Data");

                        stmt.executeUpdate(
                                        "INSERT INTO campaigns (title, description, goal_amount, raised_amount, created_by, status) "
                                                        +
                                                        "VALUES ('New Library Wing', 'Help us build a state-of-the-art reading room', 500000.0, 125000.0, 1, 'ACTIVE') ON CONFLICT DO NOTHING");
                        stmt.executeUpdate(
                                        "INSERT INTO campaigns (title, description, goal_amount, raised_amount, created_by, status) "
                                                        +
                                                        "VALUES ('Robotics Lab Equipment', 'Fund the purchase of new robotic arms', 150000.0, 160000.0, 1, 'COMPLETED') ON CONFLICT DO NOTHING");

                        System.out.println("Seeded Crowdfunding Data");

                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS learning_resources (" +
                                                        "id SERIAL PRIMARY KEY," +
                                                        "title VARCHAR(200) NOT NULL," +
                                                        "description TEXT," +
                                                        "course_id INTEGER REFERENCES courses(id)," +
                                                        "category_id INTEGER REFERENCES resource_categories(id)," +
                                                        "file_path VARCHAR(500) NOT NULL," +
                                                        "file_type VARCHAR(50)," +
                                                        "file_size BIGINT," +
                                                        "uploaded_by INTEGER REFERENCES users(id)," +
                                                        "download_count INTEGER DEFAULT 0," +
                                                        "is_public BOOLEAN DEFAULT true," +
                                                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                                        ")");

                        int catId1 = 1, catId2 = 2; // Assuming IDs align with earlier inserts
                        stmt.executeUpdate(
                                        "INSERT INTO learning_resources (title, description, course_id, category_id, file_path, file_type, file_size, uploaded_by, is_public) " +
                                                        "VALUES ('Intro to CS Slides', 'First week introduction', 1, " + catId1 + ", 'https://example.com/slides.pdf', 'pdf', 1048576, 1, true) ON CONFLICT DO NOTHING"
                        );
                        stmt.executeUpdate(
                                        "INSERT INTO learning_resources (title, description, course_id, category_id, file_path, file_type, file_size, uploaded_by, is_public) " +
                                                        "VALUES ('CS 101 Lecture Video', 'Recording of week 1', 1, " + catId2 + ", 'https://example.com/vid.mp4', 'mp4', 52428800, 1, false) ON CONFLICT DO NOTHING"
                        );

                        System.out.println("Seeded Learning Resources Data");

                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS employees (" +
                                                        "id SERIAL PRIMARY KEY," +
                                                        "employee_id VARCHAR(50) UNIQUE NOT NULL," +
                                                        "first_name VARCHAR(100)," +
                                                        "last_name VARCHAR(100)," +
                                                        "email VARCHAR(100)," +
                                                        "phone VARCHAR(20)," +
                                                        "designation VARCHAR(100)," +
                                                        "join_date DATE," +
                                                        "salary DECIMAL(10, 2)," +
                                                        "status VARCHAR(20) DEFAULT 'ACTIVE'" +
                                                        ")");

                        stmt.executeUpdate(
                                        "INSERT INTO employees (employee_id, first_name, last_name, email, phone, designation, join_date, salary, status) " +
                                                        "VALUES ('admin', 'System', 'Administrator', 'admin@example.com', '1234567890', 'Principal', '2020-01-01', 120000.00, 'ACTIVE') ON CONFLICT DO NOTHING"
                        );
                        stmt.executeUpdate(
                                        "INSERT INTO employees (employee_id, first_name, last_name, email, phone, designation, join_date, salary, status) " +
                                                        "VALUES ('E001', 'John', 'Smith', 'john.smith@example.com', '9876543210', 'Librarian', '2021-05-15', 45000.00, 'ACTIVE') ON CONFLICT DO NOTHING"
                        );

                        System.out.println("Seeded Employees Data");
                }
        }
}
