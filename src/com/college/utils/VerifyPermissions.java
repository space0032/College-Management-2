package com.college.utils;

import com.college.dao.RoleDAO;
import com.college.models.Permission;
import com.college.models.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class VerifyPermissions {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   Permission System Verification Tool   ");
        System.out.println("=========================================");

        verifyUser("admin");
        verifyUser("faculty");
        verifyUser("student");
        verifyUser("warden");

        // Check previously created faculty if exists
        checkFacultyUser("faculty3");
    }

    private static void verifyUser(String username) {
        System.out.println("\nChecking User: " + username);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get User and Role ID
            String sql = "SELECT u.id, u.username, u.role, u.role_id, r.code as role_code, r.name as role_name " +
                    "FROM users u " +
                    "LEFT JOIN roles r ON u.role_id = r.id " +
                    "WHERE u.username = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int userId = rs.getInt("id");
                    int roleId = rs.getInt("role_id");
                    String legacyRole = rs.getString("role");
                    String roleCode = rs.getString("role_code");
                    String roleName = rs.getString("role_name");

                    System.out.println("  User ID: " + userId);
                    System.out.println("  Legacy Role: " + legacyRole);
                    System.out.println("  RBAC Role: " + roleName + " (" + roleCode + ")");

                    // Get Permissions
                    RoleDAO roleDAO = new RoleDAO();
                    Role role = roleDAO.getRoleById(roleId);

                    if (role != null) {
                        System.out.println("  Permission Count: " + role.getPermissions().size());
                        System.out.println("  Permissions:");

                        List<String> sidebarItems = new ArrayList<>();
                        sidebarItems.add("Home");

                        for (Permission p : role.getPermissions()) {
                            // System.out.println(" - " + p.getName() + " [" + p.getCode() + "]");

                            // Simulate Dashboard Logic
                            String code = p.getCode();
                            if (code.equals("MANAGE_SYSTEM") || code.equals("VIEW_AUDIT_LOGS"))
                                addUnique(sidebarItems, "Institute Management");
                            if (code.equals("VIEW_STUDENTS") || code.equals("MANAGE_STUDENTS"))
                                addUnique(sidebarItems, "Student Management");
                            if (code.equals("VIEW_FACULTY") || code.equals("MANAGE_FACULTY"))
                                addUnique(sidebarItems, "Faculty Management");
                            if (code.equals("VIEW_COURSES") || code.equals("MANAGE_ALL_COURSES")
                                    || code.equals("MANAGE_OWN_COURSES"))
                                addUnique(sidebarItems, "Course Management");
                            if (code.equals("VIEW_ATTENDANCE") || code.equals("MANAGE_ATTENDANCE"))
                                addUnique(sidebarItems, "Attendance");
                            if (code.equals("VIEW_OWN_ATTENDANCE"))
                                addUnique(sidebarItems, "My Attendance");
                            if (code.equals("VIEW_GRADES") || code.equals("MANAGE_GRADES"))
                                addUnique(sidebarItems, "Grades");
                            if (code.equals("VIEW_TIMETABLE") || code.equals("MANAGE_TIMETABLE"))
                                addUnique(sidebarItems, "Timetable");
                            if (code.equals("VIEW_LIBRARY") || code.equals("MANAGE_LIBRARY"))
                                addUnique(sidebarItems, "Library");
                            if (code.equals("MANAGE_HOSTEL") || code.equals("MANAGE_ALLOCATIONS"))
                                addUnique(sidebarItems, "Hostel Management");
                            if (code.equals("VIEW_HOSTEL") && roleCode.equals("STUDENT"))
                                addUnique(sidebarItems, "My Hostel");
                            if (code.equals("APPROVE_GATE_PASS"))
                                addUnique(sidebarItems, "Gate Pass Approvals");
                            if (code.equals("VIEW_HOSTEL") && roleCode.equals("STUDENT"))
                                addUnique(sidebarItems, "Gate Pass"); // Student gate pass
                            if (code.equals("MANAGE_FEES") || code.equals("VIEW_ALL_FEES"))
                                addUnique(sidebarItems, "Fee Management");
                            if (code.equals("VIEW_OWN_FEES"))
                                addUnique(sidebarItems, "My Fees");
                            if (code.equals("VIEW_REPORTS"))
                                addUnique(sidebarItems, "Reports");
                            if (code.equals("VIEW_AUDIT_LOGS"))
                                addUnique(sidebarItems, "Audit Logs");
                            if (code.equals("VIEW_ASSIGNMENTS") || code.equals("MANAGE_ASSIGNMENTS"))
                                addUnique(sidebarItems, "Assignments");
                        }

                        System.out.println("  --> Expected Sidebar Menu:");
                        for (String item : sidebarItems) {
                            System.out.println("      [+] " + item);
                        }
                    } else {
                        System.out.println("  Error: Role not found via DAO!");
                    }

                } else {
                    System.out.println("  User not found in database.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkFacultyUser(String username) {
        verifyUser(username);
    }

    private static void addUnique(List<String> list, String item) {
        if (!list.contains(item)) {
            list.add(item);
        }
    }
}
