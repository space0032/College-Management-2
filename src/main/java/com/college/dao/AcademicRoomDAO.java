package com.college.dao;

import com.college.models.AcademicRoom;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for academic_rooms master inventory.
 */
public class AcademicRoomDAO {

    public List<AcademicRoom> findAll() {
        List<AcademicRoom> rooms = new ArrayList<>();
        String sql = "SELECT * FROM academic_rooms ORDER BY room_number";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(extract(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching academic rooms", e);
        }
        return rooms;
    }

    public List<AcademicRoom> findActive() {
        List<AcademicRoom> rooms = new ArrayList<>();
        String sql = "SELECT * FROM academic_rooms WHERE status = 'ACTIVE' ORDER BY room_number";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(extract(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching active academic rooms", e);
        }
        return rooms;
    }

    public AcademicRoom findByNumber(String roomNumber) {
        String sql = "SELECT * FROM academic_rooms WHERE room_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extract(rs);
            }
        } catch (SQLException e) {
            Logger.error("Error finding academic room", e);
        }
        return null;
    }

    public boolean create(AcademicRoom room) {
        String sql = "INSERT INTO academic_rooms (room_number, building, capacity, type, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getBuilding());
            pstmt.setInt(3, room.getCapacity() <= 0 ? 40 : room.getCapacity());
            pstmt.setString(4, normalizeType(room.getType()));
            pstmt.setString(5, normalizeStatus(room.getStatus()));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error creating academic room", e);
            return false;
        }
    }

    public boolean update(int id, AcademicRoom room) {
        String sql = "UPDATE academic_rooms SET room_number = ?, building = ?, capacity = ?, type = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getBuilding());
            pstmt.setInt(3, room.getCapacity() <= 0 ? 40 : room.getCapacity());
            pstmt.setString(4, normalizeType(room.getType()));
            pstmt.setString(5, normalizeStatus(room.getStatus()));
            pstmt.setInt(6, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error updating academic room", e);
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM academic_rooms WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error deleting academic room", e);
            return false;
        }
    }

    private AcademicRoom extract(ResultSet rs) throws SQLException {
        AcademicRoom r = new AcademicRoom();
        r.setId(rs.getInt("id"));
        r.setRoomNumber(rs.getString("room_number"));
        try {
            r.setBuilding(rs.getString("building"));
        } catch (SQLException ignored) {
        }
        try {
            r.setCapacity(rs.getInt("capacity"));
        } catch (SQLException ignored) {
        }
        try {
            r.setType(rs.getString("type"));
        } catch (SQLException ignored) {
        }
        try {
            r.setStatus(rs.getString("status"));
        } catch (SQLException ignored) {
        }
        return r;
    }

    private String normalizeType(String type) {
        if (type == null)
            return "CLASSROOM";
        String t = type.trim().toUpperCase();
        // Accept legacy "LAB" alias
        if ("LAB".equals(t))
            return "LABORATORY";
        switch (t) {
            case "CLASSROOM":
            case "LABORATORY":
            case "SEMINAR":
            case "AUDITORIUM":
            case "OFFICE":
                return t;
            default:
                return "CLASSROOM";
        }
    }

    private String normalizeStatus(String status) {
        if (status == null)
            return "ACTIVE";
        String s = status.trim().toUpperCase();
        if ("ACTIVE".equals(s) || "MAINTENANCE".equals(s) || "INACTIVE".equals(s))
            return s;
        return "ACTIVE";
    }
}
