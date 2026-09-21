package com.college.dao;

import com.college.models.Department;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DepartmentDAO - Data Access Object for Department operations
 */
public class DepartmentDAO {
    private final PayrollDAO.ConnectionProvider connections;
    public DepartmentDAO() { this(DatabaseConnection::getConnection); }
    public DepartmentDAO(PayrollDAO.ConnectionProvider connections) { this.connections = connections; }

    /**
     * Get all departments
     */
    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments ORDER BY name";

        try (Connection conn = connections.open();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                departments.add(extractDepartmentFromResultSet(rs));
            }

        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }

        return departments;
    }

    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM departments";
        try (Connection conn = connections.open();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }
        return 0;
    }

    /**
     * Get department by ID
     */
    public Department getDepartmentById(int id) {
        String sql = "SELECT * FROM departments WHERE id = ?";

        try (Connection conn = connections.open();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractDepartmentFromResultSet(rs);
            }

        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }

        return null;
    }

    /**
     * Add new department
     */
    public boolean addDepartment(Department department) {
        String sql = "INSERT INTO departments (name, code, description, head_of_department) VALUES (?, ?, ?, ?)";

        try (Connection conn = connections.open();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, department.getName());
            pstmt.setString(2, department.getCode());
            pstmt.setString(3, department.getDescription());
            pstmt.setString(4, department.getHeadOfDepartment());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }
    }

    /**
     * Update existing department
     */
    public boolean updateDepartment(Department department) {
        String sql = "UPDATE departments SET name = ?, code = ?, description = ?, head_of_department = ? WHERE id = ?";

        try (Connection conn = connections.open();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, department.getName());
            pstmt.setString(2, department.getCode());
            pstmt.setString(3, department.getDescription());
            pstmt.setString(4, department.getHeadOfDepartment());
            pstmt.setInt(5, department.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }
    }

    /**
     * Delete department
     */
    public boolean deleteDepartment(int id) {
        try (Connection conn = connections.open()) {
            conn.setAutoCommit(false);
            try {
                String name, code;
                try (PreparedStatement lock = conn.prepareStatement("SELECT name, code FROM departments WHERE id = ? FOR UPDATE")) {
                    lock.setInt(1, id);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) { conn.rollback(); return false; }
                        name = rs.getString(1); code = rs.getString(2);
                    }
                }
                DatabaseMetaData meta = conn.getMetaData();
                boolean upper = meta.storesUpperCaseIdentifiers();
                String table = upper ? "DEPARTMENTS" : "departments";
                String quote = meta.getIdentifierQuoteString().trim();
                try (ResultSet keys = meta.getExportedKeys(conn.getCatalog(), conn.getSchema(), table)) {
                    while (keys.next()) {
                        String target = keys.getString("FKTABLE_NAME"), column = keys.getString("FKCOLUMN_NAME");
                        try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM " + quote + target + quote + " WHERE " + quote + column + quote + " = ?")) {
                            check.setInt(1, id);
                            try (ResultSet rs = check.executeQuery()) { rs.next(); if (rs.getInt(1) > 0) throw new com.college.utils.ManagementException(409, "This department is in use. Reassign its linked records before deleting it."); }
                        }
                    }
                }
                // Some legacy records link departments by name/code instead of a foreign key.
                for (String related : new String[]{"students", "faculty", "courses"}) {
                    String relatedTable = upper ? related.toUpperCase(java.util.Locale.ROOT) : related;
                    String column = upper ? "DEPARTMENT" : "department";
                    try (ResultSet columns = meta.getColumns(conn.getCatalog(), conn.getSchema(), relatedTable, column)) {
                        if (!columns.next()) continue;
                    }
                    try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM " + quote + relatedTable + quote + " WHERE " + quote + column + quote + " = ? OR " + quote + column + quote + " = ?")) {
                        check.setString(1, name); check.setString(2, code);
                        try (ResultSet rs = check.executeQuery()) { rs.next(); if (rs.getInt(1) > 0) throw new com.college.utils.ManagementException(409, "This department is assigned to students, faculty or courses."); }
                    }
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM departments WHERE id = ?")) { stmt.setInt(1, id); stmt.executeUpdate(); }
                conn.commit(); return true;
            } catch (Exception e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) { throw com.college.utils.ManagementException.database(e); }
    }

    /**
     * Search departments by name or code
     */
    public List<Department> searchDepartments(String query) {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments WHERE name ILIKE ? OR code ILIKE ? ORDER BY name";

        try (Connection conn = connections.open();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                departments.add(extractDepartmentFromResultSet(rs));
            }

        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }

        return departments;
    }

    /**
     * Check if department has courses assigned
     */
    public boolean hasCourses(int departmentId) {
        String sql = "SELECT COUNT(*) as count FROM courses WHERE department_id = ?";

        try (Connection conn = connections.open();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0;
            }

        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
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
