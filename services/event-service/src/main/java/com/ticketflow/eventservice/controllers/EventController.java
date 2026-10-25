package com.ticketflow.eventservice.controllers;

import com.ticketflow.eventservice.dto.EventResponse;
import com.ticketflow.eventservice.service.EventService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventService service;
    public EventController(EventService service) { this.service = service; }

    @GetMapping
    public Page<EventResponse> search(@RequestParam(required = false) String query,
                                      @RequestParam(required = false) String city,
                                      @RequestParam(required = false) UUID categoryId,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
                                      @RequestParam(required = false) @jakarta.validation.constraints.DecimalMin("0.00") BigDecimal minPrice,
                                      @RequestParam(required = false) @jakarta.validation.constraints.DecimalMin("0.00") BigDecimal maxPrice,
                                      @RequestParam(defaultValue = "0") @Min(0) int page,
                                      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.search(query, city, categoryId, fromDate, toDate, minPrice, maxPrice, page, size);
    }
    @GetMapping("/{id}")
    public EventResponse get(@PathVariable UUID id) { return service.getPublic(id); }
}
