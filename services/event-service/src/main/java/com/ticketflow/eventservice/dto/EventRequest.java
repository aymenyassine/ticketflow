package com.ticketflow.eventservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record EventRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String venue,
        @NotNull @Future Instant startsAt,
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal price,
        @Min(1) int capacity) {}
