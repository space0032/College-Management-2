package com.college.models;

import java.util.HashSet;
import java.util.Set;

/**
 * Role Model for RBAC
 */
public class Role {
    private int id;
    private String code;
    private String name;
    private String description;
    private boolean systemRole;
    private Set<Permission> permissions;

    public Role() {
        this.permissions = new HashSet<>();
    }

    public Role(int id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.permissions = new HashSet<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public void setSystemRole(boolean systemRole) {
        this.systemRole = systemRole;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public boolean hasPermission(String permissionCode) {
        return permissions.stream()
                .anyMatch(p -> p.getCode().equals(permissionCode));
    }

    @Override
    public String toString() {
        return name;
    }
}
