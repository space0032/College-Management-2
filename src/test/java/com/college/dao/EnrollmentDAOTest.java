package com.college.dao;

import com.college.models.Role;
import com.college.models.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EnrollmentDAOTest {
    private Connection connection;
    private UserDAO userDAO;
    private StudentDAO studentDAO;
    private RoleDAO roleDAO;
    private EnhancedFeeDAO feeDAO;
    private CourseDAO courseDAO;
    private EnrollmentDAO enrollmentDAO;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        userDAO = mock(UserDAO.class);
        studentDAO = mock(StudentDAO.class);
        roleDAO = mock(RoleDAO.class);
        feeDAO = mock(EnhancedFeeDAO.class);
        courseDAO = mock(CourseDAO.class);
        Role studentRole = new Role();
        studentRole.setId(2);
        when(roleDAO.getRoleByCode(connection, "STUDENT")).thenReturn(studentRole);
        when(feeDAO.getAllCategories()).thenReturn(List.of());
        when(courseDAO.getCoreCourses(anyString(), anyInt())).thenReturn(List.of());
        enrollmentDAO = new EnrollmentDAO(userDAO, studentDAO, roleDAO, feeDAO, courseDAO,
                () -> connection, department -> "CS2026001");
    }

    @Test
    void enrollStudentCommitsSuccessfulTransaction() throws SQLException {
        when(userDAO.addUser(connection, "CS2026001", "password123", "STUDENT", 2)).thenReturn(101);
        when(studentDAO.addStudent(eq(connection), any(Student.class), eq(101))).thenReturn(202);

        Student result = enrollmentDAO.enrollStudent(student("Test Student"), "password123");

        assertNotNull(result);
        assertEquals(101, result.getUserId());
        assertEquals(202, result.getId());
        assertEquals("CS2026001", result.getUsername());
        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    void enrollStudentRollsBackWhenUserCreationFails() throws SQLException {
        when(userDAO.addUser(connection, "CS2026001", "pass", "STUDENT", 2)).thenReturn(-1);

        assertNull(enrollmentDAO.enrollStudent(student("Fail Student"), "pass"));

        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    private Student student(String name) {
        Student student = new Student();
        student.setName(name);
        student.setDepartment("CS");
        student.setSemester(1);
        student.setEnrollmentDate(new Date());
        return student;
    }
}
