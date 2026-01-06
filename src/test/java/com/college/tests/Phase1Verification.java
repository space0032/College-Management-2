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
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
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

    @BeforeAll
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

    @AfterAll
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

        assertTrue(eventDAO.createEvent(event), "Event should be created");

        List<Event> events = eventDAO.getAllEvents();
        boolean found = events.stream().anyMatch(e -> e.getName().equals("P1 Test Event"));
        assertTrue(found, "Event should be found in list");
    }

    @Test
    public void test02_EditEvent() {
        Event event = eventDAO.getAllEvents().stream()
                .filter(e -> e.getName().equals("P1 Test Event"))
                .findFirst().orElse(null);
        assertNotNull(event);

        event.setLocation("New Auditorium");
        event.setMaxParticipants(100);

        assertTrue(eventDAO.updateEvent(event), "Event update success");

        Event updated = eventDAO.getAllEvents().stream()
                .filter(e -> e.getId() == event.getId())
                .findFirst().orElse(null);
        assertEquals("New Auditorium", updated.getLocation());
        assertEquals(Integer.valueOf(100), updated.getMaxParticipants());
    }

    @Test
    public void test03_RegisterForEvent() {
        Event event = eventDAO.getAllEvents().stream()
                .filter(e -> e.getName().equals("P1 Test Event"))
                .findFirst().orElse(null);
        assertNotNull(event);

        // Register using STUDENT PROFILE ID
        assertTrue(eventDAO.registerStudent(event.getId(), studentProfileId), "Registration should succeed");
        assertTrue(eventDAO.isStudentRegistered(event.getId(), studentProfileId), "Should be registered");
    }

    @Test
    public void test04_MarkAttendance() {
        Event event = eventDAO.getAllEvents().stream()
                .filter(e -> e.getName().equals("P1 Test Event"))
                .findFirst().orElse(null);
        assertNotNull(event);

        // Find registration
        List<EventRegistration> regs = eventDAO.getEventRegistrations(event.getId());
        EventRegistration reg = regs.stream()
                .filter(r -> r.getStudentId() == studentProfileId)
                .findFirst().orElse(null);
        assertNotNull(reg, "Registration must exist");

        assertTrue(eventDAO.markAttendance(reg.getId(), "ATTENDED"), "Update attendance success");

        // Verify
        regs = eventDAO.getEventRegistrations(event.getId());
        reg = regs.stream()
                .filter(r -> r.getStudentId() == studentProfileId)
                .findFirst().orElse(null);

        assertNotNull(reg);
        assertEquals("ATTENDED", reg.getAttendanceStatus());
    }

    @Test
    public void test05_UnregisterEvent() {
        Event event = eventDAO.getAllEvents().stream()
                .filter(e -> e.getName().equals("P1 Test Event"))
                .findFirst().orElse(null);
        assertNotNull(event);

        assertTrue(eventDAO.unregisterStudent(event.getId(), studentProfileId), "Unregistration success");
        assertFalse(eventDAO.isStudentRegistered(event.getId(), studentProfileId), "Should not be registered");
    }

    @Test
    public void test06_DeleteEvent() {
        Event event = eventDAO.getAllEvents().stream()
                .filter(e -> e.getName().equals("P1 Test Event"))
                .findFirst().orElse(null);
        assertNotNull(event);

        assertTrue(eventDAO.deleteEvent(event.getId()), "Delete event success");

        Event deleted = eventDAO.getAllEvents().stream()
                .filter(e -> e.getId() == event.getId())
                .findFirst().orElse(null);
        assertNull(deleted, "Event should be gone");
    }

    // --- TEST SUITE 2: CLUB MANAGEMENT ---

    @Test
    public void test07_CreateClub() {
        Club club = new Club();
        club.setName("P1 Test Club");
        club.setDescription("Desc");
        club.setCategory("TECHNICAL");
        club.setFacultyCoordinatorId(facultyProfileId); // Use Faculty PROFILE ID
        club.setPresidentStudentId(studentProfileId); // Use Student PROFILE ID
        club.setStatus("ACTIVE");

        assertTrue(clubDAO.createClub(club), "Club creation success");

        List<Club> clubs = clubDAO.getAllClubs();
        assertTrue(clubs.stream().anyMatch(c -> c.getName().equals("P1 Test Club")), "Club found");
    }

    @Test
    public void test08_EditClub() {
        Club club = clubDAO.getAllClubs().stream()
                .filter(c -> c.getName().equals("P1 Test Club"))
                .findFirst().orElse(null);
        assertNotNull(club);

        club.setDescription("Updated Desc");
        assertTrue(clubDAO.updateClub(club), "Club update success");

        Club updated = clubDAO.getAllClubs().stream()
                .filter(c -> c.getId() == club.getId())
                .findFirst().orElse(null);
        assertEquals("Updated Desc", updated.getDescription());
    }

    @Test
    public void test09_ClubJoinWorkflow() {
        Club club = clubDAO.getAllClubs().stream()
                .filter(c -> c.getName().equals("P1 Test Club"))
                .findFirst().orElse(null);
        assertNotNull(club);

        // 1. Request Join (using Student PROFILE ID)
        assertTrue(clubDAO.joinClub(club.getId(), studentProfileId), "Join request success");

        // 2. Verify PENDING
        List<ClubMembership> members = clubDAO.getPendingMemberships(club.getId());
        ClubMembership member = members.stream()
                .filter(m -> m.getStudentId() == studentProfileId)
                .findFirst().orElse(null);
        assertNotNull(member);
        assertEquals("PENDING", member.getStatus());

        // 3. Approve
        assertTrue(clubDAO.approveMembership(member.getId()), "Approve success");

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
    public void test10_RemoveMember() {
        Club club = clubDAO.getAllClubs().stream()
                .filter(c -> c.getName().equals("P1 Test Club"))
                .findFirst().orElse(null);
        assertNotNull(club);

        assertTrue(clubDAO.leaveClub(club.getId(), studentProfileId), "Leave club success");

        List<ClubMembership> members = clubDAO.getClubMembers(club.getId());
        boolean exists = members.stream().anyMatch(m -> m.getStudentId() == studentProfileId);
        assertFalse(exists, "Member should be removed");
    }

    @Test
    public void test11_DeleteClub() {
        Club club = clubDAO.getAllClubs().stream()
                .filter(c -> c.getName().equals("P1 Test Club"))
                .findFirst().orElse(null);
        assertNotNull(club);

        assertTrue(clubDAO.deleteClub(club.getId()), "Delete club success");

        Club deleted = clubDAO.getAllClubs().stream()
                .filter(c -> c.getId() == club.getId())
                .findFirst().orElse(null);
        assertNull(deleted, "Club should be gone");
    }
}
