package com.example.booking_service.dto;
import com.example.booking_service.entities.BookingLine;
import java.math.BigDecimal;
import java.util.UUID;
public record BookingLineResponse(UUID id, UUID eventId, UUID ticketTierId, int quantity, BigDecimal unitPrice) {
    public static BookingLineResponse from(BookingLine line) {
        return new BookingLineResponse(line.getId(), line.getEventId(), line.getTicketTierId(), line.getQuantity(), line.getUnitPrice());
    }
}
