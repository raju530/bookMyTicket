package com.bookmyticket.theatre.controller;

import com.bookmyticket.theatre.dto.CreateScreenRequest;
import com.bookmyticket.theatre.dto.CreateTheatreRequest;
import com.bookmyticket.theatre.entity.Screen;
import com.bookmyticket.theatre.entity.Theatre;
import com.bookmyticket.theatre.repo.ScreenRepository;
import com.bookmyticket.theatre.repo.TheatreRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/theatres")
public class TheatreController {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;

    public TheatreController(TheatreRepository theatreRepository, ScreenRepository screenRepository) {
        this.theatreRepository = theatreRepository;
        this.screenRepository = screenRepository;
    }

    @PostMapping
    public Theatre createTheatre(@RequestBody CreateTheatreRequest req) {
        Theatre t = new Theatre();
        t.setName(req.name());
        t.setCity(req.city());
        t.setAddress(req.address());
        return theatreRepository.save(t);
    }

    @GetMapping
    public List<Theatre> list(@RequestParam(required = false) String city) {
        if (city == null || city.isBlank()) return theatreRepository.findAll();
        return theatreRepository.findByCityIgnoreCase(city);
    }

    @PostMapping("/{theatreId}/screens")
    public Screen createScreen(@PathVariable UUID theatreId, @RequestBody CreateScreenRequest req) {
        Theatre theatre = theatreRepository.findById(theatreId).orElseThrow();
        Screen s = new Screen();
        s.setTheatre(theatre);
        s.setName(req.name());
        s.setTotalSeats(req.totalSeats());
        return screenRepository.save(s);
    }

    @GetMapping("/{theatreId}/screens")
    public List<Screen> listScreens(@PathVariable UUID theatreId) {
        return screenRepository.findByTheatreId(theatreId);
    }
}
