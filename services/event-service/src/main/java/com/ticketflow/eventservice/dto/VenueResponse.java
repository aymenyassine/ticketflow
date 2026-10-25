package com.ticketflow.eventservice.dto;

import com.ticketflow.eventservice.entities.Venue;
import java.util.UUID;

public record VenueResponse(UUID id, String name, String city, String address, int capacity) {
    public static VenueResponse from(Venue venue) {
        return new VenueResponse(venue.getId(), venue.getName(), venue.getCity(),
                venue.getAddress(), venue.getCapacity());
    }
}
