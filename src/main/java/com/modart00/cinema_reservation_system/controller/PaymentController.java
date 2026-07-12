package com.modart00.cinema_reservation_system.controller;

import com.modart00.cinema_reservation_system.dto.response.PaymentResponse;
import com.modart00.cinema_reservation_system.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentResponse> makePayment(
            @PathVariable Long reservationId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(paymentService.makePayment(reservationId, authentication));
    }
}
