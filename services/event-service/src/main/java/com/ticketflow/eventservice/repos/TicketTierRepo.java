package com.ticketflow.eventservice.repos;

import com.ticketflow.eventservice.entities.TicketTier;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface TicketTierRepo extends JpaRepository<TicketTier, UUID> {
    List<TicketTier> findByEventIdOrderByPriceAsc(UUID eventId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TicketTier t where t.id = :id")
    Optional<TicketTier> findLocked(@Param("id") UUID id);
}
