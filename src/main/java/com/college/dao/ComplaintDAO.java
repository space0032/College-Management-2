package com.college.dao;

import com.college.models.Complaint;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAO {

    public boolean createComplaint(Complaint c) {
        String sql = "INSERT INTO hostel_complaints (student_id, title, description, category, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, c.getStudentId());
            pstmt.setString(2, c.getTitle());
            pstmt.setString(3, c.getDescription());
            pstmt.setString(4, c.getCategory());
            pstmt.setString(5, "OPEN");

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error creating complaint", e);
            return false;
        }
    }

    public boolean updateStatus(int id, String status, int resolvedBy, String remarks) {
        String sql = "UPDATE hostel_complaints SET status = ?, resolved_by = ?, remarks = ?, resolved_date = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, resolvedBy);
            pstmt.setString(3, remarks);
            pstmt.setInt(4, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error updating complaint status", e);
            return false;
        }
    }

    public List<Complaint> getComplaintsByStudent(int studentId) {
        String sql = "SELECT c.*, u.username as resolved_by_name " +
                "FROM hostel_complaints c " +
                "LEFT JOIN users u ON c.resolved_by = u.id " +
                "WHERE c.student_id = ? ORDER BY c.filed_date DESC";
        List<Complaint> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractComplaint(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching student complaints", e);
        }
        return list;
    }

    public List<Complaint> getAllComplaints() {
        // Simple join to get student name
        String sql = "SELECT c.*, s.name as student_name, u.username as resolved_by_name " +
                "FROM hostel_complaints c " +
                "JOIN students s ON c.student_id = s.id " +
                "LEFT JOIN users u ON c.resolved_by = u.id " +
                "ORDER BY CASE WHEN c.status = 'OPEN' THEN 1 ELSE 2 END, c.filed_date DESC";
        List<Complaint> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Complaint c = extractComplaint(rs);
                c.setStudentName(rs.getString("student_name"));
                list.add(c);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching all complaints", e);
        }
        return list;
    }

    private Complaint extractComplaint(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getInt("id"));
        c.setStudentId(rs.getInt("student_id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setCategory(rs.getString("category"));
        c.setStatus(rs.getString("status"));
        c.setFiledDate(rs.getTimestamp("filed_date"));
        c.setResolvedDate(rs.getTimestamp("resolved_date"));
        c.setResolvedBy(rs.getObject("resolved_by") != null ? rs.getInt("resolved_by") : null);
        c.setRemarks(rs.getString("remarks"));
        if (hasColumn(rs, "resolved_by_name")) {
            c.setResolvedByName(rs.getString("resolved_by_name"));
        }
        return c;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnLabel(x))) {
                return true;
            }
        }
        return false;
    }
}
