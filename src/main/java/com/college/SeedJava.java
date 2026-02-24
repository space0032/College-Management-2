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
                }
        }
}
