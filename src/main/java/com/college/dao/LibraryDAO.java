package com.college.dao;

import com.college.models.Book;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Library (Books)
 */
public class LibraryDAO {

    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (title, author, isbn, quantity, available) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setInt(4, book.getQuantity());
            pstmt.setInt(5, book.getAvailable());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title=?, author=?, isbn=?, quantity=?, available=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setInt(4, book.getQuantity());
            pstmt.setInt(5, book.getAvailable());
            pstmt.setInt(6, book.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public List<Book> getAllBooks() {
        return getAllBooks(0, Integer.MAX_VALUE);
    }

    public List<Book> getAllBooks(int page, int size) {
        return getAllBooks(page, size, null);
    }

    public List<Book> getAllBooks(int page, int size, String search) {
        List<Book> books = new ArrayList<>();
        String sql;
        if (search != null && !search.trim().isEmpty()) {
            sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR isbn LIKE ? ORDER BY title LIMIT ? OFFSET ?";
        } else {
            sql = "SELECT * FROM books ORDER BY title LIMIT ? OFFSET ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int idx = 1;
            if (search != null && !search.trim().isEmpty()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                pstmt.setString(idx++, like);
                pstmt.setString(idx++, like);
                pstmt.setString(idx++, like);
            }
            pstmt.setInt(idx++, size);
            pstmt.setInt(idx, page * size);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                books.add(extractBookFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return books;
    }

    public int getTotalBookCount() {
        return getTotalBookCount(null);
    }

    public int getTotalBookCount(String search) {
        String sql;
        if (search != null && !search.trim().isEmpty()) {
            sql = "SELECT COUNT(*) FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR isbn LIKE ?";
        } else {
            sql = "SELECT COUNT(*) FROM books";
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (search != null && !search.trim().isEmpty()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                pstmt.setString(1, like);
                pstmt.setString(2, like);
                pstmt.setString(3, like);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return 0;
    }

    private Book extractBookFromResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setQuantity(rs.getInt("quantity"));
        book.setAvailable(rs.getInt("available"));
        return book;
    }

    public boolean deleteBook(int id) {
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }
}
