package com.example.booking_service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.example.booking_service.dto.*;
import com.example.booking_service.entities.*;
import com.example.booking_service.exceptions.*;
import com.example.booking_service.repos.*;
import com.example.booking_service.service.BookingService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock BookingRepo bookings;
    @Mock BookingLineRepo lines;
    @InjectMocks BookingService service;
    @Test void createsLocalBookingAndCalculatesTotal() {
        UUID eventId = UUID.randomUUID(), tierId = UUID.randomUUID(), bookingId = UUID.randomUUID();
        when(bookings.saveAndFlush(any())).thenAnswer(inv -> {
            Booking booking = inv.getArgument(0);
            ReflectionTestUtils.setField(booking, "id", bookingId);
            return booking;
        });
        BookingResponse response = service.create(new BookingRequest(1L, eventId, tierId, 2, new BigDecimal("100.00")));
        assertEquals(new BigDecimal("200.00"), response.totalAmount());
        assertEquals(BookingStatus.PENDING_PAYMENT, response.status());
        verify(lines).saveAndFlush(argThat(line -> line.getUnitPrice().equals(new BigDecimal("100.00"))));
    }
    @Test void rejectsQuantityOverTen() {
        assertThrows(BusinessRuleException.class, () -> service.create(new BookingRequest(1L, UUID.randomUUID(), UUID.randomUUID(), 11, BigDecimal.TEN)));
        verifyNoInteractions(bookings, lines);
    }
    @Test void confirmsOnce() {
        Booking booking = booking(Instant.now().plusSeconds(900));
        UUID id = booking.getId();
        when(bookings.findById(id)).thenReturn(Optional.of(booking));
        service.confirm(id); service.confirm(id);
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertThrows(BusinessRuleException.class, () -> service.cancel(id));
    }
    @Test void cancelsLocalBooking() {
        Booking booking = booking(Instant.now().plusSeconds(900));
        UUID id = booking.getId();
        when(bookings.findById(id)).thenReturn(Optional.of(booking));
        service.cancel(id); service.cancel(id);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }
    @Test void expiresPendingBooking() {
        Booking booking = booking(Instant.now().minusSeconds(1));
        when(bookings.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING_PAYMENT), any())).thenReturn(List.of(booking));
        assertEquals(1, service.releaseExpired());
        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
    }
    private Booking booking(Instant expiry) {
        Booking booking = new Booking(1L, UUID.randomUUID().toString(), BigDecimal.TEN, expiry);
        ReflectionTestUtils.setField(booking, "id", UUID.randomUUID());
        return booking;
    }
}
