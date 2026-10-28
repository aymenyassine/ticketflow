package com.example.booking_service.controllers;
import com.example.booking_service.dto.BookingRequest;
import com.example.booking_service.dto.BookingResponse;
import com.example.booking_service.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    private final BookingService service;
    public BookingController(BookingService service) { this.service = service; }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingRequest request) { return service.create(request); }
    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable UUID id) { return service.get(id); }
    @GetMapping("/customer/{customerId}")
    public List<BookingResponse> getByCustomer(@PathVariable Long customerId) { return service.getByCustomer(customerId); }
    @PostMapping("/{id}/confirm")
    public BookingResponse confirm(@PathVariable UUID id) { return service.confirm(id); }
    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable UUID id) { return service.cancel(id); }
}
