package com.college.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String role; // Legacy role
    private int roleId;
    private String roleName; // From RBAC roles table
    private List<SecondaryRole> secondaryRoles = new ArrayList<>();
    private LocalDateTime lastLogin;

    public User() {
    }

    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<SecondaryRole> getSecondaryRoles() {
        return secondaryRoles;
    }

    public void setSecondaryRoles(List<SecondaryRole> secondaryRoles) {
        this.secondaryRoles = secondaryRoles == null ? new ArrayList<>() : secondaryRoles;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}
