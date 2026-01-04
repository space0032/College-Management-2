package com.college.models;

/**
 * Hostel Model
 */
public class Hostel {
    private int id;
    private String name;
    private String type; // BOYS, GIRLS, COED
    private String wardenName;
    private String wardenContact;
    private int totalRooms;
    private int totalCapacity;
    private String address;

    public Hostel() {
    }

    public Hostel(String name, String type) {
        this.name = name;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWardenName() {
        return wardenName;
    }

    public void setWardenName(String wardenName) {
        this.wardenName = wardenName;
    }

    public String getWardenContact() {
        return wardenContact;
    }

    public void setWardenContact(String wardenContact) {
        this.wardenContact = wardenContact;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
