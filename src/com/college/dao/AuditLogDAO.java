package com.college.dao;

import com.college.models.AuditLog;
import com.college.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditLogDAO - Data Access Object for Audit Logs
 * Handles all database operations for audit logging
 */
public class AuditLogDAO {

    /**
     * Log an action to the audit log
     */
    public static void logAction(int userId, String username, String action,
            String entityType, Integer entityId, String details) {
        String sql = "INSERT INTO audit_logs (user_id, username, action, entity_type, entity_id, details) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, action);
            pstmt.setString(4, entityType);
            if (entityId != null) {
                pstmt.setInt(5, entityId);
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            pstmt.setString(6, details);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            // Don't throw exceptions for audit logging - fail silently
            System.err.println("Audit log error: " + e.getMessage());
        }
    }

    /**
     * Get all audit logs
     */
    public static List<AuditLog> getAllLogs() {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 1000";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get logs by user ID
     */
    public static List<AuditLog> getLogsByUser(int userId) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY timestamp DESC LIMIT 500";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get logs by date range
     */
    public static List<AuditLog> getLogsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE DATE(timestamp) BETWEEN ? AND ? " +
                "ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get recent logs (last N entries)
     */
    public static List<AuditLog> getRecentLogs(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Map ResultSet to AuditLog object
     */
    private static AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));

        int entityId = rs.getInt("entity_id");
        log.setEntityId(rs.wasNull() ? null : entityId);

        log.setDetails(rs.getString("details"));

        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            log.setTimestamp(timestamp.toLocalDateTime());
        }

        return log;
    }

    /**
     * Delete old logs (cleanup - keep last 6 months)
     */
    public static int deleteOldLogs(int daysToKeep) {
        String sql = "DELETE FROM audit_logs WHERE timestamp < DATE_SUB(NOW(), INTERVAL ? DAY)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, daysToKeep);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
