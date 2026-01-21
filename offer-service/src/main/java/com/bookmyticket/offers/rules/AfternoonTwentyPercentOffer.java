package com.bookmyticket.offers.rules;

import com.bookmyticket.offers.dto.PricingRequest;
import com.bookmyticket.offers.dto.PricingResponse;

import java.util.ArrayList;

public class AfternoonTwentyPercentOffer implements OfferRule {

    @Override
    public boolean isApplicable(PricingRequest request) {
        return request.isAfternoonShow();
    }

    @Override
    public PricingResponse apply(PricingRequest request, PricingResponse current) {
        double discount = current.finalAmount() * 0.20;

        var offers = new ArrayList<>(current.appliedOffers());
        offers.add(new PricingResponse.AppliedOffer(code(), discount));

        return new PricingResponse(
                current.totalAmount(),
                current.discountAmount() + discount,
                current.finalAmount() - discount,
                offers
        );
    }

    @Override
    public String code() {
        return "AFTERNOON_20";
    }
}
