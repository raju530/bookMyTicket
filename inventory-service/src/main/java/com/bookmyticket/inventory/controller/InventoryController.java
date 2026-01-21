package com.bookmyticket.inventory.controller;

import com.bookmyticket.inventory.dto.LockSeatsRequest;
import com.bookmyticket.inventory.dto.LockSeatsResponse;
import com.bookmyticket.inventory.dto.SeatView;
import com.bookmyticket.inventory.repo.ShowSeatInventoryRepository;
import com.bookmyticket.inventory.service.SeatLockService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final ShowSeatInventoryRepository repo;
    private final SeatLockService seatLockService;

    public InventoryController(ShowSeatInventoryRepository repo, SeatLockService seatLockService) {
        this.repo = repo;
        this.seatLockService = seatLockService;
    }

    @GetMapping("/shows/{showId}/seats")
    public List<SeatView> getSeats(@PathVariable UUID showId) {
        return repo.findByShowId(showId).stream()
                .map(i -> new SeatView(i.getSeatId(), i.getStatus()))
                .toList();
    }

    @PostMapping("/locks")
    public LockSeatsResponse lock(@RequestBody LockSeatsRequest req) {
        return seatLockService.lock(req);
    }

    @PostMapping("/confirm")
    public void confirm(@RequestParam String bookingId, @RequestParam String showId) {
        seatLockService.confirm(bookingId, showId);
    }

    @PostMapping("/release")
    public void release(@RequestParam String bookingId, @RequestParam String showId) {
        seatLockService.release(bookingId, showId);
    }
}
