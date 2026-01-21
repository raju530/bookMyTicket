package com.bookmyticket.offers.rules;

import com.bookmyticket.offers.dto.PricingRequest;
import com.bookmyticket.offers.dto.PricingResponse;

public interface OfferRule {
    boolean isApplicable(PricingRequest request);
    PricingResponse apply(PricingRequest request, PricingResponse current);
    String code();
}
