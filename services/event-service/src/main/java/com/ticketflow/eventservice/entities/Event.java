package com.ticketflow.eventservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import java.util.UUID;

@Entity
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 500)
    private String summary;
    @Column(nullable = false, length = 5000)
    private String description;
    @Column(nullable = false)
    private Instant startsAt;
    @Column(nullable = false)
    private Instant endsAt;
    @Column(nullable = false)
    private String organizerId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.DRAFT;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected Event() {}
    public Event(String title, String summary, String description, Instant startsAt,
                 Instant endsAt, String organizerId, Venue venue, Category category) {
        update(title, summary, description, startsAt, endsAt, organizerId, venue, category);
    }
    public void update(String title, String summary, String description, Instant startsAt,
                       Instant endsAt, String organizerId, Venue venue, Category category) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.organizerId = organizerId;
        this.venue = venue;
        this.category = category;
    }
    public void setStatus(EventStatus status) { this.status = status; }
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public String getOrganizerId() { return organizerId; }
    public EventStatus getStatus() { return status; }
    public Venue getVenue() { return venue; }
    public Category getCategory() { return category; }
}
