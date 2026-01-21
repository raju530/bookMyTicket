package com.bookmyticket.theatre.controller;

import com.bookmyticket.theatre.dto.CreateShowRequest;
import com.bookmyticket.theatre.entity.Screen;
import com.bookmyticket.theatre.entity.Show;
import com.bookmyticket.theatre.entity.Theatre;
import com.bookmyticket.theatre.repo.ScreenRepository;
import com.bookmyticket.theatre.repo.ShowRepository;
import com.bookmyticket.theatre.repo.TheatreRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class ShowController {

    private final ShowRepository showRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;

    public ShowController(ShowRepository showRepository, TheatreRepository theatreRepository, ScreenRepository screenRepository) {
        this.showRepository = showRepository;
        this.theatreRepository = theatreRepository;
        this.screenRepository = screenRepository;
    }

    @PostMapping("/shows")
    public Show createShow(@RequestBody CreateShowRequest req) {
        Theatre theatre = theatreRepository.findById(req.theatreId()).orElseThrow();
        Screen screen = screenRepository.findById(req.screenId()).orElseThrow();

        Show show = new Show();
        show.setTheatre(theatre);
        show.setScreen(screen);
        show.setMovieId(req.movieId());
        show.setShowDate(req.showDate());
        show.setStartTime(req.startTime());
        show.setEndTime(req.endTime());
        show.setShowType(req.showType());
        show.setBaseTicketPrice(req.baseTicketPrice());
        return showRepository.save(show);
    }

    @GetMapping("/shows/{showId}")
    public Show getShow(@PathVariable UUID showId) {
        return showRepository.findById(showId).orElseThrow();
    }

    @DeleteMapping("/shows/{showId}")
    public void deleteShow(@PathVariable UUID showId) {
        showRepository.deleteById(showId);
    }

    @GetMapping("/search/shows")
    public List<Show> search(@RequestParam String city,
                             @RequestParam String movieId,
                             @RequestParam LocalDate date) {
        return showRepository.findByTheatreCityIgnoreCaseAndMovieIdAndShowDate(city, movieId, date);
    }
}
