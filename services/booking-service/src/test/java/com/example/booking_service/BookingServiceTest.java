package com.example.booking_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.booking_service.client.CustomerFeignClient;
import com.example.booking_service.client.EventCatalogueFeignClient;
import com.example.booking_service.client.EventInventoryFeignClient;
import com.example.booking_service.client.dto.*;
import com.example.booking_service.dto.BookingRequest;
import com.example.booking_service.dto.BookingResponse;
import com.example.booking_service.entities.*;
import com.example.booking_service.exceptions.BusinessRuleException;
import com.example.booking_service.exceptions.ResourceNotFoundException;
import com.example.booking_service.repos.*;
import com.example.booking_service.service.BookingService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock BookingRepo bookings;
    @Mock BookingLineRepo lines;
    @Mock CustomerFeignClient customers;
    @Mock EventCatalogueFeignClient events;
    @Mock EventInventoryFeignClient inventory;
    @InjectMocks BookingService service;

    @Test void createsBookingWithEventPriceAndInventoryHold() {
        UUID eventId = UUID.randomUUID(), tierId = UUID.randomUUID(), bookingId = UUID.randomUUID();
        when(customers.get(1L)).thenReturn(new CustomerView(1L));
        when(events.getPublished(eventId)).thenReturn(new EventView(eventId, "Concert",
                List.of(new TicketTierView(tierId, "Standard", new BigDecimal("100.00"), 50))));
        when(bookings.saveAndFlush(any())).thenAnswer(inv -> {
            Booking booking = inv.getArgument(0);
            ReflectionTestUtils.setField(booking, "id", bookingId);
            return booking;
        });
        when(inventory.hold(any(), any())).thenAnswer(inv -> {
            UUID holdId = inv.getArgument(0);
            InventoryHoldRequest request = inv.getArgument(1);
            return new InventoryHoldView(holdId, request.tierId(), request.quantity(),
                    request.expiresAt(), "HELD");
        });
        AtomicReference<BookingLine> savedLine = new AtomicReference<>();
        when(lines.saveAndFlush(any())).thenAnswer(inv -> {
            BookingLine line = inv.getArgument(0);
            savedLine.set(line);
            return line;
        });
        when(lines.findByBookingId(bookingId)).thenAnswer(inv -> List.of(savedLine.get()));

        BookingResponse response = service.create(new BookingRequest(1L, eventId, tierId, 2));

        assertEquals(new BigDecimal("200.00"), response.totalAmount());
        assertEquals(BookingStatus.PENDING_PAYMENT, response.status());
        assertEquals(new BigDecimal("100.00"), savedLine.get().getUnitPrice());
        assertEquals("Concert", savedLine.get().getEventTitleSnapshot());
        assertEquals("Standard", savedLine.get().getTierNameSnapshot());
        assertNotNull(savedLine.get().getInventoryHoldId());
        verify(inventory).hold(eq(savedLine.get().getInventoryHoldId()),
                argThat(hold -> hold.tierId().equals(tierId) && hold.quantity() == 2));
    }

    @Test void rejectsQuantityOverTenBeforeCallingOtherServices() {
        assertThrows(BusinessRuleException.class,
                () -> service.create(new BookingRequest(1L, UUID.randomUUID(), UUID.randomUUID(), 11)));
        verifyNoInteractions(customers, events, inventory, bookings, lines);
    }

    @Test void rejectsTierAbsentFromEventBeforeHoldingSeats() {
        UUID eventId = UUID.randomUUID();
        when(customers.get(1L)).thenReturn(new CustomerView(1L));
        when(events.getPublished(eventId)).thenReturn(new EventView(eventId, "Concert", List.of()));
        assertThrows(ResourceNotFoundException.class,
                () -> service.create(new BookingRequest(1L, eventId, UUID.randomUUID(), 2)));
        verifyNoInteractions(inventory, bookings, lines);
    }

    @Test void confirmsHoldOnlyOnce() {
        Booking booking = booking(Instant.now().plusSeconds(900));
        UUID holdId = UUID.randomUUID();
        when(bookings.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(lines.findByBookingId(booking.getId())).thenReturn(List.of(line(booking, holdId)));
        when(inventory.confirm(holdId)).thenReturn(new InventoryHoldView(holdId, UUID.randomUUID(),
                1, booking.getExpiresAt(), "CONFIRMED"));

        service.confirm(booking.getId());
        service.confirm(booking.getId());

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(inventory, times(1)).confirm(holdId);
        assertThrows(BusinessRuleException.class, () -> service.cancel(booking.getId()));
    }

    @Test void cancellingReleasesHoldOnlyOnce() {
        Booking booking = booking(Instant.now().plusSeconds(900));
        UUID holdId = UUID.randomUUID();
        when(bookings.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(lines.findByBookingId(booking.getId())).thenReturn(List.of(line(booking, holdId)));
        when(inventory.release(holdId)).thenReturn(new InventoryHoldView(holdId, UUID.randomUUID(),
                1, booking.getExpiresAt(), "RELEASED"));

        service.cancel(booking.getId());
        service.cancel(booking.getId());

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(inventory, times(1)).release(holdId);
    }

    @Test void expiringBookingReleasesHold() {
        Booking booking = booking(Instant.now().minusSeconds(1));
        UUID holdId = UUID.randomUUID();
        when(bookings.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING_PAYMENT), any()))
                .thenReturn(List.of(booking));
        when(lines.findByBookingId(booking.getId())).thenReturn(List.of(line(booking, holdId)));
        when(inventory.release(holdId)).thenReturn(new InventoryHoldView(holdId, UUID.randomUUID(),
                1, booking.getExpiresAt(), "RELEASED"));

        assertEquals(1, service.releaseExpired());
        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
        verify(inventory).release(holdId);
    }

    private Booking booking(Instant expiry) {
        Booking booking = new Booking(1L, UUID.randomUUID().toString(), BigDecimal.TEN, expiry);
        ReflectionTestUtils.setField(booking, "id", UUID.randomUUID());
        return booking;
    }

    private BookingLine line(Booking booking, UUID holdId) {
        return new BookingLine(booking, UUID.randomUUID(), UUID.randomUUID(), holdId,
                1, BigDecimal.TEN, "Concert", "Standard");
    }
}
