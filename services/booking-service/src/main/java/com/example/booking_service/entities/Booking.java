package com.example.booking_service.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        columnNames = {"customer_id", "idempotency_key"}))
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING_PAYMENT;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant expiresAt;

    protected Booking() {}

    public Booking(Long customerId, String idempotencyKey,
                   BigDecimal totalAmount, Instant expiresAt) {
        this.customerId = customerId;
        this.idempotencyKey = idempotencyKey;
        this.totalAmount = totalAmount;
        this.expiresAt = expiresAt;
    }

    public void setStatus(BookingStatus status) { this.status = status; }

    public UUID getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public BookingStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
}