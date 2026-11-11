package com.example.booking_service.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record BookingRequest(@NotNull @Min(1) Long customerId, @NotNull UUID eventId,
        @Min(1) @Max(10) int quantity) {}
