package com.bookmyticket.booking.client;

import com.bookmyticket.common.enums.ShowType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TheatreClient {

    private final RestClient restClient;

    public TheatreClient(@Value("${theatre.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ShowDto getShow(String showId) {
        return restClient.get()
                .uri("/shows/{showId}", showId)
                .retrieve()
                .body(ShowDto.class);
    }

    public record ShowDto(String id, String movieId, String showDate, String startTime, String endTime, ShowType showType, double baseTicketPrice) {
        public boolean isAfternoon() {
            return showType == ShowType.AFTERNOON;
        }
    }
}
