package com.example.booking_service.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketTierView(UUID id, String name, BigDecimal price, int available) {}
