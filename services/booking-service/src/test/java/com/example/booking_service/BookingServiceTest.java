package com.example.booking_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.booking_service.client.*;
import com.example.booking_service.client.dto.*;
import com.example.booking_service.dto.*;
import com.example.booking_service.entities.*;
import com.example.booking_service.exceptions.BusinessRuleException;
import com.example.booking_service.repos.*;
import com.example.booking_service.service.BookingService;
import java.math.BigDecimal;
import java.time.Instant;
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
    @Mock SeatInventoryRepo seats;
    @Mock CustomerFeignClient customers;
    @Mock EventCatalogueFeignClient events;
    @InjectMocks BookingService service;

    @Test void reservesSeatsAtEventPriceAndReleasesThemOnCancel() {
        UUID eventId = UUID.randomUUID(), bookingId = UUID.randomUUID();
        when(customers.get(1L)).thenReturn(new CustomerView(1L));
        when(events.getPublished(eventId)).thenReturn(new EventView(eventId, "Concert",
                Instant.now().plusSeconds(3600), new BigDecimal("100.00"), 3, "PUBLISHED"));
        when(seats.findById(eventId)).thenReturn(Optional.empty());
        AtomicReference<Booking> savedBooking = new AtomicReference<>();
        when(bookings.saveAndFlush(any())).thenAnswer(inv -> {
            Booking booking = inv.getArgument(0);
            ReflectionTestUtils.setField(booking, "id", bookingId);
            savedBooking.set(booking);
            return booking;
        });
        when(bookings.findById(bookingId)).thenAnswer(inv -> Optional.of(savedBooking.get()));
        SeatInventory[] saved = new SeatInventory[1];
        doAnswer(inv -> { saved[0] = inv.getArgument(0); return saved[0]; }).when(seats).saveAndFlush(any());

        BookingResponse response = service.create(new BookingRequest(1L, eventId, 2));
        assertEquals(new BigDecimal("200.00"), response.totalPrice());
        assertEquals(2, saved[0].getReserved());
        when(seats.findById(eventId)).thenReturn(Optional.of(saved[0]));
        service.cancel(bookingId);
        assertEquals(0, saved[0].getReserved());
        assertEquals(BookingStatus.CANCELLED, savedBooking.get().getStatus());
    }

    @Test void rejectsMoreSeatsThanCapacity() {
        SeatInventory inventory = new SeatInventory(UUID.randomUUID(), 2);
        inventory.reserve(2);
        assertThrows(BusinessRuleException.class, () -> inventory.reserve(1));
    }
}
