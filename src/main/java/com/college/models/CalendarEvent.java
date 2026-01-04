package com.college.models;

import java.time.LocalDate;

public class CalendarEvent {
    private int id;
    private String title;
    private LocalDate eventDate;
    private EventType eventType;
    private String description;

    public enum EventType {
        HOLIDAY, EXAM, EVENT, DEADLINE
    }

    public CalendarEvent() {
    }

    public CalendarEvent(int id, String title, LocalDate eventDate, EventType eventType, String description) {
        this.id = id;
        this.title = title;
        this.eventDate = eventDate;
        this.eventType = eventType;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
