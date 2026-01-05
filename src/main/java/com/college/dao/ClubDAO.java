package com.college.dao;

import com.college.models.Club;
import com.college.models.ClubMembership;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Club operations
 */
public class ClubDAO {

    public List<Club> getAllClubs() {
        List<Club> clubs = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "s.name as president_name, " +
                "f.name as coordinator_name " +
                "FROM clubs c " +
                "LEFT JOIN students s ON c.president_student_id = s.id " +
                "LEFT JOIN faculty f ON c.faculty_coordinator_id = f.id " +
                "WHERE c.status = 'ACTIVE' " +
                "ORDER BY c.name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clubs.add(extractClub(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching clubs: " + e.getMessage());
        }
        return clubs;
    }

    public Club getClubById(int id) {
        String sql = "SELECT c.*, " +
                "s.name as president_name, " +
                "f.name as coordinator_name " +
                "FROM clubs c " +
                "LEFT JOIN students s ON c.president_student_id = s.id " +
                "LEFT JOIN faculty f ON c.faculty_coordinator_id = f.id " +
                "WHERE c.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractClub(rs);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching club: " + e.getMessage());
        }
        return null;
    }

    public boolean createClub(Club club) {
        String sql = "INSERT INTO clubs (name, description, category, president_student_id, " +
                "faculty_coordinator_id, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, club.getName());
            pstmt.setString(2, club.getDescription());
            pstmt.setString(3, club.getCategory());

            if (club.getPresidentStudentId() != null) {
                pstmt.setInt(4, club.getPresidentStudentId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            if (club.getFacultyCoordinatorId() != null) {
                pstmt.setInt(5, club.getFacultyCoordinatorId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            pstmt.setString(6, club.getStatus());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error creating club: " + e.getMessage());
            return false;
        }
    }

    public boolean updateClub(Club club) {
        String sql = "UPDATE clubs SET name = ?, description = ?, category = ?, " +
                "president_student_id = ?, faculty_coordinator_id = ?, status = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, club.getName());
            pstmt.setString(2, club.getDescription());
            pstmt.setString(3, club.getCategory());

            if (club.getPresidentStudentId() != null) {
                pstmt.setInt(4, club.getPresidentStudentId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            if (club.getFacultyCoordinatorId() != null) {
                pstmt.setInt(5, club.getFacultyCoordinatorId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            pstmt.setString(6, club.getStatus());
            pstmt.setInt(7, club.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error updating club: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteClub(int id) {
        String sql = "DELETE FROM clubs WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error deleting club: " + e.getMessage());
            return false;
        }
    }

    // Membership methods
    public boolean joinClub(int clubId, int studentId) {
        // Create PENDING membership request
        String sql = "INSERT INTO club_memberships (club_id, student_id, role, status) VALUES (?, ?, 'MEMBER', 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clubId);
            pstmt.setInt(2, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error creating membership request: " + e.getMessage());
            return false;
        }
    }

    public boolean approveMembership(int membershipId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Get club ID before updating
            String getClubSql = "SELECT club_id FROM club_memberships WHERE id = ?";
            PreparedStatement getStmt = conn.prepareStatement(getClubSql);
            getStmt.setInt(1, membershipId);
            ResultSet rs = getStmt.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                return false;
            }
            int clubId = rs.getInt("club_id");

            // Update membership status
            String sql = "UPDATE club_memberships SET status = 'APPROVED' WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, membershipId);
            pstmt.executeUpdate();

            // Increment member count
            String updateSql = "UPDATE clubs SET member_count = member_count + 1 WHERE id = ?";
            PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
            updatePstmt.setInt(1, clubId);
            updatePstmt.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.error("Rollback error: " + ex.getMessage());
                }
            }
            Logger.error("Error approving membership: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.error("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    public boolean rejectMembership(int membershipId) {
        String sql = "UPDATE club_memberships SET status = 'REJECTED' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, membershipId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error rejecting membership: " + e.getMessage());
            return false;
        }
    }

    public List<ClubMembership> getPendingMemberships(int clubId) {
        List<ClubMembership> pending = new ArrayList<>();
        String sql = "SELECT cm.*, s.name as student_name, c.name as club_name " +
                "FROM club_memberships cm " +
                "JOIN students s ON cm.student_id = s.id " +
                "JOIN clubs c ON cm.club_id = c.id " +
                "WHERE cm.club_id = ? AND cm.status = 'PENDING' " +
                "ORDER BY cm.joined_at";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clubId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ClubMembership membership = new ClubMembership();
                membership.setId(rs.getInt("id"));
                membership.setClubId(rs.getInt("club_id"));
                membership.setStudentId(rs.getInt("student_id"));
                membership.setRole(rs.getString("role"));
                membership.setStatus(rs.getString("status"));
                membership.setJoinedAt(rs.getTimestamp("joined_at"));
                membership.setStudentName(rs.getString("student_name"));
                membership.setClubName(rs.getString("club_name"));
                pending.add(membership);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching pending memberships: " + e.getMessage());
        }
        return pending;
    }

    public boolean leaveClub(int clubId, int studentId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Remove membership
            String sql = "DELETE FROM club_memberships WHERE club_id = ? AND student_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, clubId);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();

            // Update member count
            String updateSql = "UPDATE clubs SET member_count = member_count - 1 WHERE id = ?";
            PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
            updatePstmt.setInt(1, clubId);
            updatePstmt.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.error("Rollback error: " + ex.getMessage());
                }
            }
            Logger.error("Error leaving club: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.error("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    public boolean isStudentMember(int clubId, int studentId) {
        String sql = "SELECT 1 FROM club_memberships WHERE club_id = ? AND student_id = ? AND status IN ('APPROVED', 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clubId);
            pstmt.setInt(2, studentId);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            Logger.error("Error checking membership: " + e.getMessage());
            return false;
        }
    }

    public List<ClubMembership> getClubMembers(int clubId) {
        List<ClubMembership> members = new ArrayList<>();
        String sql = "SELECT cm.*, s.name as student_name, c.name as club_name " +
                "FROM club_memberships cm " +
                "JOIN students s ON cm.student_id = s.id " +
                "JOIN clubs c ON cm.club_id = c.id " +
                "WHERE cm.club_id = ? AND cm.status = 'APPROVED' " +
                "ORDER BY cm.role, cm.joined_at";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clubId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ClubMembership membership = new ClubMembership();
                membership.setId(rs.getInt("id"));
                membership.setClubId(rs.getInt("club_id"));
                membership.setStudentId(rs.getInt("student_id"));
                membership.setRole(rs.getString("role"));
                membership.setStatus(rs.getString("status"));
                membership.setJoinedAt(rs.getTimestamp("joined_at"));
                membership.setStudentName(rs.getString("student_name"));
                membership.setClubName(rs.getString("club_name"));
                members.add(membership);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching club members: " + e.getMessage());
        }
        return members;
    }

    public List<Club> getStudentClubs(int studentId) {
        List<Club> clubs = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "s.name as president_name, " +
                "f.name as coordinator_name " +
                "FROM clubs c " +
                "JOIN club_memberships cm ON c.id = cm.club_id " +
                "LEFT JOIN students s ON c.president_student_id = s.id " +
                "LEFT JOIN faculty f ON c.faculty_coordinator_id = f.id " +
                "WHERE cm.student_id = ? AND cm.status = 'APPROVED' " +
                "ORDER BY c.name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                clubs.add(extractClub(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching student clubs: " + e.getMessage());
        }
        return clubs;
    }

    private Club extractClub(ResultSet rs) throws SQLException {
        Club club = new Club();
        club.setId(rs.getInt("id"));
        club.setName(rs.getString("name"));
        club.setDescription(rs.getString("description"));
        club.setCategory(rs.getString("category"));
        club.setLogoPath(rs.getString("logo_path"));
        club.setPresidentStudentId((Integer) rs.getObject("president_student_id"));
        club.setFacultyCoordinatorId((Integer) rs.getObject("faculty_coordinator_id"));
        club.setMemberCount(rs.getInt("member_count"));
        club.setStatus(rs.getString("status"));
        club.setCreatedAt(rs.getTimestamp("created_at"));
        club.setUpdatedAt(rs.getTimestamp("updated_at"));
        club.setPresidentName(rs.getString("president_name"));
        club.setCoordinatorName(rs.getString("coordinator_name"));
        return club;
    }
}
