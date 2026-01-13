package com.college.models;

import java.time.LocalDate;

public class PlacementDrive {
    private int id;
    private int companyId;
    private String companyName; // Helper for display
    private String jobRole;
    private double packageLpa;
    private String description;
    private LocalDate driveDate;
    private LocalDate deadline;
    private String eligibilityCriteria;

    public PlacementDrive() {
    }

    public PlacementDrive(int id, int companyId, String jobRole, double packageLpa, String description,
            LocalDate driveDate, LocalDate deadline, String eligibilityCriteria) {
        this.id = id;
        this.companyId = companyId;
        this.jobRole = jobRole;
        this.packageLpa = packageLpa;
        this.description = description;
        this.driveDate = driveDate;
        this.deadline = deadline;
        this.eligibilityCriteria = eligibilityCriteria;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public double getPackageLpa() {
        return packageLpa;
    }

    public void setPackageLpa(double packageLpa) {
        this.packageLpa = packageLpa;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDriveDate() {
        return driveDate;
    }

    public void setDriveDate(LocalDate driveDate) {
        this.driveDate = driveDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getEligibilityCriteria() {
        return eligibilityCriteria;
    }

    public void setEligibilityCriteria(String eligibilityCriteria) {
        this.eligibilityCriteria = eligibilityCriteria;
    }
}
