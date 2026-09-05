package com.college.models;

/**
 * AcademicRoom model — master inventory for classrooms/labs.
 * Replaces the previous derived DISTINCT(room_number) approach.
 */
public class AcademicRoom {
    private int id;
    private String roomNumber;
    private String building;
    private int capacity;
    private String type; // CLASSROOM, LABORATORY, SEMINAR, AUDITORIUM, OFFICE
    private String status; // ACTIVE, MAINTENANCE, INACTIVE

    public AcademicRoom() {
        this.capacity = 40;
        this.type = "CLASSROOM";
        this.status = "ACTIVE";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
