package com.college.models;

import java.util.Date;

/**
 * Student Fee Model
 */
public class StudentFee {
    private int id;
    private int studentId;
    private int categoryId;
    private String academicYear;
    private double totalAmount;
    private double paidAmount;
    private String status; // PENDING, PARTIAL, PAID
    private Date dueDate;

    // Display fields
    private String studentName;
    private String categoryName;

    public StudentFee() {
        this.paidAmount = 0.00;
        this.status = "PENDING";
    }

    public StudentFee(int studentId, int categoryId, String academicYear, double totalAmount) {
        this();
        this.studentId = studentId;
        this.categoryId = categoryId;
        this.academicYear = academicYear;
        this.totalAmount = totalAmount;
    }

    public double getBalanceAmount() {
        return totalAmount - paidAmount;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
