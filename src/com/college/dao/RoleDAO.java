package com.college.dao;

import com.college.models.Permission;
import com.college.models.Role;
import com.college.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Role operations
 */
public class RoleDAO {

    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Role role = extractRoleFromResultSet(rs);
                roles.add(role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Load permissions after closing the roles ResultSet
        for (Role role : roles) {
            loadPermissionsForRole(role);
        }

        return roles;
    }

    public Role getRoleById(int id) {
        String sql = "SELECT * FROM roles WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Role role = extractRoleFromResultSet(rs);
                loadPermissionsForRole(role);
                return role;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Role getRoleByCode(String code) {
        String sql = "SELECT * FROM roles WHERE code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Role role = extractRoleFromResultSet(rs);
                loadPermissionsForRole(role);
                return role;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Role getRoleForUser(int userId) {
        String sql = "SELECT r.* FROM roles r " +
                "INNER JOIN users u ON u.role_id = r.id " +
                "WHERE u.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Role role = extractRoleFromResultSet(rs);
                loadPermissionsForRole(role);
                return role;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createRole(Role role) {
        String sql = "INSERT INTO roles (code, name, description, is_system_role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, role.getCode());
            stmt.setString(2, role.getName());
            stmt.setString(3, role.getDescription());
            stmt.setBoolean(4, role.isSystemRole());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    role.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRole(Role role) {
        String sql = "UPDATE roles SET code = ?, name = ?, description = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.getCode());
            stmt.setString(2, role.getName());
            stmt.setString(3, role.getDescription());
            stmt.setInt(4, role.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteRole(int roleId) {
        String sql = "DELETE FROM roles WHERE id = ? AND is_system_role = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean assignPermissionToRole(int roleId, int permissionId) {
        String sql = "INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roleId);
            stmt.setInt(2, permissionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removePermissionFromRole(int roleId, int permissionId) {
        String sql = "DELETE FROM role_permissions WHERE role_id = ? AND permission_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roleId);
            stmt.setInt(2, permissionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setRolePermissions(int roleId, List<Integer> permissionIds) {
        String deleteSql = "DELETE FROM role_permissions WHERE role_id = ?";
        String insertSql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, roleId);
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                for (Integer permId : permissionIds) {
                    insertStmt.setInt(1, roleId);
                    insertStmt.setInt(2, permId);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean assignRoleToUser(int userId, int roleId) {
        String sql = "UPDATE users SET role_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roleId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void loadPermissionsForRole(Role role) {
        String sql = "SELECT p.* FROM permissions p " +
                "INNER JOIN role_permissions rp ON rp.permission_id = p.id " +
                "WHERE rp.role_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, role.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Permission p = new Permission();
                p.setId(rs.getInt("id"));
                p.setCode(rs.getString("code"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setDescription(rs.getString("description"));
                role.addPermission(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Role extractRoleFromResultSet(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getInt("id"));
        role.setCode(rs.getString("code"));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        role.setSystemRole(rs.getBoolean("is_system_role"));
        return role;
    }
}
