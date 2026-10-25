package com.ticketflow.eventservice.controllers;

import com.ticketflow.eventservice.dto.EventRequest;
import com.ticketflow.eventservice.dto.EventResponse;
import com.ticketflow.eventservice.dto.TicketTierRequest;
import com.ticketflow.eventservice.dto.TicketTierResponse;
import com.ticketflow.eventservice.service.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Validated
@RequestMapping("/api/v1/admin/events")
public class EventAdminController {
    private final EventService service;
    public EventAdminController(EventService service) { this.service = service; }

    @GetMapping
    public Page<EventResponse> list(@RequestParam(defaultValue = "0") @Min(0) int page,
                                    @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.listAdmin(page, size);
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        EventResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }
    @GetMapping("/{id}")
    public EventResponse get(@PathVariable UUID id) { return service.getAdmin(id); }
    @PutMapping("/{id}")
    public EventResponse update(@PathVariable UUID id, @Valid @RequestBody EventRequest request) {
        return service.update(id, request);
    }
    @PostMapping("/{id}/publish")
    public EventResponse publish(@PathVariable UUID id) { return service.publish(id); }
    @PostMapping("/{id}/close")
    public EventResponse close(@PathVariable UUID id) { return service.close(id); }
    @PostMapping("/{id}/archive")
    public EventResponse archive(@PathVariable UUID id) { return service.archive(id); }
    @PostMapping("/{id}/tiers")
    public ResponseEntity<TicketTierResponse> addTier(@PathVariable UUID id,
                                                       @Valid @RequestBody TicketTierRequest request) {
        TicketTierResponse created = service.addTier(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{tierId}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }
    @PutMapping("/{id}/tiers/{tierId}")
    public TicketTierResponse updateTier(@PathVariable UUID id, @PathVariable UUID tierId,
                                         @Valid @RequestBody TicketTierRequest request) {
        return service.updateTier(id, tierId, request);
    }
    @DeleteMapping("/{id}/tiers/{tierId}")
    public ResponseEntity<Void> deleteTier(@PathVariable UUID id, @PathVariable UUID tierId) {
        service.deleteTier(id, tierId);
        return ResponseEntity.noContent().build();
    }
}
