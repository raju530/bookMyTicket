package com.bookmyticket.offers.dto;

import java.util.List;

public record PricingRequest(
        String city,
        String theatreId,
        String showId,
        List<String> seatTypes,
        double baseTotalAmount,
        boolean isAfternoonShow
) {}
