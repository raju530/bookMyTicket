package com.bookmyticket.offers.dto;

import java.util.List;

public record PricingResponse(
        double totalAmount,
        double discountAmount,
        double finalAmount,
        List<AppliedOffer> appliedOffers
) {
    public record AppliedOffer(String offerCode, double discount) {}
}
