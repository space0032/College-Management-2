package com.college.models;

import java.util.Date;

/**
 * Student model class
 * Represents a student entity in the system
 */
public class Student {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String course;
    private String batch;
    private Date enrollmentDate;
    private String address;
    private int userId;
    private String department;
    private int semester;

    // Constructors
    public Student() {
    }

    public Student(int id, String name, String email, String phone,
            String course, String batch, Date enrollmentDate, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.course = course;
        this.batch = batch;
        this.enrollmentDate = enrollmentDate;
        this.address = address;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    private boolean isHostelite;

    public boolean isHostelite() {
        return isHostelite;
    }

    public void setHostelite(boolean hostelite) {
        isHostelite = hostelite;
    }

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}
