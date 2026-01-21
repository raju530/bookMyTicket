package com.bookmyticket.booking.entity;

import com.bookmyticket.common.enums.BookingStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue
    private UUID id;

    private String userId;
    private UUID showId;
    private UUID theatreId;
    private String city;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private double totalAmount;
    private double discountAmount;
    private double finalAmount;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public UUID getShowId() { return showId; }
    public void setShowId(UUID showId) { this.showId = showId; }

    public UUID getTheatreId() { return theatreId; }
    public void setTheatreId(UUID theatreId) { this.theatreId = theatreId; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
