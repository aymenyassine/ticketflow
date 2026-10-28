package com.example.booking_service.service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
public class BookingExpiryJob {
    private final BookingService service;
    public BookingExpiryJob(BookingService service) { this.service = service; }
    @Scheduled(fixedDelayString = "${booking.expiry-check-ms:60000}")
    public void releaseExpired() { service.releaseExpired(); }
}
