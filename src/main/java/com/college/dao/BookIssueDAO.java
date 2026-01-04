package com.college.dao;

import com.college.models.BookIssue;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for BookIssue entity
 */
public class BookIssueDAO {

    private static final double FINE_PER_DAY = 5.0; // Rs. 5 per day

    /**
     * Issue a book to a student
     */
    public boolean issueBook(BookIssue issue) {
        String sql = "INSERT INTO book_issues (student_id, book_id, issue_date, due_date, status, issued_by) " +
                "VALUES (?, ?, ?, ?, 'ISSUED', ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, issue.getStudentId());
            pstmt.setInt(2, issue.getBookId());
            pstmt.setDate(3, new java.sql.Date(issue.getIssueDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(issue.getDueDate().getTime()));
            pstmt.setInt(5, issue.getIssuedBy());

            int result = pstmt.executeUpdate();

            if (result > 0) {
                // Update book availability
                updateBookAvailability(issue.getBookId(), -1);
                return true;
            }
            return false;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Return a book from student
     */
    public boolean returnBook(int issueId, int returnedTo) {
        String sql = "UPDATE book_issues SET return_date = CURDATE(), returned_to = ?, " +
                "fine_amount = ?, status = 'RETURNED' WHERE id = ?";

        // Get issue details to calculate fine BEFORE opening connection
        BookIssue issue = getIssueById(issueId);
        if (issue == null) {
            return false;
        }

        double fine = issue.calculateFine(FINE_PER_DAY);

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, returnedTo);
            pstmt.setDouble(2, fine);
            pstmt.setInt(3, issueId);

            int result = pstmt.executeUpdate();

            if (result > 0) {
                // Update book availability
                updateBookAvailability(issue.getBookId(), 1);
                return true;
            }
            return false;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Get all issued books (not returned)
     */
    public List<BookIssue> getAllIssuedBooks() {
        List<BookIssue> issues = new ArrayList<>();
        String sql = "SELECT bi.*, s.name as student_name, b.title as book_title " +
                "FROM book_issues bi " +
                "JOIN students s ON bi.student_id = s.id " +
                "JOIN books b ON bi.book_id = b.id " +
                "WHERE bi.status = 'ISSUED' ORDER BY bi.due_date";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                issues.add(extractIssueFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return issues;
    }

    /**
     * Get books issued to a specific student
     */
    public List<BookIssue> getIssuedBooksByStudent(int studentId) {
        List<BookIssue> issues = new ArrayList<>();
        String sql = "SELECT bi.*, b.title as book_title " +
                "FROM book_issues bi " +
                "JOIN books b ON bi.book_id = b.id " +
                "WHERE bi.student_id = ? AND bi.status = 'ISSUED' " +
                "ORDER BY bi.due_date";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                issues.add(extractIssueFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return issues;
    }

    /**
     * Get issue by ID
     */
    public BookIssue getIssueById(int id) {
        String sql = "SELECT bi.*, s.name as student_name, b.title as book_title " +
                "FROM book_issues bi " +
                "JOIN students s ON bi.student_id = s.id " +
                "JOIN books b ON bi.book_id = b.id " +
                "WHERE bi.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractIssueFromResultSet(rs);
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return null;
    }

    /**
     * Update book availability count
     */
    private void updateBookAvailability(int bookId, int change) {
        String sql = "UPDATE books SET available = available + ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, change);
            pstmt.setInt(2, bookId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
    }

    /**
     * Check if book is available
     */
    public boolean isBookAvailable(int bookId) {
        String sql = "SELECT available FROM books WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("available") > 0;
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return false;
    }

    /**
     * Extract BookIssue from ResultSet
     */
    private BookIssue extractIssueFromResultSet(ResultSet rs) throws SQLException {
        BookIssue issue = new BookIssue();
        issue.setId(rs.getInt("id"));
        issue.setStudentId(rs.getInt("student_id"));
        issue.setBookId(rs.getInt("book_id"));
        issue.setIssueDate(rs.getDate("issue_date"));
        issue.setDueDate(rs.getDate("due_date"));
        issue.setReturnDate(rs.getDate("return_date"));
        issue.setFineAmount(rs.getDouble("fine_amount"));
        issue.setStatus(rs.getString("status"));

        try {
            issue.setStudentName(rs.getString("student_name"));
            issue.setBookTitle(rs.getString("book_title"));
        } catch (SQLException e) {
            // Fields might not be in result set
        }

        return issue;
    }
}
