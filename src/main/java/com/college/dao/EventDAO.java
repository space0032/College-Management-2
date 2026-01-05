package com.college.dao;

import com.college.models.Event;
import com.college.models.EventRegistration;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Event operations
 */
public class EventDAO {

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.*, " +
                "COALESCE(u.username, 'System') as creator_name, " +
                "COUNT(DISTINCT er.id) as registration_count " +
                "FROM events e " +
                "LEFT JOIN users u ON e.created_by = u.id " +
                "LEFT JOIN event_registrations er ON e.id = er.event_id " +
                "GROUP BY e.id " +
                "ORDER BY e.start_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                events.add(extractEvent(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching all events: " + e.getMessage());
        }
        return events;
    }

    public List<Event> getUpcomingEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.*, " +
                "COALESCE(u.username, 'System') as creator_name, " +
                "COUNT(DISTINCT er.id) as registration_count " +
                "FROM events e " +
                "LEFT JOIN users u ON e.created_by = u.id " +
                "LEFT JOIN event_registrations er ON e.id = er.event_id " +
                "WHERE e.status IN ('UPCOMING', 'ONGOING') " +
                "GROUP BY e.id " +
                "ORDER BY e.start_time ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                events.add(extractEvent(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching upcoming events: " + e.getMessage());
        }
        return events;
    }

    public Event getEventById(int id) {
        String sql = "SELECT e.*, " +
                "COALESCE(u.username, 'System') as creator_name, " +
                "COUNT(DISTINCT er.id) as registration_count " +
                "FROM events e " +
                "LEFT JOIN users u ON e.created_by = u.id " +
                "LEFT JOIN event_registrations er ON e.id = er.event_id " +
                "WHERE e.id = ? " +
                "GROUP BY e.id";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractEvent(rs);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching event by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean createEvent(Event event) {
        String sql = "INSERT INTO events (name, description, event_type, location, start_time, " +
                "end_time, max_participants, registration_deadline, created_by, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, event.getName());
            pstmt.setString(2, event.getDescription());
            pstmt.setString(3, event.getEventType());
            pstmt.setString(4, event.getLocation());
            pstmt.setTimestamp(5, new Timestamp(event.getStartTime().getTime()));
            pstmt.setTimestamp(6, new Timestamp(event.getEndTime().getTime()));

            if (event.getMaxParticipants() != null) {
                pstmt.setInt(7, event.getMaxParticipants());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            if (event.getRegistrationDeadline() != null) {
                pstmt.setTimestamp(8, new Timestamp(event.getRegistrationDeadline().getTime()));
            } else {
                pstmt.setNull(8, Types.TIMESTAMP);
            }

            pstmt.setInt(9, event.getCreatedBy());
            pstmt.setString(10, event.getStatus());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error creating event: " + e.getMessage());
            return false;
        }
    }

    public boolean updateEvent(Event event) {
        String sql = "UPDATE events SET name = ?, description = ?, event_type = ?, " +
                "location = ?, start_time = ?, end_time = ?, max_participants = ?, " +
                "registration_deadline = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, event.getName());
            pstmt.setString(2, event.getDescription());
            pstmt.setString(3, event.getEventType());
            pstmt.setString(4, event.getLocation());
            pstmt.setTimestamp(5, new Timestamp(event.getStartTime().getTime()));
            pstmt.setTimestamp(6, new Timestamp(event.getEndTime().getTime()));

            if (event.getMaxParticipants() != null) {
                pstmt.setInt(7, event.getMaxParticipants());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            if (event.getRegistrationDeadline() != null) {
                pstmt.setTimestamp(8, new Timestamp(event.getRegistrationDeadline().getTime()));
            } else {
                pstmt.setNull(8, Types.TIMESTAMP);
            }

            pstmt.setString(9, event.getStatus());
            pstmt.setInt(10, event.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error updating event: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteEvent(int id) {
        String sql = "DELETE FROM events WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error deleting event: " + e.getMessage());
            return false;
        }
    }

    // Registration methods
    public boolean registerStudent(int eventId, int studentId) {
        String sql = "INSERT INTO event_registrations (event_id, student_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            pstmt.setInt(2, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error registering student for event: " + e.getMessage());
            return false;
        }
    }

    public boolean unregisterStudent(int eventId, int studentId) {
        String sql = "DELETE FROM event_registrations WHERE event_id = ? AND student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            pstmt.setInt(2, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error unregistering student: " + e.getMessage());
            return false;
        }
    }

    public boolean isStudentRegistered(int eventId, int studentId) {
        String sql = "SELECT 1 FROM event_registrations WHERE event_id = ? AND student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            pstmt.setInt(2, studentId);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            Logger.error("Error checking registration: " + e.getMessage());
            return false;
        }
    }

    public List<EventRegistration> getEventRegistrations(int eventId) {
        List<EventRegistration> registrations = new ArrayList<>();
        String sql = "SELECT er.*, s.name as student_name " +
                "FROM event_registrations er " +
                "JOIN students s ON er.student_id = s.id " +
                "WHERE er.event_id = ? " +
                "ORDER BY er.registered_at";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EventRegistration reg = new EventRegistration();
                reg.setId(rs.getInt("id"));
                reg.setEventId(rs.getInt("event_id"));
                reg.setStudentId(rs.getInt("student_id"));
                reg.setRegisteredAt(rs.getTimestamp("registered_at"));
                reg.setAttendanceStatus(rs.getString("attendance_status"));
                reg.setStudentName(rs.getString("student_name"));
                registrations.add(reg);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching registrations: " + e.getMessage());
        }
        return registrations;
    }

    public boolean markAttendance(int registrationId, String status) {
        String sql = "UPDATE event_registrations SET attendance_status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, registrationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error marking attendance: " + e.getMessage());
            return false;
        }
    }

    public List<Event> getStudentRegisteredEvents(int studentId) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.*, " +
                "COALESCE(u.username, 'System') as creator_name, " +
                "COUNT(DISTINCT er2.id) as registration_count " +
                "FROM events e " +
                "JOIN event_registrations er ON e.id = er.event_id " +
                "LEFT JOIN users u ON e.created_by = u.id " +
                "LEFT JOIN event_registrations er2 ON e.id = er2.event_id " +
                "WHERE er.student_id = ? " +
                "GROUP BY e.id " +
                "ORDER BY e.start_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                events.add(extractEvent(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching student events: " + e.getMessage());
        }
        return events;
    }

    private Event extractEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setId(rs.getInt("id"));
        event.setName(rs.getString("name"));
        event.setDescription(rs.getString("description"));
        event.setEventType(rs.getString("event_type"));
        event.setLocation(rs.getString("location"));
        event.setStartTime(rs.getTimestamp("start_time"));
        event.setEndTime(rs.getTimestamp("end_time"));
        event.setMaxParticipants((Integer) rs.getObject("max_participants"));
        event.setRegistrationDeadline(rs.getTimestamp("registration_deadline"));
        event.setCreatedBy(rs.getInt("created_by"));
        event.setStatus(rs.getString("status"));
        event.setBannerPath(rs.getString("banner_path"));
        event.setCreatedAt(rs.getTimestamp("created_at"));
        event.setUpdatedAt(rs.getTimestamp("updated_at"));
        event.setCreatorName(rs.getString("creator_name"));
        event.setRegistrationCount(rs.getInt("registration_count"));
        return event;
    }
}
