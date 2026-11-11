package com.example.booking_service.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import feign.FeignException;
import feign.Request;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.http.HttpStatus;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void missingEventStaysNotFoundWhenFeignIsWrappedByCircuitBreaker() {
        FeignException.NotFound missing = new FeignException.NotFound(
                "Event not found", Request.create(Request.HttpMethod.GET,
                        "http://localhost/api/v1/events/missing", Map.of(), null,
                        StandardCharsets.UTF_8), null, Map.of());

        assertEquals(HttpStatus.NOT_FOUND.value(), handler.circuitBreakerFailure(
                new NoFallbackAvailableException("No fallback", missing)).getStatus());
    }

    @Test
    void eventTimeoutBecomesServiceUnavailable() {
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), handler.circuitBreakerFailure(
                new NoFallbackAvailableException("No fallback", new TimeoutException())).getStatus());
    }
}
