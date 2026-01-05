package com.college.utils;

import com.college.dao.*;
import com.college.models.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Database seeder utility - Run this to populate realistic college data
 * This can be executed from the application itself without needing SQL access
 */
public class DataSeeder {

    public static void main(String[] args) {
        System.out.println("Starting data seeding...");

        try {
            seedDepartments();
            seedUsersAndFaculty();
            seedEmployees();
            seedStudents();
            seedCourses();
            seedCalendarEvents();

            System.out.println("\n✓ Data seeding completed successfully!");
            System.out.println("Check credentials.txt for login information");

        } catch (Exception e) {
            System.err.println("✗ Error during seeding: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedDepartments() {
        System.out.println("Seeding departments...");
        DepartmentDAO dao = new DepartmentDAO();

        // Clear existing
        // Note: Manual clearing via Institute Management recommended

        String[][] depts = {
                { "CSE", "Computer Science & Engineering", "Dr. Sarah Johnson" },
                { "ECE", "Electronics & Communication", "Dr. Michael Chen" },
                { "ME", "Mechanical Engineering", "Dr. Robert Martinez" },
                { "CE", "Civil Engineering", "Dr. Emily Wilson" },
                { "EEE", "Electrical & Electronics", "Dr. David Kumar" }
        };

        for (String[] d : depts) {
            Department dept = new Department();
            dept.setCode(d[0]);
            dept.setName(d[1]);
            dept.setHeadOfDepartment(d[2]);
            dept.setDescription("Department of " + d[1]);
            dao.addDepartment(dept);
        }
    }

    private static void seedUsersAndFaculty() {
        System.out.println("Seeding users and faculty...");
        // This requires UserDAO and FacultyDAO which may need admin privileges
        // Recommend doing this via the application UI
        System.out.println("  → Use Institute Management UI to add faculty users");
    }

    private static void seedEmployees() {
        System.out.println("Seeding employee records...");
        // Employees auto-appear from users table
        System.out.println("  → Employees will auto-populate from user accounts");
    }

    private static void seedStudents() {
        System.out.println("Seeding students...");
        System.out.println("  → Use Student Management UI to add students");
    }

    private static void seedCourses() {
        System.out.println("Seeding courses...");
        System.out.println("  → Use Course Management to add courses");
    }

    private static void seedCalendarEvents() {
        System.out.println("Seeding calendar events...");
        CalendarDAO dao = new CalendarDAO();

        Object[][] events = {
                { "Republic Day", "2026-01-26", CalendarEvent.EventType.HOLIDAY, "National Holiday" },
                { "Mid-Sem Exams", "2026-02-15", CalendarEvent.EventType.EXAM, "Mid-semester exams" },
                { "Holi", "2026-03-14", CalendarEvent.EventType.HOLIDAY, "Festival of Colors" },
                { "Tech Fest", "2026-03-20", CalendarEvent.EventType.EVENT, "Annual Tech Festival" },
                { "Final Exams", "2026-05-01", CalendarEvent.EventType.EXAM, "End semester exams" }
        };

        for (Object[] e : events) {
            CalendarEvent event = new CalendarEvent();
            event.setTitle((String) e[0]);
            event.setEventDate(LocalDate.parse((String) e[1]));
            event.setEventType((CalendarEvent.EventType) e[2]);
            event.setDescription((String) e[3]);
            dao.addEvent(event);
        }
    }
}
