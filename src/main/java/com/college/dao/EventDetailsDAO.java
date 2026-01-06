package com.college.dao;

import com.college.models.*;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDetailsDAO {

    // --- Collaborators ---
    public boolean addCollaborator(int eventId, int deptId) {
        String sql = "INSERT INTO event_collaborators (event_id, department_id, status) VALUES (?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.setInt(2, deptId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error adding collaborator: " + e.getMessage());
            return false;
        }
    }

    public List<EventCollaborator> getCollaborators(int eventId) {
        List<EventCollaborator> list = new ArrayList<>();
        String sql = "SELECT ec.*, d.name as dept_name " +
                "FROM event_collaborators ec " +
                "JOIN departments d ON ec.department_id = d.id " +
                "WHERE ec.event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EventCollaborator ec = new EventCollaborator();
                ec.setId(rs.getInt("id"));
                ec.setEventId(rs.getInt("event_id"));
                ec.setDepartmentId(rs.getInt("department_id"));
                ec.setStatus(rs.getString("status"));
                ec.setDepartmentName(rs.getString("dept_name"));
                list.add(ec);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching collaborators: " + e.getMessage());
        }
        return list;
    }

    // --- Resources ---
    public boolean addResource(EventResource res) {
        String sql = "INSERT INTO event_resources (event_id, resource_name, quantity, status) VALUES (?, ?, ?, 'REQUESTED')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, res.getEventId());
            pstmt.setString(2, res.getResourceName());
            pstmt.setInt(3, res.getQuantity());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error adding resource: " + e.getMessage());
            return false;
        }
    }

    public List<EventResource> getResources(int eventId) {
        List<EventResource> list = new ArrayList<>();
        String sql = "SELECT * FROM event_resources WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EventResource er = new EventResource();
                er.setId(rs.getInt("id"));
                er.setEventId(rs.getInt("event_id"));
                er.setResourceName(rs.getString("resource_name"));
                er.setQuantity(rs.getInt("quantity"));
                er.setStatus(rs.getString("status"));
                list.add(er);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching resources: " + e.getMessage());
        }
        return list;
    }

    // --- Volunteers ---
    public boolean registerVolunteer(int eventId, int studentId, String task) {
        String sql = "INSERT INTO event_volunteers (event_id, student_id, task_description, status) VALUES (?, ?, ?, 'REGISTERED')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.setInt(2, studentId);
            pstmt.setString(3, task);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error registering volunteer: " + e.getMessage());
            return false;
        }
    }

    public List<EventVolunteer> getVolunteers(int eventId) {
        List<EventVolunteer> list = new ArrayList<>();
        String sql = "SELECT ev.*, s.name as student_name, e.name as event_name " +
                "FROM event_volunteers ev " +
                "JOIN students s ON ev.student_id = s.id " +
                "JOIN events e ON ev.event_id = e.id " +
                "WHERE ev.event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EventVolunteer ev = new EventVolunteer();
                ev.setId(rs.getInt("id"));
                ev.setEventId(rs.getInt("event_id"));
                ev.setStudentId(rs.getInt("student_id"));
                ev.setTaskDescription(rs.getString("task_description"));
                ev.setStatus(rs.getString("status"));
                ev.setHoursLogged(rs.getFloat("hours_logged"));
                ev.setStudentName(rs.getString("student_name"));
                ev.setEventName(rs.getString("event_name"));
                list.add(ev);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching volunteers: " + e.getMessage());
        }
        return list;
    }

    public boolean isVolunteer(int eventId, int studentId) {
        String sql = "SELECT 1 FROM event_volunteers WHERE event_id = ? AND student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.setInt(2, studentId);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }
}
