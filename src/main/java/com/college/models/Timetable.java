package com.college.models;

/**
 * Timetable model class
 * Represents a timetable entry for a specific department and semester
 */
public class Timetable {
    private int id;
    private String department;
    private int semester;
    private String dayOfWeek;
    private String timeSlot;
    private String subject;
    private String facultyName;
    private String roomNumber;

    // Constructors
    public Timetable() {
    }

    public Timetable(String department, int semester, String dayOfWeek, String timeSlot,
            String subject, String facultyName, String roomNumber) {
        this.department = department;
        this.semester = semester;
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
        this.subject = subject;
        this.facultyName = facultyName;
        this.roomNumber = roomNumber;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    @Override
    public String toString() {
        return subject + " - " + facultyName + " (" + roomNumber + ")";
    }
}
