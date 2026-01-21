package com.bookmyticket.inventory.dto;

import java.util.List;

public record LockSeatsRequest(String bookingId, String showId, List<String> seatIds) {}
