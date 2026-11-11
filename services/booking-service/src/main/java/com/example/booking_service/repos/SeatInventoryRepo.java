package com.example.booking_service.repos;

import com.example.booking_service.entities.SeatInventory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatInventoryRepo extends JpaRepository<SeatInventory, UUID> {}
