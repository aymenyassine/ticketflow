package com.example.booking_service.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "simple_bookings")
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Version private Long version;
    @Column(nullable = false) private Long customerId;
    @Column(nullable = false) private UUID eventId;
    @Column(nullable = false) private String eventTitle;
    @Column(nullable = false) private int quantity;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal unitPrice;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BookingStatus status = BookingStatus.PENDING;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    protected Booking() {}
    public Booking(Long customerId, UUID eventId, String eventTitle, int quantity, BigDecimal unitPrice) {
        this.customerId = customerId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    public void cancel() { status = BookingStatus.CANCELLED; }
    public UUID getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public UUID getEventId() { return eventId; }
    public String getEventTitle() { return eventTitle; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public BookingStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
