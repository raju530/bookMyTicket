package com.bookmyticket.inventory.repo;

import com.bookmyticket.inventory.entity.ShowSeatInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShowSeatInventoryRepository extends JpaRepository<ShowSeatInventory, UUID> {
    List<ShowSeatInventory> findByShowId(UUID showId);
    Optional<ShowSeatInventory> findByShowIdAndSeatId(UUID showId, UUID seatId);
}
