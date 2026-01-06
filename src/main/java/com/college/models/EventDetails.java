package com.college.models;

import java.util.List;

public class EventDetails {
    private Event event;
    private List<EventCollaborator> collaborators;
    private List<EventResource> resources;
    private List<EventVolunteer> volunteers;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<EventCollaborator> getCollaborators() {
        return collaborators;
    }

    public void setCollaborators(List<EventCollaborator> collaborators) {
        this.collaborators = collaborators;
    }

    public List<EventResource> getResources() {
        return resources;
    }

    public void setResources(List<EventResource> resources) {
        this.resources = resources;
    }

    public List<EventVolunteer> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(List<EventVolunteer> volunteers) {
        this.volunteers = volunteers;
    }
}
