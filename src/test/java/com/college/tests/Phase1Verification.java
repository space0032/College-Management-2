package com.college.tests;

import com.college.dao.*;
import com.college.models.*;
import com.college.utils.DatabaseConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Phase1Verification {

    private static EventDAO eventDAO;
    private static ClubDAO clubDAO;
    private static UserDAO userDAO;
    private static StudentDAO studentDAO;
    private static FacultyDAO facultyDAO;

    private static User testAdmin;
    private static User testStudentUser;
    private static User testFacultyUser;

    // Profile IDs (not User IDs)
    private static int studentProfileId;
    private static int facultyProfileId;

    @BeforeClass
    public static void setup() throws Exception {
        eventDAO = new EventDAO();
        clubDAO = new ClubDAO();
        userDAO = new UserDAO();
        studentDAO = new StudentDAO();
        facultyDAO = new FacultyDAO();

        // Clean up any existing test data first
        teardown();

        // 1. Create Admin User
        int adminId = userDAO.addUser("p1_admin", "pass", "ADMIN");
        if (adminId == -1 && userDAO.isUsernameTaken("p1_admin")) {
            adminId = getUserId("p1_admin");
        }
        testAdmin = new User(adminId, "p1_admin", "ADMIN");

        // 2. Create Student User & Profile
        int studentUserId = userDAO.addUser("p1_student", "pass", "STUDENT");
        if (studentUserId == -1 && userDAO.isUsernameTaken("p1_student")) {
            studentUserId = getUserId("p1_student");
        }
        testStudentUser = new User(studentUserId, "p1_student", "STUDENT");

        // Check if student profile exists, if not create
        Student s = studentDAO.getStudentByUserId(studentUserId);
        if (s == null) {
            s = new Student();
            s.setName("Test Student");
            s.setEmail("student@test.com");
            s.setPhone("1234567890");
            s.setCourse("CS");
            s.setBatch("2026");
            s.setEnrollmentDate(new Date());
            s.setAddress("Test Address");
            s.setDepartment("CS");
            s.setSemester(1);
            s.setHostelite(false);
            // Required new fields in DAO (using minimal valid data)
            s.setGender("Male");
            s.setBloodGroup("O+");
            s.setCategory("General");
            s.setNationality("Indian");
            s.setFatherName("Father");
            s.setMotherName("Mother");
            s.setGuardianContact("0000000000");
            studentProfileId = studentDAO.addStudent(s, studentUserId);
        } else {
            studentProfileId = s.getId();
        }

        // 3. Create Faculty User & Profile
        int facultyUserId = userDAO.addUser("p1_faculty", "pass", "FACULTY");
        if (facultyUserId == -1 && userDAO.isUsernameTaken("p1_faculty")) {
            facultyUserId = getUserId("p1_faculty");
        }
        testFacultyUser = new User(facultyUserId, "p1_faculty", "FACULTY");

        Faculty f = facultyDAO.getFacultyByUserId(facultyUserId);
        if (f == null) {
            f = new Faculty();
            f.setName("Test Faculty");
            f.setEmail("faculty@test.com");
            f.setPhone("9876543210");
            f.setDepartment("CS");
            f.setQualification("PhD");
            f.setJoinDate(new Date());
            facultyProfileId = facultyDAO.addFaculty(f, facultyUserId);
        } else {
            facultyProfileId = f.getId();
        }
    }

    private static int getUserId(String username) {
        return userDAO.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .map(User::getId)
                .orElse(-1);
    }

    @AfterClass
    public static void teardown() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Delete registrations and memberships first (Foreign Keys)
            if (studentProfileId > 0) {
                stmt.executeUpdate("DELETE FROM event_registrations WHERE student_id = " + studentProfileId);
                stmt.executeUpdate("DELETE FROM club_memberships WHERE student_id = " + studentProfileId);
            }

            // Delete Events and Clubs
            stmt.executeUpdate("DELETE FROM events WHERE name LIKE 'P1 Test Event%'");
            stmt.executeUpdate("DELETE FROM clubs WHERE name LIKE 'P1 Test Club%'");

            // Delete Profiles
            if (studentProfileId > 0)
                stmt.executeUpdate("DELETE FROM students WHERE id = " + studentProfileId);
            if (facultyProfileId > 0)
                stmt.executeUpdate("DELETE FROM faculty WHERE id = " + facultyProfileId);

            // Delete Users
            stmt.executeUpdate("DELETE FROM users WHERE username IN ('p1_admin', 'p1_student', 'p1_faculty')");
        }
    }

    // --- TEST SUITE 1: EVENT MANAGEMENT ---

    @Test
    public void test01_CreateEvent() {
        Event event = new Event();
        event.setName("P1 Test Event");
        event.setDescription("Description");
        event.setEventType("FEST");
        event.setLocation("Auditorium");

        LocalDateTime now = LocalDateTime.now();
        event.setStartTime(Timestamp.valueOf(now.plusDays(1)));
        event.setEndTime(Timestamp.valueOf(now.plusDays(1).plusHours(2)));
        event.setRegistrationDeadline(Timestamp.valueOf(now.plusHours(5)));

        event.setMaxParticipants(50);
        event.setStatus("UPCOMING");
        event.setCreatedBy(testAdmin.getId()); // Events are created by USERS (Admin/Faculty)

        assertTrue("Event should be created", eventDAO.createEvent(event));

        List<Event> events = eventDAO.getAllEvents();
        boolean found = events.stream().anyMatch(e -> e.getName().equals("P1 Test Event"));
        assertTrue("Event should be found in list", found);
    }

    @Test
    public void test02_RegisterForEvent() {
        Event event = eventDAO.getAllEvents().stream()
                .filter(e -> e.getName().equals("P1 Test Event"))
                .findFirst().orElse(null);
        assertNotNull(event);

        // Register using STUDENT PROFILE ID
        assertTrue("Registration should succeed", eventDAO.registerStudent(event.getId(), studentProfileId));
        assertTrue("Should be registered", eventDAO.isStudentRegistered(event.getId(), studentProfileId));
    }

    @Test
    public void test03_MarkAttendance() {
        Event event = eventDAO.getAllEvents().stream()
                .filter(e -> e.getName().equals("P1 Test Event"))
                .findFirst().orElse(null);
        assertNotNull(event);

        // Find registration
        List<EventRegistration> regs = eventDAO.getEventRegistrations(event.getId());
        EventRegistration reg = regs.stream()
                .filter(r -> r.getStudentId() == studentProfileId)
                .findFirst().orElse(null);
        assertNotNull("Registration must exist", reg);

        assertTrue("Update attendance success",
                eventDAO.markAttendance(reg.getId(), "ATTENDED"));

        // Verify
        regs = eventDAO.getEventRegistrations(event.getId());
        reg = regs.stream()
                .filter(r -> r.getStudentId() == studentProfileId)
                .findFirst().orElse(null);

        assertNotNull(reg);
        assertEquals("ATTENDED", reg.getAttendanceStatus());
    }

    @Test
    public void test04_UnregisterEvent() {
        Event event = eventDAO.getAllEvents().stream()
                .filter(e -> e.getName().equals("P1 Test Event"))
                .findFirst().orElse(null);
        assertNotNull(event);

        assertTrue("Unregistration success", eventDAO.unregisterStudent(event.getId(), studentProfileId));
        assertFalse("Should not be registered", eventDAO.isStudentRegistered(event.getId(), studentProfileId));
    }

    // --- TEST SUITE 2: CLUB MANAGEMENT ---

    @Test
    public void test05_CreateClub() {
        Club club = new Club();
        club.setName("P1 Test Club");
        club.setDescription("Desc");
        club.setCategory("TECHNICAL");
        club.setFacultyCoordinatorId(facultyProfileId); // Use Faculty PROFILE ID
        club.setPresidentStudentId(studentProfileId); // Use Student PROFILE ID
        club.setStatus("ACTIVE");

        assertTrue("Club creation success", clubDAO.createClub(club));

        List<Club> clubs = clubDAO.getAllClubs();
        assertTrue("Club found", clubs.stream().anyMatch(c -> c.getName().equals("P1 Test Club")));
    }

    @Test
    public void test06_ClubJoinWorkflow() {
        Club club = clubDAO.getAllClubs().stream()
                .filter(c -> c.getName().equals("P1 Test Club"))
                .findFirst().orElse(null);
        assertNotNull(club);

        // 1. Request Join (using Student PROFILE ID)
        assertTrue("Join request success", clubDAO.joinClub(club.getId(), studentProfileId));

        // 2. Verify PENDING
        List<ClubMembership> members = clubDAO.getPendingMemberships(club.getId());
        ClubMembership member = members.stream()
                .filter(m -> m.getStudentId() == studentProfileId)
                .findFirst().orElse(null);
        assertNotNull(member);
        assertEquals("PENDING", member.getStatus());

        // 3. Approve
        assertTrue("Approve success", clubDAO.approveMembership(member.getId()));

        // 4. Verify APPROVED in Members List
        List<ClubMembership> activeMembers = clubDAO.getClubMembers(club.getId());
        ClubMembership activeMember = activeMembers.stream()
                .filter(m -> m.getStudentId() == studentProfileId)
                .findFirst().orElse(null);
        assertNotNull(activeMember);
        assertEquals("APPROVED", activeMember.getStatus());
        assertEquals("MEMBER", activeMember.getRole());
    }

    @Test
    public void test07_RemoveMember() {
        Club club = clubDAO.getAllClubs().stream()
                .filter(c -> c.getName().equals("P1 Test Club"))
                .findFirst().orElse(null);
        assertNotNull(club);

        assertTrue("Leave club success", clubDAO.leaveClub(club.getId(), studentProfileId));

        List<ClubMembership> members = clubDAO.getClubMembers(club.getId());
        boolean exists = members.stream().anyMatch(m -> m.getStudentId() == studentProfileId);
        assertFalse("Member should be removed", exists);
    }
}
