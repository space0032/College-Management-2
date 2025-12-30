package com.college.dao;

import com.college.models.Timetable;
import com.college.utils.DatabaseConnection;

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
        String sql = "SELECT * FROM timetable WHERE department = ? AND semester = ? ORDER BY FIELD(day_of_week, 'Monday','Tuesday','Wednesday','Thursday','Friday'), time_slot";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, department);
            pstmt.setInt(2, semester);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                timetable.add(extractTimetableFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
                "ON DUPLICATE KEY UPDATE subject = ?, faculty_name = ?, room_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getDepartment());
            pstmt.setInt(2, entry.getSemester());
            pstmt.setString(3, entry.getDayOfWeek());
            pstmt.setString(4, entry.getTimeSlot());
            pstmt.setString(5, entry.getSubject());
            pstmt.setString(6, entry.getFacultyName());
            pstmt.setString(7, entry.getRoomNumber());
            // For ON DUPLICATE KEY UPDATE
            pstmt.setString(8, entry.getSubject());
            pstmt.setString(9, entry.getFacultyName());
            pstmt.setString(10, entry.getRoomNumber());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extract Timetable object from ResultSet
     */
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
}
