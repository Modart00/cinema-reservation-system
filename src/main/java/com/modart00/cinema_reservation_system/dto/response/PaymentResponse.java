package com.modart00.cinema_reservation_system.dto.response;

import com.modart00.cinema_reservation_system.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long id;
    private String paymentCode;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private Long reservationId;
}
