package com.bookmyticket.booking.dto;

import java.util.List;

public record CreateBookingRequest(
        String userId,
        String city,
        String theatreId,
        String showId,
        List<String> seatIds
) {}
