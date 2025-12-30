package com.college.utils;

import java.time.LocalDateTime;

/**
 * Session Manager - Singleton pattern
 * Manages current user session globally across the application
 */
public class SessionManager {
    
    private static SessionManager instance;
    
    private int userId;
    private String username;
    private String role;
    private LocalDateTime loginTime;
    
    // Private constructor for singleton
    private SessionManager() {
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
    }
    
    /**
     * Clear session on logout
     */
    public void clearSession() {
        this.userId = 0;
        this.username = null;
        this.role = null;
        this.loginTime = null;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return userId > 0 && username != null;
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
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
    
    /**
     * Check if current user is faculty
     */
    public boolean isFaculty() {
        return "FACULTY".equals(role);
    }
    
    /**
     * Check if current user is student
     */
    public boolean isStudent() {
        return "STUDENT".equals(role);
    }
}
