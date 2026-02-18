package com.sunbox.sdpspot.model;

import java.util.Date;

public class ScheduleEvent {
    private String EventId;
    private String EventType;
    private Date NotBefore;

    public String getEventId() {
        return EventId;
    }

    public void setEventId(String eventId) {
        EventId = eventId;
    }

    public String getEventType() {
        return EventType;
    }

    public void setEventType(String eventType) {
        EventType = eventType;
    }

    public Date getNotBefore() {
        return NotBefore;
    }

    public void setNotBefore(Date notBefore) {
        NotBefore = notBefore;
    }
}
