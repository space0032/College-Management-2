package com.college.models;

import java.time.LocalDateTime;

public class Notification {
    public enum Type {
        EMAIL, SMS, SYSTEM
    }

    public enum Status {
        PENDING, SENT, FAILED
    }

    private int id;
    private int recipientUserId;
    private String recipientContact;
    private Type type;
    private String subject;
    private String message;
    private Status status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(int recipientUserId, String recipientContact, Type type, String subject, String message) {
        this.recipientUserId = recipientUserId;
        this.recipientContact = recipientContact;
        this.type = type;
        this.subject = subject;
        this.message = message;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(int recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getRecipientContact() {
        return recipientContact;
    }

    public void setRecipientContact(String recipientContact) {
        this.recipientContact = recipientContact;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
