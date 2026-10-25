package com.ticketflow.eventservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TicketTierRequest(@NotBlank String name,
                                @NotNull @DecimalMin("0.00") BigDecimal price,
                                @Min(1) int capacity) {}
