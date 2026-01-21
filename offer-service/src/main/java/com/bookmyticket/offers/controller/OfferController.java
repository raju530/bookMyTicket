package com.bookmyticket.offers.controller;

import com.bookmyticket.offers.dto.PricingRequest;
import com.bookmyticket.offers.dto.PricingResponse;
import com.bookmyticket.offers.engine.OfferEngine;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/offers")
public class OfferController {

    private final OfferEngine offerEngine;

    public OfferController(OfferEngine offerEngine) {
        this.offerEngine = offerEngine;
    }

    @PostMapping("/calculate")
    public PricingResponse calculate(@RequestBody PricingRequest request) {
        return offerEngine.calculate(request);
    }
}
