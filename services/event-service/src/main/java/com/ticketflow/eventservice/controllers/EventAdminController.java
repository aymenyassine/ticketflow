package com.ticketflow.eventservice.controllers;

import com.ticketflow.eventservice.dto.*;
import com.ticketflow.eventservice.service.EventService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/events")
public class EventAdminController {
    private final EventService service;
    public EventAdminController(EventService service) { this.service = service; }
    @GetMapping public List<EventResponse> list() { return service.listAll(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@Valid @RequestBody EventRequest request) { return service.create(request); }
    @GetMapping("/{id}") public EventResponse get(@PathVariable UUID id) { return service.get(id); }
    @PutMapping("/{id}") public EventResponse update(@PathVariable UUID id, @Valid @RequestBody EventRequest request) {
        return service.update(id, request);
    }
    @PostMapping("/{id}/publish") public EventResponse publish(@PathVariable UUID id) { return service.publish(id); }
    @PostMapping("/{id}/close") public EventResponse close(@PathVariable UUID id) { return service.close(id); }
}
