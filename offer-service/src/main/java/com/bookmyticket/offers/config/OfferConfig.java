package com.bookmyticket.offers.config;

import com.bookmyticket.offers.rules.AfternoonTwentyPercentOffer;
import com.bookmyticket.offers.rules.OfferRule;
import com.bookmyticket.offers.rules.ThirdTicketHalfOffOffer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OfferConfig {

    @Bean
    public List<OfferRule> offerRules() {
        return List.of(
                new ThirdTicketHalfOffOffer(),
                new AfternoonTwentyPercentOffer()
        );
    }
}
