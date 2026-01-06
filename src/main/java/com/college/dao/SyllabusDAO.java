package com.college.dao;

import com.college.models.Syllabus;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SyllabusDAO {

    public boolean addSyllabus(Syllabus syllabus) {
        String sql = "INSERT INTO course_syllabi (course_id, title, file_path, version, description, uploaded_by) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, syllabus.getCourseId());
            pstmt.setString(2, syllabus.getTitle());
            pstmt.setString(3, syllabus.getFilePath());
            pstmt.setString(4, syllabus.getVersion());
            pstmt.setString(5, syllabus.getDescription());
            pstmt.setInt(6, syllabus.getUploadedBy());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to add syllabus", e);
            return false;
        }
    }

    public List<Syllabus> getSyllabiByCourse(int courseId) {
        List<Syllabus> syllabi = new ArrayList<>();
        String sql = "SELECT s.*, u.username as uploader_name FROM course_syllabi s " +
                "JOIN users u ON s.uploaded_by = u.id " +
                "WHERE s.course_id = ? ORDER BY s.version DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Syllabus s = mapResultSetToSyllabus(rs);
                syllabi.add(s);
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch syllabi for course " + courseId, e);
        }
        return syllabi;
    }

    public boolean deleteSyllabus(int id) {
        String sql = "DELETE FROM course_syllabi WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to delete syllabus", e);
            return false;
        }
    }

    private Syllabus mapResultSetToSyllabus(ResultSet rs) throws SQLException {
        Syllabus s = new Syllabus();
        s.setId(rs.getInt("id"));
        s.setCourseId(rs.getInt("course_id"));
        s.setTitle(rs.getString("title"));
        s.setFilePath(rs.getString("file_path"));
        s.setVersion(rs.getString("version"));
        s.setDescription(rs.getString("description"));
        s.setUploadedBy(rs.getInt("uploaded_by"));
        s.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
        s.setUploaderName(rs.getString("uploader_name"));
        return s;
    }
}
