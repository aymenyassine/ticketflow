package com.example.booking_service.dto;
import com.example.booking_service.entities.Booking;
import com.example.booking_service.entities.BookingLine;
import com.example.booking_service.entities.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
public record BookingResponse(UUID id, Long customerId, BookingStatus status, BigDecimal totalAmount,
                              Instant createdAt, Instant expiresAt, List<BookingLineResponse> lines) {
    public static BookingResponse from(Booking booking, List<BookingLine> lines) {
        return new BookingResponse(booking.getId(), booking.getCustomerId(), booking.getStatus(), booking.getTotalAmount(),
                booking.getCreatedAt(), booking.getExpiresAt(), lines.stream().map(BookingLineResponse::from).toList());
    }
}
