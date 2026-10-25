package com.ticketflow.eventservice.service;

import com.ticketflow.eventservice.dto.CategoryRequest;
import com.ticketflow.eventservice.dto.CategoryResponse;
import com.ticketflow.eventservice.dto.EventRequest;
import com.ticketflow.eventservice.dto.EventResponse;
import com.ticketflow.eventservice.dto.TicketTierRequest;
import com.ticketflow.eventservice.dto.TicketTierResponse;
import com.ticketflow.eventservice.dto.VenueRequest;
import com.ticketflow.eventservice.dto.VenueResponse;
import com.ticketflow.eventservice.entities.Category;
import com.ticketflow.eventservice.entities.Event;
import com.ticketflow.eventservice.entities.EventStatus;
import com.ticketflow.eventservice.entities.TicketTier;
import com.ticketflow.eventservice.entities.Venue;
import com.ticketflow.eventservice.exceptions.BusinessRuleException;
import com.ticketflow.eventservice.exceptions.ResourceNotFoundException;
import com.ticketflow.eventservice.repos.CategoryRepo;
import com.ticketflow.eventservice.repos.EventRepo;
import com.ticketflow.eventservice.repos.TicketTierRepo;
import com.ticketflow.eventservice.repos.VenueRepo;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EventService {
    private final EventRepo events;
    private final VenueRepo venues;
    private final CategoryRepo categories;
    private final TicketTierRepo tiers;

    public EventService(EventRepo events, VenueRepo venues, CategoryRepo categories, TicketTierRepo tiers) {
        this.events = events;
        this.venues = venues;
        this.categories = categories;
        this.tiers = tiers;
    }

    public VenueResponse createVenue(VenueRequest request) {
        return VenueResponse.from(venues.save(new Venue(request.name().trim(), request.city().trim(),
                request.address().trim(), request.capacity())));
    }
    public VenueResponse updateVenue(UUID id, VenueRequest request) {
        Venue venue = venue(id);
        for (Event event : events.findByVenueId(id)) {
            int tierCapacity = tiers.findByEventIdOrderByPriceAsc(event.getId()).stream()
                    .mapToInt(TicketTier::getCapacity).sum();
            if (tierCapacity > request.capacity())
                throw new BusinessRuleException("Venue capacity is below existing ticket capacity");
        }
        venue.update(request.name().trim(), request.city().trim(), request.address().trim(), request.capacity());
        return VenueResponse.from(venue);
    }
    @Transactional(readOnly = true)
    public List<VenueResponse> listVenues() {
        return venues.findAll(Sort.by("name")).stream().map(VenueResponse::from).toList();
    }
    public CategoryResponse createCategory(CategoryRequest request) {
        String name = request.name().trim();
        if (categories.existsByNameIgnoreCase(name)) throw new BusinessRuleException("Category already exists");
        return CategoryResponse.from(categories.save(new Category(name)));
    }
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        Category category = category(id);
        String name = request.name().trim();
        if (!category.getName().equalsIgnoreCase(name) && categories.existsByNameIgnoreCase(name))
            throw new BusinessRuleException("Category already exists");
        category.update(name);
        return CategoryResponse.from(category);
    }
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categories.findAll(Sort.by("name")).stream().map(CategoryResponse::from).toList();
    }
    public EventResponse create(EventRequest request) {
        validateDates(request);
        Event event = events.save(new Event(request.title().trim(), request.summary().trim(),
                request.description().trim(), request.startsAt(), request.endsAt(),
                request.organizerId().trim(), venue(request.venueId()), category(request.categoryId())));
        return response(event);
    }
    public EventResponse update(UUID id, EventRequest request) {
        Event event = event(id);
        requireStatus(event, EventStatus.DRAFT);
        validateDates(request);
        event.update(request.title().trim(), request.summary().trim(), request.description().trim(),
                request.startsAt(), request.endsAt(), request.organizerId().trim(),
                venue(request.venueId()), category(request.categoryId()));
        checkCapacity(event, null, 0);
        return response(event);
    }
    public TicketTierResponse addTier(UUID eventId, TicketTierRequest request) {
        Event event = event(eventId);
        requireStatus(event, EventStatus.DRAFT);
        checkCapacity(event, null, request.capacity());
        return TicketTierResponse.from(tiers.save(new TicketTier(event, request.name().trim(),
                request.price(), request.capacity())));
    }
    public TicketTierResponse updateTier(UUID eventId, UUID tierId, TicketTierRequest request) {
        Event event = event(eventId);
        requireStatus(event, EventStatus.DRAFT);
        TicketTier tier = tier(eventId, tierId);
        checkCapacity(event, tierId, request.capacity());
        tier.update(request.name().trim(), request.price(), request.capacity());
        return TicketTierResponse.from(tier);
    }
    public void deleteTier(UUID eventId, UUID tierId) {
        Event event = event(eventId);
        requireStatus(event, EventStatus.DRAFT);
        TicketTier tier = tier(eventId, tierId);
        if (tier.getAvailable() != tier.getCapacity())
            throw new BusinessRuleException("Tier has reserved seats");
        tiers.delete(tier);
    }
    public EventResponse publish(UUID id) {
        Event event = event(id);
        requireStatus(event, EventStatus.DRAFT);
        List<TicketTier> eventTiers = tiers.findByEventIdOrderByPriceAsc(id);
        if (eventTiers.isEmpty()) throw new BusinessRuleException("At least one ticket tier is required");
        if (!event.getStartsAt().isAfter(Instant.now())) throw new BusinessRuleException("Start date must be in the future");
        checkCapacity(event, null, 0);
        event.setStatus(EventStatus.PUBLISHED);
        return response(event);
    }
    public EventResponse close(UUID id) {
        Event event = event(id);
        requireStatus(event, EventStatus.PUBLISHED);
        event.setStatus(EventStatus.CLOSED);
        return response(event);
    }
    public EventResponse archive(UUID id) {
        Event event = event(id);
        if (event.getStatus() != EventStatus.CLOSED && event.getStatus() != EventStatus.DRAFT)
            throw new BusinessRuleException("Only draft or closed events can be archived");
        event.setStatus(EventStatus.ARCHIVED);
        return response(event);
    }
    @Transactional(readOnly = true)
    public EventResponse getPublic(UUID id) {
        Event event = event(id);
        if (event.getStatus() != EventStatus.PUBLISHED) throw new ResourceNotFoundException("Event", id);
        return response(event);
    }
    @Transactional(readOnly = true)
    public EventResponse getAdmin(UUID id) { return response(event(id)); }
    @Transactional(readOnly = true)
    public Page<EventResponse> listAdmin(int page, int size) {
        return events.findAll(PageRequest.of(page, size, Sort.by("startsAt"))).map(this::response);
    }
    @Transactional(readOnly = true)
    public Page<EventResponse> search(String query, String city, UUID categoryId,
                                      Instant fromDate, Instant toDate,
                                      BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        if (fromDate != null && toDate != null && !toDate.isAfter(fromDate))
            throw new IllegalArgumentException("toDate must be after fromDate");
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new IllegalArgumentException("minPrice must not exceed maxPrice");
        return events.search(EventStatus.PUBLISHED, blankToNull(query), blankToNull(city),
                categoryId, fromDate, toDate, minPrice, maxPrice,
                PageRequest.of(page, size, Sort.by("startsAt")))
                .map(this::response);
    }
    private EventResponse response(Event event) {
        return EventResponse.from(event, tiers.findByEventIdOrderByPriceAsc(event.getId()).stream()
                .map(TicketTierResponse::from).toList());
    }
    private void checkCapacity(Event event, UUID excludedTier, int newCapacity) {
        int sum = tiers.findByEventIdOrderByPriceAsc(event.getId()).stream()
                .filter(t -> !t.getId().equals(excludedTier)).mapToInt(TicketTier::getCapacity).sum();
        if ((long) sum + newCapacity > event.getVenue().getCapacity())
            throw new BusinessRuleException("Ticket capacity exceeds venue capacity");
    }
    private void validateDates(EventRequest request) {
        if (!request.endsAt().isAfter(request.startsAt()))
            throw new BusinessRuleException("End date must be after start date");
    }
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
    private void requireStatus(Event event, EventStatus expected) {
        if (event.getStatus() != expected)
            throw new BusinessRuleException("Event must be " + expected);
    }
    private Event event(UUID id) {
        return events.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }
    private Venue venue(UUID id) {
        return venues.findById(id).orElseThrow(() -> new ResourceNotFoundException("Venue", id));
    }
    private Category category(UUID id) {
        return categories.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
    private TicketTier tier(UUID eventId, UUID tierId) {
        TicketTier tier = tiers.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket tier", tierId));
        if (!tier.getEvent().getId().equals(eventId)) throw new ResourceNotFoundException("Ticket tier", tierId);
        return tier;
    }
}
