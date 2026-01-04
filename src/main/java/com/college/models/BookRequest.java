package com.college.models;

import java.util.Date;

/**
 * Book Request Model
 * Represents a book request made by a student
 */
public class BookRequest {
    private int id;
    private int studentId;
    private int bookId;
    private Date requestDate;
    private String status; // PENDING, APPROVED, REJECTED
    private Integer approvedBy;
    private Date approvedDate;
    private String remarks;
    private int loanPeriodDays;

    // Additional fields for display
    private String studentName;
    private String bookTitle;
    private String bookAuthor;
    private String approvedByName;

    public BookRequest() {
        this.loanPeriodDays = 14; // Default
        this.status = "PENDING";
    }

    public BookRequest(int studentId, int bookId, int loanPeriodDays) {
        this.studentId = studentId;
        this.bookId = bookId;
        this.loanPeriodDays = loanPeriodDays;
        this.status = "PENDING";
        this.requestDate = new Date();
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

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Date getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getLoanPeriodDays() {
        return loanPeriodDays;
    }

    public void setLoanPeriodDays(int loanPeriodDays) {
        this.loanPeriodDays = loanPeriodDays;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }
}
