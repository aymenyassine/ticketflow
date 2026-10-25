package com.ticketflow.eventservice.dto;

import com.ticketflow.eventservice.entities.InventoryHold;
import com.ticketflow.eventservice.entities.InventoryHoldStatus;
import java.time.Instant;
import java.util.UUID;

public record InventoryHoldResponse(UUID id, UUID tierId, int quantity,
                                    Instant expiresAt, InventoryHoldStatus status) {
    public static InventoryHoldResponse from(InventoryHold hold) {
        return new InventoryHoldResponse(hold.getId(), hold.getTier().getId(),
                hold.getQuantity(), hold.getExpiresAt(), hold.getStatus());
    }
}
