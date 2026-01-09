package com.college.models;

public class Campaign {
    private int id;
    private String title;
    private String description;
    private double goalAmount;
    private double raisedAmount;
    private int createdBy;
    private String status; // ACTIVE, COMPLETED, CLOSED

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getGoalAmount() {
        return goalAmount;
    }

    public void setGoalAmount(double goalAmount) {
        this.goalAmount = goalAmount;
    }

    public double getRaisedAmount() {
        return raisedAmount;
    }

    public void setRaisedAmount(double raisedAmount) {
        this.raisedAmount = raisedAmount;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
