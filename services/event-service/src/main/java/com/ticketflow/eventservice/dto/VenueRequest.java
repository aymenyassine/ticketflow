package com.ticketflow.eventservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record VenueRequest(@NotBlank String name, @NotBlank String city,
                           @NotBlank String address, @Min(1) int capacity) {}
