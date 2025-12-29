package com.college.dao;

import com.college.models.Faculty;
import com.college.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Faculty entity
 * Handles all database operations for faculty
 */
public class FacultyDAO {

    /**
     * Add a new faculty to the database
     * 
     * @param faculty Faculty object to add
     * @return generated faculty ID if successful, -1 otherwise
     */
    public int addFaculty(Faculty faculty) {
        String sql = "INSERT INTO faculty (name, email, phone, department, qualification, join_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, faculty.getName());
            pstmt.setString(2, faculty.getEmail());
            pstmt.setString(3, faculty.getPhone());
            pstmt.setString(4, faculty.getDepartment());
            pstmt.setString(5, faculty.getQualification());
            pstmt.setDate(6, new java.sql.Date(faculty.getJoinDate().getTime()));

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated faculty ID
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Update an existing faculty
     * 
     * @param faculty Faculty object with updated data
     * @return true if successful, false otherwise
     */
    public boolean updateFaculty(Faculty faculty) {
        String sql = "UPDATE faculty SET name=?, email=?, phone=?, department=?, qualification=?, " +
                "join_date=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, faculty.getName());
            pstmt.setString(2, faculty.getEmail());
            pstmt.setString(3, faculty.getPhone());
            pstmt.setString(4, faculty.getDepartment());
            pstmt.setString(5, faculty.getQualification());
            pstmt.setDate(6, new java.sql.Date(faculty.getJoinDate().getTime()));
            pstmt.setInt(7, faculty.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a faculty by ID
     * 
     * @param facultyId ID of the faculty to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteFaculty(int facultyId) {
        String sql = "DELETE FROM faculty WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facultyId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get a faculty by ID
     * 
     * @param facultyId ID of the faculty
     * @return Faculty object or null if not found
     */
    public Faculty getFacultyById(int facultyId) {
        String sql = "SELECT * FROM faculty WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facultyId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractFacultyFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all faculty from the database
     * 
     * @return List of all faculty
     */
    public List<Faculty> getAllFaculty() {
        List<Faculty> facultyList = new ArrayList<>();
        String sql = "SELECT * FROM faculty ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                facultyList.add(extractFacultyFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return facultyList;
    }

    /**
     * Search faculty by name or email
     * 
     * @param keyword Search keyword
     * @return List of matching faculty
     */
    public List<Faculty> searchFaculty(String keyword) {
        List<Faculty> facultyList = new ArrayList<>();
        String sql = "SELECT * FROM faculty WHERE name LIKE ? OR email LIKE ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                facultyList.add(extractFacultyFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return facultyList;
    }

    /**
     * Helper method to extract Faculty object from ResultSet
     * 
     * @param rs ResultSet from query
     * @return Faculty object
     */
    private Faculty extractFacultyFromResultSet(ResultSet rs) throws SQLException {
        Faculty faculty = new Faculty();
        faculty.setId(rs.getInt("id"));
        faculty.setName(rs.getString("name"));
        faculty.setEmail(rs.getString("email"));
        faculty.setPhone(rs.getString("phone"));
        faculty.setDepartment(rs.getString("department"));
        faculty.setQualification(rs.getString("qualification"));
        faculty.setJoinDate(rs.getDate("join_date"));
        return faculty;
    }
}
