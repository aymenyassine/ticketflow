package com.example.booking_service.repos;
import com.example.booking_service.entities.Booking;
import com.example.booking_service.entities.BookingStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BookingRepo extends JpaRepository<Booking, UUID> {
    List<Booking> findByCustomerId(Long customerId);
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, Instant now);
}
