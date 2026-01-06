package com.college.dao;

import com.college.models.ClubAnnouncement;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClubAnnouncementDAO {

    public List<ClubAnnouncement> getAnnouncementsByClub(int clubId) {
        List<ClubAnnouncement> announcements = new ArrayList<>();
        String sql = "SELECT ca.*, " +
                "COALESCE(s.name, f.name, 'Unknown') as poster_name " +
                "FROM club_announcements ca " +
                "LEFT JOIN students s ON ca.posted_by = s.id " + // Assuming posted_by matches user ID, but we usually
                                                                 // link to users table if generic, or specific role
                                                                 // tables. V33 says "User ID". So join users? Wait,
                                                                 // users table has username, but maybe we want display
                                                                 // name. Users table links to student/faculty.
                // Let's check Schema: posted_by REFERENCES users(id). Users usually have link
                // to role tables.
                // Simpler: Join users table to get username, or join student/faculty if we want
                // real name.
                // Let's use users table for now to be safe, or do a COALESCE on users.username.
                // Actually, let's fetch poster name via UserDAO or just display user ID if
                // complicated.
                // Better: Join users table.
                "LEFT JOIN users u ON ca.posted_by = u.id " +
                "WHERE ca.club_id = ? " +
                "ORDER BY ca.posted_at DESC";

        // Re-thinking: posted_by IS a user_id. Users table has `username`. Real names
        // are in `students` or `faculty`.
        // Ideally we join `users` -> `role` -> `students`/`faculty`.
        // For simplicity, let's just show "Club Admin" or fetch name separately.
        // Or simple join:
        sql = "SELECT ca.*, u.username as poster_name " +
                "FROM club_announcements ca " +
                "JOIN users u ON ca.posted_by = u.id " +
                "WHERE ca.club_id = ? " +
                "ORDER BY ca.posted_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clubId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                announcements.add(extract(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching announcements: " + e.getMessage());
        }
        return announcements;
    }

    public boolean createAnnouncement(ClubAnnouncement ann) {
        String sql = "INSERT INTO club_announcements (club_id, title, content, posted_by) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ann.getClubId());
            pstmt.setString(2, ann.getTitle());
            pstmt.setString(3, ann.getContent());
            pstmt.setInt(4, ann.getPostedBy());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error creating announcement: " + e.getMessage());
            return false;
        }
    }

    private ClubAnnouncement extract(ResultSet rs) throws SQLException {
        ClubAnnouncement ca = new ClubAnnouncement();
        ca.setId(rs.getInt("id"));
        ca.setClubId(rs.getInt("club_id"));
        ca.setTitle(rs.getString("title"));
        ca.setContent(rs.getString("content"));
        ca.setPostedBy(rs.getInt("posted_by"));
        ca.setPostedAt(rs.getTimestamp("posted_at"));
        ca.setPosterName(rs.getString("poster_name"));
        return ca;
    }
}
