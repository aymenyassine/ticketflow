package com.example.booking_service.client.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EventView(UUID id, String title, Instant startsAt, BigDecimal price, int capacity, String status) {}
