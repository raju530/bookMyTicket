package com.bookmyticket.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(@Value("${inventory.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public LockSeatsResponse lockSeats(String bookingId, String showId, List<String> seatIds) {
        var req = new LockSeatsRequest(bookingId, showId, seatIds);
        return restClient.post()
                .uri("/inventory/locks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(req)
                .retrieve()
                .body(LockSeatsResponse.class);
    }

    public void confirmSeats(String bookingId, String showId) {
        restClient.post().uri(uriBuilder -> uriBuilder
                        .path("/inventory/confirm")
                        .queryParam("bookingId", bookingId)
                        .queryParam("showId", showId)
                        .build())
                .retrieve()
                .toBodilessEntity();
    }

    public void releaseSeats(String bookingId, String showId) {
        restClient.post().uri(uriBuilder -> uriBuilder
                        .path("/inventory/release")
                        .queryParam("bookingId", bookingId)
                        .queryParam("showId", showId)
                        .build())
                .retrieve()
                .toBodilessEntity();
    }

    public record LockSeatsRequest(String bookingId, String showId, List<String> seatIds) {}
    public record LockSeatsResponse(String status, String expiryTime) {}
}
