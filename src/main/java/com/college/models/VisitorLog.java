package com.college.models;

import java.time.LocalDateTime;

public class VisitorLog {
    private int id;
    private int visitorId;
    private String visitorName; // Joined from visitors table for convenience
    private String visitorPhone; // Joined
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private String purpose;
    private String personToMeet;
    private String gateNumber;
    private String status; // 'IN', 'OUT'

    public VisitorLog(int id, int visitorId, String visitorName, String visitorPhone, LocalDateTime entryTime,
            LocalDateTime exitTime, String purpose, String personToMeet, String gateNumber, String status) {
        this.id = id;
        this.visitorId = visitorId;
        this.visitorName = visitorName;
        this.visitorPhone = visitorPhone;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.purpose = purpose;
        this.personToMeet = personToMeet;
        this.gateNumber = gateNumber;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getVisitorId() {
        return visitorId;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public String getVisitorPhone() {
        return visitorPhone;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getPersonToMeet() {
        return personToMeet;
    }

    public String getGateNumber() {
        return gateNumber;
    }

    public String getStatus() {
        return status;
    }
}
