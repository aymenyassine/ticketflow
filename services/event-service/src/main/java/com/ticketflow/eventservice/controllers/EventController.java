package com.ticketflow.eventservice.controllers;

import com.ticketflow.eventservice.dto.EventResponse;
import com.ticketflow.eventservice.service.EventService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventService service;
    public EventController(EventService service) { this.service = service; }
    @GetMapping public List<EventResponse> list(@RequestParam(required = false) String venue) {
        return service.listPublished(venue);
    }
    @GetMapping("/{id}") public EventResponse get(@PathVariable UUID id) { return service.getPublished(id); }
}
