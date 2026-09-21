package com.college.dao;

import com.college.utils.DatabaseConnection;
import com.college.utils.ManagementException;
import java.sql.*;
import java.util.*;

/** Access changes use one transaction and the same lock order to prevent admin lockout. */
public class AccessManagementDAO {
    private final PayrollDAO.ConnectionProvider connections;
    public AccessManagementDAO() { this(DatabaseConnection::getConnection); }
    public AccessManagementDAO(PayrollDAO.ConnectionProvider connections) { this.connections = connections; }
    private record RoleRow(int id, String code, boolean system) {}
    private record UserRow(int id, int roleId) {}
    public void assignRole(int requester, int user, int role) { change(requester, user, role, false); }
    public void deleteUser(int requester, int user) { change(requester, user, 0, true); }
    private void change(int requester, int user, int role, boolean deleting) {
        try (Connection conn = connections.open()) {
            conn.setAutoCommit(false);
            try {
                Map<Integer, RoleRow> roles = lockRoles(conn);
                Map<Integer, UserRow> users = lockUsers(conn);
                UserRow actor = users.get(requester), target = users.get(user);
                if (actor == null) throw new ManagementException(403, "Account no longer exists.");
                if (target == null) throw new ManagementException(404, "User not found.");
                boolean admin = isAdmin(roles.get(actor.roleId()));
                RoleRow oldRole = roles.get(target.roleId()), newRole = roles.get(role);
                if (deleting && requester == user) throw new ManagementException(409, "You cannot delete your current account.");
                if (!deleting && newRole == null) throw new ManagementException(404, "Role not found.");
                if (!admin && (isAdmin(oldRole) || (!deleting && isAdmin(newRole)))) throw new ManagementException(403, "Only administrators can change administrator accounts.");
                if (!deleting && !admin && !permissionIds(conn, actor.roleId()).containsAll(permissionIds(conn, role))) throw new ManagementException(403, "You cannot assign permissions you do not possess.");
                long admins = users.values().stream().filter(u -> isAdmin(roles.get(u.roleId()))).count();
                if (isAdmin(oldRole) && (deleting || !isAdmin(newRole)) && admins <= 1) throw new ManagementException(409, "The last administrator cannot be removed.");
                if (deleting) {
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) { stmt.setInt(1, user); stmt.executeUpdate(); }
                } else {
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET role_id = ?, role = ? WHERE id = ?")) { stmt.setInt(1, role); stmt.setString(2, newRole.code()); stmt.setInt(3, user); stmt.executeUpdate(); }
                }
                conn.commit();
            } catch (Exception e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) { throw ManagementException.database(e); }
    }
    public void deleteRole(int roleId) {
        try (Connection conn = connections.open()) {
            conn.setAutoCommit(false);
            try {
                Map<Integer, RoleRow> roles = lockRoles(conn); Map<Integer, UserRow> users = lockUsers(conn);
                RoleRow role = roles.get(roleId);
                if (role == null) throw new ManagementException(404, "Role not found.");
                if (role.system() || Set.of("ADMIN", "WARDEN", "FINANCE", "FACULTY", "STUDENT").contains(role.code().toUpperCase(Locale.ROOT))) throw new ManagementException(409, "System roles cannot be deleted.");
                if (users.values().stream().anyMatch(u -> u.roleId() == roleId)) throw new ManagementException(409, "Reassign this role's users before deleting it.");
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM role_permissions WHERE role_id = ?")) { stmt.setInt(1, roleId); stmt.executeUpdate(); }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM roles WHERE id = ?")) { stmt.setInt(1, roleId); stmt.executeUpdate(); }
                conn.commit();
            } catch (Exception e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) { throw ManagementException.database(e); }
    }
    private static boolean isAdmin(RoleRow role) { return role != null && "ADMIN".equalsIgnoreCase(role.code()); }
    private Map<Integer, RoleRow> lockRoles(Connection conn) throws SQLException {
        Map<Integer, RoleRow> rows = new LinkedHashMap<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, code, is_system_role FROM roles ORDER BY id FOR UPDATE")) { while (rs.next()) rows.put(rs.getInt(1), new RoleRow(rs.getInt(1), rs.getString(2), rs.getBoolean(3))); }
        return rows;
    }
    private Map<Integer, UserRow> lockUsers(Connection conn) throws SQLException {
        Map<Integer, UserRow> rows = new LinkedHashMap<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, role_id FROM users ORDER BY id FOR UPDATE")) { while (rs.next()) rows.put(rs.getInt(1), new UserRow(rs.getInt(1), rs.getInt(2))); }
        return rows;
    }
    private Set<Integer> permissionIds(Connection conn, int roleId) throws SQLException {
        Set<Integer> ids = new HashSet<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT permission_id FROM role_permissions WHERE role_id = ?")) { stmt.setInt(1, roleId); try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) ids.add(rs.getInt(1)); } }
        return ids;
    }
}
