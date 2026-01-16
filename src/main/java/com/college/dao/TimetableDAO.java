package com.college.dao;

import com.college.models.Timetable;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Timetable
 * Handles database operations for timetable management
 */
public class TimetableDAO {

    /**
     * Get timetable for specific department and semester
     */
    public List<Timetable> getTimetableByDepartmentAndSemester(String department, int semester) {
        List<Timetable> timetable = new ArrayList<>();
        String sql = "SELECT * FROM timetable WHERE department = ? AND semester = ? " +
                "ORDER BY CASE day_of_week " +
                "WHEN 'Monday' THEN 1 " +
                "WHEN 'Tuesday' THEN 2 " +
                "WHEN 'Wednesday' THEN 3 " +
                "WHEN 'Thursday' THEN 4 " +
                "WHEN 'Friday' THEN 5 " +
                "WHEN 'Saturday' THEN 6 " +
                "WHEN 'Sunday' THEN 7 " +
                "ELSE 8 END, time_slot";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, department);
            pstmt.setInt(2, semester);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                timetable.add(extractTimetableFromResultSet(rs));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return timetable;
    }

    /**
     * Save or update timetable entry
     */
    public boolean saveTimetableEntry(Timetable entry) {
        String sql = "INSERT INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (department, semester, day_of_week, time_slot) DO UPDATE SET " +
                "subject = EXCLUDED.subject, " +
                "faculty_name = EXCLUDED.faculty_name, " +
                "room_number = EXCLUDED.room_number";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getDepartment());
            pstmt.setInt(2, entry.getSemester());
            pstmt.setString(3, entry.getDayOfWeek());
            pstmt.setString(4, entry.getTimeSlot());
            pstmt.setString(5, entry.getSubject());
            pstmt.setString(6, entry.getFacultyName());
            pstmt.setString(7, entry.getRoomNumber());
            // Params 8-10 removed because EXCLUDED accesses VALUES(?,...) automatically

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Delete timetable entry
     */
    public boolean deleteTimetableEntry(int id) {
        String sql = "DELETE FROM timetable WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Get all unique departments
     */
    public List<String> getAllDepartments() {
        List<String> departments = new ArrayList<>();
        String sql = "SELECT DISTINCT department FROM students WHERE department IS NOT NULL ORDER BY department";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                departments.add(rs.getString("department"));
            }

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }

        // Add default if empty
        if (departments.isEmpty()) {
            departments.add("General");
            departments.add("Computer Science");
            departments.add("Electrical Engineering");
            departments.add("Mechanical Engineering");
        }

        return departments;
    }

    /**
     * Clear all timetable entries for a department and semester
     */
    public boolean clearTimetable(String department, int semester) {
        String sql = "DELETE FROM timetable WHERE department = ? AND semester = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, department);
            pstmt.setInt(2, semester);
            return pstmt.executeUpdate() >= 0; // Returns true even if 0 rows deleted

        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Get all unique rooms
     */
    public List<String> getAllRooms() {
        List<String> rooms = new ArrayList<>();
        String sql = "SELECT DISTINCT room_number FROM timetable WHERE room_number IS NOT NULL AND room_number != '' ORDER BY room_number";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(rs.getString("room_number"));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching rooms: " + e.getMessage());
        }
        return rooms;
    }

    /**
     * Extract Timetable object from ResultSet
     */
    /**
     * Check for room conflict
     */
    public boolean checkConflict(String roomNumber, String day, String timeSlot, int semester, int excludeId) {
        String sql = "SELECT COUNT(*) FROM timetable WHERE room_number = ? AND day_of_week = ? AND time_slot = ? AND semester = ? AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomNumber);
            pstmt.setString(2, day);
            pstmt.setString(3, timeSlot);
            pstmt.setInt(4, semester);
            pstmt.setInt(5, excludeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            Logger.error("Error checking conflict: " + e.getMessage());
        }
        return false;
    }

    private Timetable extractTimetableFromResultSet(ResultSet rs) throws SQLException {
        Timetable timetable = new Timetable();
        timetable.setId(rs.getInt("id"));
        timetable.setDepartment(rs.getString("department"));
        timetable.setSemester(rs.getInt("semester"));
        timetable.setDayOfWeek(rs.getString("day_of_week"));
        timetable.setTimeSlot(rs.getString("time_slot"));
        timetable.setSubject(rs.getString("subject"));
        timetable.setFacultyName(rs.getString("faculty_name"));
        timetable.setRoomNumber(rs.getString("room_number"));
        return timetable;
    }

    /**
     * Get occupied rooms for a specific day and time slot
     */
    public List<String> getOccupiedRooms(String day, String timeSlot) {
        List<String> occupied = new ArrayList<>();
        String sql = "SELECT DISTINCT room_number FROM timetable WHERE day_of_week = ? AND time_slot = ? AND room_number IS NOT NULL AND room_number != ''";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, day);
            pstmt.setString(2, timeSlot);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                occupied.add(rs.getString("room_number"));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching occupied rooms: " + e.getMessage());
        }
        return occupied;
    }

    /**
     * Get timetable by faculty name
     */
    public List<Timetable> getTimetableByFaculty(String facultyName) {
        List<Timetable> timetable = new ArrayList<>();
        String sql = "SELECT * FROM timetable WHERE faculty_name = ? ORDER BY day_of_week, time_slot";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, facultyName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                timetable.add(extractTimetableFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return timetable;
    }

    /**
     * Get timetable by subject
     */
    public List<Timetable> getTimetableBySubject(String subject) {
        List<Timetable> timetable = new ArrayList<>();
        String sql = "SELECT * FROM timetable WHERE subject = ? ORDER BY day_of_week, time_slot";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subject);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                timetable.add(extractTimetableFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return timetable;
    }
}
