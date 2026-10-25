package com.ticketflow.eventservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record InventoryHoldRequest(@NotNull UUID tierId, @Min(1) int quantity,
                                   @NotNull @Future Instant expiresAt) {}
