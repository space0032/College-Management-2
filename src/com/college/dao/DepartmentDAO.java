package com.college.dao;

import com.college.models.Department;
import com.college.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DepartmentDAO - Data Access Object for Department operations
 */
public class DepartmentDAO {

    /**
     * Get all departments
     */
    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                departments.add(extractDepartmentFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return departments;
    }

    /**
     * Get department by ID
     */
    public Department getDepartmentById(int id) {
        String sql = "SELECT * FROM departments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractDepartmentFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Add new department
     */
    public boolean addDepartment(Department department) {
        String sql = "INSERT INTO departments (name, code, description, head_of_department) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, department.getName());
            pstmt.setString(2, department.getCode());
            pstmt.setString(3, department.getDescription());
            pstmt.setString(4, department.getHeadOfDepartment());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update existing department
     */
    public boolean updateDepartment(Department department) {
        String sql = "UPDATE departments SET name = ?, code = ?, description = ?, head_of_department = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, department.getName());
            pstmt.setString(2, department.getCode());
            pstmt.setString(3, department.getDescription());
            pstmt.setString(4, department.getHeadOfDepartment());
            pstmt.setInt(5, department.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete department
     */
    public boolean deleteDepartment(int id) {
        String sql = "DELETE FROM departments WHERE id = ?";

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
     * Search departments by name or code
     */
    public List<Department> searchDepartments(String query) {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments WHERE name LIKE ? OR code LIKE ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                departments.add(extractDepartmentFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return departments;
    }

    /**
     * Check if department has courses assigned
     */
    public boolean hasCourses(int departmentId) {
        String sql = "SELECT COUNT(*) as count FROM courses WHERE department_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Extract Department from ResultSet
     */
    private Department extractDepartmentFromResultSet(ResultSet rs) throws SQLException {
        Department dept = new Department();
        dept.setId(rs.getInt("id"));
        dept.setName(rs.getString("name"));
        dept.setCode(rs.getString("code"));
        dept.setDescription(rs.getString("description"));
        dept.setHeadOfDepartment(rs.getString("head_of_department"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            dept.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            dept.setUpdatedAt(updated.toLocalDateTime());
        }

        return dept;
    }
}
