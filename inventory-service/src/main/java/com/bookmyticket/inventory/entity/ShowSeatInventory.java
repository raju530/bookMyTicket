package com.bookmyticket.inventory.entity;

import com.bookmyticket.common.enums.SeatStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "show_seat_inventory",
       uniqueConstraints = @UniqueConstraint(columnNames = {"showId", "seatId"}))
public class ShowSeatInventory {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID showId;
    private UUID seatId;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    private UUID lockedByBookingId;
    private Instant lockExpiryTime;

    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getShowId() { return showId; }
    public void setShowId(UUID showId) { this.showId = showId; }

    public UUID getSeatId() { return seatId; }
    public void setSeatId(UUID seatId) { this.seatId = seatId; }

    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    public UUID getLockedByBookingId() { return lockedByBookingId; }
    public void setLockedByBookingId(UUID lockedByBookingId) { this.lockedByBookingId = lockedByBookingId; }

    public Instant getLockExpiryTime() { return lockExpiryTime; }
    public void setLockExpiryTime(Instant lockExpiryTime) { this.lockExpiryTime = lockExpiryTime; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
