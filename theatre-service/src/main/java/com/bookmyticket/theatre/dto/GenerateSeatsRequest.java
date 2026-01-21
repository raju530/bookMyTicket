package com.bookmyticket.theatre.dto;

public record GenerateSeatsRequest(int rows, int seatsPerRow, String seatType) {}
