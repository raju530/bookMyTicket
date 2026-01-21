package com.bookmyticket.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentClient {

    private final RestClient restClient;

    public PaymentClient(@Value("${payments.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public PaymentResponse pay(String bookingId, double amount) {
        PaymentRequest req = new PaymentRequest(bookingId, amount);
        return restClient.post()
                .uri("/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .body(req)
                .retrieve()
                .body(PaymentResponse.class);
    }

    public record PaymentRequest(String bookingId, double amount) {}
    public record PaymentResponse(String status, String transactionId) {}
}
