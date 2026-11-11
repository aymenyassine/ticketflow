package com.ticketflow.eventservice.repos;

import com.ticketflow.eventservice.entities.Event;
import com.ticketflow.eventservice.entities.EventStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepo extends JpaRepository<Event, UUID> {
    List<Event> findByStatusOrderByStartsAtAsc(EventStatus status);
    List<Event> findByStatusAndVenueIgnoreCaseOrderByStartsAtAsc(EventStatus status, String venue);
}
