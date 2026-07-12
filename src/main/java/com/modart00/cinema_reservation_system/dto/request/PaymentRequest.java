package com.modart00.cinema_reservation_system.dto.request;

import lombok.Data;

@Data
public class PaymentRequest {
    private double amount;
    private Long reservationId;
}
