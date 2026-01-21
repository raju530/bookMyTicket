package com.bookmyticket.theatre.entity;

import com.bookmyticket.common.enums.ShowType;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "shows")
public class Show {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Theatre theatre;

    @ManyToOne(optional = false)
    private Screen screen;

    private String movieId;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private ShowType showType;

    private double baseTicketPrice;

    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Theatre getTheatre() { return theatre; }
    public void setTheatre(Theatre theatre) { this.theatre = theatre; }

    public Screen getScreen() { return screen; }
    public void setScreen(Screen screen) { this.screen = screen; }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public ShowType getShowType() { return showType; }
    public void setShowType(ShowType showType) { this.showType = showType; }

    public double getBaseTicketPrice() { return baseTicketPrice; }
    public void setBaseTicketPrice(double baseTicketPrice) { this.baseTicketPrice = baseTicketPrice; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
