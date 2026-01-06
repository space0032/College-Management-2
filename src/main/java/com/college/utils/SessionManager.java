package com.college.utils;

import com.college.dao.RoleDAO;
import com.college.models.Role;

import java.time.LocalDateTime;

/**
 * Session Manager - Singleton pattern
 * Manages current user session globally across the application
 */
public class SessionManager {

    private static SessionManager instance;

    private int userId;
    private String username;
    private String role; // Legacy role string
    private Role userRole; // New RBAC Role object
    private LocalDateTime loginTime;
    private RoleDAO roleDAO;

    // Private constructor for singleton
    private SessionManager() {
        this.roleDAO = new RoleDAO();
    }

    /**
     * Get singleton instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Initialize session on login
     */
    public void initSession(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.loginTime = LocalDateTime.now();

        // Load RBAC Role
        loadUserRole();
    }

    /**
     * Load user's RBAC role and permissions
     */
    private void loadUserRole() {
        try {
            this.userRole = roleDAO.getRoleForUser(userId);
        } catch (Exception e) {
            // RBAC tables may not exist yet or connection error
            this.userRole = null;
        }

        // Fallback for legacy users who have 'role' string but no 'role_id'
        if (this.userRole == null && this.role != null && !this.role.isEmpty()) {
            this.userRole = new Role();
            this.userRole.setId(0); // Legacy ID
            this.userRole.setCode(this.role);
            this.userRole.setName(this.role);
            this.userRole.setPortalType(this.role);
            this.userRole.setSystemRole(true);

            // Log this fallback
            System.out.println("Using legacy role fallback for user: " + username + " -> " + role);
        }
    }

    /**
     * Clear session on logout
     */
    public void clearSession() {
        this.userId = 0;
        this.username = null;
        this.role = null;
        this.userRole = null;
        this.loginTime = null;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return userId > 0 && username != null;
    }

    /**
     * Check if user has a specific permission
     * Falls back to legacy role check if RBAC not set up
     */
    public boolean hasPermission(String permissionCode) {
        // Null safety check
        if (permissionCode == null || permissionCode.trim().isEmpty()) {
            return false;
        }

        if (userRole != null && userRole.getPermissions() != null) {
            return userRole.hasPermission(permissionCode);
        }
        // Fallback to legacy role-based checks
        return fallbackPermissionCheck(permissionCode);
    }

    /**
     * Check if user has any of the specified permissions
     */
    public boolean hasAnyPermission(String... permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }

        for (String code : permissionCodes) {
            if (hasPermission(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fallback permission check based on legacy role
     * This ensures users can still use the system even if RBAC isn't fully set up
     */
    private boolean fallbackPermissionCheck(String permissionCode) {
        if (role == null) {
            return false;
        }

        // Admin has all permissions in legacy mode
        if ("ADMIN".equals(role)) {
            return true;
        }

        // Faculty specific permissions
        if ("FACULTY".equals(role)) {
            return permissionCode.contains("VIEW_") ||
                    permissionCode.contains("MANAGE_ATTENDANCE") ||
                    permissionCode.contains("MANAGE_GRADES") ||
                    permissionCode.contains("MANAGE_ASSIGNMENTS");
        }

        // Student specific permissions
        if ("STUDENT".equals(role)) {
            return permissionCode.contains("VIEW_OWN_") ||
                    permissionCode.contains("REQUEST_") ||
                    permissionCode.contains("SUBMIT_");
        }

        return false;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Role getUserRole() {
        return userRole;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * Check if current user is admin (legacy)
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /**
     * Check if current user is faculty (legacy)
     */
    public boolean isFaculty() {
        return "FACULTY".equals(role);
    }

    /**
     * Check if current user is student (legacy)
     */
    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    /**
     * Check if current user is warden (legacy)
     */
    public boolean isWarden() {
        return "WARDEN".equals(role);
    }
}
