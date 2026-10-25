package com.ticketflow.eventservice.dto;

import com.ticketflow.eventservice.entities.TicketTier;
import java.math.BigDecimal;
import java.util.UUID;

public record TicketTierResponse(UUID id, String name, BigDecimal price,
                                 int capacity, int available) {
    public static TicketTierResponse from(TicketTier tier) {
        return new TicketTierResponse(tier.getId(), tier.getName(), tier.getPrice(),
                tier.getCapacity(), tier.getAvailable());
    }
}
