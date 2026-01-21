package com.bookmyticket.offers.engine;

import com.bookmyticket.offers.dto.PricingRequest;
import com.bookmyticket.offers.dto.PricingResponse;
import com.bookmyticket.offers.rules.OfferRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OfferEngine {

    private final List<OfferRule> rules;

    public OfferEngine(List<OfferRule> rules) {
        this.rules = rules;
    }

    public PricingResponse calculate(PricingRequest request) {
        PricingResponse response = new PricingResponse(
                request.baseTotalAmount(),
                0.0,
                request.baseTotalAmount(),
                new ArrayList<>()
        );

        for (OfferRule rule : rules) {
            if (rule.isApplicable(request)) {
                response = rule.apply(request, response);
            }
        }
        if (response.finalAmount() < 0) {
            response = new PricingResponse(response.totalAmount(), response.discountAmount(), 0.0, response.appliedOffers());
        }
        return response;
    }
}
