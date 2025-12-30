package com.college.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * GatePass Model
 * Represents a student gate pass request
 */
public class GatePass {

    private int id;
    private int studentId;
    private String studentName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
    private String destination;
    private String parentContact;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime requestedAt;
    private Integer approvedBy;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String approvalComment;

    // Constructors
    public GatePass() {
    }

    public GatePass(int studentId, LocalDate fromDate, LocalDate toDate,
            String reason, String destination, String parentContact) {
        this.studentId = studentId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.reason = reason;
        this.destination = destination;
        this.parentContact = parentContact;
        this.status = "PENDING";
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

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getParentContact() {
        return parentContact;
    }

    public void setParentContact(String parentContact) {
        this.parentContact = parentContact;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }

    // Validation
    public boolean isValid() {
        return fromDate != null && toDate != null &&
                !fromDate.isAfter(toDate) &&
                reason != null && !reason.trim().isEmpty() &&
                parentContact != null && !parentContact.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "GatePass{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                ", status='" + status + '\'' +
                '}';
    }
}
