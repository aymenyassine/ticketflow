package com.example.booking_service.service;

import com.example.booking_service.client.CustomerFeignClient;
import com.example.booking_service.client.EventCatalogueFeignClient;
import com.example.booking_service.client.EventInventoryFeignClient;
import com.example.booking_service.client.dto.CustomerView;
import com.example.booking_service.client.dto.EventView;
import com.example.booking_service.client.dto.InventoryHoldRequest;
import com.example.booking_service.client.dto.InventoryHoldView;
import com.example.booking_service.client.dto.TicketTierView;
import com.example.booking_service.dto.*;
import com.example.booking_service.entities.*;
import com.example.booking_service.exceptions.BusinessRuleException;
import com.example.booking_service.exceptions.ResourceNotFoundException;
import com.example.booking_service.repos.BookingRepo;
import com.example.booking_service.repos.BookingLineRepo;
import feign.FeignException;
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
    private final CustomerFeignClient customers;
    private final EventCatalogueFeignClient events;
    private final EventInventoryFeignClient inventory;

    public BookingService(BookingRepo bookings, BookingLineRepo lines, CustomerFeignClient customers,
            EventCatalogueFeignClient events, EventInventoryFeignClient inventory) {
        this.bookings = bookings;
        this.lines = lines;
        this.customers = customers;
        this.events = events;
        this.inventory = inventory;
    }

    public BookingResponse create(BookingRequest request) {
        if (request.quantity() < 1 || request.quantity() > 10)
            throw new BusinessRuleException("Quantity must be between 1 and 10");
        CustomerView customer;
        try {
            customer = customers.get(request.customerId());
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Customer", request.customerId());
        }
        if (customer == null || !request.customerId().equals(customer.id()))
            throw new ResourceNotFoundException("Customer", request.customerId());

        EventView event;
        try {
            event = events.getPublished(request.eventId());
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Published event", request.eventId());
        }
        if (event == null || !request.eventId().equals(event.id()) || event.tiers() == null)
            throw new ResourceNotFoundException("Published event", request.eventId());
        TicketTierView tier = event.tiers().stream()
                .filter(value -> request.ticketTierId().equals(value.id()))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Ticket tier", request.ticketTierId()));
        if (tier.price() == null || tier.price().signum() < 0 || event.title() == null || tier.name() == null)
            throw new BusinessRuleException("Event has incomplete ticket information");
        if (tier.available() < request.quantity())
            throw new BusinessRuleException("Not enough seats available");

        BigDecimal total = tier.price().multiply(BigDecimal.valueOf(request.quantity()));
        Instant expiresAt = Instant.now().plusSeconds(900);
        Booking booking = bookings.saveAndFlush(new Booking(request.customerId(), UUID.randomUUID().toString(),
                total, expiresAt));
        UUID holdId = UUID.randomUUID();
        boolean held = false;
        try {
            InventoryHoldView hold = inventory.hold(holdId,
                    new InventoryHoldRequest(request.ticketTierId(), request.quantity(), expiresAt));
            held = true;
            if (hold == null || !holdId.equals(hold.id())
                    || !request.ticketTierId().equals(hold.tierId())
                    || hold.quantity() != request.quantity() || !"HELD".equals(hold.status()))
                throw new BusinessRuleException("Event returned an invalid inventory hold");
            lines.saveAndFlush(new BookingLine(booking, request.eventId(), request.ticketTierId(), holdId,
                    request.quantity(), tier.price(), event.title(), tier.name()));
        } catch (RuntimeException ex) {
            if (held) {
                try {
                    inventory.release(holdId);
                } catch (RuntimeException releaseError) {
                    ex.addSuppressed(releaseError);
                }
            }
            throw ex;
        }
        return response(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getByCustomer(Long customerId) {
        return bookings.findByCustomerId(customerId).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse get(UUID id) {
        return response(find(id));
    }

    public BookingResponse confirm(UUID id) {
        Booking booking = find(id);
        if (booking.getStatus() == BookingStatus.CONFIRMED)
            return response(booking);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT || !booking.getExpiresAt().isAfter(Instant.now()))
            throw new BusinessRuleException("Booking is no longer pending");
        for (BookingLine line : lines.findByBookingId(id)) {
            InventoryHoldView hold = inventory.confirm(line.getInventoryHoldId());
            if (hold == null || !"CONFIRMED".equals(hold.status()))
                throw new BusinessRuleException("Event did not confirm the inventory hold");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return response(booking);
    }

    public BookingResponse cancel(UUID id) {
        Booking booking = find(id);
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED)
            return response(booking);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT)
            throw new BusinessRuleException("Confirmed booking cannot be cancelled");
        changeStatus(booking,
                booking.getExpiresAt().isAfter(Instant.now()) ? BookingStatus.CANCELLED : BookingStatus.EXPIRED);
        return response(booking);
    }

    public int releaseExpired() {
        List<Booking> expired = bookings.findByStatusAndExpiresAtBefore(BookingStatus.PENDING_PAYMENT, Instant.now());
        for (Booking booking : expired)
            changeStatus(booking, BookingStatus.EXPIRED);
        return expired.size();
    }

    private void changeStatus(Booking booking, BookingStatus status) {
        for (BookingLine line : lines.findByBookingId(booking.getId())) {
            InventoryHoldView hold = inventory.release(line.getInventoryHoldId());
            if (hold == null || !"RELEASED".equals(hold.status()))
                throw new BusinessRuleException("Event did not release the inventory hold");
        }
        booking.setStatus(status);
    }

    private Booking find(UUID id) {
        return bookings.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    private BookingResponse response(Booking booking) {
        return BookingResponse.from(booking, lines.findByBookingId(booking.getId()));
    }
}
