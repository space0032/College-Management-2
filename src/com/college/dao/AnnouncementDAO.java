package com.college.dao;

import com.college.models.Announcement;
import com.college.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Announcement entity
 */
public class AnnouncementDAO {

    /**
     * Add a new announcement
     */
    public int addAnnouncement(Announcement announcement) {
        String sql = "INSERT INTO announcements (title, content, target_audience, priority, created_by, expires_at, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, announcement.getTitle());
            pstmt.setString(2, announcement.getContent());
            pstmt.setString(3, announcement.getTargetAudience());
            pstmt.setString(4, announcement.getPriority());
            pstmt.setInt(5, announcement.getCreatedBy());

            if (announcement.getExpiresAt() != null) {
                pstmt.setTimestamp(6, Timestamp.valueOf(announcement.getExpiresAt()));
            } else {
                pstmt.setNull(6, Types.TIMESTAMP);
            }

            pstmt.setBoolean(7, announcement.isActive());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Update an existing announcement
     */
    public boolean updateAnnouncement(Announcement announcement) {
        String sql = "UPDATE announcements SET title=?, content=?, target_audience=?, priority=?, expires_at=?, is_active=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, announcement.getTitle());
            pstmt.setString(2, announcement.getContent());
            pstmt.setString(3, announcement.getTargetAudience());
            pstmt.setString(4, announcement.getPriority());

            if (announcement.getExpiresAt() != null) {
                pstmt.setTimestamp(5, Timestamp.valueOf(announcement.getExpiresAt()));
            } else {
                pstmt.setNull(5, Types.TIMESTAMP);
            }

            pstmt.setBoolean(6, announcement.isActive());
            pstmt.setInt(7, announcement.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete an announcement
     */
    public boolean deleteAnnouncement(int id) {
        String sql = "DELETE FROM announcements WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get announcement by ID
     */
    public Announcement getAnnouncementById(int id) {
        String sql = "SELECT a.*, u.username as created_by_name FROM announcements a " +
                "LEFT JOIN users u ON a.created_by = u.id WHERE a.id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractAnnouncementFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get all announcements (for admin/faculty management view)
     */
    public List<Announcement> getAllAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "SELECT a.*, u.username as created_by_name FROM announcements a " +
                "LEFT JOIN users u ON a.created_by = u.id ORDER BY a.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                announcements.add(extractAnnouncementFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return announcements;
    }

    /**
     * Get active announcements for a specific role
     * 
     * @param role User role (STUDENT, FACULTY, etc.)
     * @return List of active announcements visible to that role
     */
    public List<Announcement> getActiveAnnouncements(String role) {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "SELECT a.*, u.username as created_by_name FROM announcements a " +
                "LEFT JOIN users u ON a.created_by = u.id " +
                "WHERE a.is_active = TRUE " +
                "AND (a.expires_at IS NULL OR a.expires_at > NOW()) " +
                "AND (a.target_audience = 'ALL' OR a.target_audience = ? OR a.target_audience = 'STUDENTS_FACULTY') " +
                "ORDER BY " +
                "  CASE a.priority " +
                "    WHEN 'URGENT' THEN 1 " +
                "    WHEN 'HIGH' THEN 2 " +
                "    WHEN 'NORMAL' THEN 3 " +
                "    WHEN 'LOW' THEN 4 " +
                "  END, " +
                "  a.created_at DESC " +
                "LIMIT 10";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Map role to target audience
            String targetAudience = role.equals("STUDENT") ? "STUDENTS" : "FACULTY";
            pstmt.setString(1, targetAudience);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                announcements.add(extractAnnouncementFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return announcements;
    }

    /**
     * Extract Announcement from ResultSet
     */
    private Announcement extractAnnouncementFromResultSet(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setId(rs.getInt("id"));
        announcement.setTitle(rs.getString("title"));
        announcement.setContent(rs.getString("content"));
        announcement.setTargetAudience(rs.getString("target_audience"));
        announcement.setPriority(rs.getString("priority"));
        announcement.setCreatedBy(rs.getInt("created_by"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            announcement.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) {
            announcement.setExpiresAt(expiresAt.toLocalDateTime());
        }

        announcement.setActive(rs.getBoolean("is_active"));

        try {
            announcement.setCreatedByName(rs.getString("created_by_name"));
        } catch (SQLException e) {
            // Column might not exist
        }

        return announcement;
    }
}
