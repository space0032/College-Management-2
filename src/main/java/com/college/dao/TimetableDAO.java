package com.college.dao;

import com.college.models.Timetable;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;
import com.college.utils.TimeSlotUtil;

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
        return getTimetableByDepartmentSemesterAndTrack(department, semester, null);
    }

    /**
     * Get timetable for specific department, semester and track/specialization.
     * Null/blank specialization returns all tracks (backward compatible).
     */
    public List<Timetable> getTimetableByDepartmentSemesterAndTrack(String department, int semester,
            String specialization) {
        List<Timetable> timetable = new ArrayList<>();
        boolean hasSpec = specialization != null && !specialization.trim().isEmpty();
        String sql;
        if (hasSpec) {
            // Tolerant: specialization column may not exist on very old DBs -> fall back below on error
            sql = "SELECT * FROM timetable WHERE department = ? AND semester = ? AND specialization = ? " +
                    "ORDER BY CASE day_of_week " +
                    "WHEN 'Monday' THEN 1 " +
                    "WHEN 'Tuesday' THEN 2 " +
                    "WHEN 'Wednesday' THEN 3 " +
                    "WHEN 'Thursday' THEN 4 " +
                    "WHEN 'Friday' THEN 5 " +
                    "WHEN 'Saturday' THEN 6 " +
                    "WHEN 'Sunday' THEN 7 " +
                    "ELSE 8 END, time_slot";
        } else {
            sql = "SELECT * FROM timetable WHERE department = ? AND semester = ? " +
                    "ORDER BY CASE day_of_week " +
                    "WHEN 'Monday' THEN 1 " +
                    "WHEN 'Tuesday' THEN 2 " +
                    "WHEN 'Wednesday' THEN 3 " +
                    "WHEN 'Thursday' THEN 4 " +
                    "WHEN 'Friday' THEN 5 " +
                    "WHEN 'Saturday' THEN 6 " +
                    "WHEN 'Sunday' THEN 7 " +
                    "ELSE 8 END, time_slot";
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, department);
            pstmt.setInt(2, semester);
            if (hasSpec) {
                pstmt.setString(3, specialization.trim());
            }
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                timetable.add(extractTimetableFromResultSet(rs));
            }

        } catch (SQLException e) {
            // If specialization column is missing, fall back to dept+sem query
            if (hasSpec && e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("specialization")) {
                return getTimetableByDepartmentAndSemester(department, semester);
            }
            Logger.error("Database operation failed", e);
        }
        return timetable;
    }

    /**
     * Save or update timetable entry
     */
    public boolean saveTimetableEntry(Timetable entry) {
        // Preferred: track-aware upsert (requires V62 columns). Conflict is per track.
        String sql = "INSERT INTO timetable (department, semester, specialization, course_id, day_of_week, time_slot, subject, faculty_name, room_number) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (department, semester, day_of_week, time_slot) DO UPDATE SET " +
                "subject = EXCLUDED.subject, " +
                "faculty_name = EXCLUDED.faculty_name, " +
                "room_number = EXCLUDED.room_number";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getDepartment());
            pstmt.setInt(2, entry.getSemester());
            pstmt.setString(3, entry.getSpecialization());
            if (entry.getCourseId() > 0) {
                pstmt.setInt(4, entry.getCourseId());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            pstmt.setString(5, entry.getDayOfWeek());
            pstmt.setString(6, entry.getTimeSlot());
            pstmt.setString(7, entry.getSubject());
            pstmt.setString(8, entry.getFacultyName());
            pstmt.setString(9, entry.getRoomNumber());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            // Fallback for DBs without V62 columns
            if (e.getMessage() != null && (e.getMessage().toLowerCase().contains("specialization")
                    || e.getMessage().toLowerCase().contains("course_id"))) {
                return saveTimetableEntryLegacy(entry);
            }
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    private boolean saveTimetableEntryLegacy(Timetable entry) {
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
     * Check for room conflict (legacy signature kept for compatibility).
     * The semester parameter is ignored: a room cannot be double-booked
     * by any department/semester on overlapping time.
     */
    public boolean checkConflict(String roomNumber, String day, String timeSlot, int semester, int excludeId) {
        return isRoomOccupied(roomNumber, day, timeSlot, excludeId);
    }

    /**
     * True if the room is occupied on the given day at an overlapping time slot.
     * Uses interval overlap instead of exact string equality.
     */
    public boolean isRoomOccupied(String roomNumber, String day, String timeSlot, int excludeId) {
        return findOccupant(roomNumber, day, timeSlot, excludeId) != null;
    }

    /**
     * Find the timetable entry occupying the room at an overlapping time, or null.
     */
    public Timetable findOccupant(String roomNumber, String day, String timeSlot, int excludeId) {
        if (roomNumber == null || roomNumber.isBlank() || day == null || timeSlot == null) {
            return null;
        }
        String normalizedDay = TimeSlotUtil.normalizeDay(day);
        for (Timetable entry : getTimetableByDay(normalizedDay)) {
            if (entry.getId() == excludeId) {
                continue;
            }
            if (entry.getRoomNumber() == null || !entry.getRoomNumber().equalsIgnoreCase(roomNumber.trim())) {
                continue;
            }
            if (TimeSlotUtil.overlaps(entry.getTimeSlot(), timeSlot)) {
                return entry;
            }
        }
        return null;
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
        try {
            timetable.setSpecialization(rs.getString("specialization"));
        } catch (SQLException e) {
            // Pre-V62 DBs have no specialization column
        }
        try {
            timetable.setCourseId(rs.getInt("course_id"));
        } catch (SQLException e) {
            // Pre-V62 DBs have no course_id column
        }
        return timetable;
    }

    /**
     * Get occupied rooms for a specific day and time slot.
     * Uses interval overlap so "09:00-10:30" blocks "09:00 - 10:00 AM".
     */
    public List<String> getOccupiedRooms(String day, String timeSlot) {
        List<String> occupied = new ArrayList<>();
        if (day == null || timeSlot == null) {
            return occupied;
        }
        String normalizedDay = TimeSlotUtil.normalizeDay(day);
        for (Timetable entry : getTimetableByDay(normalizedDay)) {
            if (entry.getRoomNumber() == null || entry.getRoomNumber().isBlank()) {
                continue;
            }
            if (TimeSlotUtil.overlaps(entry.getTimeSlot(), timeSlot)
                    && !occupied.contains(entry.getRoomNumber())) {
                occupied.add(entry.getRoomNumber());
            }
        }
        return occupied;
    }

    /**
     * Get all timetable entries for a day (case-insensitive).
     */
    public List<Timetable> getTimetableByDay(String day) {
        List<Timetable> timetable = new ArrayList<>();
        String normalizedDay = TimeSlotUtil.normalizeDay(day);
        String sql = "SELECT * FROM timetable WHERE LOWER(day_of_week) = LOWER(?) ORDER BY time_slot";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, normalizedDay);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                timetable.add(extractTimetableFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching timetable by day: " + e.getMessage());
        }
        return timetable;
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
