package com.college.dao;

import com.college.models.BookRequest;
import com.college.models.BookIssue;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

/**
 * DAO for Book Request operations
 */
public class BookRequestDAO {

    /**
     * Create a new book request
     */
    public boolean createRequest(BookRequest request) {
        String sql = "INSERT INTO book_requests (student_id, book_id, loan_period_days, remarks) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, request.getStudentId());
            pstmt.setInt(2, request.getBookId());
            pstmt.setInt(3, request.getLoanPeriodDays());
            pstmt.setString(4, request.getRemarks());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Get all pending requests
     */
    public List<BookRequest> getPendingRequests() {
        String sql = "SELECT br.*, s.name as student_name, b.title as book_title, b.author as book_author " +
                "FROM book_requests br " +
                "JOIN students s ON br.student_id = s.id " +
                "JOIN books b ON br.book_id = b.id " +
                "WHERE br.status = 'PENDING' " +
                "ORDER BY br.request_date ASC";

        List<BookRequest> requests = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return requests;
    }

    /**
     * Get requests by student
     */
    public List<BookRequest> getRequestsByStudent(int studentId) {
        String sql = "SELECT br.*, s.name as student_name, b.title as book_title, b.author as book_author " +
                "FROM book_requests br " +
                "JOIN students s ON br.student_id = s.id " +
                "JOIN books b ON br.book_id = b.id " +
                "WHERE br.student_id = ? " +
                "ORDER BY br.request_date DESC";

        List<BookRequest> requests = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return requests;
    }

    /**
     * Approve request and auto-issue book
     */
    public boolean approveRequest(int requestId, int approvedBy) {
        // Get request details first
        BookRequest request = getRequestById(requestId);
        if (request == null) {
            return false;
        }

        // Check if book is available
        BookIssueDAO bookIssueDAO = new BookIssueDAO();
        if (!bookIssueDAO.isBookAvailable(request.getBookId())) {
            return false;
        }

        String updateSql = "UPDATE book_requests SET status = 'APPROVED', approved_by = ?, " +
                "approved_date = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            pstmt.setInt(1, approvedBy);
            pstmt.setInt(2, requestId);

            if (pstmt.executeUpdate() > 0) {
                // Auto-issue the book
                java.util.Date issueDate = new java.util.Date();
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, request.getLoanPeriodDays());
                java.util.Date dueDate = cal.getTime();

                BookIssue issue = new BookIssue(request.getStudentId(), request.getBookId(),
                        issueDate, dueDate);
                issue.setIssuedBy(approvedBy);

                return bookIssueDAO.issueBook(issue);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return false;
    }

    /**
     * Reject request
     */
    public boolean rejectRequest(int requestId, int rejectedBy, String remarks) {
        String sql = "UPDATE book_requests SET status = 'REJECTED', approved_by = ?, " +
                "approved_date = CURRENT_TIMESTAMP, remarks = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rejectedBy);
            pstmt.setString(2, remarks);
            pstmt.setInt(3, requestId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Get request by ID
     */
    private BookRequest getRequestById(int id) {
        String sql = "SELECT br.*, s.name as student_name, b.title as book_title, b.author as book_author " +
                "FROM book_requests br " +
                "JOIN students s ON br.student_id = s.id " +
                "JOIN books b ON br.book_id = b.id " +
                "WHERE br.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractRequestFromResultSet(rs);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return null;
    }

    /**
     * Extract BookRequest from ResultSet
     */
    private BookRequest extractRequestFromResultSet(ResultSet rs) throws SQLException {
        BookRequest request = new BookRequest();
        request.setId(rs.getInt("id"));
        request.setStudentId(rs.getInt("student_id"));
        request.setBookId(rs.getInt("book_id"));
        request.setRequestDate(rs.getTimestamp("request_date"));
        request.setStatus(rs.getString("status"));

        int approvedBy = rs.getInt("approved_by");
        request.setApprovedBy(rs.wasNull() ? null : approvedBy);

        request.setApprovedDate(rs.getTimestamp("approved_date"));
        request.setRemarks(rs.getString("remarks"));
        request.setLoanPeriodDays(rs.getInt("loan_period_days"));

        // Display fields
        request.setStudentName(rs.getString("student_name"));
        request.setBookTitle(rs.getString("book_title"));
        request.setBookAuthor(rs.getString("book_author"));

        return request;
    }

    /**
     * Get pending request count
     */
    public int getPendingRequestCount() {
        String sql = "SELECT COUNT(*) as count FROM book_requests WHERE status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        return 0;
    }
}
