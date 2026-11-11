package com.ticketflow.eventservice.service;

import com.ticketflow.eventservice.dto.*;
import com.ticketflow.eventservice.entities.*;
import com.ticketflow.eventservice.exceptions.ResourceNotFoundException;
import com.ticketflow.eventservice.repos.EventRepo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EventService {
    private final EventRepo events;
    public EventService(EventRepo events) { this.events = events; }
    public EventResponse create(EventRequest request) { return EventResponse.from(events.save(new Event(request))); }
    public EventResponse update(UUID id, EventRequest request) {
        Event event = find(id);
        event.update(request);
        return EventResponse.from(event);
    }
    public EventResponse publish(UUID id) {
        Event event = find(id);
        event.publish();
        return EventResponse.from(event);
    }
    public EventResponse close(UUID id) {
        Event event = find(id);
        event.close();
        return EventResponse.from(event);
    }
    @Transactional(readOnly = true)
    public EventResponse get(UUID id) { return EventResponse.from(find(id)); }
    @Transactional(readOnly = true)
    public EventResponse getPublished(UUID id) {
        Event event = find(id);
        if (event.getStatus() != EventStatus.PUBLISHED) throw new ResourceNotFoundException("Published event", id);
        return EventResponse.from(event);
    }
    @Transactional(readOnly = true)
    public List<EventResponse> listPublished(String venue) {
        List<Event> result = venue == null || venue.isBlank()
                ? events.findByStatusOrderByStartsAtAsc(EventStatus.PUBLISHED)
                : events.findByStatusAndVenueIgnoreCaseOrderByStartsAtAsc(EventStatus.PUBLISHED, venue.trim());
        return result.stream().map(EventResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public List<EventResponse> listAll() {
        return events.findAll(Sort.by("startsAt")).stream().map(EventResponse::from).toList();
    }
    private Event find(UUID id) {
        return events.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }
}
