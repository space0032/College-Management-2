package com.college.models;

import java.util.Date;

/**
 * Hostel Allocation Model
 */
public class HostelAllocation {
    private int id;
    private int studentId;
    private int roomId;
    private Date checkInDate;
    private Date checkOutDate;
    private String status; // ACTIVE, VACATED
    private String remarks;
    private Integer allocatedBy;

    // Display fields
    private String studentName;
    private String roomNumber;
    private String hostelName;

    public HostelAllocation() {
        this.status = "ACTIVE";
        this.checkInDate = new Date();
    }

    public HostelAllocation(int studentId, int roomId) {
        this();
        this.studentId = studentId;
        this.roomId = roomId;
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

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getAllocatedBy() {
        return allocatedBy;
    }

    public void setAllocatedBy(Integer allocatedBy) {
        this.allocatedBy = allocatedBy;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getHostelName() {
        return hostelName;
    }

    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
    }
}
