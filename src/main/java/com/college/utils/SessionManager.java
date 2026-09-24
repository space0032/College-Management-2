package com.college.utils;

import com.college.dao.RoleDAO;
import com.college.models.Role;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Session Manager - Singleton pattern
 * Manages current user session globally across the application
 */
public class SessionManager {

    private static SessionManager instance;

    private int userId;
    private String username;
    private String role; // Legacy role string
    private Role userRole; // New RBAC primary Role object
    private List<Role> secondaryRoles = Collections.emptyList(); // Additional RBAC roles (permission perks)
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
            List<Role> allRoles = roleDAO.getRolesForUser(userId);
            this.userRole = null;
            this.secondaryRoles = new java.util.ArrayList<>();
            for (Role r : allRoles) {
                // The primary role is the first one returned (joined from users.role_id).
                if (this.userRole == null) {
                    this.userRole = r;
                } else {
                    this.secondaryRoles.add(r);
                }
            }
            if (this.secondaryRoles.isEmpty()) this.secondaryRoles = Collections.emptyList();
        } catch (Exception e) {
            // RBAC tables may not exist yet or connection error
            this.userRole = null;
            this.secondaryRoles = Collections.emptyList();
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
        this.secondaryRoles = Collections.emptyList();
        this.loginTime = null;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return userId > 0 && username != null;
    }

    /**
     * Check if user has a specific permission.
     * Effective permissions are the union of the primary role and every
     * secondary role, so a secondary role contributes its "perks" without
     * changing the primary role used for display and portal selection.
     */
    public boolean hasPermission(String permissionCode) {
        // Null safety check
        if (permissionCode == null || permissionCode.trim().isEmpty()) {
            return false;
        }

        for (Role r : allSessionRoles()) {
            if (r != null && r.getPermissions() != null && !r.getPermissions().isEmpty()) {
                if ("ADMIN".equalsIgnoreCase(r.getCode()) || r.hasPermission(permissionCode)) {
                    return true;
                }
            }
        }
        // Fallback to legacy role-based checks
        return fallbackPermissionCheck(permissionCode);
    }

    /**
     * Primary + secondary roles for the current session. The primary role is
     * first. Returns an immutable defensive copy.
     */
    private java.util.List<Role> allSessionRoles() {
        java.util.List<Role> roles = new java.util.ArrayList<>();
        if (userRole != null) roles.add(userRole);
        roles.addAll(secondaryRoles);
        return roles;
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

    /**
     * Additional roles for the current user (permission perks only).
     */
    public List<Role> getSecondaryRoles() {
        return secondaryRoles;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * Check if current user is admin (legacy)
     */
    public boolean isAdmin() {
        return role != null && "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * Check if current user is faculty (legacy)
     */
    public boolean isFaculty() {
        return role != null && "FACULTY".equalsIgnoreCase(role);
    }

    /**
     * Check if current user is student (legacy)
     */
    public boolean isStudent() {
        return role != null && "STUDENT".equalsIgnoreCase(role);
    }

    /**
     * Check if current user is warden (legacy)
     */
    public boolean isWarden() {
        return role != null && "WARDEN".equalsIgnoreCase(role);
    }
}
