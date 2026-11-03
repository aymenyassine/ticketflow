package com.example.booking_service.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class BookingLine {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID ticketTierId;

    @Column(nullable = false, unique = true)
    private UUID inventoryHoldId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private String eventTitleSnapshot;

    @Column(nullable = false)
    private String tierNameSnapshot;

    protected BookingLine() {}

    public BookingLine(Booking booking, UUID eventId, UUID ticketTierId, UUID inventoryHoldId,
                       int quantity, BigDecimal unitPrice, String eventTitleSnapshot,
                       String tierNameSnapshot) {
        this.booking = booking;
        this.eventId = eventId;
        this.ticketTierId = ticketTierId;
        this.inventoryHoldId = inventoryHoldId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.eventTitleSnapshot = eventTitleSnapshot;
        this.tierNameSnapshot = tierNameSnapshot;
    }

    public UUID getId() { return id; }
    public Booking getBooking() { return booking; }
    public UUID getEventId() { return eventId; }
    public UUID getTicketTierId() { return ticketTierId; }
    public UUID getInventoryHoldId() { return inventoryHoldId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getEventTitleSnapshot() { return eventTitleSnapshot; }
    public String getTierNameSnapshot() { return tierNameSnapshot; }
}

