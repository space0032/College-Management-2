package com.college;

import com.college.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PermissionFixer {
    public static void main(String[] args) {
        System.out.println("Fixing Missing Permissions...");

        try (Connection conn = DatabaseConnection.getConnection()) {

            // 1. Insert MANAGE_SYSTEM permission if not exists
            String permSql = "INSERT INTO permissions (code, name, category) VALUES ('MANAGE_SYSTEM', 'Manage System', 'Admin') ON CONFLICT (code) DO NOTHING";
            try (PreparedStatement ps = conn.prepareStatement(permSql)) {
                ps.executeUpdate();
                System.out.println("Inserted MANAGE_SYSTEM permission.");
            }

            // 2. Get ID of MANAGE_SYSTEM
            int permId = 0;
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT id FROM permissions WHERE code='MANAGE_SYSTEM'")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                    permId = rs.getInt(1);
            }

            // 3. Get ID of ADMIN Role
            int roleId = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM roles WHERE code='ADMIN'")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                    roleId = rs.getInt(1);
            }

            if (permId > 0 && roleId > 0) {
                // 4. Link them
                String linkSql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
                try (PreparedStatement ps = conn.prepareStatement(linkSql)) {
                    ps.setInt(1, roleId);
                    ps.setInt(2, permId);
                    ps.executeUpdate();
                    System.out.println("Linked MANAGE_SYSTEM to ADMIN role.");
                }
            } else {
                System.out.println("Failed to find IDs: PermID=" + permId + ", RoleID=" + roleId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
