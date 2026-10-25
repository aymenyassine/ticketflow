package com.ticketflow.eventservice.dto;

import com.ticketflow.eventservice.entities.Event;
import com.ticketflow.eventservice.entities.EventStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventResponse(UUID id, String title, String summary, String description,
                            Instant startsAt, Instant endsAt, String organizerId,
                            EventStatus status, VenueResponse venue,
                            CategoryResponse category, List<TicketTierResponse> tiers) {
    public static EventResponse from(Event event, List<TicketTierResponse> tiers) {
        return new EventResponse(event.getId(), event.getTitle(), event.getSummary(),
                event.getDescription(), event.getStartsAt(), event.getEndsAt(),
                event.getOrganizerId(), event.getStatus(), VenueResponse.from(event.getVenue()),
                CategoryResponse.from(event.getCategory()), tiers);
    }
}
