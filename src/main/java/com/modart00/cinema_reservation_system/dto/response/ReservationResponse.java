package com.modart00.cinema_reservation_system.dto.response;

import com.modart00.cinema_reservation_system.entity.ReservationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReservationResponse {
    private Long id;
    private String reservationCode;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private BigDecimal totalPrice;
    private Long userId;
    private Long screeningId;
}
