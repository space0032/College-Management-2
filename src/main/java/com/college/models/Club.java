package com.college.models;

import java.util.Date;

/**
 * Model representing a student club/organization
 */
public class Club {
    private int id;
    private String name;
    private String description;
    private String category; // TECHNICAL, CULTURAL, SPORTS, SOCIAL, ACADEMIC
    private String logoPath;
    private Integer presidentStudentId;
    private Integer facultyCoordinatorId;
    private int memberCount;
    private String status; // ACTIVE, INACTIVE
    private Date createdAt;
    private Date updatedAt;

    // Display fields
    private String presidentName;
    private String coordinatorName;

    public Club() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public Integer getPresidentStudentId() {
        return presidentStudentId;
    }

    public void setPresidentStudentId(Integer presidentStudentId) {
        this.presidentStudentId = presidentStudentId;
    }

    public Integer getFacultyCoordinatorId() {
        return facultyCoordinatorId;
    }

    public void setFacultyCoordinatorId(Integer facultyCoordinatorId) {
        this.facultyCoordinatorId = facultyCoordinatorId;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPresidentName() {
        return presidentName;
    }

    public void setPresidentName(String presidentName) {
        this.presidentName = presidentName;
    }

    public String getCoordinatorName() {
        return coordinatorName;
    }

    public void setCoordinatorName(String coordinatorName) {
        this.coordinatorName = coordinatorName;
    }

    @Override
    public String toString() {
        return name;
    }
}
