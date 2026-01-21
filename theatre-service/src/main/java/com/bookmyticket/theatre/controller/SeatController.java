package com.bookmyticket.theatre.controller;

import com.bookmyticket.theatre.dto.GenerateSeatsRequest;
import com.bookmyticket.theatre.entity.Screen;
import com.bookmyticket.theatre.entity.Seat;
import com.bookmyticket.theatre.repo.ScreenRepository;
import com.bookmyticket.theatre.repo.SeatRepository;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/screens")
public class SeatController {

    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

    public SeatController(ScreenRepository screenRepository, SeatRepository seatRepository) {
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
    }

    @PostMapping("/{screenId}/seats/generate")
    public List<Seat> generateSeats(@PathVariable UUID screenId, @RequestBody GenerateSeatsRequest req) {
        Screen screen = screenRepository.findById(screenId).orElseThrow();

        List<Seat> seats = new ArrayList<>();
        for (int r = 0; r < req.rows(); r++) {
            char rowChar = (char) ('A' + r);
            for (int c = 1; c <= req.seatsPerRow(); c++) {
                Seat seat = new Seat();
                seat.setScreen(screen);
                seat.setSeatNumber(rowChar + String.valueOf(c));
                seat.setSeatType(req.seatType());
                seats.add(seat);
            }
        }
        return seatRepository.saveAll(seats);
    }

    @GetMapping("/{screenId}/seats")
    public List<Seat> listSeats(@PathVariable UUID screenId) {
        return seatRepository.findByScreenId(screenId);
    }
}
