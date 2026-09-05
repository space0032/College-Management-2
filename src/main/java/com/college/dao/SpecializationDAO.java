package com.college.dao;

import com.college.models.Specialization;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SpecializationDAO - Data Access Object for track-inside-department master.
 */
public class SpecializationDAO {

    public List<Specialization> getAllSpecializations() {
        List<Specialization> list = new ArrayList<>();
        String sql = "SELECT s.*, d.name AS dept_name FROM specializations s "
                + "LEFT JOIN departments d ON s.department_id = d.id "
                + "ORDER BY d.name, s.name";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return list;
    }

    public List<Specialization> getByDepartment(int departmentId) {
        List<Specialization> list = new ArrayList<>();
        String sql = "SELECT s.*, d.name AS dept_name FROM specializations s "
                + "LEFT JOIN departments d ON s.department_id = d.id "
                + "WHERE s.department_id = ? AND s.is_active = TRUE ORDER BY s.name";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, departmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extract(rs));
                }
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return list;
    }

    public Specialization getById(int id) {
        String sql = "SELECT s.*, d.name AS dept_name FROM specializations s "
                + "LEFT JOIN departments d ON s.department_id = d.id WHERE s.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extract(rs);
                }
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return null;
    }

    public boolean addSpecialization(Specialization spec) {
        String sql = "INSERT INTO specializations (department_id, name, code, description, is_active) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, spec.getDepartmentId());
            pstmt.setString(2, spec.getName());
            pstmt.setString(3, spec.getCode());
            pstmt.setString(4, spec.getDescription());
            pstmt.setBoolean(5, spec.isActive());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean updateSpecialization(Specialization spec) {
        String sql = "UPDATE specializations SET department_id = ?, name = ?, code = ?, "
                + "description = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, spec.getDepartmentId());
            pstmt.setString(2, spec.getName());
            pstmt.setString(3, spec.getCode());
            pstmt.setString(4, spec.getDescription());
            pstmt.setBoolean(5, spec.isActive());
            pstmt.setInt(6, spec.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean deleteSpecialization(int id) {
        if (countUsage(id) > 0) {
            return false;
        }
        String sql = "DELETE FROM specializations WHERE id = ?";
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
     * Subjects + students currently referencing this track name in its department.
     */
    public int countUsage(int id) {
        Specialization spec = getById(id);
        if (spec == null) {
            return 0;
        }
        int count = 0;
        String subjectSql = "SELECT COUNT(*) FROM courses c "
                + "LEFT JOIN departments d ON c.department_id = d.id "
                + "WHERE c.specialization = ? AND (c.department = ? OR d.name = ?)";
        String studentSql = "SELECT COUNT(*) FROM students WHERE specialization = ? AND department = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement p1 = conn.prepareStatement(subjectSql);
                PreparedStatement p2 = conn.prepareStatement(studentSql)) {
            p1.setString(1, spec.getName());
            p1.setString(2, spec.getDepartmentName());
            p1.setString(3, spec.getDepartmentName());
            try (ResultSet rs = p1.executeQuery()) {
                if (rs.next()) {
                    count += rs.getInt(1);
                }
            }
            p2.setString(1, spec.getName());
            p2.setString(2, spec.getDepartmentName());
            try (ResultSet rs = p2.executeQuery()) {
                if (rs.next()) {
                    count += rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return count;
    }

    private Specialization extract(ResultSet rs) throws SQLException {
        Specialization spec = new Specialization();
        spec.setId(rs.getInt("id"));
        spec.setDepartmentId(rs.getInt("department_id"));
        spec.setName(rs.getString("name"));
        try {
            spec.setDepartmentName(rs.getString("dept_name"));
        } catch (SQLException ignored) {
        }
        try {
            spec.setCode(rs.getString("code"));
        } catch (SQLException ignored) {
        }
        try {
            spec.setDescription(rs.getString("description"));
        } catch (SQLException ignored) {
        }
        try {
            spec.setActive(rs.getBoolean("is_active"));
        } catch (SQLException ignored) {
        }
        return spec;
    }
}
