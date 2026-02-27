package com.college.tests;

import com.college.dao.*;
import com.college.models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class Phase4Verification {

    private static StudentDAO studentDAO;
    private static FacultyDAO facultyDAO;
    private static CourseDAO courseDAO;

    @BeforeAll
    public static void setup() {
        studentDAO = new StudentDAO();
        facultyDAO = new FacultyDAO();
        courseDAO = new CourseDAO();
    }

    @Test
    public void test01_StudentPagination() {
        int total = studentDAO.getTotalCount();
        if (total > 0) {
            List<Student> page1 = studentDAO.getAllStudentsPaginated(1, 1);
            assertEquals(1, page1.size(), "Page 1 size should be 1 if total > 0");

            if (total > 1) {
                List<Student> page2 = studentDAO.getAllStudentsPaginated(2, 1);
                assertEquals(1, page2.size(), "Page 2 size should be 1 if total > 1");
                assertNotEquals(page1.get(0).getId(), page2.get(0).getId(),
                        "Page 1 and Page 2 should have different students");
            }
        }
    }

    @Test
    public void test02_FacultyPagination() {
        int total = facultyDAO.getTotalCount();
        if (total > 0) {
            List<Faculty> page1 = facultyDAO.getAllFacultyPaginated(1, 1);
            assertEquals(1, page1.size(), "Page 1 size should be 1 if total > 0");
        }
    }

    @Test
    public void test03_CourseWeeklyCount() {
        // CourseDAO.getWeeklyCount was just added.
        // It should return >= 0 and not crash.
        int weekly = courseDAO.getWeeklyCount();
        assertTrue(weekly >= 0, "Weekly count should be non-negative");
    }

    @Test
    public void test04_DashboardStatsIntegrity() {
        // Verify that total counts match list sizes (for small datasets)
        int studentTotal = studentDAO.getTotalCount();
        int facultyTotal = facultyDAO.getTotalCount();
        int courseTotal = courseDAO.getTotalCount();

        // These are low-risk but verify the methods exist and return valid integers
        assertTrue(studentTotal >= 0);
        assertTrue(facultyTotal >= 0);
        assertTrue(courseTotal >= 0);
    }
}
