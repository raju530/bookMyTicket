package com.bookmyticket.inventory.dto;

import com.bookmyticket.common.enums.SeatStatus;

import java.util.UUID;

public record SeatView(UUID seatId, SeatStatus status) {}
