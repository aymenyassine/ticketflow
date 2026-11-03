package com.example.booking_service.dto;

import com.example.booking_service.entities.BookingLine;
import java.math.BigDecimal;
import java.util.UUID;

public record BookingLineResponse(UUID id, UUID eventId, UUID ticketTierId, UUID inventoryHoldId,
        int quantity, BigDecimal unitPrice, String eventTitleSnapshot, String tierNameSnapshot) {
    public static BookingLineResponse from(BookingLine line) {
        return new BookingLineResponse(line.getId(), line.getEventId(), line.getTicketTierId(),
                line.getInventoryHoldId(), line.getQuantity(), line.getUnitPrice(),
                line.getEventTitleSnapshot(), line.getTierNameSnapshot());
    }
}
