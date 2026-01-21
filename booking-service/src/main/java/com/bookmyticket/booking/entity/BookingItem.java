package com.bookmyticket.booking.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "booking_items")
public class BookingItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Booking booking;

    private UUID seatId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public UUID getSeatId() { return seatId; }
    public void setSeatId(UUID seatId) { this.seatId = seatId; }
}
