package com.example.booking_service.dto;

import com.example.booking_service.entities.Booking;
import com.example.booking_service.entities.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingResponse(UUID id, Long customerId, UUID eventId, String eventTitle, int quantity,
        BigDecimal unitPrice, BigDecimal totalPrice, BookingStatus status, Instant createdAt) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(booking.getId(), booking.getCustomerId(), booking.getEventId(),
                booking.getEventTitle(), booking.getQuantity(), booking.getUnitPrice(),
                booking.getTotalPrice(), booking.getStatus(), booking.getCreatedAt());
    }
}
