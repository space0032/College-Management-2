package com.college.utils;

import com.college.dao.RoleDAO;
import com.college.models.Role;

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
     * Check if a user has a specific permission
     */
    public boolean hasPermission(int userId, String permissionCode) {
        Role role = roleDAO.getRoleForUser(userId);
        if (role != null) {
            return role.hasPermission(permissionCode);
        }
        return false;
    }

    /**
     * Get the role for a user
     */
    public Role getUserRole(int userId) {
        return roleDAO.getRoleForUser(userId);
    }

    /**
     * Check if user has any of the specified permissions
     */
    public boolean hasAnyPermission(int userId, String... permissionCodes) {
        Role role = roleDAO.getRoleForUser(userId);
        if (role != null) {
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
        Role role = roleDAO.getRoleForUser(userId);
        if (role != null) {
            for (String code : permissionCodes) {
                if (!role.hasPermission(code)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
