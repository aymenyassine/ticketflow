package com.ticketflow.eventservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import java.util.UUID;

@Entity
public class InventoryHold {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private TicketTier tier;
    private int quantity;
    @Column(nullable = false)
    private Instant expiresAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryHoldStatus status = InventoryHoldStatus.HELD;

    protected InventoryHold() {}
    public InventoryHold(UUID id, TicketTier tier, int quantity, Instant expiresAt) {
        this.id = id;
        this.tier = tier;
        this.quantity = quantity;
        this.expiresAt = expiresAt;
    }
    public UUID getId() { return id; }
    public TicketTier getTier() { return tier; }
    public int getQuantity() { return quantity; }
    public Instant getExpiresAt() { return expiresAt; }
    public InventoryHoldStatus getStatus() { return status; }
    public void confirm() { status = InventoryHoldStatus.CONFIRMED; }
    public void release() { status = InventoryHoldStatus.RELEASED; }
}
