package com.example.booking_service.repos;

import com.example.booking_service.entities.Booking;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepo extends JpaRepository<Booking, UUID> {
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
