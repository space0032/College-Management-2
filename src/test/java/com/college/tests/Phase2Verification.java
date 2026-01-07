package com.college.tests;

import com.college.dao.*;
import com.college.models.*;
import com.college.services.FileUploadService;
import com.college.utils.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class Phase2Verification {

    private static SyllabusDAO syllabusDAO;
    private static LearningResourceDAO resourceDAO;

    private static UserDAO userDAO;
    private static FileUploadService fileUploadService;

    private static User testFaculty;
    private static int courseId;
    private static int categoryId;

    @BeforeAll
    public static void setup() throws Exception {
        syllabusDAO = new SyllabusDAO();
        resourceDAO = new LearningResourceDAO();

        userDAO = new UserDAO();
        fileUploadService = new FileUploadService();

        // 1. Create Faculty User
        String username = "p2_faculty";
        int facultyId = userDAO.addUser(username, "pass", "FACULTY");
        if (facultyId == -1 && userDAO.isUsernameTaken(username)) {
            facultyId = getUserId(username);
        }
        testFaculty = new User(facultyId, username, "FACULTY");

        // 2. Create Dummy Course
        // Check if course exists or create one (Direct SQL for simplicity as CourseDAO
        // might be complex)
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Cleanup first
            stmt.executeUpdate("DELETE FROM course_syllabi WHERE title LIKE 'P2 Test Syllabus%'");
            stmt.executeUpdate("DELETE FROM learning_resources WHERE title LIKE 'P2 Test Resource%'");
            stmt.executeUpdate("DELETE FROM courses WHERE code = 'P2CS101'");

            // Ensure Department exists
            int deptId = 1;
            ResultSet deptRs = stmt.executeQuery("SELECT id FROM departments LIMIT 1");
            if (deptRs.next()) {
                deptId = deptRs.getInt(1);
            } else {
                stmt.executeUpdate("INSERT INTO departments (name, code) VALUES ('Computer Science', 'CS')");
                deptRs = stmt.executeQuery("SELECT id FROM departments WHERE code='CS'");
                if (deptRs.next())
                    deptId = deptRs.getInt(1);
            }

            String sql = "INSERT INTO courses (name, code, credits, department_id, semester) VALUES ('P2 Test Course', 'P2CS101', 4, "
                    + deptId + ", 1)";
            try (PreparedStatement adminPstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                adminPstmt.executeUpdate();
                ResultSet rs = adminPstmt.getGeneratedKeys();
                if (rs.next())
                    courseId = rs.getInt(1);
            }
        }

        // 3. Get a valid Resource Category
        List<ResourceCategory> cats = resourceDAO.getAllCategories();
        if (cats.isEmpty()) {
            // Should insert one if empty, but usually seeded. Assuming existing seed.
            // If strictly empty, test will fail, which is good info.
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.createStatement().executeUpdate(
                        "INSERT INTO resource_categories (name, description) VALUES ('Notes', 'Lecture Notes')");
                cats = resourceDAO.getAllCategories();
            }
        }
        if (!cats.isEmpty()) {
            categoryId = cats.get(0).getId();
        }
    }

    private static int getUserId(String username) {
        return userDAO.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .map(User::getId)
                .orElse(-1);
    }

    @AfterAll
    public static void teardown() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            // Delete Syllabus & Resources
            stmt.executeUpdate("DELETE FROM course_syllabi WHERE title LIKE 'P2 Test Syllabus%'");
            stmt.executeUpdate("DELETE FROM learning_resources WHERE title LIKE 'P2 Test Resource%'");

            // Delete Course
            stmt.executeUpdate("DELETE FROM courses WHERE id = " + courseId);

            // Delete User
            stmt.executeUpdate("DELETE FROM users WHERE username = 'p2_faculty'");
        }
    }

    // --- TEST SUITE 1: FILE UPLOAD SERVICE ---

    @Test
    public void test01_FileUpload() {
        String content = "Fake PDF Content";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        String originalName = "test.pdf";

        String savedPath = fileUploadService.uploadSyllabus(is, originalName, content.length());

        assertNotNull(savedPath, "File path should not be null");
        assertTrue(new File(savedPath).exists(), "File should exist");

        // Clean up immediately for this unit test file
        new File(savedPath).delete();
    }

    @Test
    public void test02_InvalidExtensionUpload() {
        String content = "Exe Content";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        String originalName = "malware.exe";

        String savedPath = fileUploadService.uploadResource(is, originalName, content.length());

        assertNull(savedPath, "Should block invalid extension");
    }

    @Test
    public void test03_FileSizeLimit() {
        String content = "Large File Mock";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        long largeSize = 51 * 1024 * 1024; // 51 MB

        String savedPath = fileUploadService.uploadResource(is, "large.pdf", largeSize);

        assertNull(savedPath, "Should reject file > 50MB");
    }

    // --- TEST SUITE 2: SYLLABUS MANAGEMENT ---

    @Test
    public void test04_AddSyllabus() {
        Syllabus s = new Syllabus();
        s.setCourseId(courseId);
        s.setTitle("P2 Test Syllabus 1.0");
        s.setVersion("1.0");
        s.setDescription("Initial syllabus");
        s.setUploadedBy(testFaculty.getId());
        s.setFilePath("uploads/syllabi/mock_p2_syllabus.pdf");

        assertTrue(syllabusDAO.addSyllabus(s), "Add syllabus success");

        List<Syllabus> list = syllabusDAO.getSyllabiByCourse(courseId);
        assertFalse(list.isEmpty(), "List not empty");
        assertTrue(list.stream().anyMatch(sy -> sy.getTitle().equals("P2 Test Syllabus 1.0")), "Found syllabus");
    }

    // --- TEST SUITE 3: LEARNING RESOURCES ---

    @Test
    public void test05_AddLearningResource() {
        LearningResource r = new LearningResource();
        r.setTitle("P2 Test Resource Doc");
        r.setDescription("Lecture notes");
        r.setCourseId(courseId);
        r.setCategoryId(categoryId);
        r.setFilePath("uploads/resources/mock_p2_notes.docx");
        r.setFileType("docx");
        r.setFileSize(1024);
        r.setUploadedBy(testFaculty.getId());
        r.setPublic(true);

        assertTrue(resourceDAO.addResource(r), "Add resource success");

        List<LearningResource> list = resourceDAO.getResourcesByCourse(courseId);
        boolean found = list.stream().anyMatch(lr -> lr.getTitle().equals("P2 Test Resource Doc"));
        assertTrue(found, "Resource found by course");
    }

    @Test
    public void test06_SearchResources() {
        // Since Search is currently implemented in View logic (filtering the list),
        // we verify that the DAO returns data that CAN be searched/filtered correctly.

        List<LearningResource> all = resourceDAO.getResourcesByCourse(courseId);

        // Simulate Search "Doc" (match case in Title)
        long count = all.stream()
                .filter(r -> r.getTitle().contains("Doc"))
                .count();

        assertTrue(count > 0, "Search should find the resource");

        // Simulate Search "NonExistent"
        long countZero = all.stream()
                .filter(r -> r.getTitle().contains("NonExistentXYZ"))
                .count();

        assertEquals(0, countZero, "Search should find nothing");
    }

    @Test
    public void test07_ResourceDownloadCount() {
        // Find the resource
        LearningResource r = resourceDAO.getResourcesByCourse(courseId).stream()
                .filter(lr -> lr.getTitle().equals("P2 Test Resource Doc"))
                .findFirst()
                .orElse(null);
        assertNotNull(r);

        resourceDAO.incrementDownloadCount(r.getId());

        // Fetch again to verify
        // Note: getAllResources might not filter by ID easily, but we can check course
        // list again
        LearningResource updated = resourceDAO.getResourcesByCourse(courseId).stream()
                .filter(lr -> lr.getId() == r.getId())
                .findFirst()
                .orElse(null);

        assertNotNull(updated);
        assertTrue(updated.getDownloadCount() > r.getDownloadCount(), "Download count incremented");
    }
}
