package com.example.booking_service.service;
import com.example.booking_service.dto.*;
import com.example.booking_service.entities.*;
import com.example.booking_service.exceptions.BusinessRuleException;
import com.example.booking_service.exceptions.ResourceNotFoundException;
import com.example.booking_service.repos.BookingRepo;
import com.example.booking_service.repos.BookingLineRepo;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
public class BookingService {
    private final BookingRepo bookings;
    private final BookingLineRepo lines;
    public BookingService(BookingRepo bookings, BookingLineRepo lines) {
        this.bookings = bookings; this.lines = lines;
    }
    public BookingResponse create(BookingRequest request) {
        if (request.quantity() < 1 || request.quantity() > 10)
            throw new BusinessRuleException("Quantity must be between 1 and 10");
        if (request.unitPrice() == null || request.unitPrice().signum() < 0)
            throw new BusinessRuleException("Unit price must be positive or zero");
        BigDecimal total = request.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));
        Booking booking = bookings.saveAndFlush(new Booking(request.customerId(), UUID.randomUUID().toString(),
                total, Instant.now().plusSeconds(900)));
        lines.saveAndFlush(new BookingLine(booking, request.eventId(), request.ticketTierId(),
                request.quantity(), request.unitPrice()));
        return response(booking);
    }
    @Transactional(readOnly = true)
    public List<BookingResponse> getByCustomer(Long customerId) {
        return bookings.findByCustomerId(customerId).stream().map(this::response).toList();
    }
    @Transactional(readOnly = true)
    public BookingResponse get(UUID id) { return response(find(id)); }
    public BookingResponse confirm(UUID id) {
        Booking booking = find(id);
        if (booking.getStatus() == BookingStatus.CONFIRMED) return response(booking);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT || !booking.getExpiresAt().isAfter(Instant.now()))
            throw new BusinessRuleException("Booking is no longer pending");
        booking.setStatus(BookingStatus.CONFIRMED);
        return response(booking);
    }
    public BookingResponse cancel(UUID id) {
        Booking booking = find(id);
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED)
            return response(booking);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT)
            throw new BusinessRuleException("Confirmed booking cannot be cancelled");
        changeStatus(booking, booking.getExpiresAt().isAfter(Instant.now()) ? BookingStatus.CANCELLED : BookingStatus.EXPIRED);
        return response(booking);
    }
    public int releaseExpired() {
        List<Booking> expired = bookings.findByStatusAndExpiresAtBefore(BookingStatus.PENDING_PAYMENT, Instant.now());
        for (Booking booking : expired) changeStatus(booking, BookingStatus.EXPIRED);
        return expired.size();
    }
    private void changeStatus(Booking booking, BookingStatus status) {
        booking.setStatus(status);
    }
    private Booking find(UUID id) {
        return bookings.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }
    private BookingResponse response(Booking booking) {
        return BookingResponse.from(booking, lines.findByBookingId(booking.getId()));
    }
}
