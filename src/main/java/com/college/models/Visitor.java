package com.college.models;

import java.time.LocalDateTime;

public class Visitor {
    private int id;
    private String name;
    private String phone;
    private String email;
    private String idProofType;
    private String idProofNumber;
    private LocalDateTime createdAt;

    public Visitor(int id, String name, String phone, String email, String idProofType, String idProofNumber,
            LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.idProofType = idProofType;
        this.idProofNumber = idProofNumber;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getIdProofType() {
        return idProofType;
    }

    public String getIdProofNumber() {
        return idProofNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
