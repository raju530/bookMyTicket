package com.bookmyticket.theatre.repo;

import com.bookmyticket.theatre.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScreenRepository extends JpaRepository<Screen, UUID> {
    List<Screen> findByTheatreId(UUID theatreId);
}
