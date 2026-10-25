package com.ticketflow.eventservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class TicketTier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    private int capacity;
    private int available;

    protected TicketTier() {}
    public TicketTier(Event event, String name, BigDecimal price, int capacity) {
        this.event = event;
        this.name = name;
        this.price = price;
        this.capacity = capacity;
        this.available = capacity;
    }
    public void update(String name, BigDecimal price, int capacity) {
        if (capacity < this.capacity - this.available) {
            throw new IllegalArgumentException("Capacity cannot be lower than reserved seats");
        }
        this.available += capacity - this.capacity;
        this.name = name;
        this.price = price;
        this.capacity = capacity;
    }
    public void reserve(int quantity) {
        if (quantity < 1 || available < quantity)
            throw new IllegalArgumentException("Not enough seats available");
        available -= quantity;
    }
    public void release(int quantity) {
        if (quantity < 1 || available + quantity > capacity)
            throw new IllegalArgumentException("Invalid number of seats to release");
        available += quantity;
    }
    public UUID getId() { return id; }
    public Event getEvent() { return event; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getCapacity() { return capacity; }
    public int getAvailable() { return available; }
}
