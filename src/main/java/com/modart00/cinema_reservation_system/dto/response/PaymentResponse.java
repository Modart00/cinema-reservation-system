package com.modart00.cinema_reservation_system.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentResponse {
    private Long id;
    private String paymentCode;
    private double amount;
    private String status;
    private LocalDate paymentDate;
    private Long reservationId;
}
