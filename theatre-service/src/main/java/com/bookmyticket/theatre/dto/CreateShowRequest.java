package com.bookmyticket.theatre.dto;

import com.bookmyticket.common.enums.ShowType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateShowRequest(
        UUID theatreId,
        UUID screenId,
        String movieId,
        LocalDate showDate,
        LocalTime startTime,
        LocalTime endTime,
        ShowType showType,
        double baseTicketPrice
) {}
