package com.college.models;

import java.util.Date;

/**
 * Faculty model class
 * Represents a faculty member in the system
 */
public class Faculty {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String qualification;
    private Date joinDate;
    private int userId;

    // Constructors
    public Faculty() {
    }

    public Faculty(int id, String name, String email, String phone,
            String department, String qualification, Date joinDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.qualification = qualification;
        this.joinDate = joinDate;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return name + " - " + department;
    }
}
