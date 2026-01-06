package com.college;

import com.college.dao.RoleDAO;
import com.college.dao.UserDAO;
import com.college.models.Role;
import com.college.models.User;
import com.college.utils.DatabaseConnection;
import java.sql.Connection;

public class PermissionDebug {
    public static void main(String[] args) {
        System.out.println("Checking Permissions...");

        try (Connection conn = DatabaseConnection.getConnection()) {
            UserDAO userDAO = new UserDAO();
            // Assuming admin is ID 1 or we find by username
            // Let's find by username 'admin'
            int userId = 0;
            try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username='admin'")) {
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next())
                    userId = rs.getInt(1);
            }

            if (userId == 0) {
                System.out.println("No 'admin' user found.");
                return;
            }

            System.out.println("Found Admin User ID: " + userId);

            RoleDAO roleDAO = new RoleDAO();
            Role role = roleDAO.getRoleForUser(userId);

            if (role == null) {
                System.out.println("Admin has NO linked Role (role_id might be null).");
                // Check raw user row
                User u = userDAO.getUserById(userId);
                System.out.println("User Record -> Role ID: " + u.getRoleId() + ", Legacy Role: " + u.getRole());
            } else {
                System.out.println("Linked Role: " + role.getName() + " (" + role.getCode() + ")");
                System.out.println("Permission Count: " + role.getPermissions().size());
                role.getPermissions().forEach(p -> System.out.println(" - " + p.getCode()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
