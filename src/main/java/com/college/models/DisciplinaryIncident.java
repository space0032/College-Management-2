package com.college.models;

/**
 * Disciplinary / behavioral incident record (Student Affairs admin module).
 * Mirrors the shape consumed by the web Student Affairs page.
 */
public class DisciplinaryIncident {
    private int id;
    private int studentId;
    private String student;
    private String enrollNo;
    private String date;
    private String type;
    private String severity;
    private String action;
    private String status;

    public DisciplinaryIncident() {
    }

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

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String getEnrollNo() {
        return enrollNo;
    }

    public void setEnrollNo(String enrollNo) {
        this.enrollNo = enrollNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}