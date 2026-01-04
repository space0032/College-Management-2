package com.college.dao;

import com.college.models.Student;
import com.college.utils.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EnrollmentDAOTest {

    @Mock
    private Connection mockConn;
    @Mock
    private PreparedStatement mockPstmt;
    @Mock
    private ResultSet mockRs;

    private EnrollmentDAO enrollmentDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        enrollmentDAO = new EnrollmentDAO();
    }

    @Test
    void testEnrollStudentSuccess() throws SQLException {
        // Mock static DatabaseConnection
        try (MockedStatic<DatabaseConnection> dbMock = Mockito.mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            // Mock PreparedStatements (both signatures)
            when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPstmt);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPstmt);

            // Mock standard methods
            when(mockPstmt.executeUpdate()).thenReturn(1);
            when(mockPstmt.executeQuery()).thenReturn(mockRs);
            when(mockPstmt.getGeneratedKeys()).thenReturn(mockRs);

            // Mock Results
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getInt(1)).thenReturn(101).thenReturn(202); // Generated Keys
            when(mockRs.getInt("count")).thenReturn(5); // For enrollment generation

            // Prepare Input
            Student student = new Student();
            student.setName("Test Student");
            student.setDepartment("CS");
            student.setEnrollmentDate(new Date());

            // Execute
            Student result = enrollmentDAO.enrollStudent(student, "password123");

            // Verify
            assertNotNull(result);
            assertEquals(101, result.getUserId());
            assertEquals(202, result.getId());
            assertNotNull(result.getUsername());

            // Verify Transaction Commit
            verify(mockConn).setAutoCommit(false);
            verify(mockConn).commit();
            verify(mockConn).setAutoCommit(true);
            verify(mockConn, never()).rollback();
        }
    }

    @Test
    void testEnrollStudentUserCreationFails() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = Mockito.mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            // Mock PreparedStatement for SELECT (Generator) and INSERT (User)
            when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPstmt);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPstmt);

            when(mockPstmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getInt("count")).thenReturn(5);

            // User creation fails (returns 0 rows)
            when(mockPstmt.executeUpdate()).thenReturn(0);

            Student student = new Student();
            student.setName("Fail Student");
            student.setDepartment("CS");
            student.setEnrollmentDate(new Date());

            Student result = enrollmentDAO.enrollStudent(student, "pass");

            assertNull(result);

            // Verify Rollback
            verify(mockConn).rollback();
        }
    }
}
