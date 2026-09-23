package com.college.dao;

import com.college.models.DisciplinaryIncident;
import com.college.models.GrievanceTicket;
import com.college.models.ParentCommunication;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the Student Affairs admin module
 * (disciplinary incidents, grievance tickets, parent communications).
 */
public class StudentAffairsDAO {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static String toDay(java.util.Date date) {
        if (date == null) return null;
        return LocalDate.from(date.toInstant().atZone(ZoneId.systemDefault())).format(DAY);
    }

    // ─── Disciplinary incidents ───────────────────────────────────────────────

    public List<DisciplinaryIncident> getAllIncidents() {
        List<DisciplinaryIncident> list = new ArrayList<>();
        String sql = "SELECT i.*, s.name AS student_name, u.username AS enrollment_id " +
                "FROM affairs_incidents i " +
                "JOIN students s ON i.student_id = s.id " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "ORDER BY i.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                DisciplinaryIncident i = new DisciplinaryIncident();
                i.setId(rs.getInt("id"));
                i.setStudentId(rs.getInt("student_id"));
                i.setStudent(rs.getString("student_name"));
                i.setEnrollNo(rs.getString("enrollment_id"));
                i.setDate(toDay(rs.getDate("incident_date")));
                i.setType(rs.getString("incident_type"));
                i.setSeverity(rs.getString("severity"));
                i.setAction(rs.getString("action_taken"));
                i.setStatus(rs.getString("status"));
                list.add(i);
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch disciplinary incidents", e);
        }
        return list;
    }

    public boolean createIncident(DisciplinaryIncident incident, int reportedBy) {
        String sql = "INSERT INTO affairs_incidents (student_id, incident_type, action_taken, severity, status, incident_date, reported_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, incident.getStudentId());
            stmt.setString(2, incident.getType());
            stmt.setString(3, incident.getAction());
            stmt.setString(4, incident.getSeverity() == null ? "Low" : incident.getSeverity());
            stmt.setString(5, incident.getStatus() == null ? "Under Review" : incident.getStatus());
            stmt.setDate(6, incident.getDate() != null
                    ? java.sql.Date.valueOf(LocalDate.parse(incident.getDate()))
                    : java.sql.Date.valueOf(LocalDate.now()));
            if (reportedBy > 0) {
                stmt.setInt(7, reportedBy);
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to create disciplinary incident", e);
            return false;
        }
    }

    public boolean updateIncident(int id, String status, String action) {
        boolean hasStatus = status != null;
        boolean hasAction = action != null;
        if (!hasStatus && !hasAction) return false;
        String sql = "UPDATE affairs_incidents SET status = COALESCE(?, status), action_taken = COALESCE(?, action_taken), "
                + "resolved_at = CASE WHEN ? IN ('Resolved','Closed') THEN CURRENT_TIMESTAMP ELSE resolved_at END WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, action);
            stmt.setString(3, status);
            stmt.setInt(4, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to update disciplinary incident", e);
            return false;
        }
    }

    public boolean deleteIncident(int id) {
        String sql = "DELETE FROM affairs_incidents WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to delete disciplinary incident", e);
            return false;
        }
    }

    // ─── Grievance tickets ────────────────────────────────────────────────────

    public List<GrievanceTicket> getAllGrievances() {
        List<GrievanceTicket> list = new ArrayList<>();
        String sql = "SELECT g.*, s.name AS student_name, u.username AS enrollment_id " +
                "FROM affairs_grievances g " +
                "LEFT JOIN students s ON g.student_id = s.id " +
                "LEFT JOIN users u ON s.user_id = u.id " +
                "ORDER BY g.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                GrievanceTicket g = new GrievanceTicket();
                g.setId(rs.getInt("id"));
                g.setStudentId(rs.getObject("student_id") != null ? rs.getInt("student_id") : null);
                g.setCategory(rs.getString("category"));
                g.setTitle(rs.getString("title"));
                g.setDescription(rs.getString("description"));
                g.setReporter(rs.getBoolean("is_anonymous")
                        ? "Anonymous"
                        : rs.getString("reporter") != null ? rs.getString("reporter") : rs.getString("student_name"));
                g.setDate(toDay(rs.getTimestamp("created_at")));
                g.setPriority(rs.getString("priority"));
                g.setStatus(rs.getString("status"));
                list.add(g);
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch grievance tickets", e);
        }
        return list;
    }

    public boolean createGrievance(GrievanceTicket ticket) {
        String sql = "INSERT INTO affairs_grievances (student_id, category, title, description, reporter, priority, status, is_anonymous) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (ticket.getStudentId() != null) {
                stmt.setInt(1, ticket.getStudentId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, ticket.getCategory() == null ? "Infrastructure" : ticket.getCategory());
            stmt.setString(3, ticket.getTitle());
            stmt.setString(4, ticket.getDescription());
            stmt.setString(5, ticket.getReporter());
            stmt.setString(6, ticket.getPriority() == null ? "Medium" : ticket.getPriority());
            stmt.setString(7, "Open");
            stmt.setBoolean(8, "Anonymous".equalsIgnoreCase(ticket.getReporter()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to create grievance ticket", e);
            return false;
        }
    }

    public boolean updateGrievanceStatus(int id, String status) {
        String sql = "UPDATE affairs_grievances SET status = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setTimestamp(2, Timestamp.from(Instant.now()));
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to update grievance ticket", e);
            return false;
        }
    }

    // ─── Parent communications ────────────────────────────────────────────────

    public List<ParentCommunication> getAllCommunications() {
        List<ParentCommunication> list = new ArrayList<>();
        String sql = "SELECT c.*, u.username AS sent_by_username " +
                "FROM affairs_communications c " +
                "LEFT JOIN users u ON c.sent_by = u.id " +
                "ORDER BY c.sent_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ParentCommunication c = new ParentCommunication();
                c.setId(rs.getInt("id"));
                c.setSubject(rs.getString("subject"));
                c.setRecipient(rs.getString("recipient"));
                c.setChannel(rs.getString("channel"));
                c.setMessage(rs.getString("message"));
                c.setDate(toDay(rs.getTimestamp("sent_at")));
                c.setSentBy(rs.getString("sent_by_username"));
                list.add(c);
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch parent communications", e);
        }
        return list;
    }

    public boolean createCommunication(ParentCommunication comm, int sentBy) {
        String sql = "INSERT INTO affairs_communications (subject, recipient, channel, message, sent_by) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, comm.getSubject());
            stmt.setString(2, comm.getRecipient());
            stmt.setString(3, comm.getChannel());
            stmt.setString(4, comm.getMessage());
            if (sentBy > 0) {
                stmt.setInt(5, sentBy);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to create parent communication", e);
            return false;
        }
    }
}