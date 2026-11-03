package com.example.booking_service.client.dto;

import java.time.Instant;
import java.util.UUID;

public record InventoryHoldView(UUID id, UUID tierId, int quantity, Instant expiresAt, String status) {}
