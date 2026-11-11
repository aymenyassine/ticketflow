package com.ticketflow.eventservice.dto;

import com.ticketflow.eventservice.entities.Event;
import com.ticketflow.eventservice.entities.EventStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EventResponse(UUID id, String title, String description, String venue,
        Instant startsAt, BigDecimal price, int capacity, EventStatus status, Instant createdAt) {
    public static EventResponse from(Event event) {
        return new EventResponse(event.getId(), event.getTitle(), event.getDescription(), event.getVenue(),
                event.getStartsAt(), event.getPrice(), event.getCapacity(), event.getStatus(), event.getCreatedAt());
    }
}
