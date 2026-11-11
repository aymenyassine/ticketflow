package com.example.booking_service.entities;

import com.example.booking_service.exceptions.BusinessRuleException;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "seat_inventory")
public class SeatInventory {
    @Id private UUID eventId;
    @Version private Long version;
    @Column(nullable = false) private int capacity;
    @Column(nullable = false) private int reserved;

    protected SeatInventory() {}
    public SeatInventory(UUID eventId, int capacity) {
        this.eventId = eventId;
        this.capacity = capacity;
    }
    public void reserve(int quantity) {
        if (quantity > capacity - reserved) throw new BusinessRuleException("Not enough seats available");
        reserved += quantity;
    }
    public void release(int quantity) {
        if (quantity > reserved) throw new BusinessRuleException("Invalid seat inventory");
        reserved -= quantity;
    }
    public UUID getEventId() { return eventId; }
    public int getCapacity() { return capacity; }
    public int getReserved() { return reserved; }
}
