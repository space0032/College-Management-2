package com.college.models;

/**
 * Fee Category Model
 */
public class FeeCategory {
    private int id;
    private String categoryName;
    private double baseAmount;
    private String description;
    private boolean isActive;

    public FeeCategory() {
        this.isActive = true;
    }

    public FeeCategory(String categoryName, double baseAmount) {
        this();
        this.categoryName = categoryName;
        this.baseAmount = baseAmount;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(double baseAmount) {
        this.baseAmount = baseAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
