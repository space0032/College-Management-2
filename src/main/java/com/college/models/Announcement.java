package com.college.models;

import java.time.LocalDateTime;

/**
 * Announcement model class
 * Represents system announcements visible to users
 */
public class Announcement {
    private int id;
    private String title;
    private String content;
    private String targetAudience; // ALL, STUDENTS, FACULTY, STUDENTS_FACULTY
    private String priority; // LOW, NORMAL, HIGH, URGENT
    private int createdBy;
    private String createdByName; // For display purposes
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isActive;

    // Constructors
    public Announcement() {
    }

    public Announcement(int id, String title, String content, String targetAudience,
            String priority, int createdBy, LocalDateTime createdAt,
            LocalDateTime expiresAt, boolean isActive) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.targetAudience = targetAudience;
        this.priority = priority;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Get priority icon for display
     */
    public String getPriorityIcon() {
        switch (priority) {
            case "URGENT":
                return "[!]";
            case "HIGH":
                return "[H]";
            case "NORMAL":
                return "[N]";
            case "LOW":
                return "[L]";
            default:
                return "";
        }
    }

    @Override
    public String toString() {
        return title + " (" + priority + ")";
    }
}
