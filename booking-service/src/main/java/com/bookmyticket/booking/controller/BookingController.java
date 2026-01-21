package com.bookmyticket.booking.controller;

import com.bookmyticket.booking.dto.BookingRequest;
import com.bookmyticket.booking.dto.BookingResponse;
import com.bookmyticket.booking.service.BookingOrchestrator;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingOrchestrator orchestrator;

    public BookingController(BookingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping
    public BookingResponse create(@RequestBody BookingRequest request) {
        return orchestrator.createBooking(request);
    }

    @PostMapping("/{bookingId}/confirm")
    public BookingResponse confirm(@PathVariable UUID bookingId) {
        return orchestrator.confirmBooking(bookingId);
    }

    @PostMapping("/{bookingId}/cancel")
    public void cancel(@PathVariable UUID bookingId) {
        orchestrator.cancelBooking(bookingId);
    }

    @PostMapping("/cancel/bulk")
    public void cancelBulk(@RequestBody List<UUID> bookingIds) {
        bookingIds.forEach(orchestrator::cancelBooking);
    }
}
