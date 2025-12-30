package com.college.dao;

import com.college.models.Hostel;
import com.college.models.Room;
import com.college.models.HostelAllocation;
import com.college.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Hostel Management operations
 */
public class HostelDAO {

    /**
     * Get all hostels
     */
    public List<Hostel> getAllHostels() {
        List<Hostel> hostels = new ArrayList<>();
        String sql = "SELECT * FROM hostels ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                hostels.add(extractHostelFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hostels;
    }

    /**
     * Get all rooms with hostel information
     */
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, h.name as hostel_name FROM rooms r " +
                "JOIN hostels h ON r.hostel_id = h.id " +
                "ORDER BY h.name, r.floor, r.room_number";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }

    /**
     * Get rooms by hostel
     */
    public List<Room> getRoomsByHostel(int hostelId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, h.name as hostel_name FROM rooms r " +
                "JOIN hostels h ON r.hostel_id = h.id WHERE r.hostel_id = ? ORDER BY r.floor, r.room_number";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hostelId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }

    /**
     * Get available rooms by hostel
     */
    public List<Room> getAvailableRooms(int hostelId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, h.name as hostel_name FROM rooms r " +
                "JOIN hostels h ON r.hostel_id = h.id " +
                "WHERE r.hostel_id = ? AND r.status = 'AVAILABLE' AND r.occupied_count < r.capacity " +
                "ORDER BY r.floor, r.room_number";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hostelId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }

    /**
     * Allocate room to student
     */
    public boolean allocateRoom(HostelAllocation allocation) {
        String sql = "INSERT INTO hostel_allocations (student_id, room_id, check_in_date, remarks, allocated_by) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, allocation.getStudentId());
            pstmt.setInt(2, allocation.getRoomId());
            pstmt.setDate(3, new java.sql.Date(allocation.getCheckInDate().getTime()));
            pstmt.setString(4, allocation.getRemarks());
            if (allocation.getAllocatedBy() != null) {
                pstmt.setInt(5, allocation.getAllocatedBy());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            if (pstmt.executeUpdate() > 0) {
                // Update room occupied count
                updateRoomOccupancy(allocation.getRoomId(), 1);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Vacate room
     */
    public boolean vacateRoom(int allocationId) {
        // Get allocation first to update room
        HostelAllocation allocation = getAllocationById(allocationId);
        if (allocation == null) {
            return false;
        }

        String sql = "UPDATE hostel_allocations SET status = 'VACATED', check_out_date = CURDATE() WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, allocationId);

            if (pstmt.executeUpdate() > 0) {
                // Update room occupied count
                updateRoomOccupancy(allocation.getRoomId(), -1);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get active allocations
     */
    public List<HostelAllocation> getAllActiveAllocations() {
        List<HostelAllocation> allocations = new ArrayList<>();
        String sql = "SELECT ha.*, s.name as student_name, r.room_number, h.name as hostel_name " +
                "FROM hostel_allocations ha " +
                "JOIN students s ON ha.student_id = s.id " +
                "JOIN rooms r ON ha.room_id = r.id " +
                "JOIN hostels h ON r.hostel_id = h.id " +
                "WHERE ha.status = 'ACTIVE' ORDER BY h.name, r.room_number";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                allocations.add(extractAllocationFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allocations;
    }

    /**
     * Get allocations by student
     */
    public List<HostelAllocation> getAllocationsByStudent(int studentId) {
        List<HostelAllocation> allocations = new ArrayList<>();
        String sql = "SELECT ha.*, s.name as student_name, r.room_number, h.name as hostel_name " +
                "FROM hostel_allocations ha " +
                "JOIN students s ON ha.student_id = s.id " +
                "JOIN rooms r ON ha.room_id = r.id " +
                "JOIN hostels h ON r.hostel_id = h.id " +
                "WHERE ha.student_id = ? ORDER BY ha.check_in_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                allocations.add(extractAllocationFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allocations;
    }

    /**
     * Update room occupancy
     */
    private void updateRoomOccupancy(int roomId, int change) {
        String sql = "UPDATE rooms SET occupied_count = occupied_count + ?, " +
                "status = CASE WHEN occupied_count + ? >= capacity THEN 'FULL' ELSE 'AVAILABLE' END " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, change);
            pstmt.setInt(2, change);
            pstmt.setInt(3, roomId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get allocation by ID
     */
    private HostelAllocation getAllocationById(int id) {
        String sql = "SELECT ha.*, s.name as student_name, r.room_number, h.name as hostel_name " +
                "FROM hostel_allocations ha " +
                "JOIN students s ON ha.student_id = s.id " +
                "JOIN rooms r ON ha.room_id = r.id " +
                "JOIN hostels h ON r.hostel_id = h.id " +
                "WHERE ha.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractAllocationFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Add new hostel
     */
    public boolean addHostel(Hostel hostel) {
        String sql = "INSERT INTO hostels (name, type, warden_name, warden_contact, total_rooms, total_capacity, address) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hostel.getName());
            pstmt.setString(2, hostel.getType());
            pstmt.setString(3, hostel.getWardenName());
            pstmt.setString(4, hostel.getWardenContact());
            pstmt.setInt(5, hostel.getTotalRooms());
            pstmt.setInt(6, hostel.getTotalCapacity());
            pstmt.setString(7, hostel.getAddress());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Delete hostel
     */
    public boolean deleteHostel(int id) {
        String sql = "DELETE FROM hostels WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Add new room
     */
    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (hostel_id, room_number, floor, capacity, occupied_count, room_type, status) " +
                "VALUES (?, ?, ?, ?, 0, ?, 'AVAILABLE')";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, room.getHostelId());
            pstmt.setString(2, room.getRoomNumber());
            pstmt.setInt(3, room.getFloor());
            pstmt.setInt(4, room.getCapacity());
            pstmt.setString(5, room.getRoomType());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Extract methods
    private Hostel extractHostelFromResultSet(ResultSet rs) throws SQLException {
        Hostel hostel = new Hostel();
        hostel.setId(rs.getInt("id"));
        hostel.setName(rs.getString("name"));
        hostel.setType(rs.getString("type"));
        hostel.setWardenName(rs.getString("warden_name"));
        hostel.setWardenContact(rs.getString("warden_contact"));
        hostel.setTotalRooms(rs.getInt("total_rooms"));
        hostel.setTotalCapacity(rs.getInt("total_capacity"));
        hostel.setAddress(rs.getString("address"));
        return hostel;
    }

    private Room extractRoomFromResultSet(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getInt("id"));
        room.setHostelId(rs.getInt("hostel_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setFloor(rs.getInt("floor"));
        room.setCapacity(rs.getInt("capacity"));
        room.setOccupiedCount(rs.getInt("occupied_count"));
        room.setRoomType(rs.getString("room_type"));
        room.setStatus(rs.getString("status"));
        room.setHostelName(rs.getString("hostel_name"));
        return room;
    }

    private HostelAllocation extractAllocationFromResultSet(ResultSet rs) throws SQLException {
        HostelAllocation allocation = new HostelAllocation();
        allocation.setId(rs.getInt("id"));
        allocation.setStudentId(rs.getInt("student_id"));
        allocation.setRoomId(rs.getInt("room_id"));
        allocation.setCheckInDate(rs.getDate("check_in_date"));
        allocation.setCheckOutDate(rs.getDate("check_out_date"));
        allocation.setStatus(rs.getString("status"));
        allocation.setRemarks(rs.getString("remarks"));

        int allocatedBy = rs.getInt("allocated_by");
        allocation.setAllocatedBy(rs.wasNull() ? null : allocatedBy);

        allocation.setStudentName(rs.getString("student_name"));
        allocation.setRoomNumber(rs.getString("room_number"));
        allocation.setHostelName(rs.getString("hostel_name"));
        return allocation;
    }
}
