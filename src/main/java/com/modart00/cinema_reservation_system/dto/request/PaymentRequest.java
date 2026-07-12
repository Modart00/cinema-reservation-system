package com.modart00.cinema_reservation_system.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private BigDecimal amount;
    private Long reservationId;
}
