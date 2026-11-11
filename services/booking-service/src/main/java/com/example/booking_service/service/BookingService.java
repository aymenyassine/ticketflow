package com.example.booking_service.service;

import com.example.booking_service.client.CustomerFeignClient;
import com.example.booking_service.client.EventCatalogueFeignClient;
import com.example.booking_service.client.dto.CustomerView;
import com.example.booking_service.client.dto.EventView;
import com.example.booking_service.dto.*;
import com.example.booking_service.entities.*;
import com.example.booking_service.exceptions.*;
import com.example.booking_service.repos.*;
import feign.FeignException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookingService {
    private final BookingRepo bookings;
    private final SeatInventoryRepo seats;
    private final CustomerFeignClient customers;
    private final EventCatalogueFeignClient events;

    public BookingService(BookingRepo bookings, SeatInventoryRepo seats,
            CustomerFeignClient customers, EventCatalogueFeignClient events) {
        this.bookings = bookings;
        this.seats = seats;
        this.customers = customers;
        this.events = events;
    }

    public BookingResponse create(BookingRequest request) {
        if (request.quantity() < 1 || request.quantity() > 10)
            throw new BusinessRuleException("Quantity must be between 1 and 10");
        CustomerView customer;
        try { customer = customers.get(request.customerId()); }
        catch (FeignException.NotFound ex) { throw new ResourceNotFoundException("Customer", request.customerId()); }
        if (customer == null || !request.customerId().equals(customer.id()))
            throw new ResourceNotFoundException("Customer", request.customerId());

        EventView event;
        try { event = events.getPublished(request.eventId()); }
        catch (FeignException.NotFound ex) { throw new ResourceNotFoundException("Published event", request.eventId()); }
        if (event == null || !request.eventId().equals(event.id()))
            throw new ResourceNotFoundException("Published event", request.eventId());
        if (!"PUBLISHED".equals(event.status()) || event.startsAt() == null
                || !event.startsAt().isAfter(Instant.now()))
            throw new BusinessRuleException("Event is not open for booking");
        if (event.title() == null || event.price() == null || event.price().signum() < 0 || event.capacity() < 1)
            throw new BusinessRuleException("Event has incomplete booking information");

        SeatInventory inventory = seats.findById(event.id())
                .orElseGet(() -> new SeatInventory(event.id(), event.capacity()));
        inventory.reserve(request.quantity());
        seats.saveAndFlush(inventory);
        Booking booking = bookings.saveAndFlush(new Booking(request.customerId(), event.id(),
                event.title(), request.quantity(), event.price()));
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse get(UUID id) { return BookingResponse.from(find(id)); }

    @Transactional(readOnly = true)
    public List<BookingResponse> getByCustomer(Long customerId) {
        return bookings.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(BookingResponse::from).toList();
    }

    public BookingResponse cancel(UUID id) {
        Booking booking = find(id);
        if (booking.getStatus() == BookingStatus.CANCELLED) return BookingResponse.from(booking);
        SeatInventory inventory = seats.findById(booking.getEventId())
                .orElseThrow(() -> new BusinessRuleException("Seat inventory not found"));
        inventory.release(booking.getQuantity());
        seats.saveAndFlush(inventory);
        booking.cancel();
        return BookingResponse.from(bookings.saveAndFlush(booking));
    }

    private Booking find(UUID id) {
        return bookings.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }
}
