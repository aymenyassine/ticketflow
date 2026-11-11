package com.ticketflow.eventservice.entities;

import com.ticketflow.eventservice.dto.EventRequest;
import com.ticketflow.eventservice.exceptions.BusinessRuleException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "simple_events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false) private String title;
    @Column(nullable = false, length = 2000) private String description;
    @Column(nullable = false) private String venue;
    @Column(nullable = false) private Instant startsAt;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal price;
    @Column(nullable = false) private int capacity;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private EventStatus status = EventStatus.DRAFT;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    protected Event() {}
    public Event(EventRequest request) { apply(request); }
    public void update(EventRequest request) {
        if (status != EventStatus.DRAFT) throw new BusinessRuleException("Only a draft event can be updated");
        apply(request);
    }
    private void apply(EventRequest request) {
        title = request.title().trim();
        description = request.description().trim();
        venue = request.venue().trim();
        startsAt = request.startsAt();
        price = request.price();
        capacity = request.capacity();
    }
    public void publish() {
        if (status != EventStatus.DRAFT) throw new BusinessRuleException("Event must be DRAFT");
        if (!startsAt.isAfter(Instant.now())) throw new BusinessRuleException("Event start must be in the future");
        status = EventStatus.PUBLISHED;
    }
    public void close() {
        if (status != EventStatus.PUBLISHED) throw new BusinessRuleException("Event must be PUBLISHED");
        status = EventStatus.CLOSED;
    }
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getVenue() { return venue; }
    public Instant getStartsAt() { return startsAt; }
    public BigDecimal getPrice() { return price; }
    public int getCapacity() { return capacity; }
    public EventStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
