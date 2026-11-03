package com.example.booking_service.client.dto;

import java.util.List;
import java.util.UUID;

public record EventView(UUID id, String title, List<TicketTierView> tiers) {}
