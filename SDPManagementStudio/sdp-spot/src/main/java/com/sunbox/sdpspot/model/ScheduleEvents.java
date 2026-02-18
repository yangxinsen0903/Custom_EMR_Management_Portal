package com.sunbox.sdpspot.model;

import java.util.List;

public class ScheduleEvents {
    private String DocumentIncarnation;
    private List<ScheduleEvent> Events;

    public String getDocumentIncarnation() {
        return DocumentIncarnation;
    }

    public void setDocumentIncarnation(String documentIncarnation) {
        DocumentIncarnation = documentIncarnation;
    }

    public List<ScheduleEvent> getEvents() {
        return Events;
    }

    public void setEvents(List<ScheduleEvent> events) {
        Events = events;
    }
}
