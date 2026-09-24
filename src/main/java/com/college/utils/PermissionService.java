package com.college.utils;

import com.college.dao.RoleDAO;
import com.college.models.Permission;
import com.college.models.Role;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Permission Service Utility
 * Provides centralized permission checking for the application
 */
public class PermissionService {

    private static PermissionService instance;
    private RoleDAO roleDAO;

    private PermissionService() {
        this.roleDAO = new RoleDAO();
    }

    public static synchronized PermissionService getInstance() {
        if (instance == null) {
            instance = new PermissionService();
        }
        return instance;
    }

/**
     * Check if a user has a specific permission.
     * A user's effective permissions are the union of their primary role and
     * every secondary role (user_secondary_roles), so a secondary role grants
     * its permission "perks" without changing the primary role shown to others.
     */
    public boolean hasPermission(int userId, String permissionCode) {
        List<Role> roles = roleDAO.getRolesForUser(userId);
        return grantsAnyPermission(roles, permissionCode);
    }

    /**
     * Get the primary role for a user (the role displayed everywhere).
     */
    public Role getUserRole(int userId) {
        return roleDAO.getRoleForUser(userId);
    }

    /**
     * Check if user has any of the specified permissions
     */
    public boolean hasAnyPermission(int userId, String... permissionCodes) {
        List<Role> roles = roleDAO.getRolesForUser(userId);
        for (Role role : roles) {
            if (isAdministrator(role)) {
                return true;
            }
            for (String code : permissionCodes) {
                if (role.hasPermission(code)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if user has all of the specified permissions
     */
    public boolean hasAllPermissions(int userId, String... permissionCodes) {
        List<Role> roles = roleDAO.getRolesForUser(userId);
        Set<String> effective = new HashSet<>();
        for (Role role : roles) {
            if (isAdministrator(role)) {
                return true;
            }
            for (Permission p : role.getPermissions()) {
                effective.add(p.getCode());
            }
        }
        for (String code : permissionCodes) {
            if (!effective.contains(code)) {
                return false;
            }
        }
        return true;
    }

/**
     * ADMIN is the built-in superuser role. Treating it as such here keeps API
     * authorization aligned with the web client and prevents newly introduced
     * permission codes from accidentally locking administrators out.
     */
    static boolean grantsPermission(Role role, String permissionCode) {
        return role != null && (isAdministrator(role) || role.hasPermission(permissionCode));
    }

    /**
     * Whether any of a user's roles (primary + secondary) grants a permission.
     * Secondary roles contribute their permission "perks" without changing the
     * primary role used for display and portal selection.
     */
    static boolean grantsAnyPermission(List<Role> roles, String permissionCode) {
        for (Role role : roles) {
            if (grantsPermission(role, permissionCode)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAdministrator(Role role) {
        return "ADMIN".equalsIgnoreCase(role.getCode());
    }
}
