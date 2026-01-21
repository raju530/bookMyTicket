package com.bookmyticket.theatre.repo;

import com.bookmyticket.theatre.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShowRepository extends JpaRepository<Show, UUID> {
    List<Show> findByTheatreCityIgnoreCaseAndMovieIdAndShowDate(String city, String movieId, LocalDate showDate);
    List<Show> findByMovieIdAndShowDate(String movieId, LocalDate showDate);
}
