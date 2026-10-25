package com.ticketflow.eventservice.repos;

import com.ticketflow.eventservice.entities.InventoryHold;
import com.ticketflow.eventservice.entities.InventoryHoldStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryHoldRepo extends JpaRepository<InventoryHold, UUID> {
    List<InventoryHold> findByStatusAndExpiresAtBefore(InventoryHoldStatus status, Instant now);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from InventoryHold h where h.id = :id")
    Optional<InventoryHold> findLocked(@Param("id") UUID id);
}
