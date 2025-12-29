package com.college.models;

/**
 * Course model class
 * Represents a course in the system
 */
public class Course {
    private int id;
    private String name;
    private String code;
    private int credits;
    private String department;
    private int semester;

    // Constructors
    public Course() {
    }

    public Course(int id, String name, String code, int credits,
            String department, int semester) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.credits = credits;
        this.department = department;
        this.semester = semester;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
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

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
