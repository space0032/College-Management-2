package com.college.models;

import java.sql.Timestamp;

public class Submission {
    private int id;
    private int assignmentId;
    private int studentId;
    private String submissionText;
    private String filePath;
    private Timestamp submittedAt;
    private String status; // SUBMITTED, LATE, GRADED
    private Double grade;
    private String feedback;
    private int plagiarismScore;

    // Additional fields for display
    private String studentName;
    private String studentEnrollmentId;
    private String assignmentTitle;

    public Submission() {
    }

    public Submission(int assignmentId, int studentId, String submissionText, String filePath) {
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.submissionText = submissionText;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getSubmissionText() {
        return submissionText;
    }

    public void setSubmissionText(String submissionText) {
        this.submissionText = submissionText;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Timestamp getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Timestamp submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public int getPlagiarismScore() {
        return plagiarismScore;
    }

    public void setPlagiarismScore(int plagiarismScore) {
        this.plagiarismScore = plagiarismScore;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEnrollmentId() {
        return studentEnrollmentId;
    }

    public void setStudentEnrollmentId(String studentEnrollmentId) {
        this.studentEnrollmentId = studentEnrollmentId;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }
}
