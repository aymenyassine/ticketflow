package com.ticketflow.eventservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record EventRequest(
        @NotBlank String title,
        @NotBlank @Size(max = 500) String summary,
        @NotBlank @Size(max = 5000) String description,
        @NotNull @Future Instant startsAt,
        @NotNull Instant endsAt,
        @NotBlank String organizerId,
        @NotNull UUID venueId,
        @NotNull UUID categoryId) {}
