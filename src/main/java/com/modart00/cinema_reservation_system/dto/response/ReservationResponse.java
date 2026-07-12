package com.modart00.cinema_reservation_system.dto.response;

import com.modart00.cinema_reservation_system.entity.ReservationStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservationResponse {
    private Long id;
    private String reservationCode;
    private ReservationStatus status;
    private LocalDate createdAt;
    private LocalDate expiresAt;
    private double totalPrice;
    private Long userId;
    private Long screeningId;
}
