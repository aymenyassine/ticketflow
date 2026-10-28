package com.example.booking_service.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
public record BookingRequest(@NotNull @Min(1) Long customerId, @NotNull UUID eventId,
                             @NotNull UUID ticketTierId, @Min(1) @Max(10) int quantity,
                             @NotNull @DecimalMin("0.00") BigDecimal unitPrice) {}
