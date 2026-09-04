package com.college.models;

/**
 * Program Fee Structure Model
 * Customizable fee breakdown per department or program, per fee category,
 * per academic year. Used at student enrollment time.
 */
public class ProgramFeeStructure {
    private int id;
    private String department;
    private int categoryId;
    private String categoryName;
    private String academicYear;
    private double amount;

    public ProgramFeeStructure() {
    }

    public ProgramFeeStructure(String department, int categoryId, String academicYear, double amount) {
        this.department = department;
        this.categoryId = categoryId;
        this.academicYear = academicYear;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
