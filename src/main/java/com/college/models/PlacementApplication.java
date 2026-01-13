package com.college.models;

import java.time.LocalDateTime;

public class PlacementApplication {
    private int id;
    private int driveId;
    private int studentId;
    private String status; // APPLIED, SHORTLISTED, SELECTED, REJECTED
    private LocalDateTime appliedAt;

    // Helpers for display
    private String studentName;
    private String driveTitle;
    private String companyName;

    public PlacementApplication() {
    }

    public PlacementApplication(int id, int driveId, int studentId, String status, LocalDateTime appliedAt) {
        this.id = id;
        this.driveId = driveId;
        this.studentId = studentId;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDriveId() {
        return driveId;
    }

    public void setDriveId(int driveId) {
        this.driveId = driveId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDriveTitle() {
        return driveTitle;
    }

    public void setDriveTitle(String driveTitle) {
        this.driveTitle = driveTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
