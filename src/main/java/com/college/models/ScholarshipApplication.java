package com.college.models;

public class ScholarshipApplication {
    private int id;
    private int scholarshipId;
    private int studentId;
    private String studentName; // For display
    private String statement;
    private String status; // APPLIED, APPROVED, REJECTED

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScholarshipId() {
        return scholarshipId;
    }

    public void setScholarshipId(int scholarshipId) {
        this.scholarshipId = scholarshipId;
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

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
