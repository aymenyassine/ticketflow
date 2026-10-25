package com.ticketflow.eventservice.controllers;

import com.ticketflow.eventservice.dto.InventoryHoldRequest;
import com.ticketflow.eventservice.dto.InventoryHoldResponse;
import com.ticketflow.eventservice.service.InventoryService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/inventory/holds")
public class InventoryController {
    private final InventoryService service;
    public InventoryController(InventoryService service) { this.service = service; }
    @PutMapping("/{id}")
    public InventoryHoldResponse hold(@PathVariable UUID id, @Valid @RequestBody InventoryHoldRequest request) {
        return service.hold(id, request);
    }
    @PostMapping("/{id}/confirm")
    public InventoryHoldResponse confirm(@PathVariable UUID id) { return service.confirm(id); }
    @DeleteMapping("/{id}")
    public InventoryHoldResponse release(@PathVariable UUID id) { return service.release(id); }
}
