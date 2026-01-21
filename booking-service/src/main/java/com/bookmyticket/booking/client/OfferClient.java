package com.bookmyticket.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class OfferClient {

    private final RestClient restClient;

    public OfferClient(@Value("${offers.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public PricingResponse calculate(String city, String theatreId, String showId, double baseTotal, boolean isAfternoon, int ticketCount) {
        // Seat types can be expanded later; for now assume all REGULAR
        List<String> seatTypes = java.util.Collections.nCopies(ticketCount, "REGULAR");

        PricingRequest req = new PricingRequest(city, theatreId, showId, seatTypes, baseTotal, isAfternoon);

        return restClient.post()
                .uri("/offers/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(req)
                .retrieve()
                .body(PricingResponse.class);
    }

    public record PricingRequest(String city, String theatreId, String showId, List<String> seatTypes, double baseTotalAmount, boolean isAfternoonShow) {}
    public record PricingResponse(double totalAmount, double discountAmount, double finalAmount, List<AppliedOffer> appliedOffers) {
        public record AppliedOffer(String offerCode, double discount) {}
        public double total() { return totalAmount; }
        public double discount() { return discountAmount; }
        public double finalAmount() { return finalAmount; }
    }
}
