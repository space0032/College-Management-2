package com.college.dao;

import com.college.models.Visitor;
import com.college.models.VisitorLog;
import com.college.utils.DatabaseConnection;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

public class VisitorDAO {

    // --- Visitor Methods ---

    public Visitor getVisitorByPhone(String phone) {
        String sql = "SELECT * FROM visitors WHERE phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapVisitor(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Not found
    }

    public int addVisitor(Visitor visitor) {
        String sql = "INSERT INTO visitors (name, phone, email, id_proof_type, id_proof_number) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, visitor.getName());
            stmt.setString(2, visitor.getPhone());
            stmt.setString(3, visitor.getEmail());
            stmt.setString(4, visitor.getIdProofType());
            stmt.setString(5, visitor.getIdProofNumber());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // --- Log Methods ---

    public void logEntry(int visitorId, String purpose, String personToMeet, String gateNumber) {
        String sql = "INSERT INTO visitor_logs (visitor_id, purpose, person_to_meet, gate_number, status) VALUES (?, ?, ?, ?, 'IN')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, visitorId);
            stmt.setString(2, purpose);
            stmt.setString(3, personToMeet);
            stmt.setString(4, gateNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logExit(int logId) {
        String sql = "UPDATE visitor_logs SET exit_time = CURRENT_TIMESTAMP, status = 'OUT' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, logId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<VisitorLog> getActiveVisitors() {
        String sql = "SELECT vl.*, v.name as visitor_name, v.phone as visitor_phone " +
                "FROM visitor_logs vl " +
                "JOIN visitors v ON vl.visitor_id = v.id " +
                "WHERE vl.status = 'IN' " +
                "ORDER BY vl.entry_time DESC";
        return getLogs(sql);
    }

    public List<VisitorLog> getAllVisitorLogs() {
        String sql = "SELECT vl.*, v.name as visitor_name, v.phone as visitor_phone " +
                "FROM visitor_logs vl " +
                "JOIN visitors v ON vl.visitor_id = v.id " +
                "ORDER BY vl.entry_time DESC";
        return getLogs(sql);
    }

    private List<VisitorLog> getLogs(String sql) {
        List<VisitorLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(mapVisitorLog(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    private Visitor mapVisitor(ResultSet rs) throws SQLException {
        return new Visitor(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("id_proof_type"),
                rs.getString("id_proof_number"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    private VisitorLog mapVisitorLog(ResultSet rs) throws SQLException {
        Timestamp exitTs = rs.getTimestamp("exit_time");
        return new VisitorLog(
                rs.getInt("id"),
                rs.getInt("visitor_id"),
                rs.getString("visitor_name"),
                rs.getString("visitor_phone"),
                rs.getTimestamp("entry_time").toLocalDateTime(),
                exitTs != null ? exitTs.toLocalDateTime() : null,
                rs.getString("purpose"),
                rs.getString("person_to_meet"),
                rs.getString("gate_number"),
                rs.getString("status"));
    }
}
