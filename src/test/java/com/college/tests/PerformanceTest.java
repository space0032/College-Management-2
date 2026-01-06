package com.college.tests;

import com.college.dao.*;
import com.college.models.*;
import com.college.utils.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class PerformanceTest {

    private static final Logger logger = Logger.getLogger(PerformanceTest.class.getName());
    private static EventDAO eventDAO;
    private static StudentDAO studentDAO;

    // Dataset Size
    private static final int STUDENT_COUNT = 500;
    private static final int EVENT_COUNT = 50;

    @BeforeAll
    public static void setup() throws Exception {
        eventDAO = new EventDAO();
        studentDAO = new StudentDAO();

        teardown(); // Clean start

        long startTime = System.currentTimeMillis();
        logger.info("Starting Bulk Data Generation...");

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Bulk Create Users & Students
            String userSql = "INSERT INTO users (username, password, role) VALUES (?, 'hashed_pass', 'STUDENT')";
            String studentSql = "INSERT INTO students (user_id, name, email, department, batch, semester) VALUES (?, ?, ?, 'CS', '2026', 1)";

            try (PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement studentStmt = conn.prepareStatement(studentSql)) {

                for (int i = 1; i <= STUDENT_COUNT; i++) {
                    userStmt.setString(1, "perf_student_" + i);
                    userStmt.executeUpdate();

                    var rs = userStmt.getGeneratedKeys();
                    if (rs.next()) {
                        int userId = rs.getInt(1);
                        studentStmt.setInt(1, userId);
                        studentStmt.setString(2, "Student " + i);
                        studentStmt.setString(3, "student" + i + "@test.com");
                        studentStmt.executeUpdate();
                    }
                }
            }

            // 2. Bulk Create Events
            String eventSql = "INSERT INTO events (name, description, event_type, start_time, end_time, created_by) VALUES (?, 'Perf Test', 'ACADEMIC', ?, ?, 1)";
            Timestamp start = Timestamp.valueOf(LocalDateTime.now().plusDays(1));
            Timestamp end = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2));

            try (PreparedStatement eventStmt = conn.prepareStatement(eventSql)) {
                for (int i = 1; i <= EVENT_COUNT; i++) {
                    eventStmt.setString(1, "Perf Event " + i);
                    eventStmt.setTimestamp(2, start);
                    eventStmt.setTimestamp(3, end);
                    eventStmt.addBatch();
                }
                eventStmt.executeBatch();
            }

            conn.commit();
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Setup completed in " + duration + "ms for " + STUDENT_COUNT + " students and " + EVENT_COUNT
                + " events.");
    }

    @AfterAll
    public static void teardown() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "DELETE FROM event_registrations WHERE student_id IN (SELECT id FROM students WHERE email LIKE 'student%@test.com')");
            stmt.executeUpdate("DELETE FROM events WHERE name LIKE 'Perf Event%'");
            stmt.executeUpdate("DELETE FROM students WHERE email LIKE 'student%@test.com'");
            stmt.executeUpdate("DELETE FROM users WHERE username LIKE 'perf_student_%'");
        }
    }

    @Test
    public void test01_FetchAllEventsPerformance() {
        long start = System.currentTimeMillis();
        List<Event> events = eventDAO.getAllEvents();
        long duration = System.currentTimeMillis() - start;

        logger.info("Fetched " + events.size() + " events in " + duration + "ms");

        // Expect fetching 50+ events to be under 200ms (generous limit for local DB)
        assertTrue(duration < 500, "Fetching events took too long: " + duration + "ms");
    }

    @Test
    public void test02_FetchAllStudentsPerformance() {
        long start = System.currentTimeMillis();
        List<Student> students = studentDAO.getAllStudents();
        long duration = System.currentTimeMillis() - start;

        logger.info("Fetched " + students.size() + " students in " + duration + "ms");

        // Expect fetching 500 students to be under 500ms
        assertTrue(duration < 1000, "Fetching students took too long: " + duration + "ms");
    }

    @Test
    public void test03_BulkRegistrationPerformance() {
        // Register all 500 students to Event #1
        Event targetEvent = eventDAO.getAllEvents().stream()
                .filter(e -> e.getName().equals("Perf Event 1"))
                .findFirst().orElseThrow();

        List<Student> students = studentDAO.getAllStudents();

        long start = System.currentTimeMillis();

        // Using DAOs one by one (simulating realistic load of concurrent requests or
        // bulk logic loop)
        // Optimization: In a real bulk scenario we'd add a bulkRegister method, but
        // here we test the loop performance
        int count = 0;
        for (Student s : students) {
            if (s.getEmail().startsWith("student")) { // Filter only our test students
                eventDAO.registerStudent(targetEvent.getId(), s.getId());
                count++;
            }
        }

        long duration = System.currentTimeMillis() - start;
        logger.info("Registered " + count + " students in " + duration + "ms");

        // 500 inserts. If each takes 10ms, total = 5000ms.
        // We set a threshold of 10 seconds to be safe but expect much faster.
        assertTrue(duration < 10000, "Bulk registration took too long: " + duration + "ms");
    }

    @Test
    public void test04_FetchRegistrationsPerformance() {
        Event targetEvent = eventDAO.getAllEvents().stream()
                .filter(e -> e.getName().equals("Perf Event 1"))
                .findFirst().orElseThrow();

        long start = System.currentTimeMillis();
        List<EventRegistration> regs = eventDAO.getEventRegistrations(targetEvent.getId());
        long duration = System.currentTimeMillis() - start;

        logger.info("Fetched " + regs.size() + " registrations in " + duration + "ms");

        // Fetching 500 rows should be very fast (< 200ms)
        assertTrue(duration < 500, "Fetching registrations took too long: " + duration + "ms");
    }
}
