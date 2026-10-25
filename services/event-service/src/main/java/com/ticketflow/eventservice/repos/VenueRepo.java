package com.ticketflow.eventservice.repos;

import com.ticketflow.eventservice.entities.Venue;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepo extends JpaRepository<Venue, UUID> {}
