package com.bookmyticket.theatre.repo;

import com.bookmyticket.theatre.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TheatreRepository extends JpaRepository<Theatre, UUID> {
    List<Theatre> findByCityIgnoreCase(String city);
}
