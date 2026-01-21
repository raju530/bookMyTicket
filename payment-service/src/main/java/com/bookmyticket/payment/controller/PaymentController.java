package com.bookmyticket.payment.controller;

import com.bookmyticket.payment.dto.PayRequest;
import com.bookmyticket.payment.dto.PayResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @PostMapping("/pay")
    public PayResponse pay(@RequestBody PayRequest req) {
        // Simulated always-success payment for demo
        return new PayResponse("SUCCESS", UUID.randomUUID().toString());
    }
}
