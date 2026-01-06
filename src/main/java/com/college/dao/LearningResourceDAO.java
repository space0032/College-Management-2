package com.college.dao;

import com.college.models.LearningResource;
import com.college.models.ResourceCategory;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LearningResourceDAO {

    public boolean addResource(LearningResource resource) {
        String sql = "INSERT INTO learning_resources (title, description, course_id, category_id, file_path, file_type, file_size, uploaded_by, is_public) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resource.getTitle());
            pstmt.setString(2, resource.getDescription());
            if (resource.getCourseId() > 0) {
                pstmt.setInt(3, resource.getCourseId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setInt(4, resource.getCategoryId());
            pstmt.setString(5, resource.getFilePath());
            pstmt.setString(6, resource.getFileType());
            pstmt.setLong(7, resource.getFileSize());
            pstmt.setInt(8, resource.getUploadedBy());
            pstmt.setBoolean(9, resource.isPublic());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to add learning resource", e);
            return false;
        }
    }

    public List<LearningResource> getResourcesByCourse(int courseId) {
        String sql = "SELECT r.*, c.name as category_name, u.username as uploader_name, co.name as course_name " +
                "FROM learning_resources r " +
                "JOIN resource_categories c ON r.category_id = c.id " +
                "JOIN users u ON r.uploaded_by = u.id " +
                "LEFT JOIN courses co ON r.course_id = co.id " +
                "WHERE r.course_id = ? OR r.is_public = TRUE " +
                "ORDER BY r.created_at DESC";
        return fetchResources(sql, courseId);
    }

    public List<LearningResource> getAllResources() {
        String sql = "SELECT r.*, c.name as category_name, u.username as uploader_name, co.name as course_name " +
                "FROM learning_resources r " +
                "JOIN resource_categories c ON r.category_id = c.id " +
                "JOIN users u ON r.uploaded_by = u.id " +
                "LEFT JOIN courses co ON r.course_id = co.id " +
                "ORDER BY r.created_at DESC";
        return fetchResources(sql, null);
    }

    private List<LearningResource> fetchResources(String sql, Integer param) {
        List<LearningResource> resources = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (param != null) {
                pstmt.setInt(1, param);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                resources.add(mapResultSetToResource(rs));
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch resources", e);
        }
        return resources;
    }

    public List<ResourceCategory> getAllCategories() {
        List<ResourceCategory> categories = new ArrayList<>();
        String sql = "SELECT * FROM resource_categories ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(new ResourceCategory(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")));
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch resource categories", e);
        }
        return categories;
    }

    public boolean deleteResource(int id) {
        String sql = "DELETE FROM learning_resources WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to delete resource", e);
            return false;
        }
    }

    public void incrementDownloadCount(int id) {
        String sql = "UPDATE learning_resources SET download_count = download_count + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Failed to increment download count", e);
        }
    }

    private LearningResource mapResultSetToResource(ResultSet rs) throws SQLException {
        LearningResource r = new LearningResource();
        r.setId(rs.getInt("id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setCourseId(rs.getInt("course_id"));
        r.setCategoryId(rs.getInt("category_id"));
        r.setFilePath(rs.getString("file_path"));
        r.setFileType(rs.getString("file_type"));
        r.setFileSize(rs.getLong("file_size"));
        r.setUploadedBy(rs.getInt("uploaded_by"));
        r.setDownloadCount(rs.getInt("download_count"));
        r.setPublic(rs.getBoolean("is_public"));
        r.setUploadedAt(rs.getTimestamp("created_at").toLocalDateTime());

        r.setCategoryName(rs.getString("category_name"));
        r.setUploaderName(rs.getString("uploader_name"));
        r.setCourseName(rs.getString("course_name"));

        return r;
    }
}
