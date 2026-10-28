package com.example.booking_service.repos;
import com.example.booking_service.entities.BookingLine;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BookingLineRepo extends JpaRepository<BookingLine, UUID> {
    List<BookingLine> findByBookingId(UUID bookingId);
}
