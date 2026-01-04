package com.college.tests;

import com.college.dao.PermissionDAO;
import com.college.dao.RoleDAO;
import com.college.dao.UserDAO;
import com.college.models.Permission;
import com.college.models.Role;
import com.college.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FinanceRoleVerification {

    public static void main(String[] args) {
        System.out.println("Verifying Finance Role Implementation...");
        boolean success = true;

        // 1. Verify Role Exists
        RoleDAO roleDAO = new RoleDAO();
        Role financeRole = roleDAO.getRoleByCode("FINANCE");
        if (financeRole == null) {
            System.err.println("[FAIL] FINANCE role not found.");
            success = false;
        } else {
            System.out.println("[PASS] FINANCE role exists.");
        }

        // 2. Verify Permissions Assigned
        if (financeRole != null) {
            String[] expectedPermissions = { "VIEW_FEES_REPORT", "MANAGE_FEES", "VIEW_ALL_FEES" };
            for (String perm : expectedPermissions) {
                if (!financeRole.hasPermission(perm)) {
                    System.err.println("[FAIL] FINANCE role missing permission: " + perm);
                    success = false;
                } else {
                    System.out.println("[PASS] FINANCE role has permission: " + perm);
                }
            }
        }

        // 3. Verify Test User Exists
        int userId = getUserId("finance");
        if (userId <= 0) {
            System.err.println("[FAIL] User 'finance' not found.");
            success = false;
        } else {
            System.out.println("[PASS] User 'finance' exists with ID: " + userId);

            // 4. Verify User has Correct Role ID
            if (financeRole != null) {
                int userRoleId = getUserRoleId(userId);
                if (userRoleId == financeRole.getId()) {
                    System.out.println("[PASS] User 'finance' is assigned to FINANCE role ID: " + userRoleId);
                } else {
                    System.err.println("[FAIL] User 'finance' has incorrect role ID. Expected: " + financeRole.getId()
                            + ", Found: " + userRoleId);
                    success = false;
                }
            }
        }

        if (success) {
            System.out.println("\nVERIFICATION SUCCESSFUL: Finance Role is correctly configured.");
        } else {
            System.out.println("\nVERIFICATION FAILED: Issues found in configuration.");
            System.exit(1);
        }
    }

    private static int getUserId(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static int getUserRoleId(int userId) {
        String sql = "SELECT role_id FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt("role_id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
