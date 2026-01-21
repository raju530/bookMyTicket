package com.bookmyticket.offers.rules;

import com.bookmyticket.offers.dto.PricingRequest;
import com.bookmyticket.offers.dto.PricingResponse;

import java.util.ArrayList;

public class ThirdTicketHalfOffOffer implements OfferRule {

    @Override
    public boolean isApplicable(PricingRequest request) {
        return request.seatTypes() != null && request.seatTypes().size() >= 3;
    }

    @Override
    public PricingResponse apply(PricingRequest request, PricingResponse current) {
        double avgTicketPrice = current.totalAmount() / request.seatTypes().size();
        double discount = avgTicketPrice * 0.5;

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
        return "THIRD_TICKET_50";
    }
}
