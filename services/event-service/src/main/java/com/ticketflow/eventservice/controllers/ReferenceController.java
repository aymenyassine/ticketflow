package com.ticketflow.eventservice.controllers;

import com.ticketflow.eventservice.dto.CategoryRequest;
import com.ticketflow.eventservice.dto.CategoryResponse;
import com.ticketflow.eventservice.dto.VenueRequest;
import com.ticketflow.eventservice.dto.VenueResponse;
import com.ticketflow.eventservice.service.EventService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1")
public class ReferenceController {
    private final EventService service;
    public ReferenceController(EventService service) { this.service = service; }

    @GetMapping("/venues")
    public List<VenueResponse> venues() { return service.listVenues(); }
    @PostMapping("/admin/venues")
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody VenueRequest request) {
        VenueResponse created = service.createVenue(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }
    @PutMapping("/admin/venues/{id}")
    public VenueResponse updateVenue(@PathVariable UUID id, @Valid @RequestBody VenueRequest request) {
        return service.updateVenue(id, request);
    }
    @GetMapping("/categories")
    public List<CategoryResponse> categories() { return service.listCategories(); }
    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse created = service.createCategory(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }
    @PutMapping("/admin/categories/{id}")
    public CategoryResponse updateCategory(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        return service.updateCategory(id, request);
    }
}
