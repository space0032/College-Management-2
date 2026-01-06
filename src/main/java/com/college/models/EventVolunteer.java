package com.college.models;

public class EventVolunteer {
    private int id;
    private int eventId;
    private int studentId;
    private String taskDescription;
    private String status; // REGISTERED, APPROVED, COMPLETED
    private float hoursLogged;

    // UI Fields
    private String studentName;
    private String eventName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getHoursLogged() {
        return hoursLogged;
    }

    public void setHoursLogged(float hoursLogged) {
        this.hoursLogged = hoursLogged;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
