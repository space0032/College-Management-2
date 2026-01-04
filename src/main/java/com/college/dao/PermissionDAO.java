package com.college.dao;

import com.college.models.Permission;
import com.college.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Permission operations
 */
public class PermissionDAO {

    public List<Permission> getAllPermissions() {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT * FROM permissions ORDER BY category, name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                permissions.add(extractPermissionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    public List<Permission> getPermissionsByCategory(String category) {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT * FROM permissions WHERE category = ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                permissions.add(extractPermissionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    public Permission getPermissionByCode(String code) {
        String sql = "SELECT * FROM permissions WHERE code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractPermissionFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createPermission(Permission permission) {
        String sql = "INSERT INTO permissions (code, name, category, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, permission.getCode());
            stmt.setString(2, permission.getName());
            stmt.setString(3, permission.getCategory());
            stmt.setString(4, permission.getDescription());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        permission.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM permissions ORDER BY category";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    private Permission extractPermissionFromResultSet(ResultSet rs) throws SQLException {
        Permission p = new Permission();
        p.setId(rs.getInt("id"));
        p.setCode(rs.getString("code"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setDescription(rs.getString("description"));
        return p;
    }
}
