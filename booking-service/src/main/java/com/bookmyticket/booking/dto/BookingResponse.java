package com.bookmyticket.booking.dto;

public record BookingResponse(
        String bookingId,
        String status,
        double totalAmount,
        double discount,
        double finalAmount,
        String lockExpiryTime
) {}
