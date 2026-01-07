package com.college.dao;

import com.college.models.Notification;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public boolean createNotification(Notification note) {
        String sql = "INSERT INTO notifications (recipient_user_id, recipient_contact, type, subject, message, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, note.getRecipientUserId());
            pstmt.setString(2, note.getRecipientContact());
            pstmt.setString(3, note.getType().name());
            pstmt.setString(4, note.getSubject());
            pstmt.setString(5, note.getMessage());
            pstmt.setString(6, note.getStatus().name());
            pstmt.setTimestamp(7, Timestamp.valueOf(note.getCreatedAt()));

            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public List<Notification> getPendingNotifications(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE status = 'PENDING' AND recipient_user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return list;
    }

    public List<Notification> getPendingNotifications() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return list;
    }

    public void updateStatus(int id, Notification.Status status) {
        String sql = "UPDATE notifications SET status = ?, sent_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setTimestamp(2,
                    status == Notification.Status.SENT ? new Timestamp(System.currentTimeMillis()) : null);
            pstmt.setInt(3, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setRecipientUserId(rs.getInt("recipient_user_id"));
        n.setRecipientContact(rs.getString("recipient_contact"));
        n.setType(Notification.Type.valueOf(rs.getString("type")));
        n.setSubject(rs.getString("subject"));
        n.setMessage(rs.getString("message"));
        n.setStatus(Notification.Status.valueOf(rs.getString("status")));
        n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("sent_at") != null) {
            n.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
        }
        return n;
    }
}
