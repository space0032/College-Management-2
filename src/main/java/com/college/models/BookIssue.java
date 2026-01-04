package com.college.models;

import java.util.Date;

/**
 * BookIssue Model
 * Represents a book issued to a student
 */
public class BookIssue {

    private int id;
    private int studentId;
    private int bookId;
    private Date issueDate;
    private Date dueDate;
    private Date returnDate;
    private double fineAmount;
    private String status; // ISSUED, RETURNED, OVERDUE
    private int issuedBy;
    private int returnedTo;
    private String remarks;

    // Display fields
    private String studentName;
    private String bookTitle;

    public BookIssue() {
    }

    public BookIssue(int studentId, int bookId, Date issueDate, Date dueDate) {
        this.studentId = studentId;
        this.bookId = bookId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = "ISSUED";
        this.fineAmount = 0.0;
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

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(int issuedBy) {
        this.issuedBy = issuedBy;
    }

    public int getReturnedTo() {
        return returnedTo;
    }

    public void setReturnedTo(int returnedTo) {
        this.returnedTo = returnedTo;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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

    /**
     * Calculate fine based on overdue days
     * 
     * @param finePerDay Fine amount per day
     * @return Calculated fine
     */
    public double calculateFine(double finePerDay) {
        if (returnDate == null) {
            returnDate = new Date();
        }

        long diff = returnDate.getTime() - dueDate.getTime();
        long overdueDays = diff / (1000 * 60 * 60 * 24);

        if (overdueDays > 0) {
            return overdueDays * finePerDay;
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return "BookIssue{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", bookId=" + bookId +
                ", issueDate=" + issueDate +
                ", dueDate=" + dueDate +
                ", status='" + status + '\'' +
                '}';
    }
}
