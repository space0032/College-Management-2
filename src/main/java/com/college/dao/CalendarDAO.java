package com.college.dao;

import com.college.models.CalendarEvent;
import com.college.models.CalendarEvent.EventType;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CalendarDAO {

    public boolean addEvent(CalendarEvent event) {
        String sql = "INSERT INTO calendar_events (title, event_date, event_type, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, event.getTitle());
            pstmt.setDate(2, Date.valueOf(event.getEventDate()));
            pstmt.setString(3, event.getEventType().name());
            pstmt.setString(4, event.getDescription());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to add calendar event", e);
            return false;
        }
    }

    public List<CalendarEvent> getEventsByMonth(int year, int month) {
        List<CalendarEvent> events = new ArrayList<>();
        // Fetch events for the specified month/year
        String sql = "SELECT * FROM calendar_events WHERE MONTH(event_date) = ? AND YEAR(event_date) = ? ORDER BY event_date";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, month);
            pstmt.setInt(2, year);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CalendarEvent event = new CalendarEvent();
                event.setId(rs.getInt("id"));
                event.setTitle(rs.getString("title"));
                event.setEventDate(rs.getDate("event_date").toLocalDate());
                event.setEventType(EventType.valueOf(rs.getString("event_type")));
                event.setDescription(rs.getString("description"));
                events.add(event);
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch calendar events", e);
        }
        return events;
    }

    public boolean deleteEvent(int id) {
        String sql = "DELETE FROM calendar_events WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to delete event", e);
            return false;
        }
    }
}
